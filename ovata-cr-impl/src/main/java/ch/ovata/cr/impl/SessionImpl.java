/*
 * $Id: SessionImpl.java 2944 2020-01-27 14:14:20Z dani $
 * Created on 14.11.2016, 12:00:00
 * 
 * Copyright (c) 2016 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.impl;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.impl.values.ValueFactoryImpl;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Reference;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Roles;
import ch.ovata.cr.api.SameNameSiblingException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.TrxCallback;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.ValueFactory;
import ch.ovata.cr.api.Workspace;
import ch.ovata.cr.api.security.Permission;
import ch.ovata.cr.api.security.RolePrincipal;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Session captures a view to the underlying workspace. This view consists of a current revision 
 * that is applied when reading the persisten store and a transient state containing the 
 * modifications applied during the life of the session.
 * The transient modifications are written to the persisten store when committing the session.
 * When rolling back the session, the transient modifications are thrown away.
 * 
 * @author dani
 */
public class SessionImpl implements Session {

    private static final Logger logger = LoggerFactory.getLogger( SessionImpl.class);
    
    private final RepositoryImpl repository;
    private final WorkspaceImpl workspace;
    private final ValueFactoryImpl valueFactory;
    private final Mode mode;
    private final DirtyStateImpl dirtyState;
    private final Collection<String> blobsCreated = new ArrayList<>();
    private final Map<String, NodeImpl> lockedNodes = new HashMap<>();
    private final Set<Listener> listeners = new HashSet<>();
    private long revision;
    
    public SessionImpl( RepositoryImpl repository, String workspaceName, long revision) {
        this.repository = repository;
        this.valueFactory = new ValueFactoryImpl( this);
        this.workspace = new WorkspaceImpl( repository, this, workspaceName);
        this.dirtyState = new DirtyStateImpl( this.workspace.getName(), this.valueFactory);
        this.revision = revision;
        this.mode = Mode.READ_ONLY;
        
        logger.debug( "New session on workspace <{}> on revision <{}>.", workspaceName, this.revision);
    }
    
    public SessionImpl( RepositoryImpl repository, String workspaceName, Mode mode) {
        this.repository = repository;
        this.valueFactory = new ValueFactoryImpl( this);
        this.workspace = new WorkspaceImpl( repository, this, workspaceName);
        this.dirtyState = new DirtyStateImpl( this.workspace.getName(), this.valueFactory);
        this.revision = repository.currentRevisionId();
        this.mode = mode;
        
        logger.debug( "New session on workspace <{}> on revision <{}>.", workspaceName, this.revision);
    }
    
    @Override
    public Repository getRepository() {
        return this.repository;
    }
    
    @Override
    public Workspace getWorkspace() {
        return this.workspace;
    }
    
    @Override
    public ValueFactory getValueFactory() {
        return this.valueFactory;
    }
    
    @Override
    public long getRevision() {
        return this.revision;
    }

    @Override
    public Node getRoot() {
        return getNodeByIdentifier( Node.ROOT_ID);
    }

    @Override
    public Node getNodeByIdentifier(String id) {
        return findNodeByIdentifier( id).orElseThrow( () -> new NotFoundException( "Could not find node <" + id + ">."));
    }

    @Override
    public Optional<Node> findNodeByIdentifier(String id) {
        Optional<Node> node = this.workspace.findNodeById( id);

        return node.filter( n -> !this.dirtyState.isNodeRemoved( n.getId())).filter( n -> ((NodeImpl)n).hasPermission( Permission.READ));
    }

    @Override
    public Node getNodeByPath(String absolutePath) {
        StringTokenizer tokenizer = new StringTokenizer( absolutePath, NodeImpl.PATH_DELIMITER);
        Node node = this.getRoot();
        
        while( tokenizer.hasMoreTokens()) {
            node = node.getNode( tokenizer.nextToken());
        }
        
        return node;
    }

    @Override
    public Optional<Node> findNodeByPath(String absolutePath) {
        try {
            return Optional.of( getNodeByPath( absolutePath));
        }
        catch( NotFoundException e) {
            return Optional.empty();
        }
    }

    public DirtyState getDirtyState() {
        return this.dirtyState;
    }
    
    @Override
    public void commit() {
        this.commit( null, null);
    }
    
    @Override
    public void commit( String message) {
        this.commit( null, message);
    }
    
    @Override
    public void commit( TrxCallback callback) {
        this.commit( callback, null);
    }
    
    @Override
    public void commit( TrxCallback callback, String message) {
        if( this.mode == Mode.READ_ONLY) {
            throw new IllegalStateException( "Commit not allowed on read-only session.");
        }
        
        if( this.isDirty()) {
            try {
                Transaction trx = this.repository.getTransactionMgr().commit( this, message, Optional.ofNullable( callback));
    
                logger.info( "Committed dirty state to {}@{} creating revision <{}>.", this.workspace.getName(), this.repository.getName(), trx.getRevision());
                
                ((RepositoryImpl)this.getRepository()).getSearchProvider().indexNodes( this.dirtyState, trx.getRevision());

                this.dirtyState.clear();
                this.workspace.clear();
                this.blobsCreated.clear();
                this.unlockNodes();
                
                this.revision = this.repository.currentRevisionId();        
                
                this.fireEvent( new Session.Event.Committed( this));
            }
            catch( Throwable e) {
                logger.warn( "Error writing to " + this.workspace.getName() + "@" + this.repository.getName() + ".", e);
                
                throw e;
            }
        }
    }

    @Override
    public void rollback() {
        this.unlockNodes();
        
        if( this.isDirty()) {
            logger.debug( "Rolling back transient state based on revision <{}>.", this.getRevision());
            
            this.dirtyState.clear();
            this.workspace.clear();

            this.revision = this.repository.currentRevisionId();
            
            Collection<String> blobIds = new ArrayList<>( this.blobsCreated);
            this.blobsCreated.clear();
            
            ((RepositoryImpl)this.getRepository()).getBlobStore().deleteBlobs( blobIds);
            
            this.fireEvent( new Session.Event.Rolledback( this));
        }
    }

    @Override
    public void refresh() {
        if( !this.isDirty()) {
            this.workspace.clear();
            this.revision = this.repository.currentRevisionId();

            logger.debug( "Refreshed session. Now on revision <{}>.", this.revision);
            
            this.fireEvent( new Session.Event.Refreshed( this));
        }
    }

   @Override
    public boolean isDirty() {
        return this.dirtyState.isDirty();
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add( listener);
    }

    @Override
    public void removeListener(Listener listener) {
        this.listeners.remove( listener);
    }

    @Override
    public void clone( String workspaceName) {
        Session cloneSession = this.getRepository().createWorkspace( workspaceName);

        Node root = this.getRoot();
        Node cloneRoot = cloneSession.getRoot();

        cloneNode( root, cloneRoot);

        cloneSession.commit( "Cloned workspace from <" + this.getWorkspace().getName() + "> at revision <" + this.getRevision() + ">.");
    }

    @Override
    public <T extends Query> T createQuery( Class<T> type) {
        return this.workspace.createQuery( type);
    }

    public void registerBlob( Binary binary) {
        logger.trace( "Registering Blob <{}>.", binary.getId());
        this.blobsCreated.add( binary.getId());
    }

    boolean isNodeRemoved( String nodeId) {
        return this.dirtyState.isNodeRemoved( nodeId);
    }

    NodeImpl findDirtyNode( String nodeId) {
        return (NodeImpl)this.dirtyState.findNode( nodeId);
    }

    NodeImpl addNode( NodeImpl parent, String name, String type) {
        boolean dirtyStateBefore = this.isDirty();

        NodeImpl node = createNewNode( parent, name, type);

        this.dirtyState.addNode( node);

        if( !dirtyStateBefore) {
            fireEvent( new Session.Event.MarkedDirty( this));
        }

        return node;
    }

    void renameNode( NodeImpl node, String name) {
        String oldName = node.getName();
        node.setName( name);

        this.dirtyState.renameNode( node, oldName);
    }

    void moveNode( Node node, Node parent) {
        NodeImpl oldParent = (NodeImpl)node.getParent();
        ((NodeImpl)node).setParent( (NodeImpl)parent);

        this.dirtyState.moveNode( (NodeImpl)node, oldParent);
    }

    void markNodeDirty( NodeImpl node) {
        boolean dirtyStateBefore = this.isDirty();

        if( !node.isDirty()) {
            node.getDocument().append( Node.CREATE_TIME_FIELD, new Date());

            this.dirtyState.modifyNode( node);
        }

        if( !dirtyStateBefore ) {
            fireEvent( new Session.Event.MarkedDirty( this));
        }
    }

    void markNodeRemoved( NodeImpl node) {
        boolean dirtyStateBefore = this.isDirty();

        this.workspace.invalidateCache( node.getNodeId());
        this.dirtyState.removeNode( node);

        if( !dirtyStateBefore && this.isDirty()) {
            fireEvent( new Session.Event.MarkedDirty( this));
        }
    }

    Collection<Node> getChildren( NodeImpl parent) {
        Collection<NodeImpl> nodes = this.workspace.getChildren( parent);

        nodes.addAll( (List)this.dirtyState.getAddedChildren( parent));
        nodes.removeAll( this.dirtyState.getRemovedChildren( parent));

        return nodes.stream().filter( n -> n.hasPermission( Permission.READ)).collect( Collectors.toList());
    }

    Optional<Node> getChildByName( NodeImpl parent, String name) {
        Optional<Node> child = this.workspace.getChildByName( parent, name).filter( n -> !this.dirtyState.isChildRemoved( parent, name));
        Node childNode = child.orElse( this.dirtyState.getAddedChildByName( parent, name));

        return Optional.ofNullable( childNode).filter( n -> ((NodeImpl)n).hasPermission( Permission.READ));
    }

    void lockNode(NodeImpl node) {
        this.lockedNodes.put( node.getId(), node);
        this.repository.getTransactionMgr().lockNode( node);
    }

    void breakLock(NodeImpl node) {
        if( this.repository.getSubject().getPrincipals( RolePrincipal.class).contains( new RolePrincipal( Roles.ADMINISTRATOR))) {
            this.lockedNodes.remove( node.getId(), node);

            this.repository.getTransactionMgr().breakNodeLock( node);
        }
    }

    boolean isNodeLocked(NodeImpl node) {
        return this.repository.getTransactionMgr().getLockInfo( node).isPresent();
    }

    Optional<LockInfo> getLockInfo(NodeImpl node) {
        return this.repository.getTransactionMgr().getLockInfo( node);
    }
    
    protected void fireEvent( Event event) {
        this.listeners.stream().forEach( l -> l.onStateChange( event));
    }
    
    private NodeImpl createNewNode( NodeImpl parent, String name, String type) {
        NodeId nodeId = new NodeId( this.getRevision(), 0l);
        StoreDocument document = this.getValueFactory().newDocument()   
                                                            .append( Node.NODE_ID_FIELD, nodeId.toDocument( this.getValueFactory().newDocument()))
                                                            .append( Node.CREATE_TIME_FIELD, new Date())
                                                            .append( Node.TYPE_FIELD, type)
                                                            .append( Node.NAME_FIELD, name)
                                                            .append( Node.PARENT_ID_FIELD, parent.getId());

        return new NodeImpl( this, document, true);
    }

    private void cloneNode( Node original, Node clone) {
        copyProperties( original, clone);

        for( Node n : original.getNodes()) {
            try {
                Node c = clone.addNode( n.getName(), n.getType());

                cloneNode( n, c);
            }
            catch( SameNameSiblingException e) {
                logger.warn( "Skipping same name siblings.", e);
            }
        }
    }

    private void copyProperties( Node original, Node clone) {
        for( Property p : original.getProperties()) {
            Value value = p.getValue();

            if( value.getType() == Value.Type.REFERENCE) {
                Reference r = value.getReference();

                if( r.getWorkspace().equals( original.getSession().getWorkspace().getName())) {
                    Session session = clone.getSession();
                    clone.setProperty( p.getName(), session.getValueFactory().referenceByPath( session.getWorkspace().getName(), r.getPath()));
                }
            }
            else {
                clone.setProperty( p.getName(), value);
            }
        }
    }

    private void unlockNodes() {
        this.lockedNodes.values().stream().forEach( n -> this.repository.getTransactionMgr().releaseNodeLock( n));
        this.lockedNodes.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        this.rollback();
    }
}

/*
 * $Id: WorkspaceImpl.java 703 2017-03-08 15:14:54Z dani $
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

import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NotAuthorizedException;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Roles;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.ValueFactory;
import ch.ovata.cr.api.Workspace;
import ch.ovata.cr.api.security.Permission;
import ch.ovata.cr.impl.DirtyStateImpl.AbstractModification;
import ch.ovata.cr.impl.NodeImpl.PolicyImpl;
import ch.ovata.cr.impl.NodeImpl.PolicyImpl.AclImpl;
import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.store.StoreCollection;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import ch.ovata.cr.spi.store.Transaction;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The workspace abstracts away access to the underlying persistent store for the session.
 * It caches in a small first level cache nodes used in the associated session and in a larger, 
 * shared second level cache (potentially using off-heap memory) results of queries to the persistent store.
 * 
 * @author dani
 */
public class WorkspaceImpl implements Workspace {

    private static final Logger logger = LoggerFactory.getLogger( WorkspaceImpl.class);
    
    private static int SESSION_CACHE_SIZE = 1024;
    
    static {
        SESSION_CACHE_SIZE = Integer.valueOf( System.getProperty( "ovatacr.session.cache.size", "1024"));
    }
    
    private final RepositoryImpl repository;
    private final SessionImpl session;
    private final StoreCollection collection;
    private final String name;
    private final StoreCollectionWrapper store;
    private final Cache<String, NodeImpl> nodeCache;
    
    public WorkspaceImpl( RepositoryImpl repository, SessionImpl session, String name) {
        this.repository = repository;
        this.session = session;
        this.name = name;
        this.collection = this.repository.getDatabase().getOrCreateCollection( name);
        this.nodeCache = Caffeine.newBuilder().maximumSize( SESSION_CACHE_SIZE).weakValues().build();
        this.store = new CachingStoreCollectionWrapper( this.repository.getName(), this.name, new DirectStoreCollectionWrapper( this.collection));
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void initWorkspace() {
        this.collection.init( createRootDocument( session.getValueFactory()));
    }

    RepositoryImpl getRepository() {
        return this.repository;
    }

    Node getNodeById(String id) {
        return findNodeById( id).orElseThrow( () -> new NotFoundException( "Node with id <" + id + "> not found."));
    }

    Optional<Node> findNodeById(String id) {
        NodeImpl node = this.session.findDirtyNode( id);

        if( node != null) {
            return Optional.of( node);
        }

        node = this.nodeCache.getIfPresent( id);

        if( node != null) {
            return Optional.of( node);
        }

        return Optional.ofNullable( createNode( store.getById( id, this.session.getRevision())));
    }
    
    Optional<Node> getChildByName( NodeImpl parent, String name) {
        StoreDocument document = this.store.getChildByName( parent.getNodeId().getUUID(), name, this.session.getRevision());
        
        return Optional.ofNullable( createNode( document));
    }
    
    Collection<NodeImpl> getChildren( NodeImpl parent) {
        Collection<StoreDocument> documents = this.store.getChildren( parent.getNodeId().getUUID(), this.session.getRevision());
        
        return documents.stream().map( this::createNode).filter( Objects::nonNull).collect( Collectors.toList());
    }
    
    private NodeImpl createNode( StoreDocument document) {
        if( document == null) {
            return null;
        }
        
        NodeId nodeId = new NodeId( document.getDocument( Node.NODE_ID_FIELD));       
        
        if( this.session.isNodeRemoved( nodeId.getUUID())) {
            return null;
        }
        
        NodeImpl node = this.session.findDirtyNode( nodeId.getUUID());
        
        if( node != null) {
            return node;
        } 
        
        return this.nodeCache.get( nodeId.getUUID(), id -> createNewNode( nodeId, document));
    }
    
    private NodeImpl createNewNode( NodeId nodeId, StoreDocument document) {
        return nodeId.isRoot() ? new RootNodeImpl( this.session, document) : new NodeImpl( this.session, document, false);
    }
    
    void invalidateCache( NodeId nodeId) {
        nodeCache.invalidate( nodeId.getUUID());    
    }
    
    void clear() {
        this.nodeCache.invalidateAll();
    }

    public void appendChanges( Transaction trx) {
        List<StoreDocument> documents = ((SessionImpl)trx.getSession()).getDirtyState().getModifications().stream().map( m -> ((AbstractModification)m).getDocument( trx.getRevision())).flatMap( List::stream).collect( Collectors.toList());
        
        logger.debug( "Writing {} documents.", documents.size());
        
        if( !documents.isEmpty()) {
            long startWrite = System.currentTimeMillis();
            
            this.collection.insertMany( trx, documents);
            
            logger.debug( "Took {}ms to write {} documents.", (System.currentTimeMillis() - startWrite), documents.size());
        }
    }
    
    private StoreDocument createRootDocument( ValueFactory vf) {
        return vf.newDocument()
                    .append( Node.NODE_ID_FIELD, vf.newDocument().append( Node.UUID_FIELD, Node.ROOT_ID).append( Node.REVISION_FIELD, 0l))
                    .append( Node.CREATE_TIME_FIELD, new Date())
                    .append( Node.TYPE_FIELD, CoreNodeTypes.UNSTRUCTURED)
                    .append( Node.NAME_FIELD, "/")
                    .append( Node.PARENT_ID_FIELD, "-")
                    .append( Node.POLICY_FIELD, createDefaultRootPolicy( vf));
    }
    
    private StoreDocument createDefaultRootPolicy( ValueFactory vf) {
        StoreDocument rawAcl = vf.newDocument()
                                    .append( AclImpl.PROP_PRINCIPAL, createRawEveryonePrincipal( vf))
                                    .append( AclImpl.PROP_IS_NEGATIVE, Boolean.FALSE)
                                    .append( AclImpl.PROP_PERMISSIONS, Arrays.asList( Permission.READ.name()));
        
        StoreDocument rawAcl2 = vf.newDocument()
                                    .append( AclImpl.PROP_PRINCIPAL, createRawAdministratorPrincipal( vf))
                                    .append( AclImpl.PROP_IS_NEGATIVE, Boolean.FALSE)
                                    .append( AclImpl.PROP_PERMISSIONS, Arrays.asList( Permission.READ.name(), Permission.MODIFY_ACL.name(), Permission.WRITE.name(), 
                                                                                      Permission.ADD.name(), Permission.REMOVE.name()));
        
        StoreDocumentList acls = vf.newList();
        
        acls.add( rawAcl);
        acls.add( rawAcl2);

        if( this.name.equals( WORKSPACE_CONFIGURATION) || this.name.equals( WORKSPACE_USERS)) {
            StoreDocument rawAcl3 = vf.newDocument()
                                        .append( AclImpl.PROP_PRINCIPAL, createRawSystemPrincipal( vf))
                                        .append( AclImpl.PROP_IS_NEGATIVE, Boolean.FALSE)
                                        .append( AclImpl.PROP_PERMISSIONS, Arrays.asList( Permission.READ.name()));

            acls.add( rawAcl3);
        }
        
        return vf.newDocument().append( PolicyImpl.PROP_ACLS, acls);
    }
    
    private StoreDocument createRawAdministratorPrincipal( ValueFactory vf) {
        return vf.newDocument().append( AclImpl.PROP_TYPE, AclImpl.TYPE_ROLE).append( AclImpl.PROP_NAME, Roles.ADMINISTRATOR);
    }
    
    private StoreDocument createRawEveryonePrincipal( ValueFactory vf) {
        return vf.newDocument().append( AclImpl.PROP_TYPE, AclImpl.TYPE_ROLE).append( AclImpl.PROP_NAME, Roles.EVERYONE);
    }
    
    private StoreDocument createRawSystemPrincipal( ValueFactory vf) {
        return vf.newDocument().append( AclImpl.PROP_TYPE, AclImpl.TYPE_ROLE).append( AclImpl.PROP_NAME, Roles.SYSTEM);
    }
    
    @Override
    public void reindex( Repository repository) {
        SearchProvider provider = this.repository.getSearchProvider();
        
        provider.reindexWorkspace( repository, this.getName());
    }
    
    public <T extends Query> T createQuery( Class<T> type) {
        SearchProvider provider = this.repository.getSearchProvider();
        
        return provider.createQuery( this.session, type);
    }
    
    @Override
    public void vacuum( Repository repository, long revision) {
        if( !repository.isAdministrator()) {
            throw new NotAuthorizedException( "Must be administrator to execute vacuum.");
        }
        
        this.repository.getDatabase().vacuum( this.name, revision);
        this.repository.getTransactionMgr().removeTransactions( this.name, revision - 2);
    }
}

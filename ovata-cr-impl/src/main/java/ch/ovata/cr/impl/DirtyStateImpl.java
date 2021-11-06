/*
 * $Id: DirtyState.java 679 2017-02-25 10:28:00Z dani $
 * Created on 04.01.2017, 12:00:00
 * 
 * Copyright (c) 2017 by Ovata GmbH,
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
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.ValueFactory;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.spi.store.StoreDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class DirtyStateImpl implements DirtyState {
    
    private static final Logger logger = LoggerFactory.getLogger(DirtyStateImpl.class);
    
    private final String workspaceName;
    private final ValueFactory vf;
    private final Map<String, Modification> modifications = new LinkedHashMap<>();
    private final Map<ParentChildId, Modification> parentChildUpdates = new HashMap<>();
    
    public DirtyStateImpl( String workspaceName, ValueFactory vf) {
        this.workspaceName = workspaceName;
        this.vf = vf;
    }
    
    @Override
    public String getWorkspaceName() {
        return this.workspaceName;
    }
    
    private Modification getModification( String nodeId) {
        return this.modifications.get( nodeId);
    }
    
    @Override
    public Collection<String> getModifiedNodeIds() {
        Set<String> ids = new HashSet<>();
        
        for( Modification m : this.modifications.values()) {
            ids.add( m.getNodeId());
            
            if( m instanceof Add) {
                ids.add( ((Add)m).getNode().getParentId());
            }
        }
        
        return ids;
    }
    
    @Override
    public Collection<Modification> getModifications() {
        return modifications.values();
    }
    
    @Override
    public Node findNode( String nodeId) {
        Modification m = this.modifications.get( nodeId);
        
        if( (m instanceof Modify) || (m instanceof Add) || (m instanceof Rename) || (m instanceof Move) ) {
            return m.getNode();
        }
        
        return null;
    }
    
    public Node getAddedChildByName(NodeImpl parent, String name) {
        Modification modification = this.parentChildUpdates.get( new ParentChildId( parent.getId(), name));
        Collection<Rename> renames = getRenamedChildren( parent);
        
        Node child = (modification instanceof Add || modification instanceof Move) ? modification.getNode() : null;
        
        if( child == null) {
            child = renames.stream().filter( m -> m.getNode().getName().equals( name)).findFirst().map( Rename::getNode).orElse( null);
        }
        
        return child;
    }
    
    public Collection<Node> getAddedChildren( NodeImpl parent) {
        return this.modifications.values().stream().filter( m -> m instanceof Add || m instanceof Move).filter( m -> m.getNode().getParent().equals( parent)).map( Modification::getNode).collect( Collectors.toList());
    }
    
    public boolean isChildRemoved( NodeImpl parent, String name) {
        Collection<Node> removedChildren = this.getRemovedChildren( parent);
        Collection<Rename> renames = getRenamedChildren( parent);
        
        return removedChildren.stream().anyMatch( n -> n.getName().equals( name)) || renames.stream().anyMatch( m -> m.getOldName().equals( name));
    }
    
    public Collection<Rename> getRenamedChildren( NodeImpl parent) {
        return this.modifications.values().stream().filter( m -> m instanceof Rename).map( m -> (Rename)m).filter( m -> m.getParent().getId().equals( parent.getId())).collect( Collectors.toList());
    }
    
    public Collection<Node> getRemovedChildren( NodeImpl parent) {
        Collection<Node> moved = this.modifications.values().stream().filter( m -> m instanceof Move).filter( m -> ((Move)m).oldParent.equals( parent)).map( Modification::getNode).collect( Collectors.toList());
        Collection<Node> removed = this.modifications.values().stream().filter( m -> m instanceof Remove).filter( m -> ((NodeImpl)m.getNode()).getParentId().equals( parent.getId())).map( Modification::getNode).collect( Collectors.toList());
        
        List<Node> nodes = new ArrayList<>( moved.size() + removed.size());
        
        nodes.addAll( moved);
        nodes.addAll( removed);
        
        return nodes;
    }
    
    public void addNode( NodeImpl node) {
        logger.trace( "Node <{}> added to dirty state for 'Add'.", node.getId());
        
        AbstractModification add = new Add( node);
        
        this.modifications.put( node.getId(), add);
        this.parentChildUpdates.put( new ParentChildId( node.getParent().getId(), node.getName()), add);
    }
    
    public void removeNode( NodeImpl node) {
        logger.trace( "Node <{}> added to dirty state for 'Remove'.", node.getId());
        
        if( !node.isNew()) {
            AbstractModification remove = new Remove( node);

            this.modifications.put( node.getId(), remove);
            this.parentChildUpdates.put( new ParentChildId( node.getParent().getId(), node.getName()), remove);
        }
        else {
            this.modifications.remove( node.getId());
            this.parentChildUpdates.remove( new ParentChildId( node.getParent().getId(), node.getName()));
        }
    }
    
    public void renameNode( NodeImpl node, String oldName) {
        logger.trace( "Node <{}> added to dirty state for 'Rename'.", node.getId());
        
        if( !node.isNew()) {
            AbstractModification rename = new Rename( node, oldName);
            
            this.modifications.put( node.getId(), rename);
        }
        else {
            Modification add = this.modifications.get( node.getId());
            ParentChildId pid = new ParentChildId( node.getParent().getId(), oldName);
            
            this.parentChildUpdates.remove( pid);
            this.parentChildUpdates.put( new ParentChildId( node.getParent().getId(), node.getName()), add);
        }
    }
    
    public void moveNode( NodeImpl node, NodeImpl oldParent) {
        logger.trace( "Node <{}> added to dirty state for 'Move'.", node.getId());
        
        if( node.isNew()) {
            ParentChildId old = new ParentChildId( oldParent.getId(), node.getName());
            
            Add add = (Add)this.parentChildUpdates.remove( old);
            this.parentChildUpdates.put( new ParentChildId( node.getParent().getId(), node.getName()), add);
        }
        else {
            AbstractModification move = new Move( node, oldParent);

            this.modifications.put( node.getId(), move);
            this.parentChildUpdates.put( new ParentChildId( node.getParent().getId(), node.getName()), move);
        }
    }
    
    public void modifyNode( NodeImpl node) {
        logger.trace( "Node <{}> added to dirty state for 'Modify'.", node.getId());
        
        if( this.modifications.containsKey( node.getId())) {
            throw new RepositoryException( "Messed up transient state for node <" + node.getId() + ">.");
        }
        
        this.modifications.put( node.getId(), new Modify( node));
    }
    
    public boolean isDirty() {
        return !this.modifications.isEmpty();
    }
    
    public boolean isNodeDirty( String nodeId) {
        return this.modifications.containsKey( nodeId);
    }
    
    public boolean isNodeRemoved( String nodeId) {
        Modification modification = getModification( nodeId);
        
        return (modification instanceof Remove);
    }
    
    public boolean isNodeModified( String nodeId) {
        Modification modification = getModification( nodeId);
        
        return (modification instanceof Modify);
    }
    
    public boolean isNodeAdded( String nodeId) {
        Modification modification = getModification( nodeId);
        
        return (modification instanceof Add);
    }
    
    public void clear() {
        logger.trace( "DirtyState cleared.");
        
        this.modifications.clear();
        this.parentChildUpdates.clear();
    }
    
    public abstract class AbstractModification implements DirtyState.Modification {
        
        private final NodeImpl node;
        
        protected AbstractModification( NodeImpl node)  {
            this.node = node;
        }
        
        @Override
        public String getNodeId() {
            return this.node.getId();
        }
        
        @Override
        public NodeImpl getNode() {
            return this.node;
        }
        
        public abstract List<StoreDocument> getDocument( long revision);
    }
    
    public class Modify extends AbstractModification {
        
        public Modify( NodeImpl node) {
            super( node);
        }
        
        @Override
        public DirtyState.Modification.Type getType() {
            return DirtyState.Modification.Type.MODIFY;
        }
        
        @Override
        public List<StoreDocument> getDocument( long revision) {
            StoreDocument document = this.getNode().getDocument();
            
            document.getDocument( Node.NODE_ID_FIELD).append( NodeId.REVISION_FIELD, revision);
            
            return Arrays.asList( document);
        }
    }
    
    public class Add extends AbstractModification {
        
        public Add( NodeImpl node) {
            super( node);
        }
        
        @Override
        public DirtyState.Modification.Type getType() {
            return DirtyState.Modification.Type.ADD;
        }
        
        @Override
        public List<StoreDocument> getDocument( long revision) {
            StoreDocument document = this.getNode().getDocument();
            
            document.getDocument( Node.NODE_ID_FIELD).append( NodeId.REVISION_FIELD, revision);
            
            return Arrays.asList( document);
        }
    }
    
    public class Remove extends AbstractModification {
    
        public Remove( NodeImpl node) {
            super( node);
        }
        
        @Override
        public DirtyState.Modification.Type getType() {
            return DirtyState.Modification.Type.REMOVE;
        }
        
        @Override
        public List<StoreDocument> getDocument( long revision) {
            NodeImpl node = this.getNode();
            
            StoreDocument document = vf.newDocument().append( Node.NODE_ID_FIELD, new NodeId( node.getId(), revision, 0l).toDocument( vf.newDocument()))
                                        .append( Node.CREATE_TIME_FIELD, new Date())
                                        .append( Node.REMOVED_FIELD, Boolean.TRUE)
                                        .append( Node.PARENT_ID_FIELD, node.getDocument().getString( Node.PARENT_ID_FIELD))
                                        .append( Node.NAME_FIELD, node.getName());
            
            return Arrays.asList( document);
        }
    }
    
    public class Move extends AbstractModification {
        
        private final NodeImpl oldParent;
        
        public Move( NodeImpl node, NodeImpl oldParent) {
            super( node);
            
            this.oldParent = oldParent;
        }
        
        @Override
        public DirtyState.Modification.Type getType() {
            return DirtyState.Modification.Type.MODIFY;
        }
        
        public Node getOldParent() {
            return this.oldParent;
        }
        
        @Override
        public List<StoreDocument> getDocument( long revision) {
            NodeImpl node = this.getNode();

            StoreDocument removeDocument = vf.newDocument()
                                                .append( Node.NODE_ID_FIELD, new NodeId( node.getId(), revision, 0l).toDocument( vf.newDocument()))
                                                .append( Node.CREATE_TIME_FIELD, new Date())
                                                .append( Node.REMOVED_FIELD, Boolean.TRUE)
                                                .append( Node.PARENT_ID_FIELD, oldParent.getId())
                                                .append( Node.NAME_FIELD, node.getName());

            StoreDocument addDocument = this.getNode().getDocument();
            
            addDocument.getDocument( Node.NODE_ID_FIELD).append( NodeId.REVISION_FIELD, revision).append( NodeId.STEP_FIELD, 1l);
            
            return Arrays.asList( removeDocument, addDocument);
        }
    }

    public class Rename extends AbstractModification {
        
        private final NodeImpl parent;
        private final String oldName;
        
        public Rename( NodeImpl node, String oldName) {
            super( node);
            
            this.parent = (NodeImpl)node.getParent();
            this.oldName = oldName;
        }
        
        @Override
        public DirtyState.Modification.Type getType() {
            return DirtyState.Modification.Type.RENAME;
        }
        
        @Override
        public List<StoreDocument> getDocument( long revision) {
            NodeImpl node = this.getNode();

            StoreDocument removeDocument = vf.newDocument()
                                                .append( Node.NODE_ID_FIELD, new NodeId( node.getId(), revision, 0l).toDocument( vf.newDocument()))
                                                .append( Node.CREATE_TIME_FIELD, new Date())
                                                .append( Node.REMOVED_FIELD, Boolean.TRUE)
                                                .append( Node.PARENT_ID_FIELD, parent.getId())
                                                .append( Node.NAME_FIELD, this.oldName);

            StoreDocument addDocument = this.getNode().getDocument();
            
            addDocument.getDocument( Node.NODE_ID_FIELD).append( NodeId.REVISION_FIELD, revision).append( NodeId.STEP_FIELD, 1l);
            
            return Arrays.asList( removeDocument, addDocument);
        }
        
        public String getOldName() {
            return this.oldName;
        }
        
        public NodeImpl getParent() {
            return this.parent;
        }
    }
    
    private static class ParentChildId {

        private final String parentId;
        private final String name;

        public ParentChildId( String parentId, String name) {
            this.parentId = parentId;
            this.name = name;
        }

        public String getParentId() {
            return this.parentId;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public int hashCode() {
            return Objects.hash( parentId, name);
        }

        @Override
        public boolean equals( Object o) {
            if( o instanceof ParentChildId) {
                ParentChildId other = (ParentChildId)o;

                return parentId.equals( other.parentId) && name.equals( other.name);
            }

            return false;
        }
    }
}

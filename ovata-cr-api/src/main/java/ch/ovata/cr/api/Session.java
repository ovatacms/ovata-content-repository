/*
 * $Id: Session.java 703 2017-03-08 15:14:54Z dani $
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
package ch.ovata.cr.api;

import ch.ovata.cr.api.query.Query;
import java.util.Optional;

/**
 * Acts as the central entry point to access a workspace for read or write operations.
 * @author dani
 */
public interface Session {
    
    enum Mode { UPDATEABLE, READ_ONLY }
    
    interface Listener {
        void onStateChange(  Event event);
    }
    
    interface Event {
        
        public abstract class AbstractEvent implements Event {
            private final Session session;
            
            protected AbstractEvent( Session session) {
                this.session = session;
            }
            
            public Session getSession() {
                return this.session;
            }
        }
        
        public class MarkedDirty extends AbstractEvent {

            public MarkedDirty(Session session) {
                super(session);
            }
        }
        
        public class Rolledback extends AbstractEvent {

            public Rolledback(Session session) {
                super(session);
            }
        }
        
        public class Committed extends AbstractEvent {

            public Committed(Session session) {
                super(session);
            }
        }
        
        public class Refreshed extends AbstractEvent {
            
            public Refreshed( Session session) {
                super( session);
            }
        }
    }
    
    /**
     * Retrieves the repository this session belongs to
     * @return the repository of this session
     */
    Repository getRepository();
    
    /**
     * The associated workspace
     * @return the workspace
     */
    Workspace getWorkspace();

    /**
     * The required ValueFactory
     * @return the value factory
     */
    ValueFactory getValueFactory();

    /**
     * The revision this session reflects
     * @return the revision
     */
    long getRevision();
    
    /**
     * Retrieves the root node of this workspace
     * @return the root node
     */
    Node getRoot();
    
    /**
     * Retrieve node by identifier
     * @param id the identifier of the node to retrieve
     * @return the node with the given id
     * @throws NotFoundException if the node does not exist
     */
    Node getNodeByIdentifier( String id);
    
    /**
     * Retrieve node by identifier
     * @param id the identifier of the node to retrieve
     * @return optional containing the node if it exists
     */
    Optional<Node> findNodeByIdentifier( String id);
    
    /**
     * Retrieve node by absolute path
     * @param absolutePath the path of the node to retrieve
     * @return node with given path
     * @throws NotFoundException if node does not exist
     */
    Node getNodeByPath( String absolutePath);
    
    /**
     * Retrieve node by absolute path
     * @param absolutePath the path of the node to retrieve
     * @return optional containing the node if it exists
     */
    Optional<Node> findNodeByPath( String absolutePath);
    
    /**
     * Commit changes made in this session to the storage
     */
    void commit();
    
    /**
     * Commit changes made in this session to the underlying storage
     * @param message commit message describing the changes
     */
    void commit( String message);
    
    /**
     * Commit changes made in this session to the underlying storage.
     * Trigger callback within underlying atomic transaction to the configured store.
     * @param callback the callback triggered within transaction
     */
    void commit( TrxCallback callback);
    
    /**
     * Commit changes made in this session to the underlying storage.
     * Trigger callbback within underlying atomic transaction to the configured store.
     * @param callback the callback to be triggered within transaction
     * @param message commit message describing the changes
     */
    void commit( TrxCallback callback, String message);
    
    /**
     * Rollback all changes made in this session
     */
    void rollback();
    
    /**
     * Refresh the visible state to the current revision
     */
    void refresh();
    
    /**
     * Returns true if any node managed by this session was modified
     * @return true if modifications were done through this session
     */
    boolean isDirty();

    /**
     * Clones the current session into a new workspace with the given name.
     * All transient modifications are reflected in the clone.
     * @param workspaceName the name of the new workspace
     */
    void clone( String workspaceName);

    /**
     * Adds a session listener
     * @param listener the listener to add
     */
    void addListener( Listener listener);
    
    /**
     * Removes a session listener
     * @param listener the listener to remove
     */
    void removeListener( Listener listener);
    
    /**
     * Query the content repository
     * @param <T> type of query to create
     * @param q the query to execute
     * @return list of matching nodes
     */
    <T extends Query> T createQuery( Class<T> q);
}

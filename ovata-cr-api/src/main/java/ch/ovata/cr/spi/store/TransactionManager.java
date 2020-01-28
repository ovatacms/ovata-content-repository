/*
 * $Id: TransactionManager.java 679 2017-02-25 10:28:00Z dani $
 * Created on 12.01.2017, 19:00:00
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
package ch.ovata.cr.spi.store;

import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.TrxCallback;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author dani
 */
public interface TransactionManager {

    /**
     * Commits the dirty state of the provided session to the persistent store
     * @param session the session to persist
     * @param message the associated commit message
     * @param callback the callback triggered within the store's transaction
     * @return the generated transaction
     */
    Transaction commit( Session session, String message, Optional<TrxCallback> callback);
    
    /**
     * Retrieves the latest revision written to the underlying data store
     * @return the current revision
     */
    long currentRevision();
    
    /**
     * Returns the list of transactions between a defined lower bound and the current revision
     * @param workspaceName then name of the workspace or "*" for all workspaces
     * @param lowerBound the lower bound
     * @return list of transactions
     */
    List<Transaction> getTransactions( String workspaceName, long lowerBound);
    
    /**
     * Restores the state of a workspace to the state at the given transaction.
     * @param trx the transaction marking the revision to revert to
     */
    void revertTo( Transaction trx);
    
    /**
     * Remove all transactions for a workspace up to a specific revision
     * @param workspaceName the name of the workspace
     * @param revisionLimit the revision limit
     */
    void removeTransactions( String workspaceName, long revisionLimit);
    
    /**
     * Globally locks the node for modification
     * @param node the node to lock
     */
    void lockNode( Node node);
    
    /**
     * Releases the globally locked node
     * @param node the node to unlock
     */
    void releaseNodeLock( Node node);
    
    /**
     * Information about the locked node.
     * @param node
     * @return lock information or empty optional if node is not locked
     */
    Optional<LockInfo> getLockInfo( Node node);
    
    /**
     * Breaks an existing lock. Does nothing if the node is not locked
     * @param node the node to break the lock for
     */
    void breakNodeLock( Node node);
    
    /**
     * Signals all participating members to clear their node caches
     */
    void signalClearCache();
}

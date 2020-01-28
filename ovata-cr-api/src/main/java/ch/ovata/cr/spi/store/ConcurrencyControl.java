/*
 * $Id: ConcurrencyControl.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.spi.store;

import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.api.Node;
import java.util.Optional;

/**
 *
 * @author dani
 */
public interface ConcurrencyControl {
    
    /**
     * @return The next revision id of the repository
     */
    long nextRevision();
    
    /**
     * @return The latest revision that was active in the repository
     */
    long currentRevision();
    
    /**
     * Acquire global repository lock.
     * @return true if the lock could be acquired
     * @throws InterruptedException if we failed to acquire the lock
     */
    boolean acquireLock() throws InterruptedException;
    
    /**
     * Releases global repository lock.
     */
    void releaseLock();
    
    /**
     * Signals all instances to clear their caches
     */
    void signalClearCache();

    /**
     * Locks the specified node
     * @param node the node to lock
     */
    void lockNode(Node node);

    /**
     * Releases the lock on the specified node.
     * @param node the node to unlock
     */
    void releaseNodeLock(Node node);

    /**
     * Gets information about the lock on this node
     * @param node the node to inspect
     * @return a <code>LockInfo</code> or an empty optional if the node is not locked
     */
    Optional<LockInfo> getNodeLockInfo(Node node);

    /**
     * Breaks the lock on this node
     * @param node the node to break the lock
     */
    void breakNodeLock(Node node);
}

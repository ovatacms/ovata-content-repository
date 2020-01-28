/*
 * $Id: Transaction.java 679 2017-02-25 10:28:00Z dani $
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

import ch.ovata.cr.api.Session;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * Represents a transaction that was written to the backend store.
 * @author dani
 */
public interface Transaction {
    
    enum State { IN_FLIGHT, COMMITTED, ROLLED_BACK }

    /**
     * The session initiating this transaction
     * @return the session
     */
    Session getSession();
    
    /**
     * Retrieves the time this transaction was started
     * @return the trx timestamp
     */
    Date getStartTime();
    
    /**
     * Retrieves the time this transaction was committed
     * @return the time this trx was committed
     */
    Date getEndTime();
    
    /**
     * Name of the user that started this transaction
     * @return name of the user
     */
    String getUsername();
    
    /**
     * Name of the workspace affected by this transaction
     * @return name of the workspace
     */
    String getWorkspaceName();
    
    /**
     * Unique revision id of this transaction
     * @return the revision id of this transaction
     */
    long getRevision();
    
    /**
     * The list of affected nodes by this transaction
     * @return list of affected nodes
     */
    Collection<String> getAffectedNodeIds();
    
    /**
     * Retrieves the commit message for this transaction
     * @return the commit message of the transaction
     */
    Optional<String> getMessage();
    
    /**
     * Sets the state of the transaction object to "COMMITTED"
     */
    void markCommitted();
}

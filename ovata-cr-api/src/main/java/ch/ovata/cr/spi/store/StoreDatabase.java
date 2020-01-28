/*
 * $Id: StoreDatabase.java 679 2017-02-25 10:28:00Z dani $
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

import ch.ovata.cr.spi.store.blob.BlobStore;
import java.util.stream.Stream;

/**
 *
 * @author dani
 */
public interface StoreDatabase extends StoreDocumentFactory {
    
    /**
     * Retrieves the name of the database.
     * @return name of the database
     */
    String getName();
    
    /**
     * Retrieves the transaction manager
     * @return the transaction manager
     */
    TransactionManager getTransactionManager();
    
    /**
     * Gets and if not created creates a new collection
     * @param name name of the collection to get/create
     * @return the collection
     */
    StoreCollection getOrCreateCollection( String name);
    
    /**
     * Drops a collection from the underlying database
     * @param name name of the collection
     */
    void dropCollection( String name);
    
    /**
     * Retrieves the list of collection names
     * @return list of collection names
     */
    Stream<String> listCollectionNames();
    
    /**
     * Returns the current revision
     * @return the current revision
     */
    long currentRevision();
    
    /**
     * Retrieves the <code>BlobStore</code> for this database
     * @return the <code>BlobStore</code>
     */
    BlobStore getBlobStore();
    
    /**
     * Remove old revisions from database up to the provided revision
     * @param workspaceName workspace to vacuum
     * @param revision last revision to keep
     */
    void vacuum( String workspaceName, long revision);
}

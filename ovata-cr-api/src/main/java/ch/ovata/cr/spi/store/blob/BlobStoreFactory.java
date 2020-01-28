/*
 * $Id: BlobStoreFactory.java 679 2017-02-25 10:28:00Z dani $
 * Created on 13.01.2017, 12:00:00
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
package ch.ovata.cr.spi.store.blob;

/**
 *
 * @author dani
 */
public interface BlobStoreFactory {
    /**
     * Creates a <code>BlobStore</code> for a specific database.
     * @param databaseName name of the database
     * @return the new <code>BlobStore</code>
     */
    BlobStore createBlobStore( String databaseName);
    
    /**
     * Shuts down this <code>BlobStoreFactory</code>
     */
    void shutdown();
    
    /**
     * Unwrap an object representing an underlying service
     * @param <T> the type of object to unwrap
     * @param type class representing the type
     * @return null or the unwrapped object
     */
    <T> T unwrap( Class<T> type);
}

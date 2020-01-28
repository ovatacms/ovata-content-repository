/*
 * $Id: SearchProviderFactory.java 679 2017-02-25 10:28:00Z dani $
 * Created on 02.03.2017, 16:00:00
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
package ch.ovata.cr.spi.search;

/**
 *
 * @author dani
 */
public interface SearchProviderFactory {
    /**
     * Creates a <code>BlobStore</code> for a specific database.
     * @param databaseName name of the database
     * @return the new <code>SearchProvider</code>
     */
    SearchProvider createSearchProvider( String databaseName);
    
    /**
     * Shuts down this <code>BlobStoreFactory</code>
     */
    void shutdown();
}

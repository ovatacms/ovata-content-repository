/*
 * $Id: MySqlSearchProviderFactory.java 697 2017-03-06 18:12:59Z dani $
 * Created on 22.01.2018, 16:00:00
 * 
 * Copyright (c) 2018 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.mysql.search;

import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.search.SearchProviderFactory;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import ch.ovata.cr.store.mysql.MySqlConnection;

/**
 *
 * @author dani
 */
public class MySqlSearchProviderFactory implements SearchProviderFactory {

    private final BlobStoreFactory blobStoreFactory;
    private final MySqlConnection connection;
    
    public MySqlSearchProviderFactory( BlobStoreFactory blobStoreFactory, MySqlConnection connection) {
        this.blobStoreFactory = blobStoreFactory;
        this.connection = connection;
    }
    
    @Override
    public SearchProvider createSearchProvider(String databaseName) {
        return new MySqlSearchProvider( this.blobStoreFactory, this.connection, databaseName);
    }

    @Override
    public void shutdown() {
    }
}

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
import javax.sql.DataSource;

/**
 *
 * @author dani
 */
public class MySqlFulltextSearchProviderFactory implements SearchProviderFactory {

    private final BlobStoreFactory blobStoreFactory;
    private final MySqlConnection connection;
    private final FulltextIndexer indexer;
    
    public MySqlFulltextSearchProviderFactory( BlobStoreFactory blobStoreFactory, MySqlConnection connection) {
        this.blobStoreFactory = blobStoreFactory;
        this.connection = connection;
        this.indexer = new FulltextIndexer( blobStoreFactory, this.connection.unwrap( DataSource.class));
    }
    
    @Override
    public SearchProvider createSearchProvider(String databaseName) {
        return new MySqlFulltextSearchProvider( this.blobStoreFactory, this.connection, databaseName, this.indexer);
    }

    @Override
    public void shutdown() {
        this.indexer.shutdown();
    }
}

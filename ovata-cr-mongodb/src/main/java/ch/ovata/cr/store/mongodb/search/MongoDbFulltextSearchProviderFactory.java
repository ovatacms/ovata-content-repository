/*
 * $Id: MongoDbFulltextSearchProviderFactory.java 2674 2019-09-18 11:45:20Z dani $
 * Created on 03.05.2018, 12:00:00
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
package ch.ovata.cr.store.mongodb.search;

import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import ch.ovata.cr.store.mongodb.MongoDbConnection;
import ch.ovata.cr.store.mongodb.fulltext.FulltextIndexer;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MongoDbFulltextSearchProviderFactory extends MongoDbSearchProviderFactory {
    
    private static final Logger logger = LoggerFactory.getLogger( MongoDbFulltextSearchProviderFactory.class);
    
    private final MongoClient client;
    private final FulltextIndexer indexer;
    
    public MongoDbFulltextSearchProviderFactory( BlobStoreFactory blobStoreFactory, MongoDbConnection connection) {
        super( connection);
        
        this.client = connection.unwrap( MongoClient.class);
        this.indexer = new FulltextIndexer( blobStoreFactory, client);
    }

    @Override
    public SearchProvider createSearchProvider(String databaseName) {
        logger.debug( "Creating search provider for database <{}>.", databaseName);
        
        return new MongoDbFulltextSearchProvider( this.client, this.indexer, databaseName);
    }
    
    @Override
    public void shutdown() {
        this.indexer.shutdown();
    }
}

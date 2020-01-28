/*
 * $Id: MongoDbSearchProviderFactory.java 1475 2018-05-08 11:21:45Z dani $
 * Created on 06.03.2017, 12:00:00
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
package ch.ovata.cr.store.mongodb.search;

import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.search.SearchProviderFactory;
import ch.ovata.cr.store.mongodb.MongoDbConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MongoDbSearchProviderFactory implements SearchProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger( MongoDbSearchProviderFactory.class);
    
    protected final MongoDbConnection connection;
    
    public MongoDbSearchProviderFactory( MongoDbConnection connection) {
        this.connection = connection;
    }
    
    @Override
    public SearchProvider createSearchProvider(String databaseName) {
        logger.debug( "Creating search provider for database <{}>.", databaseName);
        
        return new MongoDbSearchProvider( this.connection.unwrap(), databaseName);
    }

    @Override
    public void shutdown() {
        // connection is shut down externally
    }
}

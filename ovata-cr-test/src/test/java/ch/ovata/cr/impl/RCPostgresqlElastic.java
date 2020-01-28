/*
 * $Id: RCPostgresqlElastic.java 2897 2019-12-18 12:48:09Z dani $
 * Created on 25.01.2018, 12:00:00
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.RepositoryConnection;
import ch.ovata.cr.api.RepositoryConnectionDataSource;
import ch.ovata.cr.elastic.ElasticSearchProviderFactory;
import ch.ovata.cr.spi.search.SearchProviderFactory;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import ch.ovata.cr.spi.store.StoreConnection;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import ch.ovata.cr.store.postgresql.PostgresqlBlobStoreFactory;
import ch.ovata.cr.store.postgresql.PostgresqlConnection;
import ch.ovata.cr.store.postgresql.concurrency.PostgresqlConcurrencyControlFactory;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class RCPostgresqlElastic implements RepositoryConnectionDataSource {

    private static final Logger logger = LoggerFactory.getLogger( RCPostgresqlElastic.class);
    
    private BasicDataSource dataSource = new BasicDataSource();
    
    @Override
    public RepositoryConnection getConnection() {
        dataSource.setDriverClassName( "org.postgresql.Driver");
        dataSource.setUrl( "jdbc:postgresql://localhost/dani");
        dataSource.setUsername( "postgres");
        dataSource.setPassword( "");
        dataSource.setInitialSize( 5);
        dataSource.setDefaultAutoCommit( false);
        
        ConcurrencyControlFactory ccf = new PostgresqlConcurrencyControlFactory( dataSource);
        BlobStoreFactory bsf = new PostgresqlBlobStoreFactory( dataSource);
        StoreConnection connection = new PostgresqlConnection( dataSource, bsf, ccf);
        SearchProviderFactory spf = new ElasticSearchProviderFactory( bsf, "http://localhost:9200", "");
        
        return new RepositoryConnectionImpl( connection, spf);
    }

    @Override
    public void close() {
        logger.info( "Closing RepositoryConnectionDataSource.");
        try {
            dataSource.close();
        }
        catch( SQLException e) {
            logger.warn( "Could not close.", e);
        }
    }
}

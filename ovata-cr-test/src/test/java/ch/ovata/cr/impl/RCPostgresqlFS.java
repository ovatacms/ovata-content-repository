/*
 * $Id: RCPostgresqlFS.java 2804 2019-11-15 12:20:47Z dani $
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
import ch.ovata.cr.store.postgresql.PostgresqlBlobStoreFactory;
import ch.ovata.cr.store.postgresql.PostgresqlConnection;
import ch.ovata.cr.store.postgresql.search.PostgresqlSearchProviderFactory;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author dani
 */
public class RCPostgresqlFS implements RepositoryConnectionDataSource {

    @Override
    public RepositoryConnection getConnection() {
        BasicDataSource dataSource = new BasicDataSource();
        
        dataSource.setDriverClassName( "org.postgresql.Driver");
        dataSource.setUrl( "jdbc:postgresql://localhost/ovata");
        dataSource.setUsername( "postgres");
        dataSource.setPassword( "postgres");
        dataSource.setInitialSize( 5);
        dataSource.setDefaultAutoCommit( false);

        PostgresqlBlobStoreFactory bsf = new PostgresqlBlobStoreFactory( dataSource);
        PostgresqlConnection connection = new PostgresqlConnection( dataSource, bsf);
        PostgresqlSearchProviderFactory spf = new PostgresqlSearchProviderFactory();
//        ElasticSearchProviderFactory spf = new ElasticSearchProviderFactory( bsf, "https://e2c3b0ad5192fec499800cdefbd80393.eu-west-1.aws.found.io:9243", "elastic:7uGyeNax0a2nrDuwW1IDANUo");
        
        return new RepositoryConnectionImpl( connection, spf);
    }

    @Override
    public void close() {
    }
}

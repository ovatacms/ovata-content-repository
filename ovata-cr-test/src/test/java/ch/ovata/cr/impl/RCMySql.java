/*
 * $Id: RCMySqlFS.java 690 2017-03-02 22:03:30Z dani $
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
import ch.ovata.cr.impl.concurrency.LocalConcurrencyControlFactory;
import ch.ovata.cr.store.mysql.MySqlBlobStoreFactory;
import ch.ovata.cr.store.mysql.MySqlConnection;
import ch.ovata.cr.store.mysql.search.MySqlSearchProviderFactory;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class RCMySql implements RepositoryConnectionDataSource {

    private static final Logger logger = LoggerFactory.getLogger( RCMySql.class);
    
    private BasicDataSource dataSource = new BasicDataSource();
    
    @Override
    public RepositoryConnection getConnection() {
        dataSource.setDriverClassName( "com.mysql.cj.jdbc.Driver");
        dataSource.setUrl( "jdbc:mysql://192.168.5.25:3307/test");
        dataSource.setUsername( "admin");
        dataSource.setPassword( "superman");
        dataSource.setInitialSize( 5);
        dataSource.setDefaultAutoCommit( false);
        
        LocalConcurrencyControlFactory ccf = new LocalConcurrencyControlFactory();
        MySqlBlobStoreFactory bsf = new MySqlBlobStoreFactory( dataSource);
        MySqlConnection connection = new MySqlConnection( dataSource, bsf, ccf);
        MySqlSearchProviderFactory spf = new MySqlSearchProviderFactory();
        
        return new RepositoryConnectionImpl( connection, spf);
    }

    @Override
    public void close() {
        logger.info( "Closing data source.");
        
        try {
            dataSource.close();
        }
        catch( SQLException e) {
            
        }
    }
}

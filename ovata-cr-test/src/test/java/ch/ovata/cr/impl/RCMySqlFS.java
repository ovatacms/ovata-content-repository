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
import ch.ovata.cr.store.fs.FSBlobStoreFactory;
import ch.ovata.cr.store.mysql.MySqlConnection;
import ch.ovata.cr.store.mysql.search.MySqlFulltextSearchProviderFactory;
import java.io.File;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author dani
 */
public class RCMySqlFS implements RepositoryConnectionDataSource {

    @Override
    public RepositoryConnection getConnection() {
        BasicDataSource dataSource = new BasicDataSource();
        
        dataSource.setDriverClassName( "com.mysql.cj.jdbc.Driver");
        dataSource.setUrl( "jdbc:mysql://192.168.5.25:3307/test");
        dataSource.setUsername( "admin");
        dataSource.setPassword( "superman");
        dataSource.setInitialSize( 5);
        dataSource.setDefaultAutoCommit( false);
        
        LocalConcurrencyControlFactory ccf = new LocalConcurrencyControlFactory();
        FSBlobStoreFactory bsf = new FSBlobStoreFactory( new File( "target/blob-store"));
        MySqlConnection connection = new MySqlConnection( dataSource, bsf, ccf);
        MySqlFulltextSearchProviderFactory spf = new MySqlFulltextSearchProviderFactory( bsf, connection);
        
        return new RepositoryConnectionImpl( connection, spf);
    }

    @Override
    public void close() {
    }
}

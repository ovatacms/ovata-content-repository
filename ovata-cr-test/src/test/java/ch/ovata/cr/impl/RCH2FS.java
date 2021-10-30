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
import ch.ovata.cr.store.fs.FSBlobStoreFactory;
import ch.ovata.cr.store.h2.H2Connection;
import ch.ovata.cr.store.h2.search.H2SearchProviderFactory;
import java.io.File;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author dani
 */
public class RCH2FS implements RepositoryConnectionDataSource {

    @Override
    public RepositoryConnection getConnection() {
        BasicDataSource dataSource = new BasicDataSource();
        
        dataSource.setDriverClassName( "org.h2.Driver");
        dataSource.setUrl( "jdbc:h2:mem:db1");
        dataSource.setUsername( "sa");
        dataSource.setPassword( "");
        dataSource.setInitialSize( 5);
        dataSource.setDefaultAutoCommit( false);
        
        FSBlobStoreFactory bsf = new FSBlobStoreFactory( new File( "target/blob-store"));
        H2Connection connection = new H2Connection( dataSource, bsf);
        H2SearchProviderFactory spf = new H2SearchProviderFactory();
        
        return new RepositoryConnectionImpl( connection, spf);
    }

    @Override
    public void close() {
    }
}

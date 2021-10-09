/*
 * $Id: AbstractOvataRepositoryTest.java 690 2017-03-02 22:03:30Z dani $
 * Created on 14.11.2016, 12:00:00
 * 
 * Copyright (c) 2016 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.impl;

import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.RepositoryConnection;
import ch.ovata.cr.api.RepositoryConnectionDataSource;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.utils.YamlImporter;
import com.mongodb.client.MongoClient;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author dani
 */
public abstract class AbstractOvataRepositoryTest {
    
    protected static final String TEST_REPO_NAME = "ovatadev";
    protected static final String TEST_DATABASE_USERNAME = "test@ovata-cloud.com";
    protected static final String TEST_DATABASE_PASSWORD = "testpassword";
    protected static final String TEST_WORKSPACE_NAME = "test";

    private RepositoryConnectionDataSource ds = null;

    @BeforeClass
    public static void setupClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    protected RepositoryConnection connection;

    public AbstractOvataRepositoryTest() {
    }
    
    public Repository getRepositoryAsAnonymous() {
        return connection.login( TEST_REPO_NAME);
    }
    
    public Repository getRepositoryAsAdministrator() throws LoginException {
        return connection.login( TEST_REPO_NAME, TEST_DATABASE_USERNAME, TEST_DATABASE_PASSWORD.toCharArray());
    }
    
    @Before
    public void setUp() throws Exception {
        try {
        System.setProperty( "org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty( "org.slf4j.simpleLogger.log.ch.ovata.cr", "debug");
        // Choose implementation to test
        // ds = new RCMySql();
        // ds = new RCPostgresqlElastic();
         ds = new RCPostgresqlFS();
//         ds = new RCMongoDbS3();
//        ds = new RCH2FS();
        
        this.connection = ds.getConnection();

        CachingStoreCollectionWrapper.clearCache();

        MongoClient mongo = this.connection.unwrap( MongoClient.class);
        
        if( mongo != null) {
            System.out.println( "-----> Removing database before test run and clearing cache.");
            mongo.getDatabase( "ovatadev").drop();
        }
        
        DataSource ds = this.connection.unwrap( DataSource.class);
        
        if( ds != null) {
            System.out.println( "-----> Dropping tables.");
            try( Connection c = ds.getConnection()) {
                dropTable( c, "ovatadev_system_transactions");
                dropTable( c, "ovatadev_system_users");
                dropTable( c, "ovatadev_system_configuration");
                dropTable( c, "ovatadev_test");
                dropTable( c, "ovatadev_system_config_clone");
                dropTable( c, "ovatadev_other");
                dropTable( c, "blobstore");
            }
        }

        Repository repository = connection.create( TEST_REPO_NAME, TEST_DATABASE_USERNAME, TEST_DATABASE_PASSWORD);
        
        repository.createWorkspace( TEST_WORKSPACE_NAME);
        }
        catch( Exception e) {
            e.printStackTrace();
        }
    }
    
    private void dropDatabase( Connection c, String name) throws SQLException {
        try( Statement stmt = c.createStatement()) {
            stmt.execute( "DROP DATABASE IF EXISTS " + name + "");
        }
    }
    
    private void dropTable( Connection c, String tableName) throws SQLException {
        try( Statement stmt = c.createStatement()) {
            stmt.execute( "DROP TABLE IF EXISTS " + tableName);
            c.commit();
        }
    }
    
    public boolean initWorkspace( Session session) {
        
        try( InputStream in = RepositoryConnectionImpl.class.getClassLoader().getResourceAsStream( session.getWorkspace().getName() + ".yml")) {
            if( in != null) {
                new YamlImporter().importStream( session.getRoot(), in);
                
                session.commit();
                
                return true;
            }
            
            return false;
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not initialize workspace <" + session.getWorkspace().getName() + ">.", e);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Wait for asynchronous actions to terminate
        Thread.sleep( 5000l);

        this.connection.shutdown();
        ds.close();
    }
}

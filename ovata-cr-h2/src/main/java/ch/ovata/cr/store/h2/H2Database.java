/*
 * $Id: H2Database.java 2944 2020-01-27 14:14:20Z dani $
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
package ch.ovata.cr.store.h2;

import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.StoreCollection;
import ch.ovata.cr.spi.store.StoreDatabase;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import ch.ovata.cr.spi.store.TransactionManager;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.bson.MongoDbDocument;
import ch.ovata.cr.bson.MongoDbDocumentList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 *
 * @author dani
 */
public class H2Database implements StoreDatabase {

    private final H2Connection connection;
    private final BlobStore blobStore;
    private final TransactionManager transactionManager;
    private final String databaseName;
    
    public H2Database( H2Connection connection, BlobStore blobStore, String databaseName) {
        this.databaseName = databaseName;
        this.connection = connection;
        this.blobStore = blobStore;
        this.transactionManager = new H2TrxManager( this);
    }
    
    @Override
    public String getName() {
        return this.databaseName;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public StoreCollection getOrCreateCollection(String tableName) {
        if( listCollectionNames().anyMatch( tableName::equalsIgnoreCase)) {
            return new H2Collection( this, tableName);
        }

        createCollection( tableName);
        
        return new H2Collection( this, tableName);
    }
    
    private void createCollection( String tableName) {
        String fqtn = this.databaseName + "_" + tableName;
        String sql = SqlUtils.createStatement( "CREATE TABLE %s (" +
                    "NODE_ID CHAR( 36)," +
                    "REVISION BIGINT," +
                    "PARENT_ID CHAR( 36)," +
                    "NAME VARCHAR( 255)," +
                    "REMOVED BOOLEAN," +
                    "PAYLOAD CLOB NOT NULL," +
                    "PRIMARY KEY (NODE_ID, REVISION)" +
                ")", fqtn);
        
        String i1 = String.format( "CREATE INDEX %s ON %s (PARENT_ID, NAME, REVISION);", fqtn + "_idx1", fqtn);
        
        try( Connection c = getDbConnection(); Statement stmt = c.createStatement()) {
            stmt.execute( sql);
            stmt.execute( i1);
            
            c.commit();
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not create collection <" + tableName + ">.", e);
        }
    }

    @Override
    public void dropCollection(String name) {
        try( Connection c = getDbConnection()) {
            try( Statement stmt = c.createStatement()) {
                stmt.execute( SqlUtils.createStatement( "DROP TABLE %s", this.databaseName + "_" + name));
            }
            
            c.commit();
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not drop collection <" + name + ">.", e);
        }
    }

    Connection getDbConnection() throws SQLException {
        return connection.getDbConnection( this.databaseName);
    }
    
    @Override
    public Stream<String> listCollectionNames() {
        try( Connection c = getDbConnection()) {
            try( ResultSet r = c.getMetaData().getTables( c.getCatalog(), c.getSchema(), "%", null)) {
                ArrayList<String> names = new ArrayList();
                
                while( r.next()) {
                    String tableName = r.getString( 3);
                    String type = r.getString( 4);

                    if( "TABLE".equals( type) && tableName.startsWith( this.databaseName.toUpperCase() + "_")) {
                        names.add( tableName.substring( this.databaseName.length() + 1));
                    }
                }
                
                return names.stream();
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not retrieve list of collections.", e);
        }
    }

    @Override
    public long currentRevision() {
        return this.transactionManager.currentRevision();
    }

    @Override
    public BlobStore getBlobStore() {
        return this.blobStore;
    }

    @Override
    public StoreDocument newDocument() {
        return new MongoDbDocument();
    }

    @Override
    public StoreDocumentList newList() {
        return new MongoDbDocumentList();
    }
    
    @Override
    public void vacuum( String workspaceName, long revision) {
        throw new UnsupportedOperationException( "Not implemented yet.");
    }
}

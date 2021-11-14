/*
 * $Id: PostgresqlDatabase.java 2944 2020-01-27 14:14:20Z dani $
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
package ch.ovata.cr.store.postgresql;

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
public class PostgresqlDatabase implements StoreDatabase {

    private final PostgresqlConnection connection;
    private final BlobStore blobStore;
    private final TransactionManager transactionManager;
    private final String databaseName;
    
    public PostgresqlDatabase( PostgresqlConnection connection, BlobStore blobStore, String databaseName) {
        this.databaseName = databaseName;
        this.connection = connection;
        this.blobStore = blobStore;
        this.transactionManager = new PostgresqlTrxManager( this);
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
        if( listCollectionNames().anyMatch( tableName::equals)) {
            return new PostgresqlCollection( this, tableName);
        }

        createCollection( tableName);
        
        return new PostgresqlCollection( this, tableName);
    }
    
    private void createCollection( String tableName) {
        String fqtn = this.databaseName + "." + tableName;
        String fqin = this.databaseName + "_" + tableName + "_idx1";
        String sql = SqlUtils.createStatement( "CREATE TABLE %s (" +
                    "NODE_ID CHAR( 36)," +
                    "REVISION BIGINT," +
                    "STEP BIGINT," +
                    "PARENT_ID CHAR( 36)," +
                    "NAME VARCHAR( 255)," +
                    "REMOVED BOOLEAN," +
                    "PAYLOAD JSON NOT NULL," +
                    "PRIMARY KEY (NODE_ID, REVISION, STEP)" +
                ")", fqtn);
        
        String i1 = String.format( "CREATE INDEX %s ON %s (PARENT_ID, NAME, REVISION DESC, STEP DESC);", fqin, fqtn);
        
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
                stmt.execute( SqlUtils.createStatement( "DROP TABLE %s", this.databaseName + "." + name));
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
            try( ResultSet r = c.getMetaData().getTables( null, this.databaseName, "%", null)) {
                ArrayList<String> names = new ArrayList();
                
                while( r.next()) {
                    String tableName = r.getString( 3);
                    String type = r.getString( 4);

                    if( "TABLE".equals( type)) {
                        names.add( tableName);
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

/*
 * $Id: PostgresqlConnection.java 2663 2019-09-11 13:51:41Z dani $
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
import ch.ovata.cr.spi.store.StoreConnection;
import ch.ovata.cr.spi.store.StoreDatabase;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

/**
 *
 * @author dani
 */
public class PostgresqlConnection implements StoreConnection {

    private final DataSource ds;
    private final BlobStoreFactory blobStoreFactory;

    private final Map<String, PostgresqlDatabase> databases = new ConcurrentHashMap<>();
    
    public PostgresqlConnection( DataSource ds, BlobStoreFactory blobStoreFactory) {
        this.ds = ds;
        this.blobStoreFactory = blobStoreFactory;
    }

    DataSource getDataSource() {
        return this.ds;
    }

    @SuppressWarnings( "all")
    Connection getDbConnection(String databaseName) throws SQLException {
        return this.getDataSource().getConnection();
    }

    @Override
    public StoreDatabase getDatabase(String name) {
        PostgresqlDatabase database = this.databases.get( name);
        
        if( database == null) {
            database = new PostgresqlDatabase(this, this.blobStoreFactory.createBlobStore( name), name);

            this.databases.put( name, database);
        }
        
        return database;
    }

    @Override
    public StoreDatabase createDatabase(String name) {
        return getDatabase( name);
    }
    
    @Override
    public boolean checkIfDatabaseExists(String name) {
        try (Connection c = ds.getConnection()) {
            try (ResultSet r = c.getMetaData().getTables( c.getCatalog(), c.getSchema(), "%", null)) {

                while (r.next()) {
                    String tableName = r.getString(3);

                    if ( tableName.startsWith( name)) {
                        return true;
                    }
                }

                return false;
            }
        } 
        catch (SQLException e) {
            throw new RepositoryException("Could not check for database.", e);
        }
    }

    @Override
    public String getInfo() {
        return "Postgresql Connection";
    }

    @Override
    public <T> T unwrap(Class<T> c) {
        if( DataSource.class.isAssignableFrom( c)) {
            return (T)this.ds;
        }
        
        return blobStoreFactory.unwrap( c);
    }

    @Override
    public void shutdown() {
        this.databases.clear();
        this.blobStoreFactory.shutdown();
    }
}

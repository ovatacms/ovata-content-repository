/*
 * $Id: MySqlBlobStore.java 690 2017-03-02 22:03:30Z dani $
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
package ch.ovata.cr.store.mysql;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.blob.BlobStore;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import javax.sql.DataSource;

/**
 *
 * @author dani
 */
public class MySqlBlobStore implements BlobStore {

    private final DataSource ds;
    private final String databaseName;
    
    public MySqlBlobStore( DataSource ds, String databaseName) {
        this.ds = ds;
        this.databaseName = databaseName;
        
        createTable();
    }
    
    @SuppressWarnings( "all")
    Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }
    
    private void createTable() {
        try( Connection c = this.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS BLOBSTORE(" +
                            "BLOB_ID CHAR( 36)," +
                            "DB_NAME VARCHAR(255)," +
                            "FILENAME VARCHAR(512)," +
                            "CONTENT_TYPE VARCHAR(256)," +
                            "CONTENT_LENGTH BIGINT," +
                            "CONTENT LONGBLOB," +
                            "PRIMARY KEY (BLOB_ID)" +
                    ")";

            try( PreparedStatement stmt = c.prepareStatement( sql)) {
                stmt.execute();
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not create table for blob store.", e);
        }
    }
    
    @Override
    public Binary findBlob(String id) {
        String sql = "SELECT FILENAME, CONTENT_TYPE, CONTENT_LENGTH FROM BLOBSTORE WHERE (DB_NAME = ?) AND (BLOB_ID = ?)";
        
        try( Connection c = this.getConnection()) {
            try( PreparedStatement stmt = c.prepareStatement( sql)) {
                stmt.setString( 1, this.databaseName);
                stmt.setString( 2, id);
                
                try( ResultSet r = stmt.executeQuery()) {
                    if( r.next()) {
                        return new MySqlBlob( this, id, r.getString( "FILENAME"), r.getString( "CONTENT_TYPE"), r.getLong( "CONTENT_LENGTH"));
                    }
                    else {
                        throw new NotFoundException( "Could not retrieve blob <" + id + "> for database <" + this.databaseName + ">.");
                    }
                }
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not retrieve BLOB <" + id + ">.", e);
        }
    }

    @Override
    public Binary createBlob(InputStream in, String filename, String contentType) {
        return createBlob( in, -1, filename, contentType);
    }

    @Override
    public Binary createBlob(InputStream in, long contentLength, String filename, String contentType) {
        String sql = "INSERT INTO BLOBSTORE (BLOB_ID, DB_NAME, FILENAME, CONTENT_TYPE, CONTENT_LENGTH, CONTENT) VALUES (?,?,?,?,?,?)";
        String id = UUID.randomUUID().toString();
        
        try( Connection c = this.getConnection()) {
            try( PreparedStatement stmt = c.prepareStatement( sql)) {
                stmt.setString( 1, id);
                stmt.setString( 2, this.databaseName);
                stmt.setString( 3, filename);
                stmt.setString( 4, contentType);
                stmt.setLong( 5, contentLength);
                stmt.setBinaryStream( 6, in);
                
                stmt.execute();
                
                return new MySqlBlob( this, id, filename, contentType, contentLength);
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not create blob.", e);
        }
    }

    @Override
    public Binary createBlob(byte[] data, String filename, String contentType) {
        return createBlob( new ByteArrayInputStream( data), data.length, filename, contentType);
    }

    @Override
    public void deleteBlobs(Collection<String> ids) {
        String sql = "DELETE FROM BLOBSTORE WHERE (DB_NAME = ?) AND (BLOB_ID = ?)";
        
        try( Connection c = this.getConnection()) {
            try( PreparedStatement stmt = c.prepareStatement( sql)) {
                for( String id : ids) {
                    stmt.setString( 1, this.databaseName);
                    stmt.setString( 2, id);
                    
                    stmt.addBatch();
                }
                
                stmt.executeBatch();
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not delete blobs.", e);
        }
    }
}

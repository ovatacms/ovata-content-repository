/*
 * $Id: PostgresqlBlob.java 2662 2019-09-11 12:27:35Z dani $
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
package ch.ovata.cr.store.postgresql;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.tools.LimitedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author dani
 */
public class PostgresqlBlob implements Binary {

    private final String id;
    private final PostgresqlBlobStore store;
    private final String filename;
    private final String contentType;
    private final long length;
    
    public PostgresqlBlob( PostgresqlBlobStore store, String id, String filename, String contentType, long length) {
        this.id = id;
        this.store = store;
        this.filename = filename;
        this.contentType = contentType;
        this.length = length;
    }
    
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public InputStream getInputStream() {
        String sql = "SELECT CONTENT FROM BLOBSTORE WHERE BLOB_ID = ?";
        
        try( Connection c = this.store.getConnection()) {
            try( PreparedStatement stmt = c.prepareStatement( sql)) {
                stmt.setString( 1, id);
                
                try( ResultSet r = stmt.executeQuery()) {
                    if( r.next()) {
                        return r.getBinaryStream( "CONTENT");
                    }
                    else {
                        throw new IllegalStateException( "Could not retrieve input stream as this is a newly created Blob.");
                    }
                }
            }
            
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not read BLOB <"  + this.id + ">.", e);
        }
    }

    @Override
    public InputStream getInputStream(long start, Long end) {
        try {
            InputStream in = getInputStream();

            return new LimitedInputStream( in, start, end);
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not get limited input stream.", e);
        }
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public long getLength() {
        return this.length;
    }
}

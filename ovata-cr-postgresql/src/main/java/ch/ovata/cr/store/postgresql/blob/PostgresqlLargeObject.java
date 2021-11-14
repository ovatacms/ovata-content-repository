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
package ch.ovata.cr.store.postgresql.blob;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.tools.LimitedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

/**
 *
 * @author dani
 */
public class PostgresqlLargeObject implements Binary {

    private final String id;
    private final PostgresqlLargeObjectStore store;
    private final String filename;
    private final String contentType;
    private final long length;
    
    public PostgresqlLargeObject( PostgresqlLargeObjectStore store, String id, String filename, String contentType, long length) {
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
        try {
            var sql = "SELECT CONTENT FROM " + this.store.getTableName() + " WHERE BLOB_ID = ?";
            var c = this.store.getConnection();
            var lobj = c.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();

            try( PreparedStatement stmt = c.prepareStatement( sql)) {
                stmt.setString( 1, id);

                try( ResultSet r = stmt.executeQuery()) {
                    if( r.next()) {
                        var oid = r.getLong( 1);
                        var largeObject = lobj.open( oid, LargeObjectManager.READ);

                        return new LargeObjectInputStream( c, largeObject);
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
            return new LimitedInputStream( getInputStream(), start, end);
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
    
    private class LargeObjectInputStream extends InputStream {

        private final Connection connection;
        private final LargeObject largeObject;
        private final InputStream in;
        
        public LargeObjectInputStream( Connection c, LargeObject lobj) throws SQLException {
            this.connection = c;
            this.largeObject = lobj;
            this.in = this.largeObject.getInputStream();
        }
        
        @Override
        public int read() throws IOException {
            return this.in.read();
        }

        @Override
        public boolean markSupported() {
            return this.in.markSupported();
        }

        @Override
        public void reset() throws IOException {
            this.in.reset();
        }

        @Override
        public void mark(int readlimit) {
            this.in.mark( readlimit);
        }

        @Override
        public void close() throws IOException {
            try {
                this.in.close();
                this.largeObject.close();
                this.connection.close();
            }
            catch( SQLException e) {
                throw new IOException( "Could not close large object.", e);
            }
        }

        @Override
        public int available() throws IOException {
            return this.in.available();
        }

        @Override
        public void skipNBytes(long n) throws IOException {
            this.in.skipNBytes( n);
        }

        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            return this.in.readNBytes(b, off, len);
        }

        @Override
        public byte[] readNBytes(int len) throws IOException {
            return this.in.readNBytes( len);
        }

        @Override
        public byte[] readAllBytes() throws IOException {
            return this.in.readAllBytes();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return this.in.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return this.in.read( b);
        }
    }
}

/*
 * $Id: FSBlobStoreTest.java 1079 2017-10-17 19:46:11Z dani $
 * Created on 22.01.2018, 12:00:00
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
package ch.ovata.cr.blob;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.store.fs.FSBlobStoreFactory;
import ch.ovata.cr.store.postgresql.blob.PostgresqlBlobStoreFactory;
import ch.ovata.cr.store.postgresql.blob.PostgresqlLargeObjectStoreFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class BlobStoreTest {

    private byte[] data = new byte[64];

    @BeforeEach
    public void setUp() {
        for( int i = 0; i < 64; i++) {
            data[i] = (byte)i;
        }
    }
    
    @AfterEach
    public void tearDown() {
    }
    
    @Test
    public void testFilesystemBlobStore() throws Exception {
        BlobStore fsblobstore = new FSBlobStoreFactory( new File( "target")).createBlobStore( "sample-repository");
        
        testMetaData( fsblobstore);
        testRetrieval( fsblobstore);
        testRemoval( fsblobstore);
        testInputStream( fsblobstore);
        testLimitedInputStreamReadBytes( fsblobstore);
        testLimitedInputStreamReadByteArray( fsblobstore);
    }
    
    @Test
    public void testPostgresqlLargeObject() throws Exception {
        BasicDataSource ds = new BasicDataSource();
        
        ds.setDriverClassName( "org.postgresql.Driver");
        ds.setUrl( "jdbc:postgresql://localhost/ovata");
        ds.setUsername( "postgres");
        ds.setPassword( "postgres");
        ds.setInitialSize( 5);
        ds.setDefaultAutoCommit( false);
        
        dropBlobstore( ds);
        
        try {
            BlobStore psqllobstore = new PostgresqlLargeObjectStoreFactory( ds).createBlobStore( "public");
            
            testMetaData( psqllobstore);
            testRetrieval( psqllobstore);
            testRemoval( psqllobstore);
            testInputStream( psqllobstore);
            testLimitedInputStreamReadBytes( psqllobstore);
            testLimitedInputStreamReadByteArray( psqllobstore);
        }
        finally {
            ds.close();
        }
    }
    
    @Test
    public void testPostgresqlByteA() throws Exception {
        BasicDataSource ds = new BasicDataSource();
        
        ds.setDriverClassName( "org.postgresql.Driver");
        ds.setUrl( "jdbc:postgresql://localhost/ovata");
        ds.setUsername( "postgres");
        ds.setPassword( "postgres");
        ds.setInitialSize( 5);
        ds.setDefaultAutoCommit( false);
        
        dropBlobstore(ds);
        
        try {
            BlobStore psqlbyteastore = new PostgresqlBlobStoreFactory( ds).createBlobStore( "public");
            
            testMetaData( psqlbyteastore);
            testRetrieval( psqlbyteastore);
            testRemoval( psqlbyteastore);
            testInputStream( psqlbyteastore);
            testLimitedInputStreamReadBytes( psqlbyteastore);
            testLimitedInputStreamReadByteArray( psqlbyteastore);
        }
        finally {
            ds.close();
        }
    }

    private void dropBlobstore(BasicDataSource ds) throws SQLException {
        try( Connection c = ds.getConnection()) {
            try( PreparedStatement stmt = c.prepareStatement( "DROP TABLE public.blobstore")) {
                stmt.execute();
                
                c.commit();
            }
        }
    }
    
    public void testMetaData( BlobStore store) {
        Binary binary = store.createBlob( data, "sample.txt", "text/plain");

        Assertions.assertEquals( 64, binary.getLength());
        Assertions.assertEquals( "sample.txt", binary.getFilename());
        Assertions.assertEquals( "text/plain", binary.getContentType());
    }

    public void testRetrieval( BlobStore store) {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");
        String id = binary.getId();
        
        Binary binary2 = store.findBlob( id);
        
        Assertions.assertEquals( 64, binary2.getLength());
        Assertions.assertEquals( "sample.txt", binary2.getFilename());
        Assertions.assertEquals( "application/octet-stream", binary2.getContentType());
    }
    
    public void testRemoval( BlobStore store) {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");
        String id = binary.getId();

        store.deleteBlobs( Arrays.asList( id));

        Assertions.assertThrows( NotFoundException.class, () -> store.findBlob( id));
    }
    
    public void testInputStream( BlobStore store) throws IOException {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");

        try( InputStream in = binary.getInputStream()) {
            for( int i = 0; i < 64; i++) {
                int b = in.read();
                
                Assertions.assertEquals( i, b);
            }
        }
    }
    
    public void testLimitedInputStreamReadBytes( BlobStore store) throws IOException {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");

        try( InputStream in = binary.getInputStream( 5, 10l)) {
            for( int i = 5; i <= 10; i++) {
                int b = in.read();
                
                Assertions.assertEquals( i, b);
            }
        
            Assertions.assertEquals( -1, in.read());
        }
    }
    
    public void testLimitedInputStreamReadByteArray( BlobStore store) throws IOException {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");
        byte[] data = new byte[6];
        
        try( InputStream in = binary.getInputStream( 5, 10l)) {
            int len = in.read( data);
            
            Assertions.assertEquals( 6, len);
            Assertions.assertEquals( -1, in.read());
        }
    }
}

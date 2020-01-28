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
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.store.aws.S3BlobStoreFactory;
import ch.ovata.cr.store.fs.FSBlobStoreFactory;
import com.amazonaws.regions.Regions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 *
 * @author dani
 */
// TODO Fix Test and remove Ignore-Annotation
@Ignore
@RunWith( Theories.class)
public class BlobStoreTest {

    @DataPoint
    public static BlobStore fsblobstore = new FSBlobStoreFactory( new File( "target")).createBlobStore( "sample-repository");
    
    public static BasicDataSource ds;
    
    static {
        ds = new BasicDataSource();
        
        ds.setDriverClassName( "com.mysql.cj.jdbc.Driver");
        ds.setUrl( "jdbc:mysql://192.168.5.25:3307/test");
        ds.setUsername( "admin");
        ds.setPassword( "superman");
        ds.setInitialSize( 5);
    }
    
//    @DataPoint
//    public static BlobStore mysqlblobstore = new MySqlBlobStoreFactory( ds).createBlobStore( "test");
    
    @DataPoint
    public static BlobStore s3blobstore = new S3BlobStoreFactory( Regions.EU_WEST_1, "ovata-cr-bucket-dev").createBlobStore( "sample-repository");
    
    private byte[] data = new byte[64];
    
    public BlobStoreTest() {
    }

    @Before
    public void setUp() {
        for( int i = 0; i < 64; i++) {
            data[i] = (byte)i;
        }
    }
    
    @After
    public void tearDown() {
    }
    
    @Theory
    public void testMetaData( BlobStore store) {
        Binary binary = store.createBlob( new byte[] {}, "sample.txt", "text/plain");

        Assert.assertEquals( 0, binary.getLength());
        Assert.assertEquals( "sample.txt", binary.getFilename());
        Assert.assertEquals( "text/plain", binary.getContentType());
    }

    @Theory
    public void testRetrieval( BlobStore store) {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");
        String id = binary.getId();
        
        Binary binary2 = store.findBlob( id);
        
        Assert.assertEquals( 64, binary2.getLength());
        Assert.assertEquals( "sample.txt", binary2.getFilename());
        Assert.assertEquals( "application/octet-stream", binary2.getContentType());
    }
    
    @Theory
    public void testRemoval( BlobStore store) {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");
        String id = binary.getId();

        store.deleteBlobs( Arrays.asList( id));

        try {
            Binary binary2 = store.findBlob( id);
            
            binary2.getFilename();
            
            Assert.assertFalse( true);
        }
        catch( RepositoryException e) {
            // expected
        }
    }
    
    @Theory
    public void testInputStream( BlobStore store) throws IOException {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");

        try( InputStream in = binary.getInputStream()) {
            for( int i = 0; i < 64; i++) {
                int b = in.read();
                
                Assert.assertEquals( i, b);
            }
        }
    }
    
    @Theory
    public void testLimitedInputStreamReadBytes( BlobStore store) throws IOException {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");

        try( InputStream in = binary.getInputStream( 5, 10l)) {
            for( int i = 5; i <= 10; i++) {
                int b = in.read();
                
                Assert.assertEquals( i, b);
            }
        
            Assert.assertEquals( -1, in.read());
        }
    }
    
    @Theory
    public void testLimitedInputStreamReadByteArray( BlobStore store) throws IOException {
        Binary binary = store.createBlob( data, "sample.txt", "application/octet-stream");
        byte[] data = new byte[6];
        
        try( InputStream in = binary.getInputStream( 5, 10l)) {
            int len = in.read( data);
            
            Assert.assertEquals( 6, len);
            Assert.assertEquals( -1, in.read());
        }
    }
}

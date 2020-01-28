/*
 * $Id: S3BlobStoreFactory.java 708 2017-03-09 23:44:40Z dani $
 * Created on 13.12.2017, 12:00:00
 * 
 * Copyright (c) 2017 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.aws;

import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class S3BlobStoreFactory implements BlobStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger( S3BlobStoreFactory.class);
    
    private final AmazonS3Client s3client;
    private final String s3BucketName;
    
    public S3BlobStoreFactory( Regions region, String bucketName) {
        logger.info( "Creating S3 client for region <{}> and bucket <{}>.", region, bucketName);
        
        this.s3client = (AmazonS3Client)AmazonS3ClientBuilder.standard().withRegion( region).build();
        this.s3BucketName = bucketName;
    }
    
    @Override
    public BlobStore createBlobStore(String databaseName) {
        return new S3BlobStore( s3client, s3BucketName, databaseName);
    }

    @Override
    public void shutdown() {
        s3client.shutdown();
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if( AmazonS3Client.class.equals( type)) {
            return (T)this.s3client;
        }
        
        return null;
    }
}

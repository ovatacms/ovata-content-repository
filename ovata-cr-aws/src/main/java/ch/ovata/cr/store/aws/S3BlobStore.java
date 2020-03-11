/*
 * $Id: S3BlobStore.java 708 2017-03-09 23:44:40Z dani $
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
package ch.ovata.cr.store.aws;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.blob.BlobStore;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class S3BlobStore implements BlobStore {
    
    private static final Logger logger = LoggerFactory.getLogger( S3BlobStore.class);
    
    private final AmazonS3 s3client;
    private final String bucketName;
    private final String databaseName;
    
    public S3BlobStore( AmazonS3 s3client, String bucketName, String databaseName) {
        this.s3client = s3client;
        this.bucketName = bucketName;
        this.databaseName = databaseName;
    }

    @Override
    public Binary findBlob( String id) {
        return new S3Blob( this, id);
    }

    @Override
    public Binary createBlob( InputStream in, String filename, String contentType) {

        try {
            File temp = File.createTempFile( "blob_", "_upload");
            
            try {
                long length = -1l;
                
                try( FileOutputStream out = new FileOutputStream( temp)) {
                    length = IOUtils.copy(in, out);
                }

                try( FileInputStream filein = new FileInputStream( temp)) {
                    return createBlob( filein, length, filename, contentType);
                }
            }
            finally {
                Files.delete( temp.toPath());
            }
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not create Blob.", e);
        }
    }
    
    @Override
    public Binary createBlob( InputStream in, long contentLength, String filename, String contentType) {
        String id = UUID.randomUUID().toString();
        String key = createKey( id);

        logger.trace( "Creating object for content <{}> with size {} and content type <{}> with key <{}> in bucket <{}>.", filename, contentLength, contentType, key, this.bucketName);
        
        try {
            try {
                ObjectMetadata meta = new ObjectMetadata();

                meta.setContentLength( contentLength);
                meta.setContentType( contentType);
                meta.setContentDisposition( "attachment; filename=" + filename);

                s3client.putObject( this.bucketName, key, in, meta);

                return new S3Blob( this, id, filename, contentType, contentLength);
            }
            finally {
                in.close();
            }
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not create Blob.", e);
        }
    }

    @Override
    public Binary createBlob(byte[] data, String filename, String contentType) {
        return createBlob( new ByteArrayInputStream( data), data.length, filename, contentType);
    }
    
    @Override
    public void deleteBlobs( Collection<String> ids) {
        logger.trace( "Delete blobs <{}> from bucket <{}>.", ids, this.bucketName);
        
        if( !ids.isEmpty()) {
            
            DeleteObjectsRequest dr = new DeleteObjectsRequest( this.bucketName);

            dr.setKeys( ids.stream().map( id -> new KeyVersion( createKey( id))).collect( Collectors.toList()));
            dr.setQuiet( true);

            s3client.deleteObjects( dr);
        }
    }
    
    InputStream getObjectContent( String id) {
        try {
            String key = createKey( id);

            logger.trace( "Reading object content for key <{}> from bucket <{}>.", key, this.bucketName);

            GetObjectRequest request = new GetObjectRequest( this.bucketName, key);
            S3Object object = s3client.getObject( request);

            return new S3ObjectInputStreamWrapper( object);
        }
        catch( AmazonS3Exception e) {
            logger.warn( "Could not read object with key <{}> from bucket <{}>.", createKey( id), this.bucketName);
            
            throw new RepositoryException( "Could not read blob <" + id + ">.", e);
        }
    }
    
    InputStream getPartialObjectContent( String id, long start, Long end) {
        try {
            String key = createKey( id);

            logger.trace( "Reading partial object content for key <{}> from bucket <{}>. Range: {} to {}.", key, this.bucketName, start, end);

            GetObjectRequest request = new GetObjectRequest( this.bucketName, key);

            if( end == null) {
                request.setRange( start);
            }
            else {
                request.setRange( start, end);
            }

            S3Object object = s3client.getObject( request);

            return new S3ObjectInputStreamWrapper( object);
        }
        catch( AmazonS3Exception e) {
            logger.warn( "Could not read object with key <{}> from bucket <{}>.", createKey( id), this.bucketName);
            
            throw new RepositoryException( "Could not read blob <" + id + "> partially.", e);
        }
    }
    
    S3Object getS3Object( S3Blob object) {
        try {
            String key = createKey( object.getId());

            logger.trace( "Reading object for key <{}> in bucket<{}>.", key, this.bucketName);

            GetObjectRequest request = new GetObjectRequest( this.bucketName, key);

            return s3client.getObject( request);
        }
        catch( AmazonS3Exception e) {
            logger.warn( "Could not read meta-data for object with key <{}> from bucket <{}>.", createKey( object.getId()), this.bucketName);
            
            throw new RepositoryException( "Could not read blob.", e);
        }
    }
    
    private String createKey( String id) {
        return this.databaseName + "/" + id;
    }
}

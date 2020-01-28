/*
 * $Id: S3Blob.java 679 2017-02-25 10:28:00Z dani $
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
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class S3Blob implements Binary {

    private static final Logger logger = LoggerFactory.getLogger(S3Blob.class);

    private final S3BlobStore store;
    private final String id;
    private String filename;
    private String contentType;
    private long length = -1l;

    public S3Blob(S3BlobStore store, String id, String filename, String contentType, long length) {
        this.store = store;
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.length = length;
    }

    public S3Blob(S3BlobStore store, String id) {
        this.store = store;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public InputStream getInputStream() {
        return this.store.getObjectContent(id);
    }
    
    @Override
    public InputStream getInputStream( long start, Long end) {
        return this.store.getPartialObjectContent( id, start, end);
    }

    @Override
    public String getFilename() {
        if (filename == null) {
            readMetadata();
        }

        return this.filename;
    }

    @Override
    public String getContentType() {
        if (this.contentType == null) {
            readMetadata();
        }
        return this.contentType;
    }

    @Override
    public long getLength() {
        if( length == -1) {
            readMetadata();
        }
        
        return this.length;
    }

    @Override
    public String toString() {
        return "S3Blob{" + "id=" + id + '}';
    }

    private void readMetadata() {
        logger.trace("Reading meta-data for S3Object <{}>.", this.id);

        try( S3Object object = this.store.getS3Object(this)) {
            int index = object.getObjectMetadata().getContentDisposition().indexOf('=');

            this.filename = (index != -1) ? object.getObjectMetadata().getContentDisposition().substring(index + 1) : null;
            this.contentType = object.getObjectMetadata().getContentType();
            this.length = object.getObjectMetadata().getInstanceLength();
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not read object meatadata for <" + this.id + ">.", e);
        }
    }
}

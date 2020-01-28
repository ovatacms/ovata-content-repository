/*
 * $Id: BinaryWrapper.java 679 2017-02-25 10:28:00Z dani $
 * Created on 19.01.2017, 12:00:00
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
package ch.ovata.cr.impl.values;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.spi.store.blob.BlobStore;
import java.io.InputStream;

/**
 *
 * @author dani
 */
public class BinaryWrapper implements Binary {

    private final String id;
    private final String filename;
    private final String contentType;
    private final Long contentLength;
    private final BlobStore store;
    private Binary blob = null;
    
    public BinaryWrapper( String id, String filename, String contentType, Long contentLength, BlobStore store) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.store = store;
    }
    
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public InputStream getInputStream() {
        return this.getBlob().getInputStream();
    }
    
    @Override
    public InputStream getInputStream( long start, Long end) {
        return this.getBlob().getInputStream( start, end);
    }

    @Override
    public String getFilename() {
        return (this.filename != null) ? this.filename : getBlob().getFilename();
    }

    @Override
    public String getContentType() {
        return (this.contentType != null) ? this.contentType : getBlob().getContentType();
    }

    @Override
    public long getLength() {
        return (this.contentLength != null) ? this.contentLength : getBlob().getLength();
    }
    
    private Binary getBlob() {
        if( blob == null) {
            blob = store.findBlob( this.id);
        }
        
        return this.blob;
    }

    @Override
    public String toString() {
        return "BinaryWrapper{" + "id=" + id + ", filename=" + filename + ", contentType=" + contentType + ", contentLength=" + contentLength + '}';
    }
}

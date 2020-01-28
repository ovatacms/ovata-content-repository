/*
 * $Id: S3ObjectInputStreamWrapper.java 679 2017-02-25 10:28:00Z dani $
 * Created on 22.01.2017, 12:00:00
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

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class S3ObjectInputStreamWrapper extends InputStream {

    private static final Logger logger = LoggerFactory.getLogger( S3ObjectInputStreamWrapper.class);
    
    private final S3Object s3object;
    private final S3ObjectInputStream stream;
    
    public S3ObjectInputStreamWrapper( S3Object s3object) {
        this.s3object = s3object;
        this.stream = s3object.getObjectContent();
    }
    
    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        stream.mark( readlimit);
    }

    @Override
    public void close() throws IOException {
        logger.trace( "Closing s3object <{}> through input stream wrapper.", this.s3object.getKey());
        
        stream.close();
        s3object.close();
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip( n);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read( b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return stream.read( b);
    }
}

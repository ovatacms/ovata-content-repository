/*
 * $Id: LimitedInputStream.java 1079 2017-10-17 19:46:11Z dani $
 * Created on 19.01.2018, 12:00:00
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
package ch.ovata.cr.tools;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author dani
 */
public class LimitedInputStream extends InputStream {
    
    private final InputStream fis;
    private long position;
    private final long end;
    
    public LimitedInputStream( InputStream in, long start, Long end) throws IOException {
        this.fis = in;
        this.position = start;
        this.end = (end != null) ? end : Long.MAX_VALUE;
        
        long skipped = this.fis.skip( start);
        
        if( skipped != start) {
            throw new IOException( "Could not skip required bytes <" + start + "> but only <" + skipped + ">.");
        }
    }

    @Override
    public int read() throws IOException {
        if( position > end) {
            return -1;
        }
        
        this.position++;
        
        return this.fis.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen = (int)Math.min( (long)len, (this.end - this.position) + 1);
        
        if( readLen <= 0) {
            return -1;
        }
        
        int result = this.fis.read( b, off, readLen);
        
        this.position += result;
        
        return result;
    }

    @Override
    public void close() throws IOException {
        this.fis.close();
    }
}

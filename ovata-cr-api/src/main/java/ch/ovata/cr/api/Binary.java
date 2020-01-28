/*
 * $Id: Binary.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.api;

import java.io.InputStream;

/**
 *
 * @author dani
 */
public interface Binary {
    /**
     * Returns the unique id of this binary
     * @return the unique id
     */
    String getId();
    
    /**
     * Returns a <code>InputStream</code> to retrieve the contents of this binary
     * @return the input stream
     */
    InputStream getInputStream();
    
    /**
     * Returns an <code>InputStream</code> to retrieve a part of the content of this binary
     * @param start first byte to retrieve
     * @param end optional last byte (inclusive) to retrieve
     * @return the input stream
     */
    InputStream getInputStream( long start, Long end);
    
    /**
     * Returns the filename associated with this binary
     * @return the filename
     */
    String getFilename();
    
    /**
     * Returns the content type of this binary
     * @return the content type
     */
    String getContentType();
    
    /**
     * Returns the size of the binary or -1 if not known
     * @return the size of the binary
     */
    long getLength();
}

/*
 * $Id: IOUtils.java 1253 2018-02-15 21:30:42Z dani $
 * Created on 13.12.2016, 12:00:00
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
package ch.ovata.cr.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author dani
 */
public final class IOUtils {
    
    private static final int BUFFER_SIZE = 8 * 1024;
    
    private IOUtils() {
    }
    
    public static long copy( InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[ BUFFER_SIZE];
        long count = 0;
        int len;

        while( (len = in.read(buffer)) != -1 ) {
            out.write( buffer, 0, len);
            count += len;
        }
        
        return count;
    }
}

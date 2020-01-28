/*
 * $Id: IllegalImporterStateException.java 2712 2019-09-28 09:40:17Z dani $
 * Created on 06.07.2019, 12:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.utils;

import javax.json.stream.JsonParser;

/**
 *
 * @author dani
 */
public class IllegalImporterStateException extends IllegalStateException {
    
    public IllegalImporterStateException( String message, JsonParser reader) {
        super( message + " @ (" + reader.getLocation().getLineNumber() + ":" + reader.getLocation().getColumnNumber() + ").");
    }
    
    public IllegalImporterStateException( String message) {
        super( message);
    }
    
    public IllegalImporterStateException( String message, Throwable cause) {
        super( message, cause);
    }
}

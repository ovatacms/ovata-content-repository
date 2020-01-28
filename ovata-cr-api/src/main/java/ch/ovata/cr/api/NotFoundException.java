/*
 * $Id: NotFoundException.java 679 2017-02-25 10:28:00Z dani $
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

/**
 * Raised if a node or property could not be found.
 * @author dani
 */
public class NotFoundException extends RepositoryException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException( String message, Throwable cause) {
        super( message, cause);
    }
}

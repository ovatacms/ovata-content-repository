/*
 * $Id: NotAuthorizedException.java 679 2017-02-25 10:28:00Z dani $
 * Created on 06.12.2016, 21:00:00
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
 *
 * @author dani
 */
public class NotAuthorizedException extends RepositoryException {
    
    public NotAuthorizedException(String message) {
        super(message);
    }
    
    public NotAuthorizedException( String message, Throwable cause) {
        super( message, cause);
    }
}

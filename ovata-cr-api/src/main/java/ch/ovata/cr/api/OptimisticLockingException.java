/*
 * $Id: OptimisticLockingException.java 679 2017-02-25 10:28:00Z dani $
 * Created on 05.01.2017, 12:00:00
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
package ch.ovata.cr.api;

/**
 * Raised if the changes in a session could not be persisted due to a conflict with
 * another concurrent write operation.
 * @author dani
 */
public class OptimisticLockingException extends RepositoryException {
    
    public OptimisticLockingException(String message) {
        super(message);
    }
    
    public OptimisticLockingException( String message, Throwable cause) {
        super( message, cause);
    }
}

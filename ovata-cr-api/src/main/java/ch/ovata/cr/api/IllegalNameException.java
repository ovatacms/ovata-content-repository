/*
 * $Id: IllegalNameException.java 2014 2018-10-06 11:04:29Z dani $
 * Created on 06.10.2018, 12:00:00
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
package ch.ovata.cr.api;

/**
 *
 * @author dani
 */
public class IllegalNameException extends RepositoryException {
    
    public IllegalNameException(String message) {
        super(message);
    }
}

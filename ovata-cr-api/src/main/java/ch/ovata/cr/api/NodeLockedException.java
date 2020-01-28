/*
 * $Id: NodeLockedException.java 1456 2018-05-07 10:54:36Z dani $
 * Created on 04.05.2018, 222:00:00
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
public class NodeLockedException extends RepositoryException {
    
    private final String username;
    
    public NodeLockedException( String message, String username) {
        super( message);
        
        this.username = username;
    }
    
    public String getUsername() {
        return this.username;
    }
}

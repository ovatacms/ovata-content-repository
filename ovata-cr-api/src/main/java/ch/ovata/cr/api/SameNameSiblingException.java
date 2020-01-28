/*
 * $Id: SameNameSiblingException.java 2944 2020-01-27 14:14:20Z dani $
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
 * Raised if one tries to add a node with the same name as a sibling of the node added.
 * @author dani
 */
public class SameNameSiblingException extends RepositoryException {
    
    public SameNameSiblingException(String message) {
        super(message);
    }
    
}

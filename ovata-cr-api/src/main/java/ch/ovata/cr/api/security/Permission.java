/*
 * $Id: Permission.java 679 2017-02-25 10:28:00Z dani $
 * Created on 06.12.2016, 19:00:00
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
package ch.ovata.cr.api.security;

/**
 *
 * @author dani
 */
public enum Permission {
    /**
     * Allows to read the subtree
     */
    READ,
    /**
     * Allows to modify properties on nodes on the subtree
     */
    WRITE,
    /**
     * Allows to add nodes
     */
    ADD,
    /**
     * Allows to remove nodes
     */
    REMOVE,
    /**
     * Allows to modify the access control list of a node
     */
    MODIFY_ACL
}

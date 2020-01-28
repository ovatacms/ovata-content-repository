/*
 * $Id: Roles.java 679 2017-02-25 10:28:00Z dani $
 * Created on 03.08.2017, 20:21:00
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
public interface Roles {
    /**
     * An administrator has always full control over the content of a workspace, irrespective of the ACL.
     */
    String ADMINISTRATOR = "administrator";
    
    /**
     * This role represents any user, even unauthenticated ones.
     */
    String EVERYONE = "everyone";
    
    /**
     * This is the system user. System functions access the repository with this subject for reading configuration.
     * (e.g. passwords, email, etc.)
     */
    String SYSTEM = "system";
    
    /**
     * Every authenticated user automatically gets this role assigend.
     */
    String USER = "user";
    
    /**
     * A template developer using special commands to create templates, components and forms
     */
    String DEVELOPER = "developer";
    
    /**
     * A content author generating pages or custom content
     */
    String AUTHOR = "author";
    
    /**
     * A publisher make information publicly available
     */
    String PUBLISHER = "publisher";
    
    /**
     * Path to the administrator role in the system_users workspace.
     */
    String ADMINISTRATOR_PATH = "/roles/administrator";
}

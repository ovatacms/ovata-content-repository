/*
 * $Id: Workspace.java 690 2017-03-02 22:03:30Z dani $
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A session interacts with the persistent store through a workspace instance.
 * @author dani
 */
public interface Workspace {

    String WORKSPACE_CONFIGURATION = "system_configuration";
    String WORKSPACE_USERS = "system_users";
    String WORKSPACE_APPLICATIONS = "system_applications";
    
    /**
     * Returns the name of the current workspace
     * @return the name of the workspace
     */
    String getName();
    
    /**
     * Re-Index this workspace
     * @param repository the repository used to traverse the node tree of the workspace (beware of ACLs)
     */
    void reindex( Repository repository);
    
    /**
     * Remove old versions up to the specified revision
     * @param repository the repository used to access the workspace
     * @param revision oldest revision to keep
     */
    void vacuum( Repository repository, long revision);
    
    static Pattern NAME_PATTERN = Pattern.compile( "[A-Za-z_\\$][A-Za-z0-9_\\$]*");
    
    public static void validateName( String name) {
        if( (name == null) || name.trim().isEmpty()) {
            throw new IllegalNameException( "Name may not be null or blank.");
        }

        Matcher matcher = NAME_PATTERN.matcher( name);
        
        if( !matcher.matches()) {
            throw new IllegalNameException( "Name not valid: <" + name + ">.");
        }
    }
}

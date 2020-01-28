/*
 * $Id: Repository.java 679 2017-02-25 10:28:00Z dani $
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

import ch.ovata.cr.spi.store.Transaction;
import ch.ovata.cr.spi.store.TransactionManager;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.Subject;

/**
 *
 * @author dani
 */
public interface Repository {
    
    /**
     * Retrieves the name of the repository
     * @return name of the repository
     */
    String getName();
    
    /**
     * Retrieves the subject of the currently authenticated user
     * @return the current subject
     */
    Subject getSubject();

    /**
     * @return The principal logged in to the repository
     */
    Principal getPrincipal();
    
    /**
     * Retrieves the transaction manager responsible for this repository
     * @return the responsible transaction manager
     */
    TransactionManager getTransactionMgr();
    
    /**
     * Lists all available workspaces in this repository
     * @return collection of workspace names
     */
    Collection<String> listWorkspaceNames();
    
    /**
     * Tests if the given workspace exists
     * @param workspaceName the name of the workspace
     * @return true if the workspace exists
     */
    boolean hasWorkspace( String workspaceName);
    
    /**
     * Retrieves a workspace in the current repository
     * @param workspaceName name of the workspace
     * @return session referencing the workspace with the given name
     * @throws NotFoundException if the workspace does not exist
     */
    Session getSession( String workspaceName);

    /**
     * Retrieves a read-only session to view the content of the repository at the given revision
     * @param workspaceName name of the workspace
     * @param revision id of the revision to view
     * @return read-only session of the repository at the given revision
     */
    Session getSession( String workspaceName, long revision);

    /**
     * Creates and initializes a workspace with the given name
     * @param workspaceName name of the workspace to create
     * @return Session referencing the new workspace
     */    
    Session createWorkspace( String workspaceName);
    
    /**
     * Drops an entire workspace.
     * @param workspaceName name of the workspace to drop
     */
    void dropWorkspace( String workspaceName);
    
    /**
     * Retrieves the commit history of a specific worksapce in this repository
     * @param workspaceName name of the workspace
     * @return the list of relevant transactions
     */
    List<Transaction> getHistory( String workspaceName);
    
    /**
     * Unwrap blob store or other composite objects
     * @param <T> type to unwrap
     * @param type type to unwrap
     * @return the unwrap object of given type
     */
    <T> T unwrap( Class<T> type);
    
    /**
     * Checks if the user is logged in as administrator
     * @return true if the user is an administrator
     */
    boolean isAdministrator();

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

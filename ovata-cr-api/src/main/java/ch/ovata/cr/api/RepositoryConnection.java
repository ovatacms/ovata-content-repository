/*
 * $Id: RepositoryConnection.java 715 2017-03-14 22:13:40Z dani $
 * Created on 28.11.2016, 12:00:00
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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

/**
 *
 * @author dani
 */
public interface RepositoryConnection {

    /**
     * Login to the repository with the given username and password.
     * Uses the configured JAAS stack for authentication.
     * @param repositoryName name of the repository to login to
     * @param username the username
     * @param password the password
     * @return the Repository when successfully logged in
     * @throws LoginException if the authentication fails
     */
    Repository login( String repositoryName, String username, char[] password) throws LoginException;

    /**
     * Login to the repository with the anonymous user in role everyone
     * @param repositoryName name of the repository to login to
     * @return the Repository when successfully logged in
     */
    Repository login(String repositoryName);
    
    /**
     * Login to the repository as system user in role system
     * @param repositoryName name of the repository to login to
     * @return the requested repository authenticated as system
     */
    Repository loginAsSystem( String repositoryName);

    /**
     * Login to the repository as a user in the role administrator.
     * This is used to act-on-behalf of someone
     * @param repositoryName name of the repository to login to
     * @param username name of the user to login
     * @return the requested repository authenticated as system
     */
    Repository loginAsAdministrator( String repositoryName, String username);
    
    /**
     * Login to the repository as the given subject.
     * @param repositoryName the name of the repository to login to
     * @param subject the subject to use
     * @return the requested repository authenticated with the given subject
     */
    Repository loginAsSubject( String repositoryName, Subject subject);
    
    /**
     * Create and initialize a new repository
     * @param repositoryName name of the repository
     * @param username name of the initial administrator
     * @param password password of the initial administrator
     * @return the newly created repository
     */
    Repository create( String repositoryName, String username, String password);
    
    /**
     * Unwraps an implementation specific object (e.g. underlying database connection)
     * @param <T> type of the object to unwrap
     * @param c class of the type to unwrap
     * @return the unwrapped object or null if unavailable
     */
    <T> T unwrap( Class<T> c);
    
    /**
     * Shut down the repository connection
     */
    void shutdown();
}

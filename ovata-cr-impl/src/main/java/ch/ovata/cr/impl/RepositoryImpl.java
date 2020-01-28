/*
 * $Id: RepositoryImpl.java 690 2017-03-02 22:03:30Z dani $
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Roles;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Session.Mode;
import ch.ovata.cr.api.Workspace;
import ch.ovata.cr.api.security.RolePrincipal;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.api.security.UserPrincipal;
import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.store.StoreDatabase;
import ch.ovata.cr.spi.store.Transaction;
import ch.ovata.cr.spi.store.TransactionManager;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.security.auth.Subject;

/**
 *
 * @author dani
 */
public class RepositoryImpl implements Repository {

    private static final Set<String> SYSTEM_WORKSPACES = new HashSet<>( Arrays.asList( Workspace.WORKSPACE_CONFIGURATION, Workspace.WORKSPACE_USERS, Workspace.WORKSPACE_APPLICATIONS));
    
    private final String name;
    private final StoreDatabase database;
    private final Subject subject;
    private final SearchProvider searchProvider;
    
    public RepositoryImpl( String name, StoreDatabase database, Subject subject, SearchProvider searchProvider) {
        this.name = name;
        this.database = database;
        this.subject = subject;
        this.searchProvider = searchProvider;
    }
    
    public StoreDatabase getDatabase() {
        return this.database;
    }
    
    @Override
    public TransactionManager getTransactionMgr() {
        return this.database.getTransactionManager();
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Principal getPrincipal() {
        return this.subject.getPrincipals( UserPrincipal.class).iterator().next();
    }
    
    @Override
    public Session createWorkspace( String workspaceName) {
        Workspace.validateName( workspaceName);
        
        if( listAllWorkspaceNames().contains( workspaceName)) {
            throw new RepositoryException( "Workspace with name <" + workspaceName + "> already exists.");
        }

        SessionImpl session =  new SessionImpl( this, workspaceName, Mode.UPDATEABLE);
        
        ((WorkspaceImpl)session.getWorkspace()).initWorkspace();

        return session;
    }
    
    @Override
    public void dropWorkspace( String workspaceName) {
        if( !listAllWorkspaceNames().contains( workspaceName)) {
            throw new RepositoryException( "Workspace with name <" + workspaceName + "> does not exist.");
        }
        
        if( SYSTEM_WORKSPACES.contains( workspaceName)) {
            throw new RepositoryException( "Cannot drop system workspace <" + workspaceName + ">.");
        }
        
        this.database.dropCollection( workspaceName);
    }
    
    @Override
    public Session getSession(String workspaceName) {
        checkWorkspace( workspaceName);
        
        return new SessionImpl( this, workspaceName, Session.Mode.UPDATEABLE);
    }

    @Override
    public Session getSession(String workspaceName, long revision) {
        checkWorkspace( workspaceName);
        
        return new SessionImpl( this, workspaceName, revision);
    }

    @Override
    public Subject getSubject() {
        return this.subject;
    }

    long currentRevisionId() {
        return this.database.currentRevision();
    }

    @Override
    public boolean hasWorkspace( String workspaceName) {
        return listAllWorkspaceNames().contains( workspaceName);
    }
    
    @Override
    public Collection<String> listWorkspaceNames() {
        ArrayList<String> names = new ArrayList<>();
        Collection<String> workspaceNames = listAllWorkspaceNames();
        
        for( String workspaceName : workspaceNames) {
            try {
                this.getSession( workspaceName).getRoot();
                
                names.add( workspaceName);
            }
            catch( NotFoundException e) {
                // Don't list workspaces the currently authenticated user may not read
            }
        }
        
        return names;
    }
    
    private Collection<String> listAllWorkspaceNames() {
        return this.database.listCollectionNames().filter( n -> !n.startsWith( "system_transactions") && !n.startsWith( "BLOBSTORE") && !n.startsWith( "fs.")).sorted( (n1, n2) -> n1.compareTo( n2)).collect( Collectors.toSet());
    }
    
    public BlobStore getBlobStore() {
        return this.database.getBlobStore();
    }
    
    public SearchProvider getSearchProvider() {
        return this.searchProvider;
    }
    
    private void checkWorkspace( String workspaceName) {
        if( this.database.listCollectionNames().noneMatch( workspaceName::equals) ) {
            throw new NotFoundException( "Did not find workspace with name <" + workspaceName + ">.");
        }
    }

    @Override
    public List<Transaction> getHistory(String workspaceName) {
        return this.getTransactionMgr().getTransactions( workspaceName, 0);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if( BlobStore.class.isAssignableFrom( type)) {
            return (T)this.getBlobStore();
        }
        
        return null;
    }

    @Override
    public boolean isAdministrator() {
        return this.subject.getPrincipals( RolePrincipal.class).stream().anyMatch( r -> Roles.ADMINISTRATOR.equals( r.getName()));
    }
}

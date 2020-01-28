/*
 * $Id: RepositoryConnectionImpl.java 715 2017-03-14 22:13:40Z dani $
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

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.RepositoryConnection;
import javax.security.auth.login.LoginException;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Roles;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.Workspace;
import ch.ovata.cr.api.security.Acl;
import ch.ovata.cr.api.security.Permission;
import ch.ovata.cr.api.security.RolePrincipal;
import ch.ovata.cr.api.security.UserPrincipal;
import ch.ovata.cr.impl.security.JaasConfiguration;
import ch.ovata.cr.impl.security.ModifiableAcl;
import ch.ovata.cr.tools.PasswordChecker;
import ch.ovata.cr.impl.security.SimpleCallbackHandler;
import ch.ovata.cr.spi.search.SearchProviderFactory;
import ch.ovata.cr.spi.store.StoreConnection;
import ch.ovata.cr.spi.store.StoreDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import javax.security.auth.Subject;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class RepositoryConnectionImpl implements RepositoryConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(RepositoryConnectionImpl.class);
    
    private final StoreConnection connection;
    private final SearchProviderFactory searchProviderFactory;
    
    public RepositoryConnectionImpl( StoreConnection connection, SearchProviderFactory searchProviderFactory) {
        logger.info( "Initializing content repository with connection <{}>.", connection.getInfo());
        
        this.connection = connection;
        this.searchProviderFactory = searchProviderFactory;
    }
    
    public StoreDatabase getDatabase( String name) {
        return connection.getDatabase( name);
    }
    
    public StoreConnection getConnection() {
        return this.connection;
    }
    
    @Override
    public Repository login( String repositoryName, String username, char[] password) throws LoginException {
        Repository repository = this.loginAsSystem( repositoryName);
        LoginContext context = new LoginContext( "jaasLogin", null, new SimpleCallbackHandler( username, password), new JaasConfiguration( repository));

        context.login();

        return this.loginAsSubject( repositoryName, context.getSubject());
    }
    
    @Override
    public Repository login( String repositoryName) {
        return this.loginAsSubject( repositoryName, createAnonymous());
    }
    
    @Override
    public Repository loginAsSystem( String repositoryName) {
        return this.loginAsSubject( repositoryName, createSystem());
    }

    @Override
    public Repository loginAsAdministrator( String repositoryName, String username) {
        return this.loginAsSubject( repositoryName, createAdministrator( username));
    }
    
    @Override
    public Repository loginAsSubject( String repositoryName, Subject subject) {
        checkIfDatabaseExists( repositoryName);
        
        return new RepositoryImpl( repositoryName, connection.getDatabase( repositoryName), subject, searchProviderFactory.createSearchProvider( repositoryName));
    }
    
    private void checkIfDatabaseExists( String name) {
        if( (name == null) || name.isEmpty() || !this.connection.checkIfDatabaseExists( name)) {
            throw new NotFoundException( "Cannot login to repository <" + name + ">.");
        }
    }
    
    @Override
    public Repository create( String repositoryName, String username, String password) {
        Repository.validateName( repositoryName);

        try {
            if( this.connection.checkIfDatabaseExists( repositoryName)) {
                throw new RepositoryException( "Repository with name <" + repositoryName + "> already exists.");
            }

            Repository repository = new RepositoryImpl( repositoryName, connection.createDatabase( repositoryName), createAnonymous(), searchProviderFactory.createSearchProvider( repositoryName));

            repository.createWorkspace( Workspace.WORKSPACE_CONFIGURATION);
            repository.createWorkspace( Workspace.WORKSPACE_USERS);
            
            Repository systemRepo = this.loginAsAdministrator( repositoryName, "system");
            
            setupSystemUsers( systemRepo, username, password);
            setupSystemConfiguration( systemRepo);

            return this.login( repositoryName, username, password.toCharArray());
        }
        catch( LoginException e) {
            throw new RepositoryException( "Could not login to newly created repository.", e);
        }
    }

    @Override
    public <T> T unwrap(Class<T> c) {
        return this.connection.unwrap( c);
    }
    
    @Override
    public void shutdown() {
        this.connection.shutdown();
        this.searchProviderFactory.shutdown();
    }
    
    private Subject createAnonymous() {
        Subject subject = new Subject();
        
        subject.getPrincipals().add( new UserPrincipal( "anonymous"));
        subject.getPrincipals().add( new RolePrincipal( Roles.EVERYONE));
        
        subject.setReadOnly();

        return subject;
    }
    
    private Subject createSystem() {
        Subject subject = new Subject();
        
        subject.getPrincipals().add( new UserPrincipal( "system"));
        subject.getPrincipals().add( new RolePrincipal( Roles.SYSTEM));
        
        subject.setReadOnly();
        
        return subject;
    }
    
    private Subject createAdministrator( String username) {
        Subject subject = new Subject();
        
        subject.getPrincipals().add( new UserPrincipal( username));
        subject.getPrincipals().add( new RolePrincipal( Roles.EVERYONE));
        subject.getPrincipals().add( new RolePrincipal( Roles.ADMINISTRATOR));
        
        subject.setReadOnly();
        
        return subject;
    }

    public void setupSystemUsers( Repository repository, String username, String password) {
        try {
            Session session = repository.getSession( Workspace.WORKSPACE_USERS);

            Node rolesRoot = session.getRoot().getOrAddNode( "roles", CoreNodeTypes.FOLDER);
            Node usersRoot = session.getRoot().getOrAddNode( "users", CoreNodeTypes.FOLDER);
            
            rolesRoot.getOrAddNode( Roles.ADMINISTRATOR, CoreNodeTypes.ROLE);
            rolesRoot.getOrAddNode( Roles.EVERYONE, CoreNodeTypes.ROLE);
            rolesRoot.getOrAddNode( Roles.SYSTEM, CoreNodeTypes.ROLE);
            rolesRoot.getOrAddNode( Roles.USER, CoreNodeTypes.ROLE);
            rolesRoot.getOrAddNode( Roles.DEVELOPER, CoreNodeTypes.ROLE);
            rolesRoot.getOrAddNode( Roles.AUTHOR, CoreNodeTypes.ROLE);
            rolesRoot.getOrAddNode( Roles.PUBLISHER, CoreNodeTypes.ROLE);
            
            Value adminRoleRef = session.getValueFactory().referenceByPath( Workspace.WORKSPACE_USERS, Roles.ADMINISTRATOR_PATH);
            Value roles = session.getValueFactory().of( Arrays.asList( adminRoleRef));

            Node user = usersRoot.addNode( username, "ovata:user");

            user.setProperty( "roles", roles);
            user.setProperty( "password", session.getValueFactory().of( generatePasswordToken( password)));

            restrictRootNodeAcl( session);

            session.commit( "Initialized system_users.");
        }
        catch( CredentialException e) {
            throw new RepositoryException( "Could not create new repository.", e);
        }
    }
    
    private void setupSystemConfiguration( Repository repository) {
        Session session = repository.getSession( Workspace.WORKSPACE_CONFIGURATION);
        Node security = session.getRoot().getOrAddNode( "security", CoreNodeTypes.FOLDER);

        Node jaas = security.getOrAddNode( "jaas", CoreNodeTypes.FOLDER);
        Node sso = security.getOrAddNode( "sso", CoreNodeTypes.FOLDER);
        
        Node loginModule = jaas.getOrAddNode( "ovata_login_module", CoreNodeTypes.JAAS_ENTRY);
        
        loginModule.setProperty( "name", session.getValueFactory().of( "ch.ovata.cr.impl.security.OvataLoginModule"));
        loginModule.setProperty( "order", session.getValueFactory().of( 0l));
        loginModule.setProperty( "flag", session.getValueFactory().of( "SUFFICIENT"));
        
        restrictRootNodeAcl( session);

        session.commit( "Initialized system_configuration.");
    }
    
    private void restrictRootNodeAcl( Session session) {
        Node root = session.getRoot();
        List<Acl> acls = new ArrayList<>( root.getPolicy().getAcls());
        ListIterator<Acl> i = acls.listIterator();
        
        while( i.hasNext()) {
            if( i.next().getPrincipal().getName().equals( Roles.EVERYONE)) {
                i.remove();
            }
        }
        
        acls.add( new ModifiableAcl( new RolePrincipal( Roles.SYSTEM), false, Arrays.asList( Permission.READ)));

        root.getPolicy().updateAcls( acls);
    }
    
    private String generatePasswordToken( String password) throws CredentialException {
        return PasswordChecker.instance().generateToken( "sha256", password);
    }
}

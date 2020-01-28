/*
 * $Id: OvataLoginModule.java 708 2017-03-09 23:44:40Z dani $
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
package ch.ovata.cr.impl.security;

import ch.ovata.cr.tools.PasswordChecker;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Roles;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.Workspace;
import ch.ovata.cr.api.security.RolePrincipal;
import ch.ovata.cr.api.security.UserPrincipal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class OvataLoginModule implements LoginModule {

    private static final Logger logger = LoggerFactory.getLogger( OvataLoginModule.class);
    
    private Map<String, ?> options;
    private Subject subject;
    private CallbackHandler callbackHandler;
    
    private String username;
    private final List<String> roleNames = new ArrayList<>();
    
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.options = options;
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        try {
            NameCallback nc = new NameCallback( "username");
            PasswordCallback pc = new PasswordCallback( "password", false);

            callbackHandler.handle( new Callback[] { nc, pc });

            String uid = nc.getName();
            
            logger.debug( "Authenticating user <{}>.", uid);
            
            Repository repository = (Repository)this.options.get( "repository");
            Session session = repository.getSession( Workspace.WORKSPACE_USERS);
        
            Node user = session.getNodeByPath( "/users/" + uid);
            
            if( checkPassword( user, pc)) {
                this.username = user.getName();
                
                Value[] roles = user.findProperty( "roles").map( p -> p.getValue().getArray()).orElse( new Value[0]);
                
                for( Value v : roles) {
                    this.roleNames.add( v.getReference().getNode().getName());
                }
            
                return true;
            }
            else {
                return false;
            }
        }
        catch( IOException | UnsupportedCallbackException e) {
            throw new LoginException( "Could not login.");
        }
        catch( NotFoundException e) {
            logger.info( "User not found.");
            return false;
        }
    }

    private boolean checkPassword( Node user, PasswordCallback pc) throws FailedLoginException {
        String pwd = new String( pc.getPassword());
        String token = user.getProperty( "password").getValue().getString();
        
        return PasswordChecker.instance().verifyPassword( pwd, token);
    }
    
    @Override
    public boolean commit() throws LoginException {
        this.subject.getPrincipals().add( new UserPrincipal( this.username));
        this.subject.getPrincipals().add( new RolePrincipal( Roles.EVERYONE));
        this.subject.getPrincipals().add( new RolePrincipal( Roles.USER));
        
        for( String rolename : this.roleNames) {
            logger.debug( "Assigning role <{}> to user <{}>.", rolename, this.username);
            this.subject.getPrincipals().add( new RolePrincipal( rolename));
        }
        
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        this.username = null;
        this.roleNames.clear();
        
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        this.username = null;
        this.roleNames.clear();
        
        return true;
    }
}

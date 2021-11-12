/*
 * $Id: AccessPolicyTest.java 690 2017-03-02 22:03:30Z dani $
 * Created on 08.12.2016, 21:00:00
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
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.security.Acl;
import ch.ovata.cr.api.security.Permission;
import ch.ovata.cr.api.security.RolePrincipal;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.login.LoginException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class AccessPolicyTest extends AbstractOvataRepositoryTest {
    
    public static class SimpleAcl implements Acl {

        private final Principal principal;
        private final boolean isNegative;
        private final Set<Permission> permissions = new HashSet<>();
        
        public SimpleAcl( Principal p, boolean isNegative, Collection<Permission> permissions) {
            this.principal = p;
            this.isNegative = isNegative;
            this.permissions.addAll( permissions);
        }
        
        @Override
        public Principal getPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isNegative() {
            return this.isNegative;
        }

        @Override
        public Set<Permission> getPermissions() {
            return this.permissions;
        }
    }
    
    @Test
    public void testAddAcl() throws LoginException {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);

        Node root = session.getRoot();
        Node dam = root.addNode( "assets", CoreNodeTypes.FOLDER);
        Node other = root.addNode( "other", CoreNodeTypes.FOLDER);
        
        String nodeId = dam.getId();
        
        dam.getPolicy().updateAcls( Arrays.asList( new SimpleAcl( new RolePrincipal( "everyone"), true, Arrays.asList( Permission.READ))));
        
        session.commit();
        
        session = repository.getSession( TEST_WORKSPACE_NAME);
        
        Repository anonymousRepo = connection.login( TEST_REPO_NAME);
        Session testSession = anonymousRepo.getSession( TEST_WORKSPACE_NAME);
        
        try {
            dam = testSession.getNodeByPath( "/assets");
            
            Assertions.assertTrue( false);
        }
        catch( NotFoundException e) {
            // Expected
        }
        
        try {
            dam = testSession.getNodeByIdentifier( nodeId);
            
            Assertions.assertTrue( false);
        }
        catch( NotFoundException e) {
            // Expected
        }
        
        Collection<? extends Node> nodes = testSession.getRoot().getNodes();
        
        Assertions.assertEquals( 1, nodes.size());
        
        Repository repository2 = connection.login( TEST_REPO_NAME, TEST_DATABASE_USERNAME, TEST_DATABASE_PASSWORD.toCharArray());
        
        session = repository2.getSession( "test");
        dam = session.getNodeByPath( "/assets");
        dam = session.getNodeByIdentifier( nodeId);
        
        nodes = session.getRoot().getNodes();
        
        Assertions.assertEquals( 2, nodes.size());
    }
}

/*
 * $Id: NodeLockTest.java 1457 2018-05-07 10:56:06Z dani $
 * Created on 07.05.2018, 12:00:00
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeLockedException;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class NodeLockTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testNodeLockInfoTransient() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        
        Assertions.assertFalse( dani.isLocked());
        
        dani.lock();
        
        Assertions.assertTrue( dani.isLocked());
        
        Optional<LockInfo> info = dani.getLockInfo();
        
        Assertions.assertTrue( info.isPresent());
        Assertions.assertEquals( TEST_DATABASE_USERNAME, info.get().getPrincipal().getName());
        Assertions.assertEquals( TEST_REPO_NAME, info.get().getRepositoryName());
        Assertions.assertEquals( TEST_WORKSPACE_NAME, info.get().getWorkspaceName());
        Assertions.assertEquals( dani.getId(), info.get().getNodeId());
        
        session.rollback();
        
        Assertions.assertFalse( dani.isLocked());
    }
    
    @Test
    public void testNodeLockInfoPersistent() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        
        dani.lock();
        
        Assertions.assertTrue( dani.isLocked());
        
        session.commit();
        
        session = repository.getSession( TEST_WORKSPACE_NAME);
        
        dani = session.getNodeByPath( "/users/dani");
        
        Assertions.assertFalse( dani.isLocked());
    }
    
    @Test
    public void testConcurrentLocking() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);
        
        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        
        session.commit();
        
        session = repository.getSession( TEST_WORKSPACE_NAME);
        
        Repository repo2 = this.getRepositoryAsAnonymous();
        Session session2 = repo2.getSession( TEST_WORKSPACE_NAME);
        
        Node dani1 = session.getNodeByPath( "/users/dani");
        Node dani2 = session2.getNodeByPath( "/users/dani");
        
        Assertions.assertFalse( dani1.isLocked());
        Assertions.assertFalse( dani2.isLocked());
        
        dani1.lock();
        
        dani1.lock();
        
        Assertions.assertTrue( dani1.isLocked());
        
        try {
            dani2.lock();
            
            Assertions.assertTrue( false);
        }
        catch( NodeLockedException e) {
            System.out.println( "Could not lock a second time : " + e.getMessage());
        }
        finally {
            session.rollback();
        }
        
        dani2.lock();
    }
}

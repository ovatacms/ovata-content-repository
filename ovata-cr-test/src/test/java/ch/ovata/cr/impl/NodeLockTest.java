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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import patterntesting.runtime.annotation.IntegrationTest;
import patterntesting.runtime.junit.SmokeRunner;

/**
 *
 * @author dani
 */
@IntegrationTest( "Requires MongoDb and S3.")
@RunWith( SmokeRunner.class)
public class NodeLockTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testNodeLockInfoTransient() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        
        Assert.assertFalse( dani.isLocked());
        
        dani.lock();
        
        Assert.assertTrue( dani.isLocked());
        
        Optional<LockInfo> info = dani.getLockInfo();
        
        Assert.assertTrue( info.isPresent());
        Assert.assertEquals( TEST_DATABASE_USERNAME, info.get().getPrincipal().getName());
        Assert.assertEquals( TEST_REPO_NAME, info.get().getRepositoryName());
        Assert.assertEquals( TEST_WORKSPACE_NAME, info.get().getWorkspaceName());
        Assert.assertEquals( dani.getId(), info.get().getNodeId());
        
        session.rollback();
        
        Assert.assertFalse( dani.isLocked());
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
        
        Assert.assertTrue( dani.isLocked());
        
        session.commit();
        
        session = repository.getSession( TEST_WORKSPACE_NAME);
        
        dani = session.getNodeByPath( "/users/dani");
        
        Assert.assertFalse( dani.isLocked());
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
        
        Assert.assertFalse( dani1.isLocked());
        Assert.assertFalse( dani2.isLocked());
        
        dani1.lock();
        
        dani1.lock();
        
        Assert.assertTrue( dani1.isLocked());
        
        try {
            dani2.lock();
            
            Assert.assertTrue( false);
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

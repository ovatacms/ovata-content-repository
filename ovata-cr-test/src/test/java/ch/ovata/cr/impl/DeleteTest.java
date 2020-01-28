/*
 * $Id: DeleteTest.java 690 2017-03-02 22:03:30Z dani $
 * Created on 25.11.2016, 12:00:00
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
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
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
public class DeleteTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testRemoveAndAdd() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);

        dani.setProperty( "cn", session.getValueFactory().of( "Daniel Hasler"));
        dani.setProperty( "mail", session.getValueFactory().of("daniel@hasler-gisin.ch"));

        roman.setProperty( "cn", session.getValueFactory().of( "Roman Hasler"));
        roman.setProperty( "mail", session.getValueFactory().of( "roman@hawklan.ch"));

        session.commit();

        session = repository.getSession( TEST_WORKSPACE_NAME);
        users = session.getNodeByPath( "/users");
        dani = session.getNodeByPath( "/users/dani");
        
        dani.remove();
        
        dani = users.addNode( "dani", CoreNodeTypes.FOLDER);
        
        session.commit();
        
        session = repository.getSession( TEST_WORKSPACE_NAME);
        
        dani = session.getNodeByPath( "/users/dani");
        
        Assert.assertTrue( dani.isOfType( CoreNodeTypes.FOLDER));
    }
    
    @Test
    public void testRemoveAndAddMultiple() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);
        
        Node root = session.getRoot().getOrAddNode( "myroot", CoreNodeTypes.FOLDER);
        
        root.getNodes().forEach( Node::remove);
        
        for( int i = 0; i < 20; i++) {
            root.addNode( "Node" + i, CoreNodeTypes.UNSTRUCTURED);
        }
        
        session.commit();
        
        session = repository.getSession( TEST_WORKSPACE_NAME);
        root = session.getRoot().getOrAddNode( "myroot", CoreNodeTypes.FOLDER);
        
        Assert.assertEquals( 20, root.getNodes().size());
        
        root.getNodes().forEach( Node::remove);
        
        for( int i = 0; i < 10; i++) {
            root.addNode( "Node" + i, CoreNodeTypes.UNSTRUCTURED);
        }
        
        session.commit();
        
        session = repository.getSession( TEST_WORKSPACE_NAME);
        root = session.getRoot().getOrAddNode( "myroot", CoreNodeTypes.FOLDER);
        
        Assert.assertEquals( 10, root.getNodes().size());
    }
}

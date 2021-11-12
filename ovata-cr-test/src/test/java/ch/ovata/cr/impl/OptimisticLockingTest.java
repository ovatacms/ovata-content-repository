/*
 * $Id: OptimisticLockingTest.java 690 2017-03-02 22:03:30Z dani $
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
import ch.ovata.cr.api.OptimisticLockingException;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class OptimisticLockingTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testOptimisticLockingAdd() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        
        session.commit();
        
        Session session1 = repository.getSession( "test");
        Session session2 = repository.getSession( "test");

        Node dani1 = session1.getNodeByPath( "/users/dani");
        Node dani2 = session2.getNodeByPath( "/users/dani");
        
        dani1.addNode( "timon", CoreNodeTypes.UNSTRUCTURED);
        dani2.addNode( "jonas", CoreNodeTypes.UNSTRUCTURED);
        
        session1.commit();
        
        try {
            session2.commit();
            
            Assertions.assertTrue( false);
        }
        catch( OptimisticLockingException e) {
            // Expected conflict on parent node
        }
    }
    
    @Test
    public void testOptimisticLockingAddToDifferentNodes() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        
        session.commit();
        
        Session session1 = repository.getSession( "test");
        Session session2 = repository.getSession( "test");

        Node dani1 = session1.getNodeByPath( "/users/dani");
        Node dani2 = session2.getNodeByPath( "/users/dani");
        
        dani1.addNode( "timon", CoreNodeTypes.UNSTRUCTURED);
        dani2.addNode( "jonas", CoreNodeTypes.UNSTRUCTURED);
        
        session1.commit();
        
        try {
            session2.commit();
            
            Assertions.assertTrue( false);
        }
        catch( OptimisticLockingException e) {
            // Expected conflict on parent node
        }
    }
    
    @Test
    public void testOptimisticLocking() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);

        dani.setProperty( "cn", session.getValueFactory().of( "Daniel Hasler"));
        dani.setProperty( "mail", session.getValueFactory().of("daniel@hasler-gisin.ch"));

        roman.setProperty( "cn", session.getValueFactory().of( "Roman Hasler"));
        roman.setProperty( "mail", session.getValueFactory().of( "roman@hawklan.ch"));

        session.commit();

        Session session1 = repository.getSession( "test");
        Session session2 = repository.getSession( "test");
        
        Node dani1 = session1.getNodeByPath( "/users/dani");
        Node roman1 = session2.getNodeByPath( "/users/roman");
        
        dani1.addNode( "node1", CoreNodeTypes.UNSTRUCTURED);
        
        session1.commit();
        
        roman1.addNode( "node2", CoreNodeTypes.UNSTRUCTURED);

        session2.commit();
    }
}

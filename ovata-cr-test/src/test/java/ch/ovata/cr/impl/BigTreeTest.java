/*
 * $Id: BigTreeTest.java 690 2017-03-02 22:03:30Z dani $
 * Created on 27.11.2016, 12:00:00
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
import ch.ovata.cr.spi.base.DirtyState.Modification;
import ch.ovata.cr.spi.store.Transaction;
import ch.ovata.cr.store.mongodb.MongoDbTransaction;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Ignore;
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
public class BigTreeTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testBigTree() throws Exception {
        long start = System.currentTimeMillis();
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node root = session.getRoot();
        Node users = root.addNode( "users", CoreNodeTypes.FOLDER);
        
        for( int i = 0; i < 1000; i++) {
            Node user = users.addNode( "user" + i, CoreNodeTypes.USER);
            
            user.setProperty( "uid", session.getValueFactory().of( "user" + i));
        }
        
        session.commit();
        
        System.out.println( "Time to add: " + (System.currentTimeMillis() - start) + "ms");
        
        start = System.currentTimeMillis();
        
        Session session2 = repository.getSession( "test");
        
        Node users2 = session2.getNodeByPath( "/users");
        List<Node> userssorted = users2.getNodes().stream().sorted( (n1, n2) -> n1.getProperty( "uid").getValue().compareTo( n2.getProperty( "uid").getValue())).collect( Collectors.toList());

        for( Node user : userssorted) {
        }
        
        System.out.println( "Time to read: " + (System.currentTimeMillis() - start) + "ms");
    }
    
    @Test
    @Ignore
    public void testDeepBigTree() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");
        Node root = session.getRoot();
        
        addLevel( root, 20, 2);
        
        session.commit( this::onCallback);
        
        Session session2 = repository.getSession( "test");
        long start = System.currentTimeMillis();
        
        traverseTree( session2.getRoot());

        System.out.println( "Took " + (System.currentTimeMillis() - start) + "ms.");
//        Assert.assertTrue( (System.currentTimeMillis() - start) < 5000);
    }
    
    private void onCallback( Transaction trx) {
        MongoDbTransaction transaction = (MongoDbTransaction)trx;
        
        ((SessionImpl)trx.getSession()).getDirtyState().getModifications().stream().forEach( m -> this.writeStuff( transaction, m));
    }
    
    private void writeStuff( MongoDbTransaction trx, Modification m) {
        if( m.getNode().isOfType( "sirene:dossier:sis")) {
            
        }
    }
    
    private void addLevel( Node root, int nodes, int recursions) {
        System.out.println( "Adding " + nodes + " on level " + recursions);

        for( int i = 0; i < nodes; i++) {
            Node n = root.addNode( "child" + i, CoreNodeTypes.UNSTRUCTURED);
            
            if( recursions > 0) {
                addLevel( n, nodes, recursions - 1);
            }
        }
    }
    
    private void traverseTree( Node root) {
        for( Node c : root.getNodes()) {
            Node r = getRoot( c);
            traverseTree( c);
        }
    }
    
    private Node getRoot( Node n) {
        Node p = n.getParent();
        
        if( p != null) {
            return getRoot( p);
        }
        else {
            return n;
        }
    }
}

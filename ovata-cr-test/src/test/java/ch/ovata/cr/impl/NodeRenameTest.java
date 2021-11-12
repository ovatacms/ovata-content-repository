/*
 * $Id: NodeMoveTest.java 690 2017-03-02 22:03:30Z dani $
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
import ch.ovata.cr.api.IllegalNameException;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.SameNameSiblingException;
import ch.ovata.cr.api.Session;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class NodeRenameTest extends AbstractOvataRepositoryTest {

    @Test
    public void testRenameNodeTransient() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        String romanId = roman.getId();

        dani.setProperty( "cn", session.getValueFactory().of( "Daniel Hasler"));
        dani.setProperty( "mail", session.getValueFactory().of("daniel@hasler-gisin.ch"));

        roman.setProperty( "cn", session.getValueFactory().of( "Roman Hasler"));
        roman.setProperty( "mail", session.getValueFactory().of( "roman@hawklan.ch"));

        session.commit();

        roles = session.getNodeByPath( "/roles");
        users = session.getNodeByPath( "/users");
        roman = session.getNodeByPath( "/users/roman");
        
        roman.rename( "rhodan");
        
        Optional<Node> n1 = session.findNodeByPath( "/users/roman");
        
        Assertions.assertFalse( n1.isPresent());
        
        Optional<Node> n2 = session.findNodeByPath( "/users/rhodan");
        
        Assertions.assertTrue( n2.isPresent());
        
        session.rollback();
    }
    
    @Test
    public void testRenameNode() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        String romanId = roman.getId();

        dani.setProperty( "cn", session.getValueFactory().of( "Daniel Hasler"));
        dani.setProperty( "mail", session.getValueFactory().of("daniel@hasler-gisin.ch"));

        roman.setProperty( "cn", session.getValueFactory().of( "Roman Hasler"));
        roman.setProperty( "mail", session.getValueFactory().of( "roman@hawklan.ch"));

        session.commit();

        roles = session.getNodeByPath( "/roles");
        users = session.getNodeByPath( "/users");
        roman = session.getNodeByPath( "/users/roman");
        
        roman.rename( "rhodan");
        
        session.commit();
        
        Optional<Node> n1 = session.findNodeByPath( "/users/roman");
        
        Assertions.assertFalse( n1.isPresent());
        Optional<Node> n2 = session.findNodeByPath( "/users/rhodan");
        
        Assertions.assertTrue( n2.isPresent());
        
        session.rollback();
        
        Collection<Node> nodes = session.getRoot().getNode( "users").getNodes();
        
        Assertions.assertEquals( 0, nodes.stream().filter( n -> n.getName().equals( "roman")).count());
        Assertions.assertEquals( 1, nodes.stream().filter( n -> n.getName().equals( "rhodan")).count());
    }
    
    @Test
    public void testRenameAndFindByPath() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);

        Node users2 = session.getNodeByPath( "/users");
        
        users2.rename( "anothername");
        
        Node users3 = session.getNodeByPath( "/anothername");
        
        Optional<Node> users4 = session.findNodeByPath( "/users");

        Assertions.assertFalse( users4.isPresent());
    }
    
    @Test
    public void testSameNameSiblingConflict() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node user1 = users.addNode( "user1", CoreNodeTypes.UNSTRUCTURED);
        Node user2 = users.addNode( "user2", CoreNodeTypes.UNSTRUCTURED);
        
        Assertions.assertThrows( SameNameSiblingException.class, () -> users.addNode( "user1", CoreNodeTypes.UNSTRUCTURED));
    }
    
    @Test
    public void testSameNameSiblingConflictRename() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node user1 = users.addNode( "user1", CoreNodeTypes.UNSTRUCTURED);
        Node user2 = users.addNode( "user2", CoreNodeTypes.UNSTRUCTURED);

        Assertions.assertThrows( SameNameSiblingException.class, () -> user2.rename( "user1"));
    }
    
    @Test
    public void testAddNodeBlankName() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        
        Assertions.assertThrows( IllegalNameException.class, () -> node.addNode( "  ", CoreNodeTypes.UNSTRUCTURED));
    }
    
    @Test
    public void testSetPropertyIllegalName() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        
        Assertions.assertThrows( IllegalNameException.class, () -> node.setProperty( "öüää", session.getValueFactory().of( "")));
    }
    
    @Test
    public void testAddPropertyEmptyName() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        
        Assertions.assertThrows( IllegalNameException.class, () -> node.setProperty( "", session.getValueFactory().of( "Test")));
    }
    
    @Test
    public void testSetNullValue() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        
        node.setProperty( "test", null);
        
        Assertions.assertFalse( node.hasProperty( "test"));
    }
}

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
import ch.ovata.cr.api.Node;
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
public class NodeMoveTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testMoveNodeTransient() throws Exception {
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
        
        roman.moveTo( roles);
        
        Assertions.assertFalse( session.findNodeByPath( "/users/roman").isPresent());
        Assertions.assertTrue( session.findNodeByPath( "/roles/roman").isPresent());
        
        session.rollback();
    }
    
    @Test
    public void testMoveNodePersistent() throws Exception {
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
        
        roman.moveTo( roles);
        
        session.commit();
        
        roman = session.getNodeByPath( "/roles/roman");
        
        roman = session.getNodeByIdentifier( romanId);
        roles = roman.getParent();
        
        Assertions.assertEquals( "roles", roles.getName());
        
        users = session.getNodeByPath( "/users");
        
        Assertions.assertEquals( 1, users.getNodes().size());
        Assertions.assertEquals( "dani", users.getNodes().iterator().next().getName());
        Assertions.assertEquals( 1, roles.getNodes().size());
    }
    
    @Test
    public void testAndModifyNode() throws Exception {
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
        
        roman.setProperty( "additional", session.getValueFactory().of( "Additional Value"));
        roman.moveTo( roles);
        
        session.commit();
        
        roman = session.getNodeByPath( "/roles/roman");
        
        Assertions.assertEquals( "Additional Value", roman.getString( "additional"));
        
        session.rollback();
    }
    
    @Test
    public void testMoveTransientNode() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node other = node.addNode( "other", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        String romanId = roman.getId();
        
        Node timon = users.addNode( "timon", CoreNodeTypes.UNSTRUCTURED);
        
        Assertions.assertTrue( session.findNodeByPath( "/users/timon").isPresent());
        
        timon.moveTo( other);
        
        Assertions.assertFalse( session.findNodeByPath( "/users/timon").isPresent());
        Assertions.assertTrue( session.findNodeByPath( "/other/timon").isPresent());
        
        Assertions.assertEquals( other, timon.getParent());
        
        session.commit();
    }
    
    @Test
    public void testMoveNodeAndReadByIdTransient() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node other = node.addNode( "other", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        Node timon = users.addNode( "timon", CoreNodeTypes.UNSTRUCTURED);
        
        String timonId = timon.getId();
        
        Assertions.assertTrue( session.findNodeByPath( "/users/timon").isPresent());
        
        timon.moveTo( other);
        
        Node timon2 = session.getNodeByIdentifier( timonId);
        
        Assertions.assertEquals( "other", timon2.getParent().getName());
        
        Assertions.assertEquals( other, timon.getParent());
        
        session.commit();
    }

    @Test
    public void testMoveNodeAndReadByIdPersistent() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node roles = node.addNode( "roles", CoreNodeTypes.UNSTRUCTURED);
        Node other = node.addNode( "other", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        Node timon = users.addNode( "timon", CoreNodeTypes.UNSTRUCTURED);
        
        String timonId = timon.getId();

        session.commit();
        
        Session session2 = repository.getSession( "test");
        Node timon2 = session2.getNodeByPath( "/users/timon");
        Node other2 = session2.getNodeByPath( "/other");
        
        Assertions.assertTrue( session.findNodeByPath( "/users/timon").isPresent());
        
        timon2.moveTo( other2);
        
        session2.commit();
        
        Session session3 = repository.getSession( "test");
        
        Node timon3 = session3.getNodeByIdentifier( timonId);
    }
}

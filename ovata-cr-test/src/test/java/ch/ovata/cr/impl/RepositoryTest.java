/*
 * $Id: RepositoryTest.java 690 2017-03-02 22:03:30Z dani $
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
import ch.ovata.cr.api.ValueFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
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
public class RepositoryTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testCreateSession() throws Exception {
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

        long oldRevision = session.getRevision();

        Collection<? extends Node> nodes = users.getNodes();

        Assert.assertEquals( 2, nodes.size());

        Node rdani = users.getNode( "dani");

        Assert.assertNotNull( rdani);
        Assert.assertEquals( "dani", rdani.getName());
        Assert.assertEquals( "Daniel Hasler", rdani.getProperty( "cn").getValue().getString());

        rdani.setProperty( "mail", session.getValueFactory().of( "daniel.hasler@bluesky-it.ch"));

        session.commit();

        Node udani = session.getNodeByPath( "/users/dani");

        Assert.assertNotNull( udani);
        Assert.assertEquals( "dani", udani.getName());
        Assert.assertEquals( "daniel.hasler@bluesky-it.ch", udani.getProperty( "mail").getValue().getString());

        Session ro_session = repository.getSession( "test", oldRevision);

        Node rodani = ro_session.getNodeByPath( "/users/dani");

        Assert.assertNotNull( rodani);
        Assert.assertEquals( "dani", rodani.getName());
        Assert.assertEquals( "daniel@hasler-gisin.ch", rodani.getProperty( "mail").getValue().getString());

        Assert.assertEquals( 2, rodani.getProperties().size());

        session = repository.getSession( "test");

        Node users2 = session.getNodeByPath( "/users");

        Assert.assertEquals( 2, users2.getNodes().size());
    }
    
    @Test
    public void testRemovedNodeAfterCommit() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);

        Assert.assertTrue( session.isDirty());
        
        session.commit();

        Node rdani = session.getNodeByPath( "/users/dani");

        rdani.remove();

        session.commit();

        Optional<? extends Node> odani = session.findNodeByPath( "/users/dani");

        Assert.assertFalse( odani.isPresent());
    }
    
    @Test 
    public void testRemovedNodeBeforeCommit() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);

        session.commit();

        Node rdani = session.getNodeByPath( "/users/dani");

        rdani.remove();

        Optional<? extends Node> odani = session.findNodeByPath( "/users/dani");

        Assert.assertFalse( odani.isPresent());
    }
    
    @Test
    public void testValueTypes() throws Exception {
        long lvalue = 1234567l;
        double dvalue = 12312341.123123d;
        
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");
        ValueFactory vf = session.getValueFactory();

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);

        dani.setProperty( "longvalue", vf.of( lvalue));
        dani.setProperty( "doublevalue", vf.of( dvalue));
        dani.setProperty( "booleanvalue", vf.of( true));
        dani.setProperty( "decimalvalue", vf.of( new BigDecimal( "123123.1231231")));
        dani.setProperty( "localdate", vf.of( LocalDate.now()));
        dani.setProperty( "zoneddatetime", vf.of( ZonedDateTime.now()));

        session.commit();

        Session session2 = repository.getSession( "test");

        Node dani2 = session2.getNodeByPath( "/users/dani");

        Assert.assertEquals( lvalue, (long)dani2.getProperty( "longvalue").getValue().getLong());
        Assert.assertEquals( true, (boolean)dani2.getProperty( "booleanvalue").getValue().getBoolean());
    }
    
    @Test
    public void testRenameNode() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        
        String id = dani.getId();
        
        session.commit();
        
        dani = session.getNodeByPath( "/users/dani");
        
        dani.remove();
        
        Optional<? extends Node> odani = session.findNodeByPath( "/users/dani");
        
        Assert.assertFalse( odani.isPresent());
        
        odani = session.findNodeByIdentifier( id);
        
        Assert.assertFalse( odani.isPresent());
        
        session.commit();
        
        odani = session.findNodeByPath( "/users/dani");
        
        Assert.assertFalse( odani.isPresent());
        
        odani = session.findNodeByIdentifier( id);
        
        Assert.assertFalse( odani.isPresent());
    }
    
    @Test
    public void testAddChild() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");
        
        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);        
        
        session.commit();
        
        users = session.getNodeByPath( "/users");
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        
        Optional<? extends Node> odani = session.findNodeByPath( "/users/dani");
        
        Assert.assertTrue( odani.isPresent());
        
        Collection<? extends Node> children = users.getNodes();
        
        Assert.assertEquals( 1, children.size());
    }
}

/*
 * $Id: MultiValueTest.java 690 2017-03-02 22:03:30Z dani $
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
import ch.ovata.cr.api.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class MultiValueTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testAddMultiValueProperty() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        List<Value> values = new ArrayList<>();
        
        values.add( session.getValueFactory().of( "1"));
        values.add( session.getValueFactory().of( "2"));
        values.add( session.getValueFactory().of( "3"));
        values.add( session.getValueFactory().of( "4"));
        values.add( session.getValueFactory().of( "5"));
        
        dani.setProperty( "values", session.getValueFactory().of( values));
        
        session.commit();
        
        Node node2 = session.getNodeByPath( "/users/dani");
        Value[] values2 = node2.getProperty( "values").getValue().getArray();
        
        Assertions.assertEquals( Value.Type.ARRAY, node2.getProperty( "values").getValue().getType());
        Assertions.assertEquals( 5, values2.length);
        
        int index = 1;
        
        for( Value v : values2) {
            Assertions.assertEquals( Integer.toString( index++), v.getString());
        }
    }
    
    @Test
    public void testMapValueProperty() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Map<String, Value> values = new HashMap<>();
        
        values.put( "1", session.getValueFactory().of( "1"));
        values.put( "2", session.getValueFactory().of( "2"));
        values.put( "3", session.getValueFactory().of( "3"));
        values.put( "4", session.getValueFactory().of( "4"));
        values.put( "5", session.getValueFactory().of( "5"));
        
        dani.setProperty( "values", session.getValueFactory().of( values));
        
        session.commit();
        
        Node node2 = session.getNodeByPath( "/users/dani");
        Map<String, Value> values2 = node2.getProperty( "values").getValue().getMap();
        
        Assertions.assertEquals( Value.Type.MAP, node2.getProperty( "values").getValue().getType());
        Assertions.assertEquals( 5, values2.size());
        
        Assertions.assertEquals( "1", values2.get( "1").getString());
        Assertions.assertEquals( "2", values2.get( "2").getString());
        Assertions.assertEquals( "3", values2.get( "3").getString());
        Assertions.assertEquals( "4", values2.get( "4").getString());
        Assertions.assertEquals( "5", values2.get( "5").getString());
    }
}

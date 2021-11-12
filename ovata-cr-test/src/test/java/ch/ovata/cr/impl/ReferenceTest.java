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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class ReferenceTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testResolveReference() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        
        Session other = repository.createWorkspace( "other");
        
        Node test = other.getRoot().addNode( "anothernode", CoreNodeTypes.UNSTRUCTURED);
        test.setProperty( "name", other.getValueFactory().of( "Daniel"));
        
        other.commit();
        
        Session session = repository.getSession( TEST_WORKSPACE_NAME);
        Node node = session.getRoot().addNode( "basenode", CoreNodeTypes.UNSTRUCTURED);

        node.setProperty( "reference", session.getValueFactory().referenceByPath( test.getSession().getWorkspace().getName(), test.getPath()));
        
        session.commit();

        session = repository.getSession( TEST_WORKSPACE_NAME);
        
        Node base = session.getNodeByPath( "/basenode");
        Node ref = base.getReference( "reference").getNode();
        
        Assertions.assertNotNull( ref);
        Assertions.assertEquals( "Daniel", ref.getString( "name"));
    }
}

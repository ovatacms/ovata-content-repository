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
        
        Assert.assertNotNull( ref);
        Assert.assertEquals( "Daniel", ref.getString( "name"));
    }
}

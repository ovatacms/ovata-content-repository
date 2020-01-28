/*
 * $Id: MassDataTest.java 2446 2019-04-16 13:43:48Z dani $
 * Created on 14.04.2019, 12:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.mass;

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.ValueFactory;
import ch.ovata.cr.impl.AbstractOvataRepositoryTest;
import java.time.ZonedDateTime;
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
@Ignore
public class MassDataTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testRemoveAndAdd() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session s = repository.getSession( TEST_WORKSPACE_NAME);
        
        s.getRoot().addNode( "data", CoreNodeTypes.UNSTRUCTURED);
        s.commit();
        
        for( int i = 0; i < 20000; i++) {
            long start = System.currentTimeMillis();
            Session session = repository.getSession( TEST_WORKSPACE_NAME);
            ValueFactory vf = session.getValueFactory();
            Node root = session.getNodeByPath( "/data");
            
            root.addNode( "user" + i + "@mydomain.ch", CoreNodeTypes.USER);
            root.setProperty( "timestamp", vf.of( ZonedDateTime.now()));
            root.setProperty( "firstname", vf.of( "User" + i));
            root.setProperty( "lastnmae", vf.of( "Lastuser" + i));
            root.setProperty( "mail", vf.of( "user" + i + "@mydomain.ch"));
            
            session.commit();
            
            System.out.println( "Time for #" + i + " : " + (System.currentTimeMillis() - start) + "ms.");
        }
    }
}

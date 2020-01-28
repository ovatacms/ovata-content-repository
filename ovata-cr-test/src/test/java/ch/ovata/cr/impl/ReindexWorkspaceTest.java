/*
 * $Id: ReindexWorkspaceTest.java 2674 2019-09-18 11:45:20Z dani $
 * Created on 15.07.2019, 12:00:00
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.spi.store.StoreDocument;
import java.util.List;
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
public class ReindexWorkspaceTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void testQuery() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( "test");

        Node node = session.getRoot();
        Node users = node.addNode( "users", CoreNodeTypes.UNSTRUCTURED);
        Node dani = users.addNode( "dani", CoreNodeTypes.UNSTRUCTURED);
        Node roman = users.addNode( "roman", CoreNodeTypes.UNSTRUCTURED);
        Node timon = dani.addNode( "timon", CoreNodeTypes.UNSTRUCTURED);
        Node jonas = dani.addNode( "jonas", CoreNodeTypes.UNSTRUCTURED);
        
        timon.setProperty( "search", session.getValueFactory().of( "supersonic"));
        jonas.setProperty( "search", session.getValueFactory().of( "smarties"));
        
        session.commit();
        
        repository.getSession( "test").getWorkspace().reindex( repository);
        
        Thread.sleep( 5000l);
        
        FulltextQuery q = session.createQuery( FulltextQuery.class);
        
        List<Node> result = q.term( "supersonic").execute();
        
        Assert.assertEquals( 1, result.size());
        Assert.assertEquals( "timon", result.get( 0).getName());
        
        QueryByExample q2 = session.createQuery( QueryByExample.class);
        
        StoreDocument doc = session.getValueFactory().newDocument();
        
        doc.append( "search.@Value", "smarties");
        
        List<Node> result2 = q2.document( doc).execute();
        
        Assert.assertEquals( 1, result2.size());
        Assert.assertEquals( "jonas", result2.get( 0).getName());
        
        session = repository.getSession( "test");
        dani = session.getNodeByPath( "/users/dani");
        dani.remove();
        session.commit();

        FulltextQuery q3 = session.createQuery( FulltextQuery.class);
        List<Node> result3 = q3.term( "supersonic").execute();
        
        Assert.assertEquals( 0, result3.size());
    }
}

/*
 * $Id: ElasticQueryTest.java 2850 2019-11-22 13:46:25Z dani $
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
package ch.ovata.cr.query;

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.impl.AbstractOvataRepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class FulltextSearchTest extends AbstractOvataRepositoryTest {

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
        
        FulltextQuery q = session.createQuery( FulltextQuery.class);
        
        List<Node> result = q.term( "supersonic").execute();
        
        Assertions.assertEquals( 1, result.size());
        Assertions.assertEquals( "timon", result.get( 0).getName());
    }
}

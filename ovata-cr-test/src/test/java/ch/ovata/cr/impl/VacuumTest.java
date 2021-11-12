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
import ch.ovata.cr.spi.store.Transaction;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class VacuumTest extends AbstractOvataRepositoryTest {
    
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
        
        Node dani2 = session.getNodeByPath( "/users/dani");
        
        dani2.setProperty( "mail", session.getValueFactory().of( "daniel.hasler@bluesky-it.ch"));
        
        session.commit();
        
        List<Transaction> trxs = session.getRepository().getTransactionMgr().getTransactions( "test", 0l);
        
        Assertions.assertEquals( 2, trxs.size());
        
        long revision = session.getRevision();
        
        System.out.println( "Revision to keep: " + revision);
        
        session.getWorkspace().vacuum( repository, revision);
        
        Node dani3 = session.getNodeByPath( "/users/dani");
        
        Assertions.assertNotNull( dani3);
        Assertions.assertEquals( "daniel.hasler@bluesky-it.ch", dani3.getString( "mail"));
        
        trxs = session.getRepository().getTransactionMgr().getTransactions( "test", 0l);
        
        Assertions.assertEquals( 1, trxs.size());
    }
}

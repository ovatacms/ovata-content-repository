/*
 * $Id: IndexTest.java 690 2017-03-02 22:03:30Z dani $
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
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.query.FulltextQuery;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Assert;
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
public class IndexTest extends AbstractOvataRepositoryTest {
    
    @Test
    @Ignore
    public void testReindex() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);
        
        initNodesWithBinaries( session);
        
        session.getWorkspace().reindex( repository);
        
        Thread.sleep( 10000);
        
        FulltextQuery query = session.createQuery( FulltextQuery.class);
        List<Node> nodes = query.term ( "pdf995").execute();
        
        Assert.assertEquals( 1, nodes.size());
        
        nodes.stream().forEach( n -> System.out.println( "Found node : " + n.getName()));
    }
    
    @Test
    @Ignore
    public void testFulltextIndexer() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);

        initNodesWithBinaries( session);
        
        Thread.sleep( 5000);
        
        FulltextQuery query = session.createQuery( FulltextQuery.class);
        List<Node> nodes = query.term ( "OASIS").execute();
        
        Assert.assertEquals( 2, nodes.size());
        
        nodes.stream().forEach( n -> System.out.println( "Found node : " + n.getName()));
    }
    
    private void initNodesWithBinaries( Session session) throws IOException {
        Node root = session.getRoot();
        Node pdf = root.addNode( "pdf.pdf", CoreNodeTypes.RESOURCE);
        Node word = root.addNode( "word.doc", CoreNodeTypes.RESOURCE);
        Node wordx = root.addNode( "word.docx", CoreNodeTypes.RESOURCE);
        
        try( InputStream in = new FileInputStream( "src/test/resources/pdf.pdf")) {
            Value binary = session.getValueFactory().binary( in, "pdf.pdf", "application/pdf");
            
            pdf.setProperty( "content", binary);
        }
        
        try( InputStream in = new FileInputStream( "src/test/resources/word.doc")) {
            Value binary = session.getValueFactory().binary( in, "word.doc", "application/msword");
            
            word.setProperty( "content", binary);
        }
        
        try( InputStream in = new FileInputStream( "src/test/resources/word.docx")) {
            Value binary = session.getValueFactory().binary( in, "word.docx", "application/msword");
            
            wordx.setProperty( "content", binary);
        }
        
        session.commit();
    }
}

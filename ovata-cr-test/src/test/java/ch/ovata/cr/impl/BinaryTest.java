/*
 * $Id: BinaryTest.java 2787 2019-11-11 09:00:35Z dani $
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

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.query.FulltextQuery;
import java.io.FileInputStream;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class BinaryTest extends AbstractOvataRepositoryTest {
    
    @Test
    public void createBinary() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( TEST_WORKSPACE_NAME);

        Node root = session.getRoot();
        Node dam = root.addNode( "assets", CoreNodeTypes.FOLDER);
        
        FileInputStream in = new FileInputStream( "src/test/resources/sample.pdf");
        
        Value binary = session.getValueFactory().binary( in, "sample.pdf", "application/pdf");
        
        dam.setProperty( "asset", binary);
        
        session.commit();
        
        session = repository.getSession( "test");
        dam = session.getNodeByPath( "/assets");
        
        Binary value = dam.getProperty( "asset").getValue().getBinary();
        
        Assertions.assertEquals(  "sample.pdf", value.getFilename());
        Assertions.assertEquals( "application/pdf", value.getContentType());
        
        Thread.sleep( 2000l);
        
        FulltextQuery q = session.createQuery( FulltextQuery.class);
        
        q.term( "demonstration");
        
        List<Node> result = q.execute();
        
        Assertions.assertEquals( 1, result.size());
    }
}

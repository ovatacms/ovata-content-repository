package ch.ovata.cr.utils;

/*
 * $Id: JsonImporterTest.java 2712 2019-09-28 09:40:17Z dani $
 * Created on 05.07.2019, 12:00:00
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


import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.ValueFactory;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

/**
 *
 * @author dani
 */
public class JsonImporterTest {
    
    @Test
    public void testImport() throws IOException {
        JsonImporter importer = new JsonImporter();
        
        Value sv = Mockito.mock( Value.class);
        ValueFactory vf = Mockito.mock( ValueFactory.class, i -> sv);
        Session session = Mockito.mock( Session.class);
        Node node = Mockito.mock( Node.class);
        
        when( node.addNode( any(String.class), any( String.class))).thenReturn( node);
        when( node.getOrAddNode( any(String.class), any(String.class))).thenReturn( node);
        when( vf.of( any( String.class))).thenReturn( sv);
        
        when( session.getValueFactory()).thenReturn( vf);
        when( node.getSession()).thenReturn( session);
        
        try( FileInputStream in = new FileInputStream( "src/test/resources/test.json")) {
            importer.importStream( in, node);
        }
        catch( Throwable t) {
            t.printStackTrace();
        }
    }
}

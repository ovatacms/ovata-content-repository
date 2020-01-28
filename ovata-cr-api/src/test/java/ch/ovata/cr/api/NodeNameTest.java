/*
 * $Id: NodeNameTest.java 2895 2019-12-18 10:41:39Z dani $
 * Created on 29.09.2018, 12:00:00
 * 
 * Copyright (c) 2018 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.api;

import org.junit.Test;

/**
 *
 * @author dani
 */
public class NodeNameTest {
    
    @Test
    public void testNodeNameCheck() {
        Node.validateName( "abc_-efghijkl89;.()[]{}$");
        Node.validateName( "a");
        Node.validateName( "dani@bluesky-it.ch");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testNodeNameCheckEmpty() {
        Node.validateName( "");
    }

    @Test( expected = IllegalNameException.class)
    public void testNullName() {
        Node.validateName( null);
    }
    
    @Test( expected = IllegalNameException.class)
    public void testStartSpace() {
        Node.validateName( " test");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testEmptyName() {
        Node.validateName( "");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testBlankName() {
        Node.validateName( "    ");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testRootPath() {
        Node.validateName( "/");
    }
    
    @Test
    public void testWithBlanks() {
        Node.validateName( "asdf ");
        Node.validateName( "This is a test");
    }
}

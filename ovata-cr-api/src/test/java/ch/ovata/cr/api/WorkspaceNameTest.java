/*
 * $Id: WorkspaceNameTest.java 2895 2019-12-18 10:41:39Z dani $
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
public class WorkspaceNameTest {
    
    @Test
    public void testWorkspaceNameCheck() {
        Workspace.validateName( "$myrepo");
        Workspace.validateName( "myrepository98");
        Workspace.validateName( "my_repository");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testWorkspaceNameCheckEmpty() {
        Workspace.validateName( "9test");
    }

    @Test( expected = IllegalNameException.class)
    public void testNullName() {
        Workspace.validateName( null);
    }
    
    @Test( expected = IllegalNameException.class)
    public void testStartSpace() {
        Workspace.validateName( " test");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testEmptyName() {
        Workspace.validateName( "");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testBlankName() {
        Workspace.validateName( "    ");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testRootPath() {
        Workspace.validateName( "/");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testWithBlanks() {
        Workspace.validateName( "asdf ");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testWithBlanks2() {
        Workspace.validateName( "This is a test");
    }
}

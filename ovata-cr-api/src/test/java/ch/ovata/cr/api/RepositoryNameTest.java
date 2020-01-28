/*
 * $Id: RepositoryNameTest.java 2895 2019-12-18 10:41:39Z dani $
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
public class RepositoryNameTest {
    
    @Test
    public void testRepositoryNameCheck() {
        Repository.validateName( "$myrepo");
        Repository.validateName( "myrepository98");
        Repository.validateName( "my_repository");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testRepositoryNameCheckEmpty() {
        Repository.validateName( "9test");
    }

    @Test( expected = IllegalNameException.class)
    public void testNullName() {
        Repository.validateName( null);
    }
    
    @Test( expected = IllegalNameException.class)
    public void testStartSpace() {
        Repository.validateName( " test");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testEmptyName() {
        Repository.validateName( "");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testBlankName() {
        Repository.validateName( "    ");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testRootPath() {
        Repository.validateName( "/");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testWithBlanks() {
        Repository.validateName( "asdf ");
    }
    
    @Test( expected = IllegalNameException.class)
    public void testWithBlanks2() {
        Repository.validateName( "This is a test");
    }
}

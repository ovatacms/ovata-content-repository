/*
 * $Id: InitWorkspaceTest.java 690 2017-03-02 22:03:30Z dani $
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

import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Workspace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author dani
 */
@Tag( "integration-test")
public class CloneWorkspaceTest extends AbstractOvataRepositoryTest {
    @Test
    public void testInitWorkspace() throws Exception {
        Repository repository = this.getRepositoryAsAdministrator();
        Session session = repository.getSession( Workspace.WORKSPACE_CONFIGURATION);
        
        Assertions.assertNotNull( session.getNodeByPath( "/security/jaas"));
        
        session.clone( "system_config_clone");
        
        Session session2 = repository.getSession( "system_config_clone");
        
        Assertions.assertNotNull( session2.getNodeByPath( "/security/jaas"));
    }
}

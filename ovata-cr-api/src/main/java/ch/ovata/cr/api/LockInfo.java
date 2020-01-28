/*
 * $Id: LockInfo.java 1456 2018-05-07 10:54:36Z dani $
 * Created on 07.05.2018, 12:00:00
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

import ch.ovata.cr.api.security.UserPrincipal;
import java.io.Serializable;

/**
 *
 * @author dani
 */
public interface LockInfo extends Serializable {
    
    /**
     * The principal of the user holding the lock
     * @return principal hodling the lock
     */
    UserPrincipal getPrincipal();
    
    /**
     * Name of the repository the node belongs to
     * @return name of the repository
     */
    String getRepositoryName();
    
    /**
     * Name of the workspace the node belongs to
     * @return name of the workspace
     */
    String getWorkspaceName();
    
    /**
     * Node Id of the locked node this <code>LockInfo</code> belongs to
     * @return id of the locked node
     */
    String getNodeId();
}

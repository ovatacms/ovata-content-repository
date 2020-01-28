/*
 * $Id: Policy.java 679 2017-02-25 10:28:00Z dani $
 * Created on 06.12.2016, 19:00:00
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
package ch.ovata.cr.api.security;

import java.util.List;

/**
 * A wrapper around a set of access control lists the determine who is allowed to access 
 * a node and what operations may be performed.
 * @author dani
 */
public interface Policy {
    
    enum Result { GRANTED, DENIED, INDETERMINATE }
        
    /**
     * Updates all ACLs in this policy (removes all existing and adds the new ones)
     * @param acls the new set of acls to associate this policy with
     */
    void updateAcls( List<Acl> acls);

    /**
     * Retrieve all access control lists belonging to this policy
     * @return the list of acls
     */
    List<Acl> getAcls();
    
    /**
     * Checks if the authenticated subject of the current session has the required privilege granted through one of it's associated principal
     * @param permission
     * @return 
     */
    Result check( Permission permission);
}

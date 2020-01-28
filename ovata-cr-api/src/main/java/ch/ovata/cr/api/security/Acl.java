/*
 * $Id: Acl.java 679 2017-02-25 10:28:00Z dani $
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

import java.security.Principal;
import java.util.Set;

/**
 *
 * @author dani
 */
public interface Acl {
    /**
     * @return The principal to authorize
     */
    Principal getPrincipal();
    
    /**
     * @return true if the set of permissions is to allow, false if the set of permissions is to deny
     */
    boolean isNegative();
    
    /**
     * @return Set of permissions associated with ths Acl
     */
    Set<Permission> getPermissions();
}

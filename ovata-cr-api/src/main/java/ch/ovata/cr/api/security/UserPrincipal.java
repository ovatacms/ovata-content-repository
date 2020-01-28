/*
 * $Id: UserPrincipal.java 679 2017-02-25 10:28:00Z dani $
 * Created on 28.11.2016, 12:00:00
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

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

/**
 * Represents a user accessing the content repository.
 * This information is matched against a <code>Policy</code>
 * @author dani
 */
public class UserPrincipal implements Principal, Serializable {

    private final String name;
    
    public UserPrincipal( String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash( "user", this.name);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof UserPrincipal) && Objects.equals( this.name, ((UserPrincipal)obj).name);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" + "name=" + name + '}';
    }    
}

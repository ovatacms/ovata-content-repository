/*
 * $Id: RolePrincipal.java 679 2017-02-25 10:28:00Z dani $
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
 * Represents a role a user may attain to access the repository.
 * This information is checked against a <code>Policy</code>.
 * @author dani
 */
public class RolePrincipal implements Principal, Serializable {

    private final String name;
    
    public RolePrincipal( String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash( "role", this.name);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RolePrincipal) && Objects.equals( this.name, ((RolePrincipal)obj).name);
    }

    @Override
    public String toString() {
        return "RolePrincipal{" + "name=" + name + '}';
    }
}

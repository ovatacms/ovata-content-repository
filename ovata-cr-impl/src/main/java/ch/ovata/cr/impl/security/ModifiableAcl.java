/*
 * $Id: ModifiableAcl.java 1398 2018-04-23 14:07:17Z dani $
 * Created on 08.08.2017, 12:00:00
 * 
 * Copyright (c) 2017 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.impl.security;

import ch.ovata.cr.api.security.Acl;
import ch.ovata.cr.api.security.Permission;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class ModifiableAcl implements Acl, Serializable {

    private Principal principal;
    private boolean isNegative;
    private final Set<Permission> permissions = EnumSet.noneOf( Permission.class);

    public ModifiableAcl() {
        this.principal = null;
        this.isNegative = false;
    }

    public ModifiableAcl(Acl acl) {
        this.principal = acl.getPrincipal();
        this.isNegative = acl.isNegative();
        this.permissions.addAll(acl.getPermissions());
    }
    
    public ModifiableAcl( Principal principal, boolean isNegative, Collection<Permission> permissions) {
        this.principal = principal;
        this.isNegative = isNegative;
        this.permissions.addAll( permissions);
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isNegative() {
        return this.isNegative;
    }

    @Override
    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    /**
     * Assigns a new principal to ths Acl
     * @param principal the new principal
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    /**
     * Modifies this Acls negative flag
     * @param flag the new flag
     */
    public void setNegative(boolean flag) {
        this.isNegative = flag;
    }

    /**
     * Alters the set of permissions associated with this Acl
     * @param permissions the new permissions
     */
    public void setPermissions(Set<Permission> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    @Override
    public String toString() {
        return "ModifiableAcl{" + "principal=" + principal + ", isNegative=" + isNegative + ", permissions=" + permissions + '}';
    }
}

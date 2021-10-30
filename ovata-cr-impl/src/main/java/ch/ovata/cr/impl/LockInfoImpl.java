/*
 * $Id: LockInfoImpl.java 1456 2018-05-07 10:54:36Z dani $
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.security.UserPrincipal;
import java.util.Objects;

/**
 *
 * @author dani
 */
public class LockInfoImpl implements LockInfo {
    
    private final UserPrincipal principal;
    private final String repositoryName;
    private final String workspaceName;
    private final String nodeId;
    
    public LockInfoImpl( Node node) {
        this.principal = (UserPrincipal)node.getSession().getRepository().getPrincipal();
        this.repositoryName = node.getSession().getRepository().getName();
        this.workspaceName = node.getSession().getWorkspace().getName();
        this.nodeId = node.getId();
    }
    
    @Override
    public UserPrincipal getPrincipal() {
        return this.principal;
    }

    @Override
    public String getRepositoryName() {
        return this.repositoryName;
    }

    @Override
    public String getWorkspaceName() {
        return this.workspaceName;
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }
    
    public String getKey() {
        return this.repositoryName + ":" + this.workspaceName + ":" + this.nodeId;
    }
    
    public static String getKey( Node node) {
        return node.getSession().getRepository().getName()  + ":" + node.getSession().getWorkspace().getName() + ":" + node.getId();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.repositoryName);
        hash = 59 * hash + Objects.hashCode(this.workspaceName);
        hash = 59 * hash + Objects.hashCode(this.nodeId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LockInfoImpl other = (LockInfoImpl) obj;
        if (!Objects.equals(this.repositoryName, other.repositoryName)) {
            return false;
        }
        if (!Objects.equals(this.workspaceName, other.workspaceName)) {
            return false;
        }
        if (!Objects.equals(this.nodeId, other.nodeId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LockInfoImpl{" + "principal=" + principal + ", repositoryName=" + repositoryName + ", workspaceName=" + workspaceName + ", nodeId=" + nodeId + '}';
    }
}

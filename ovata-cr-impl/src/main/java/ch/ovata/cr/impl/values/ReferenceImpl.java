/*
 * $Id: ReferenceImpl.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.impl.values;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Reference;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.Session;
import java.util.Optional;
import ch.ovata.cr.impl.SessionImpl;
import java.util.Objects;

/**
 *
 * @author dani
 */
public class ReferenceImpl implements Reference {

    private final SessionImpl session;
    private final String workspaceName;
    private final String nodeReference;
    
    public ReferenceImpl( SessionImpl session, String workspaceName, String path) {
        this.session = session;
        this.workspaceName = workspaceName;
        this.nodeReference = path;
    }
    
    @Override
    public String getWorkspace() {
        return this.workspaceName;
    }

    @Override
    public String getPath() {
        return this.nodeReference;
    }

    @Override
    public Node getNode() {
        return findNode().orElseThrow( () -> new NotFoundException( "Could not find node for reference <" + getWorkspace() + ":" + getPath()));
    }

    @Override
    public Optional<Node> findNode() {
        if( nodeReference.startsWith( "/")) {
            return getSession().findNodeByPath(nodeReference);
        }
        else {
            return getSession().findNodeByIdentifier(nodeReference);
        }
    }
    
    private Session getSession() {
        if( session.getWorkspace().getName().equals( workspaceName)) {
            return session;
        }
        else {
            return session.getRepository().getSession( workspaceName, session.getRevision());
        }
    }
    
    @Override
    public String toString() {
        return workspaceName + ":" + nodeReference;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( workspaceName, nodeReference);
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof ReferenceImpl) {
            ReferenceImpl other = (ReferenceImpl)o;
            
            return this.workspaceName.equals( other.workspaceName) && this.nodeReference.equals( other.nodeReference);
        }
        
        return false;
    }
}

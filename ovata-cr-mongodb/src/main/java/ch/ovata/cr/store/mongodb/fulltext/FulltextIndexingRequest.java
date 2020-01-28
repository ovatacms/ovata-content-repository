/*
 * $Id: FulltextIndexingRequest.java 2674 2019-09-18 11:45:20Z dani $
 * Created on 16.09.2019, 16:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.mongodb.fulltext;

import ch.ovata.cr.api.NodeId;

/**
 *
 * @author dani
 */
public class FulltextIndexingRequest {
    
    private final String repositoryName;
    private final String workspaceName;
    private final NodeId nodeId;
    private final long revision;
    private final String binaryId;
    
    public FulltextIndexingRequest( String repositoryName, String workspaceName, NodeId nodeId, long revision, String binaryId) {
        this.workspaceName = workspaceName;
        this.repositoryName = repositoryName;
        this.nodeId = nodeId;
        this.revision = revision;
        this.binaryId = binaryId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public NodeId getNodeId() {
        return nodeId;
    }
    
    public long getRevision() {
        return this.revision;
    }
    
    public String getBinaryId() {
        return this.binaryId;
    }
}

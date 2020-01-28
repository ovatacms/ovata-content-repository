/*
 * $Id: IndexingRequest.java 2944 2020-01-27 14:14:20Z dani $
 * Created on 25.04.2018, 12:00:00
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
package ch.ovata.cr.spi.search;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a request to index a blob.
 * @author dani
 */
public class IndexingRequest {
    
    public static class Bundle {
        private String nodeId;
        private long revision;
        private String blobId;
        
        public Bundle() {
        }
        
        public Bundle( String nodeId, long revision, String blobId) {
            this.nodeId = nodeId;
            this.revision = revision;
            this.blobId = blobId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }
        
        public long getRevision() {
            return this.revision;
        }
        
        public void setRevision( long revision) {
            this.revision = revision;
        }

        public String getBlobId() {
            return blobId;
        }

        public void setBlobId(String blobId) {
            this.blobId = blobId;
        }
    }
    
    private String repositoryName;
    private String workspaceName;
    private long revision;
    private Collection<Bundle> bundles;
    private int textLimit;
    
    public IndexingRequest() {
    }
    
    public IndexingRequest( String repositoryName, String workspaceName, long revision, Collection<Bundle> bundles, int textLimit) {
        this.repositoryName = repositoryName;
        this.workspaceName = workspaceName;
        this.revision = revision;
        this.bundles = new ArrayList<>( bundles);
        this.textLimit = textLimit;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public Collection<Bundle> getBundles() {
        return bundles;
    }

    public void setBundles(Collection<Bundle> bundles) {
        this.bundles = bundles;
    }
    
    public int getTextLimit() {
        return this.textLimit;
    }
    
    public void setTextLimit( int limit) {
        this.textLimit = limit;
    }

    @Override
    public String toString() {
        return "IndexingRequest{" + "repositoryName=" + repositoryName + ", workspaceName=" + workspaceName + ", revision=" + revision + ", bundles=" + bundles + ", textLimit=" + textLimit + '}';
    }
}

/*
 * $Id: NodeId.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.api;

import ch.ovata.cr.spi.store.StoreDocument;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author dani
 */
public class NodeId implements Serializable {
    
    public static final String UUID_FIELD = Node.UUID_FIELD;
    public static final String REVISION_FIELD = Node.REVISION_FIELD;
    public static final String STEP_FIELD = Node.STEP_FIELD;
    
    private final String uuid;
    private final long revision;
    private final long step;
    
    public NodeId( long revision, long step) {
        this( UUID.randomUUID().toString().toUpperCase(), revision, step);
    }

    public NodeId( StoreDocument document) {
        this( document.getString( UUID_FIELD), document.getLong( REVISION_FIELD), document.getLong( STEP_FIELD));
    }
    
    public NodeId( String uuid, long revision, long step) {
        this.uuid = uuid;
        this.revision = revision;
        this.step = step;
    }
    
    public String getUUID() {
        return this.uuid;
    }
    
    public long getRevision() {
        return this.revision;
    }
    
    public long getStep() {
        return this.step;
    }
    
    public boolean isRoot() {
        return Node.ROOT_ID.equals( this.uuid);
    }
    
    public StoreDocument toDocument( StoreDocument document) {
        return document.append( UUID_FIELD, this.uuid).append( REVISION_FIELD, this.revision).append( STEP_FIELD, 0l);
    }
    
    @Override
    public String toString() {
        return uuid + '#' + Long.toString( this.revision);
    }
    
    @Override
    public boolean equals( Object other) {
        return (other instanceof NodeId o) && (this.revision == o.revision) && this.uuid.equals( o.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash( uuid, revision);
    }
}

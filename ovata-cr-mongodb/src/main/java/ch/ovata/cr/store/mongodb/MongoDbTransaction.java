/*
 * $Id: MongoDbTransaction.java 679 2017-02-25 10:28:00Z dani $
 * Created on 12.01.2017, 19:00:00
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
package ch.ovata.cr.store.mongodb;

import ch.ovata.cr.api.Session;
import ch.ovata.cr.impl.SessionImpl;
import ch.ovata.cr.spi.store.Transaction;
import com.mongodb.client.ClientSession;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.Document;

/**
 *
 * @author dani
 */
public class MongoDbTransaction implements Transaction {
    
    public static final String REVISION_FIELD = "_id";
    public static final String REPOSITORY_NAME_FIELD = "repositoryName";
    public static final String WORKSPACENAME_FIELD = "workspaceName";
    public static final String USERNAME_FIELD = "username";
    public static final String STARTTIME_FIELD = "startTime";
    public static final String ENDTIME_FIELD = "endTime";
    public static final String STATE_FIELD = "state";
    public static final String COMMIT_MESSAGE_FIELD = "message";
    public static final String AFFECTED_NODE_IDS_FIELD = "affectedNodeIds";
            
    private final SessionImpl session;
    private final String repositoryName;
    private final String workspaceName;
    private final long revision;
    private State state;
    private final String username;
    private final Date startTime;
    private final String message;
    private final Collection<String> affectedNodeIds;
    private Date endTime;
    private ClientSession mongoDbClientSession  = null;
    
    public MongoDbTransaction( SessionImpl session, long revision, String message) {
        this.session = session;
        this.repositoryName = session.getRepository().getName();
        this.username = session.getRepository().getPrincipal().getName();
        this.workspaceName = session.getWorkspace().getName();
        this.revision = revision;
        this.startTime = new Date();
        this.endTime = null;
        this.state = State.IN_FLIGHT;
        this.message = message;
        this.affectedNodeIds = session.getDirtyState().getModifiedNodeIds();
    }
    
    public MongoDbTransaction( Document document) {
        this.session = null;
        this.repositoryName = document.getString( REPOSITORY_NAME_FIELD);
        this.username = document.getString( USERNAME_FIELD);
        this.workspaceName = document.getString( WORKSPACENAME_FIELD);
        this.revision = document.getLong( REVISION_FIELD);
        this.startTime = document.getDate( STARTTIME_FIELD);
        this.endTime = document.getDate( ENDTIME_FIELD);
        this.state = State.valueOf( document.getString( STATE_FIELD));
        this.message = document.getString( COMMIT_MESSAGE_FIELD);
        this.affectedNodeIds = document.get( AFFECTED_NODE_IDS_FIELD, List.class);
    }

    @Override
    public Session getSession() {
        return this.session;
    }
    
    @Override
    public Date getStartTime() {
        return this.startTime;
    }
    
    @Override
    public Date getEndTime() {
        return this.endTime;
    }
    
    @Override
    public Collection<String> getAffectedNodeIds() {
        return (this.affectedNodeIds != null) ? this.affectedNodeIds : Collections.emptyList();
    }
    
    @Override
    public long getRevision() {
        return this.revision;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getWorkspaceName() {
        return this.workspaceName;
    }
    
    @Override
    public Optional<String> getMessage() {
        return Optional.ofNullable( this.message);
    }
    
    @Override
    public void markCommitted() {
        if( this.state != State.IN_FLIGHT) {
            throw new IllegalStateException( "Only transactions in state 'IN_FLIGHT' can be commited.");
        }
        
        this.endTime = new Date();
        this.state = State.COMMITTED;
    }
    
    public void setMongoDbClientSession( ClientSession cs) {
        this.mongoDbClientSession = cs;
    }
    
    public ClientSession getMongoDbClientSession() {
        return this.mongoDbClientSession;
    }
    
    public Document toDocument() {
        return new Document()
                    .append( REVISION_FIELD, this.revision)
                    .append( REPOSITORY_NAME_FIELD, this.repositoryName)
                    .append( WORKSPACENAME_FIELD, this.workspaceName)
                    .append( STATE_FIELD, this.state.name())
                    .append( USERNAME_FIELD, this.username)
                    .append( STARTTIME_FIELD, this.startTime)
                    .append( ENDTIME_FIELD, this.endTime)
                    .append( COMMIT_MESSAGE_FIELD, this.message)
                    .append( AFFECTED_NODE_IDS_FIELD, affectedNodeIds);
    }
    
    @Override
    public String toString() {
        return "Transaction{" + "username=" + username + ", workspaceName=" + workspaceName + ", revision=" + revision + ", startTime=" + startTime + ", endTime=" + endTime + ", state=" + state + '}';
    }
}

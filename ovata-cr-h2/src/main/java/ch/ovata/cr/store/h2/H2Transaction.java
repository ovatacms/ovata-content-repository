/*
 * $Id: PostgresqlTransaction.java 2943 2020-01-22 12:30:24Z dani $
 * Created on 22.01.2018, 16:00:00
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
package ch.ovata.cr.store.h2;

import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.impl.SessionImpl;
import ch.ovata.cr.spi.store.Transaction;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class H2Transaction implements Transaction {
            
    private static final String REVISION_FIELD = "REVISION";
    private static final String REPOSITORY_NAME_FIELD = "REPOSITORY_NAME";
    private static final String WORKSPACENAME_FIELD = "WORKSPACE_NAME";
    private static final String USERNAME_FIELD = "USERNAME";
    private static final String STARTTIME_FIELD = "STARTTIME";
    private static final String ENDTIME_FIELD = "ENDTIME";
    private static final String STATE_FIELD = "TRXSTATE";
    private static final String COMMIT_MESSAGE_FIELD = "COMMIT_MESSAGE";
    private static final String AFFECTED_NODE_IDS_FIELD = "AFFECTED_NODE_IDS";
    
    private final SessionImpl session;
    private final Connection connection;
    private final String repositoryName;
    private final String workspaceName;
    private final long revision;
    private State state;
    private final String username;
    private final Date startTime;
    private final String message;
    private final Collection<String> affectedNodeIds;
    private Date endTime;
    
    public H2Transaction( SessionImpl session, Connection connection, long revision, String message) {
        this.session = session;
        this.connection = connection;
        this.repositoryName = session.getRepository().getName();
        this.username = session.getRepository().getPrincipal().getName();
        this.workspaceName = session.getWorkspace().getName();
        this.revision = revision;
        this.startTime = new Date();
        this.endTime = null;
        this.state = State.IN_FLIGHT;
        this.message = message;
        this.affectedNodeIds = this.session.getDirtyState().getModifiedNodeIds();
    }
    
    public H2Transaction( ResultSet resultSet) throws SQLException {
        try {
            this.session = null;
            this.connection = null;
            this.repositoryName = resultSet.getString( REPOSITORY_NAME_FIELD);
            this.username = resultSet.getString( USERNAME_FIELD);
            this.workspaceName = resultSet.getString( WORKSPACENAME_FIELD);
            this.revision = resultSet.getLong( REVISION_FIELD);
            this.startTime = resultSet.getTimestamp( STARTTIME_FIELD);
            this.endTime = resultSet.getTimestamp( ENDTIME_FIELD);
            this.state = State.valueOf( resultSet.getString( STATE_FIELD));
            this.message = resultSet.getString( COMMIT_MESSAGE_FIELD);

            String json = H2Collection.toString( resultSet.getCharacterStream( AFFECTED_NODE_IDS_FIELD));
            Document document = Document.parse( json);

            this.affectedNodeIds = document.get( "ids", List.class);
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not read transaction.", e);
        }
    }

    @Override
    public Session getSession() {
        return this.session;
    }
    
    public Connection getConnection() {
        return this.connection;
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
    
    public String getRepositoryName() {
        return this.repositoryName;
    }
    
    public State getState() {
        return this.state;
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
    
    @Override
    public String toString() {
        return "Transaction{" + "username=" + username + ", workspaceName=" + workspaceName + ", revision=" + revision + ", startTime=" + startTime + ", endTime=" + endTime + ", state=" + state + '}';
    }
}

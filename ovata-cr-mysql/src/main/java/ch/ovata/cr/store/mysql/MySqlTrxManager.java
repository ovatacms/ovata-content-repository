/*
 * $Id: MySqlTrxManager.java 697 2017-03-06 18:12:59Z dani $
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
package ch.ovata.cr.store.mysql;

import ch.ovata.cr.api.OptimisticLockingException;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.TrxCallback;
import ch.ovata.cr.impl.AbstractTrxManager;
import ch.ovata.cr.impl.SessionImpl;
import ch.ovata.cr.impl.WorkspaceImpl;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import ch.ovata.cr.spi.store.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MySqlTrxManager extends AbstractTrxManager {

    private static final Logger logger = LoggerFactory.getLogger( MySqlTrxManager.class);
    
    public static final String TRANSACTIONS_TABLE = "system_transactions";
    
    public MySqlTrxManager( MySqlDatabase database, ConcurrencyControlFactory ccontrolfactory) {
        super( database, ccontrolfactory, () -> 0);
        
        createTransactionsTable( database);
    }

    @Override
    public long currentRevision() {
        String sql = "SELECT MAX(REVISION) AS MAXREV FROM " + getTrxTableName();
        
        try( Connection c = this.getDatabase().getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            try( ResultSet r = stmt.executeQuery()) {
                if( r.next()) {
                    return r.getLong( "MAXREV");
                }
                else {
                    return 0l;
                }
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not retrieve latest transaction.", e);
        }
    }
    
    @Override
    public List<Transaction> getTransactions( String workspaceName, long lowerBound) {
        String sql = "SELECT * FROM " + getTrxTableName() + " WHERE (WORKSPACE_NAME = ?) AND (REVISION > ?) ORDER BY REVISION DESC";
        
        try( Connection c = this.getDatabase().getDbConnection(); PreparedStatement stmt = c.prepareStatement( sql)) {
            stmt.setString( 1, workspaceName);
            stmt.setLong( 2, lowerBound);

            try( ResultSet r = stmt.executeQuery()) {
                List<Transaction> trxs = new ArrayList<>();

                while( r.next()) {
                    trxs.add( new MySqlTransaction( r));
                }

                return trxs;
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not read transactions.", e);
        }
    }

    private MySqlTransaction createTransaction( Connection connection, Session session, String message) throws SQLException {
        return new MySqlTransaction( (SessionImpl)session, connection, getNextRevision( connection), message);
    }

    @Override
    public void revertTo( Transaction trx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTransactions( String workspaceName, long revisionLimit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Transaction commit( Session session, String message, Optional<TrxCallback> callback) {
        try {
            if( this.ccontrol.acquireLock()) {
                try( Connection connection = ((MySqlDatabase)this.database).getDbConnection()) {
                    try {
                        MySqlTransaction trx = createTransaction( connection, session, message);

                        checkForConflicts( trx);

                        ((WorkspaceImpl)trx.getSession().getWorkspace()).appendChanges( trx);
                        insertTrx( trx);

                        callback.ifPresent( c -> c.doWork( trx));

                        connection.commit();

                        return trx;
                    }
                    catch( OptimisticLockingException e) {
                        connection.rollback();
                        
                        throw e;
                    }
                    catch( SQLException | RuntimeException e) {
                        connection.rollback();
                        
                        throw new RepositoryException( "Could not persist changes.", e);
                    }
                }
                catch( SQLException e) {
                    throw new RepositoryException( "Could not acquire database connection.", e);
                }
                finally {
                    this.ccontrol.releaseLock();
                }
            }
            else {
                throw new RepositoryException( "Could not acquire repository lock when starting a transaction.");
            }
        }
        catch( InterruptedException e) {
            logger.warn( "Thread was interrupted during the start of a transaction.", e);
            
            throw new RepositoryException( "Could not acquire repository lock.", e);
        }
    }
    
    private void insertTrx( MySqlTransaction trx) throws SQLException {
        String sql = "INSERT INTO " + getTrxTableName() + " (REVISION, REPOSITORY_NAME, WORKSPACE_NAME, USERNAME, STARTTIME, ENDTIME, TRXSTATE, AFFECTED_NODE_IDS) VALUES (?,?,?,?,?,?,?,?)";
        
        try( PreparedStatement stmt = trx.getConnection().prepareStatement( sql)) {
            Document document = new Document().append( "ids", trx.getAffectedNodeIds());

            stmt.setLong( 1, trx.getRevision());
            stmt.setString( 2, trx.getRepositoryName());
            stmt.setString( 3, trx.getWorkspaceName());
            stmt.setString( 4, trx.getUsername());
            stmt.setTimestamp( 5, new java.sql.Timestamp( trx.getStartTime().getTime()));
            stmt.setTimestamp( 6, new java.sql.Timestamp( System.currentTimeMillis()));
            stmt.setString( 7, trx.getState().name());
            stmt.setString( 8, document.toJson());

            stmt.execute();
        }
    }
    
    private long getNextRevision( Connection connection) throws SQLException {
        try( Statement stmt = connection.createStatement()) {
            try( ResultSet r = stmt.executeQuery( "SELECT max(revision) as CURRENT_REVISION FROM " + getTrxTableName())) {
                if( r.next()) {
                    return r.getLong( "CURRENT_REVISION") + 2;
                }
                else {
                    throw new RepositoryException( "Could not get next revision.");
                }
            }
        }
    }
    
    private void createTransactionsTable( MySqlDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + getTrxTableName() + "(" +
                        "REVISION BIGINT," +
                        "REPOSITORY_NAME VARCHAR(255)," +
                        "WORKSPACE_NAME VARCHAR(255)," +
                        "USERNAME VARCHAR(255)," +
                        "STARTTIME TIMESTAMP," + 
                        "ENDTIME TIMESTAMP," +
                        "TRXSTATE VARCHAR(32)," +
                        "AFFECTED_NODE_IDS MEDIUMTEXT," +
                        "COMMIT_MESSAGE MEDIUMTEXT," +
                        "PRIMARY KEY (REVISION)" +
                        ")";
        
        try( Connection c = db.getDbConnection(); Statement stmt = c.createStatement()) {
            stmt.execute( sql);
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not initiaize transactions table.", e);
        }
    }
    
    private MySqlDatabase getDatabase() {
        return (MySqlDatabase)this.database;
    }
    
    private String getTrxTableName() {
        return this.database.getName() + "_" + TRANSACTIONS_TABLE;
    }
}

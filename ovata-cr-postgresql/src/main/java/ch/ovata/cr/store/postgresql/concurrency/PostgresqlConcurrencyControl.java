/*
 * $Id: PostgresqlConcurrencyControl.java 2804 2019-11-15 12:20:47Z dani $
 * Created on 07.11.2019, 17:00:00
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
package ch.ovata.cr.store.postgresql.concurrency;

import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.ConcurrencyControl;
import ch.ovata.cr.store.postgresql.PostgresqlTrxManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import javax.sql.DataSource;

/**
 *
 * @author dani
 */
public class PostgresqlConcurrencyControl implements ConcurrencyControl {

    private final DataSource ds;
    private final String repositoryName;
    
    public PostgresqlConcurrencyControl( DataSource ds, String repositoryName) {
        this.ds = ds;
        this.repositoryName = repositoryName;
    }
    
    @Override
    public long nextRevision() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long currentRevision() {
        try( Connection con = ds.getConnection()) {
            try( Statement stmt = con.createStatement()) {
                try( ResultSet r = stmt.executeQuery( "SELECT max(revision) as CURRENT_REVISION FROM " + this.repositoryName + "_" + PostgresqlTrxManager.TRANSACTIONS_TABLE)) {
                    if( r.next()) {
                        return r.getLong( "CURRENT_REVISION");
                    }
                    else {
                        return 0l;
                    }
                }
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not retrieve current revision.", e);
        }
    }

    @Override
    public boolean acquireLock() throws InterruptedException {
        return true;
    }

    @Override
    public void releaseLock() {
    }

    @Override
    public void signalClearCache() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void lockNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseNodeLock(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<LockInfo> getNodeLockInfo(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void breakNodeLock(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

/*
 * $Id: AbstractTrxManager.java 2899 2020-01-06 09:54:24Z dani $
 * Created on 08.05.2018, 19:00:00
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
import ch.ovata.cr.api.OptimisticLockingException;
import ch.ovata.cr.spi.store.ConcurrencyControl;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import ch.ovata.cr.spi.store.StoreDatabase;
import ch.ovata.cr.spi.store.Transaction;
import ch.ovata.cr.spi.store.TransactionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public abstract class AbstractTrxManager implements TransactionManager {

    private static final Logger logger = LoggerFactory.getLogger( AbstractTrxManager.class);
    
    protected final StoreDatabase database;
    protected ConcurrencyControl ccontrol = null;
    
    protected AbstractTrxManager( StoreDatabase database, ConcurrencyControlFactory ccontrolfactory, LongSupplier latestTrx) {
        this.database = database;

        try {        
            this.ccontrol = ccontrolfactory.setupRepository( this.database.getName(), latestTrx);
        }
        catch( InterruptedException e) {
            logger.warn( "Thread was interrupted during the setup of the concurrency control factory.", e);
            
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void lockNode(Node node) {
        this.ccontrol.lockNode(node);
    }

    @Override
    public void releaseNodeLock(Node node) {
        this.ccontrol.releaseNodeLock(node);
    }

    @Override
    public Optional<LockInfo> getLockInfo(Node node) {
        return this.ccontrol.getNodeLockInfo(node);
    }

    @Override
    public void breakNodeLock(Node node) {
        this.ccontrol.breakNodeLock(node);
    }

    @Override
    public void signalClearCache() {
        this.ccontrol.signalClearCache();
    }

    protected void checkForConflicts( Transaction currentTrx) {
        Set<String> nodeIdsInConflict = new HashSet<>();
        List<Transaction> trxs = this.getTransactions( currentTrx.getWorkspaceName(), currentTrx.getSession().getRevision());

        for( Transaction trx : trxs) {
            if(  currentTrx.getRevision() != trx.getRevision()) {
                List<String> intersection = new ArrayList<>( trx.getAffectedNodeIds());

                intersection.retainAll( currentTrx.getAffectedNodeIds());
                nodeIdsInConflict.addAll( intersection);
            }
        }

        if( !nodeIdsInConflict.isEmpty()) {
            logger.debug( "Optimistic locking exception. Node-Ids in conflict: {}.", nodeIdsInConflict);
            throw new OptimisticLockingException( "Conflicting modification for nodes : [" + concatNodeIds( nodeIdsInConflict) + "].");
        }
    }
    
    private String concatNodeIds( Collection<String> nodeIds) {
        String ids = nodeIds.stream().limit( 3).collect( Collectors.joining( ","));
        
        return nodeIds.size() > 3 ? ids + ", ..." : ids;
    }
}

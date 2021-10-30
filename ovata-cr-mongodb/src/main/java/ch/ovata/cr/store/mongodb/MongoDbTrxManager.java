/*
 * $Id: MongoDbTrxManager.java 697 2017-03-06 18:12:59Z dani $
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

import ch.ovata.cr.impl.AbstractTrxManager;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.TrxCallback;
import ch.ovata.cr.impl.SessionImpl;
import ch.ovata.cr.impl.WorkspaceImpl;
import ch.ovata.cr.spi.store.Transaction;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MongoDbTrxManager extends AbstractTrxManager {
    
    public static final String TRANSACTIONS_COLLECTION = "system_transactions";
    public static final Logger logger = LoggerFactory.getLogger( MongoDbTrxManager.class);
    
    public MongoDbTrxManager( MongoDbDatabase database) {
        super( database, () -> MongoDbTrxManager.getLatestRevision( database, TRANSACTIONS_COLLECTION));
        
        createTransactionsCollection( database);
    }
    
    private void createTransactionsCollection( MongoDbDatabase db) {
        if( db.listCollectionNames().noneMatch( TRANSACTIONS_COLLECTION::equals)) {
            db.getClient().getDatabase( database.getName()).createCollection( TRANSACTIONS_COLLECTION);
        }
    }
    
    @Override
    public long currentRevision() {
        throw new UnsupportedOperationException( "Not Implemented.");
    }
    
    @Override
    public List<Transaction> getTransactions( String workspaceName, long lowerBound) {
        MongoCollection<Document> collection = this.getDatabase().getTransactionsTable( TRANSACTIONS_COLLECTION);
        Document filter = new Document().append( MongoDbTransaction.WORKSPACENAME_FIELD, workspaceName)
                                        .append( MongoDbTransaction.STATE_FIELD, Transaction.State.COMMITTED.name())
                                        .append( MongoDbTransaction.REVISION_FIELD, new Document( "$gt", lowerBound));
        FindIterable<Document> documents = collection.find( filter);

        return StreamSupport.stream( documents.spliterator(), false).map( d -> new MongoDbTransaction( d)).collect( Collectors.toList());
    }
    
    @Override
    public Transaction commit( Session session, String message, Optional<TrxCallback> callback) {
        throw new UnsupportedOperationException( "Not implemented.");
//        try {
//            if( this.ccontrol.acquireLock()) {
//                try {
//                    MongoDbTransaction trx = createTransaction( session, message);
//
//                    checkForConflicts( trx);
//            
//                    TransactionOptions txnOptions = TransactionOptions.builder().readPreference( ReadPreference.primary())
//                                                                                .readConcern( ReadConcern.LOCAL)
//                                                                                .writeConcern( WriteConcern.MAJORITY).build();        
//
//                    try( ClientSession cs = this.getDatabase().getClient().startSession()) {
//                        cs.withTransaction( () -> {
//                            trx.setMongoDbClientSession( cs);
//                            trx.markCommitted();
//
//                            ((WorkspaceImpl)trx.getSession().getWorkspace()).appendChanges( trx);
//                            getTransactionCollection().insertOne( cs, trx.toDocument());
//
//                            callback.ifPresent( c -> c.doWork( trx));
//                            
//                            return "";
//                        }, txnOptions);
//                    }
//                    
//                    return trx;
//                }
//                finally {
//                    this.ccontrol.releaseLock();
//                }
//            }
//            else {
//                throw new RepositoryException( "Could not acquire repository lock when starting a transaction.");
//            }
//        }
//        catch( InterruptedException e) {
//            logger.warn( "Thread was interrupted during the start of a transaction.", e);
//            
//            throw new RepositoryException( "Could not acquire repository lock.", e);
//        }
    }
    
    private MongoDbTransaction createTransaction( Session session, String message) {
//        long nextRevision = this.ccontrol.nextRevision();
//
//        return new MongoDbTransaction( (SessionImpl)session, nextRevision, message);
        return null;
    }

    @Override
    public void revertTo(Transaction trx) {
        MongoDbCollection collection = this.getDatabase().getCollection( trx.getWorkspaceName());
        Document filter = new Document( MongoDbTransaction.WORKSPACENAME_FIELD, trx.getWorkspaceName()).append( MongoDbTransaction.REVISION_FIELD, new Document( "$gt", trx.getRevision()));
        
        collection.revertTo( trx.getRevision());
        this.getDatabase().getTransactionsTable( TRANSACTIONS_COLLECTION).deleteMany( filter);
        
//        this.ccontrol.signalClearCache();
    }
    
    @Override
    public void removeTransactions(String workspaceName, long revisionLimit) {
        Document doc = this.getDatabase().getTransactionsTable( TRANSACTIONS_COLLECTION).find( new Document( MongoDbTransaction.WORKSPACENAME_FIELD, workspaceName)).sort( new Document( MongoDbTransaction.REVISION_FIELD, -1)).first();
        
        if( doc != null) {
            long maxrevision = doc.getLong( MongoDbTransaction.REVISION_FIELD);
            
            if( maxrevision <= revisionLimit) {
                revisionLimit = maxrevision - 2;
            }
        }
        
        this.getDatabase().getTransactionsTable( TRANSACTIONS_COLLECTION).deleteMany( new Document( MongoDbTransaction.WORKSPACENAME_FIELD, workspaceName).append( MongoDbTransaction.REVISION_FIELD, new Document( "$lte", revisionLimit)));
        
//        this.ccontrol.signalClearCache();
    }
    
    private  MongoCollection<Document> getTransactionCollection() {
        return this.getDatabase().getTransactionsTable( TRANSACTIONS_COLLECTION);
    }
    
    private static long getLatestRevision( MongoDbDatabase db, String collectionName) {
        Document document = db.getTransactionsTable( collectionName).find().sort( new Document( MongoDbTransaction.REVISION_FIELD, -1)).first();
        
        if( document != null) {
            return document.getLong( MongoDbTransaction.REVISION_FIELD);
        }
        
        return 0;
    }

    private MongoDbDatabase getDatabase() {
        return (MongoDbDatabase)this.database;
    }
}

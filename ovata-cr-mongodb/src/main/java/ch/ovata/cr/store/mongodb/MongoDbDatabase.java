/*
 * $Id: MongoDbDatabase.java 679 2017-02-25 10:28:00Z dani $
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

import ch.ovata.cr.bson.MongoDbDocument;
import ch.ovata.cr.bson.MongoDbDocumentList;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import ch.ovata.cr.spi.store.StoreCollection;
import ch.ovata.cr.spi.store.StoreDatabase;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import ch.ovata.cr.spi.store.TransactionManager;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MongoDbDatabase implements StoreDatabase {
    
    private static final Logger logger = LoggerFactory.getLogger( MongoDbDatabase.class);
    
    private final MongoClient client;
    private final MongoDatabase database;
    private final TransactionManager trxMgr;
    private final BlobStore blobStore;
    
    public MongoDbDatabase( MongoClient client, MongoDatabase database, BlobStore blobStore, ConcurrencyControlFactory ccontrol) {
        this.client = client;
        this.database = database;
        this.trxMgr = new MongoDbTrxManager( this, ccontrol);
        this.blobStore = blobStore;
    }
    
    @Override
    public TransactionManager getTransactionManager() {
        return this.trxMgr;
    }
    
    @Override
    public String getName() {
        return this.database.getName();
    }
    
    MongoClient getClient() {
        return this.client;
    }
    
    public MongoDbCollection getCollection( String tableName) {
        return new MongoDbCollection( this.database.getCollection( tableName));
    }
    
    @Override
    public StoreCollection getOrCreateCollection( String tableName) {
        if( listCollectionNames().anyMatch( tableName::equals)) {
            return new MongoDbCollection( this.database.getCollection( tableName));
        }

        return new MongoDbCollection( createCollection( tableName));
    }
    
    public MongoCollection<Document> getTransactionsTable( String tableName) {
        return this.database.getCollection( tableName);
    }
    
    private MongoCollection<Document> createCollection( String tableName) {
        MongoCollection<Document> collection = this.database.getCollection( tableName);

        Bson index1 = new Document( Node.PARENT_ID_FIELD, 1).append( "_id.uuid", 1).append( "_id.revsion", -1);
        Bson index2 = new Document( "_id.uuid", 1).append( "_id.revision", -1).append( "@Removed", 1);
        Bson index3 = Indexes.ascending( Node.PARENT_ID_FIELD, Node.NAME_FIELD);
        
        collection.createIndex( index1);
        collection.createIndex( index2);
        collection.createIndex( index3);

        collection.createIndex( Indexes.text( "$**"), new IndexOptions().languageOverride( "@language"));
        
        return collection;
    }
    
    @Override
    public void dropCollection( String name) {
        this.database.getCollection( name).drop();
        this.trxMgr.signalClearCache();
    }

    @Override
    public Stream<String> listCollectionNames() {
        return StreamSupport.stream( this.database.listCollectionNames().spliterator(), false);
    }

    @Override
    public StoreDocument newDocument() {
        return new MongoDbDocument();
    }

    @Override
    public StoreDocumentList newList() {
        return new MongoDbDocumentList();
    }

    @Override
    public long currentRevision() {
        return this.trxMgr.currentRevision();
    }

    @Override
    public BlobStore getBlobStore() {
        return this.blobStore;
    }

    @Override
    public void vacuum( String workspaceName, long revision) {
        logger.info( "Performing vacuum on workspace <{}> up to revision <{}>.", workspaceName, revision);
        
        MongoCollection<Document> collection = this.database.getCollection( workspaceName);
        DistinctIterable<String> ids = collection.distinct( "_id.uuid", String.class);
        
        for( String id : ids) {
            Document first = collection.find( new Document( "_id.uuid", id).append( "_id.revision", new Document( "$lte", revision))).sort( new Document( "_id.revision", -1)).first();
            
            if( first != null) {
                NodeId iddoc = createNodeId( first);
                long firstRevisionToDelete = iddoc.getRevision();

                if( first.getBoolean( Node.REMOVED_FIELD) == null) {
                    firstRevisionToDelete -= 2;
                }
                
                logger.debug( "Removing revisions older or equal to  <{}> for node-id <{}>.", firstRevisionToDelete, id);
                collection.deleteMany( new Document( "_id.uuid", id).append( "_id.revision", new Document( "$lte", firstRevisionToDelete)));
            }
        }
    }
    
    private NodeId createNodeId( Document document) {
        Document iddoc = document.get( Node.NODE_ID_FIELD, Document.class);

        return new NodeId( iddoc.getString( Node.UUID_FIELD), iddoc.getLong( Node.REVISION_FIELD));
    }
}

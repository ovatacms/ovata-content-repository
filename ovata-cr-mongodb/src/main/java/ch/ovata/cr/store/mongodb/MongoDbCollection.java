/*
 * $Id: MongoDbCollection.java 679 2017-02-25 10:28:00Z dani $
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
import ch.ovata.cr.spi.store.StoreCollection;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.Document;
import ch.ovata.cr.spi.store.StoreDocumentIterable;
import ch.ovata.cr.spi.store.Transaction;
import com.mongodb.client.MongoClient;

/**
 *
 * @author dani
 */
public class MongoDbCollection implements StoreCollection {
    
    private static final String ID_REVISION_FIELD = "_id.revision";
    private static final String ID_UUID_FIELD = "_id.uuid";
    
    private final MongoClient client;
    private final MongoCollection<Document> collection;
    
    public MongoDbCollection( MongoClient client, MongoCollection<Document> collection) {
        this.client = client;
        this.collection = collection;
    }
    
    public MongoCollection<Document> unwrap() {
        return this.collection;
    }
    
    @Override
    public void init( StoreDocument document) {
        this.collection.insertOne( ((MongoDbDocument)document).toDocument());
    }
    
    @Override
    public void insertMany( Transaction trx, Collection<StoreDocument> documents) {
        MongoDbTransaction mongoTrx = (MongoDbTransaction)trx;
        List<Document> mongoDocuments = documents.stream().map( d -> ((MongoDbDocument)d).toDocument()).collect( Collectors.toList());

        MongoDbCollection.this.collection.insertMany( mongoTrx.getMongoDbClientSession(), mongoDocuments);
    }

    /**
     * Returns the document for a given id with the minimum positive or 0 delta between (revision - document.revision)
     * @param id id of the document
     * @param revision max revision of the document
     * @return the latest document with the given id respecting the provided revision
     */
    @Override
    public StoreDocument findById(String id, long revision) {
        Document filter = new Document( ID_UUID_FIELD, id).append( ID_REVISION_FIELD, new Document( "$lte", revision));
        Document sort = new Document( ID_REVISION_FIELD, -1).append( Node.REMOVED_FIELD, 1);
        Document doc = this.collection.find( filter).projection( Projections.exclude( Node.FULLTEXT_FIELD)).sort( sort).first();
        
        if( (doc != null) && !doc.containsKey( Node.REMOVED_FIELD)) {
            return new MongoDbDocument( doc);
        }
        
        return null;
    }

    /**
     * Returns the list of documents belonging to the given parent.
     * @param parentUuid
     * @param revision
     * @return the result is sortd by uuid and then by revision (descending)
     */
    @Override
    public StoreDocumentIterable findByParentId(String parentUuid, long revision) {
        Document filter = new Document( Node.PARENT_ID_FIELD, parentUuid).append( ID_REVISION_FIELD, new Document( "$lte", revision));
        Document sort = new Document( ID_UUID_FIELD, 1).append(  ID_REVISION_FIELD, -1);
        FindIterable<Document> documents = this.collection.find( filter).projection( Projections.exclude( Node.FULLTEXT_FIELD)).sort( sort).batchSize( 8192);

        return new MongoDbDocumentIterable( documents);
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
    public StoreDocument findByParentIdAndName(String parentUuid, String name, long revision) {
        Document filter = new Document( Node.PARENT_ID_FIELD, parentUuid).append( Node.NAME_FIELD, name).append( ID_REVISION_FIELD, new Document( "$lte", revision));
        Document sort = new Document( ID_REVISION_FIELD, -1).append( Node.REMOVED_FIELD, 1);
        Document document = this.collection.find( filter).projection( Projections.exclude( Node.FULLTEXT_FIELD)).sort( sort).first();
        
        return ((document != null) && !document.containsKey( Node.REMOVED_FIELD)) ? new MongoDbDocument( document) : null;
    }

    public void revertTo(long revision) {
        this.collection.deleteMany( new Document( ID_REVISION_FIELD, new Document( "$gt", revision)));
    }
}

/*
 * $Id: FulltextIndexer.java 2918 2020-01-08 14:58:39Z dani $
 * Created on 16.09.2019, 16:00:00
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
package ch.ovata.cr.store.mongodb.fulltext;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.tika.exception.TikaException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author dani
 */
public class FulltextIndexer {
        
    private static final Logger logger = LoggerFactory.getLogger( FulltextIndexer.class);

    private ThreadPoolExecutor executor = new ThreadPoolExecutor( 1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue( 1000));
    
    private final BlobStoreFactory factory;
    private final MongoClient client;
    
    public FulltextIndexer( BlobStoreFactory factory, MongoClient client) {
        this.factory = factory;
        this.client = client;
    }
    
    public void shutdown() {
        executor.shutdownNow();
    }
    
    public void index( FulltextIndexingRequest request) {
        executor.execute( new IndexRequest( request));
    }
    
    public class IndexRequest implements Runnable {
        
        private final FulltextIndexingRequest request;
        
        public IndexRequest( FulltextIndexingRequest request) {
            this.request = request;
        }

        @Override
        public void run() {
            logger.info( "Indexing binary <{}> for node <{}> in repository <{}> and workspace <{}>.", request.getBinaryId(), request.getNodeId(), request.getRepositoryName(), request.getWorkspaceName());
            
            BlobStore store = factory.createBlobStore( request.getRepositoryName());
            Binary binary = store.findBlob( request.getBinaryId());
            
            try {
                String text = TextExtractor.extract( binary);

                MongoDatabase database = client.getDatabase( request.getRepositoryName());
                MongoCollection<Document> collection = database.getCollection( request.getWorkspaceName());

                Document filter = new Document( Node.NODE_ID_FIELD, new Document( Node.UUID_FIELD, request.getNodeId().getUUID()).append( Node.REVISION_FIELD, request.getRevision()));
                Document update = new Document( "$set", new Document( Node.FULLTEXT_FIELD, text));

                UpdateResult result = collection.updateOne( filter, update);

                if( result.getModifiedCount() != 1 ) {
                    logger.warn( "Could not update fulltext field for node with id <{}> and revision <{}>.", request.getNodeId().getUUID(), request.getNodeId().getRevision());
                }
            }
            catch( IOException | SAXException | TikaException e) {
                logger.warn( "Could not extract text from {}.", binary.getId());
            }
        }
    }
}

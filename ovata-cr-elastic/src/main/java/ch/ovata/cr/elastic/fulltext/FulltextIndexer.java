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
package ch.ovata.cr.elastic.fulltext;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author dani
 */
public class FulltextIndexer {
        
    private static final Logger logger = LoggerFactory.getLogger( FulltextIndexer.class);

    private static int THREAD_POOL_SIZE = Integer.parseInt( System.getProperty( "ovatacr.fulltext.threads", "2"));
    
    private ThreadPoolExecutor executor = new ThreadPoolExecutor( 1, THREAD_POOL_SIZE, 5, TimeUnit.SECONDS, new ArrayBlockingQueue( 1000));
    
    private final BlobStoreFactory factory;
    private final RestHighLevelClient client;
    
    public FulltextIndexer( BlobStoreFactory factory, RestHighLevelClient client) {
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
            logger.info( "Indexing binary <{}> for node <{}> in <{}@{}>.", request.getBinaryId(), request.getNodeId(), request.getWorkspaceName(), request.getRepositoryName());
            
            BlobStore store = factory.createBlobStore( request.getRepositoryName());
            Binary binary = store.findBlob( request.getBinaryId());

            try {            
                String text = TextExtractor.extract( binary);
                
                XContentBuilder builder = XContentFactory.jsonBuilder();
                
                builder.startObject();
                builder.field( "@fulltext", text);
                builder.endObject();
                
                UpdateRequest ur = new UpdateRequest( "ovata-cr-" + request.getRepositoryName() + "-" + request.getWorkspaceName(), request.getNodeId().getUUID()).doc( builder);
                
                client.update(ur, RequestOptions.DEFAULT);
            }
            catch( IOException | SAXException | TikaException e) {
                logger.warn( "Could not extract text from {}.", binary.getId());
            }
        }
    }
}

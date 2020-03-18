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
package ch.ovata.cr.store.mysql.search;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author dani
 */
public class FulltextIndexer {
        
    private static final Logger logger = LoggerFactory.getLogger( FulltextIndexer.class);

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor( 1, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue( 1000));
    
    private final BlobStoreFactory factory;
    private final DataSource dataSource;
    
    public FulltextIndexer( BlobStoreFactory factory, DataSource dataSource) {
        this.factory = factory;
        this.dataSource = dataSource;
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
            
            try( Connection c = dataSource.getConnection()) {
                String text = TextExtractor.extract( binary);
                
                MySqlFulltextSearchProvider.updateFulltext( c, request.getRepositoryName(), request.getWorkspaceName(), request.getNodeId(), request.getRevision(), text);
            }
            catch( IOException | SQLException | SAXException | TikaException e) {
                logger.warn( "Could not extract/update fulltext index for node <{}>.", request.getNodeId());
            }
        }
    }
}

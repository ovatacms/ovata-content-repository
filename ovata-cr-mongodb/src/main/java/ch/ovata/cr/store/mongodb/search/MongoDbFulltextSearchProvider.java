/*
 * $Id: MongoDbFulltextSearchProvider.java 2918 2020-01-08 14:58:39Z dani $
 * Created on 03.05.2018, 12:00:00
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
package ch.ovata.cr.store.mongodb.search;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.store.mongodb.fulltext.FulltextIndexer;
import ch.ovata.cr.store.mongodb.fulltext.FulltextIndexingRequest;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MongoDbFulltextSearchProvider extends MongoDbSearchProvider {

    private static final Logger logger = LoggerFactory.getLogger( MongoDbFulltextSearchProvider.class);

    private final FulltextIndexer indexer;
    
    public MongoDbFulltextSearchProvider( MongoClient client, FulltextIndexer indexer, String repositoryName) {
        super( client, repositoryName);
        
        this.indexer = indexer;
    }
    
    @Override
    public void reindexWorkspace( Repository repository, String workspaceName) {
        Session session = repository.getSession( workspaceName);
        
        indexNode( session.getRoot());
    }
    
    private void indexNode( Node node) {
        indexProperties( node, node.getNodeId().getRevision());
        
        node.getNodes().stream().forEach( this::indexNode);
    }

    @Override
    public void indexNodes( DirtyState dirtyState, long revision) {
        for( DirtyState.Modification m : dirtyState.getModifications()) {
            switch( m.getType()) {
                case MODIFY:
                case ADD:
                case MOVE:
                case RENAME:
                    indexProperties( m.getNode(), revision);
                    break;
            }
        }
    }
    
    private void indexProperties( Node node, long revision) {
        for( Property p : node.getProperties()) {
            Value v = p.getValue();
            if( v.getType() == Value.Type.BINARY) {
                indexer.index( new FulltextIndexingRequest( node.getSession().getRepository().getName(), node.getSession().getWorkspace().getName(), node.getNodeId(), revision, v.getBinary().getId()));
            }
        }
    }
}

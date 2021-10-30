/*
 * $Id: MongoDbSearchProvider.java 2787 2019-11-11 09:00:35Z dani $
 * Created on 06.03.2017, 12:00:00
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
package ch.ovata.cr.store.mongodb.search;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.bson.MongoDbDocument;
import com.mongodb.client.MongoClient;
import com.mongodb.client.FindIterable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MongoDbSearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger( MongoDbSearchProvider.class);
    
    protected final MongoClient client;
    protected final String repositoryName;
    
    public MongoDbSearchProvider( MongoClient client, String repositoryName) {
        this.client = client;
        this.repositoryName = repositoryName;
    }
    
    @Override
    public void clearIndex(String workspaceName) {
        // implicit through mongodb index
    }

    @Override
    public void reindexWorkspace( Repository repository, String workspaceName) {
        logger.info( "Fulltext indexation not supported yet.");
    }

    @Override
    public void indexNodes( DirtyState dirtyState, long revision) {
        logger.info( "Fulltext indexation not supported yet.");
    }

    public List<NodeId> executeQuery( String workspaceName, StoreDocument q) {
        Document query = ((MongoDbDocument)q).toDocument();
        Document fields = new Document( Node.NODE_ID_FIELD, 1).append( "score", new Document( "$meta", "textScore"));
        
        FindIterable<Document> docs = client.getDatabase( this.repositoryName).getCollection( workspaceName).find( query).projection( fields).sort( new Document( "score", new Document( "$meta", "textScore")));
        Map<String, NodeId> ids = new LinkedHashMap<>();
        
        for( Document doc : docs) {
            Document docid = doc.get( Node.NODE_ID_FIELD, Document.class);
            NodeId id = new NodeId( docid.getString( Node.UUID_FIELD), docid.getLong( Node.REVISION_FIELD), docid.getLong( Node.STEP_FIELD));
            NodeId prevId = ids.get( id.getUUID());
            
            if( (prevId == null) || (prevId.getRevision() < id.getRevision()) ) {
                ids.put( id.getUUID(), id);
            }
        }
        
        return new ArrayList<>( ids.values());
    }

    @Override
    public <T extends Query> T createQuery( Session session, Class<T> type) {
        if( QueryByExample.class == type) {
            return (T)new MongoDbQueryByExample( this, session);
        }
        else if( FulltextQuery.class == type) {
            return (T)new MongoDbFulltextQuery( this, session);
        }
        else {
            throw new RepositoryException( "Unknown query type <" + type.getName() + ">.");
        }
    }
}

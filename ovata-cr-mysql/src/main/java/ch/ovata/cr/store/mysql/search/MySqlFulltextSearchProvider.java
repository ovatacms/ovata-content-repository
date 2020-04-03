/*
 * $Id: MySqlSearchProvider.java 2668 2019-09-13 12:24:59Z dani $
 * Created on 22.01.2018, 16:00:00
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
package ch.ovata.cr.store.mysql.search;

import ch.ovata.cr.api.CoreNodeTypes;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import ch.ovata.cr.store.mysql.MySqlConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MySqlFulltextSearchProvider extends MySqlSearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(MySqlFulltextSearchProvider.class);
    
    private final FulltextIndexer indexer;
    
    
    public MySqlFulltextSearchProvider( BlobStoreFactory blobStoreFactory, MySqlConnection connection, String databaseName, FulltextIndexer indexer) {
        super( blobStoreFactory, connection, databaseName);

        this.indexer = indexer;
    }

    @Override
    public void clearIndex(String workspaceName) {
        logger.info( "Index is not cleared as it is managed in the node table.");
    }

    @Override
    public void reindexWorkspace( Repository repository, String workspaceName) {
        Session session = repository.getSession( workspaceName);
        
        try( Connection c = connection.unwrap( DataSource.class).getConnection()) {
            indexNode( c , session.getRoot());
            
            c.commit();
        }
        catch( SQLException e) {
            logger.warn( "Could not reindex workspace.", e);
        }
    }
    
    private void indexNode( Connection c, Node node) {
        indexProperties( c, node, node.getNodeId().getRevision());
        
        node.getNodes().stream().forEach( n -> this.indexNode( c, node));
    }

    @Override
    public void indexNodes( DirtyState dirtyState, long revision) {
        try( Connection c = connection.unwrap( DataSource.class).getConnection()) {
            for( DirtyState.Modification m : dirtyState.getModifications()) {
                switch( m.getType()) {
                    case MODIFY:
                    case ADD:
                    case MOVE:
                    case RENAME:
                        indexProperties( c, m.getNode(), revision);
                        break;
                }
            }
            
            c.commit();
        }
        catch( SQLException e) {
            logger.warn( "Could not create fulltext index.", e);
        }
    }
    
    private void indexProperties( Connection c, Node node, long revision) {
        if( node.isOfType( CoreNodeTypes.RESOURCE)) {
            for( Property p : node.getProperties()) {
                Value v = p.getValue();
                if( v.getType() == Value.Type.BINARY) {
                    indexer.index( new FulltextIndexingRequest( node.getSession().getRepository().getName(), node.getSession().getWorkspace().getName(), node.getNodeId(), revision, v.getBinary().getId()));
                }
            }
        }
        else {
            StringBuilder text = new StringBuilder();
            
            for( Property p : node.getProperties()) {
                String s = toString( p);
                
                if( !StringUtils.isBlank( s)) {
                    text.append( s);
                    text.append( "\n");
                }
            }
            
            if( text.length() > 0) {
                try {
                    updateFulltext( c, this.getDatabaseName(), node.getSession().getWorkspace().getName(), node.getNodeId(), revision, text.toString());
                }
                catch( SQLException e) {
                    logger.warn( "Could not update fulltext index for node <{}>.", node.getNodeId());
                }
            }
        }
    }
    
    public static void updateFulltext( Connection c, String repositoryName, String workspaceName, NodeId nodeId, long revision, String text) throws SQLException {
        String sql = "UPDATE " + repositoryName + "_" + workspaceName + " SET CONTENTTEXT = ? WHERE NODE_ID = ? AND REVISION = ?";

        try( PreparedStatement stmt = c.prepareStatement( sql)) {
            stmt.setString( 1, text);
            stmt.setString( 2, nodeId.getUUID());
            stmt.setLong( 3, revision);

            stmt.execute();
        }
    }
    
    private String toString( Property p) {
        Value v = p.getValue();
        
        switch( v.getType()) {
            case STRING:
                return Jsoup.parse( v.getString()).text();
            default:
                return "";
        }
    }

    @Override
    public <T extends Query> T createQuery(Session session, Class<T> type) {
        if( QueryByExample.class == type) {
            return (T)new MySqlQueryByExample( this, session);
        }
        else if( FulltextQuery.class == type) {
            return (T)new MySqlFulltextQuery( session, this);
        }
        else {
            throw new RepositoryException( "Unknown query type <" + type.getName() + ">.");
        }
    }
}

/*
 * $Id: ElasticSearchProvider.java 2944 2020-01-27 14:14:20Z dani $
 * Created on 02.03.2017, 16:00:00
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
package ch.ovata.cr.elastic;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.elastic.fulltext.FulltextIndexer;
import ch.ovata.cr.elastic.fulltext.FulltextIndexingRequest;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.spi.base.DirtyState.Modification;
import ch.ovata.cr.spi.search.SearchProvider;
import java.io.IOException;
import java.util.Map;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class ElasticSearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger( ElasticSearchProvider.class);
    
    protected final RestHighLevelClient client;
    protected final String repositoryName;
    private final FulltextIndexer indexer;
    
    public ElasticSearchProvider( RestHighLevelClient client, String repositoryName, FulltextIndexer indexer) {
        this.client = client;
        this.repositoryName = repositoryName;
        this.indexer = indexer;
    }
    
    public RestHighLevelClient getClient() {
        return this.client;
    }
    
    @Override
    public void reindexWorkspace(Repository repository, String workspaceName) {
        try {
            clearIndex( workspaceName);
            
            Session session = repository.getSession( workspaceName);

            indexNode( session.getRoot());
        }
        catch( IOException | ElasticsearchException e) {
            logger.warn( "Could not index workspace.", e);
        }
    }
    
    @Override
    public void indexNodes( DirtyState dirtyState, long revision) {
        try {
            BulkRequest bulk = new BulkRequest();

            for( Modification m : dirtyState.getModifications()) {
                switch( m.getType()) {
                    case MODIFY:
                    case ADD:
                    case MOVE:
                    case RENAME:
                        indexSingleNodeBulk( bulk, m.getNode());
                        break;
                    case REMOVE:
                        removeSingleNodeBulk( bulk, dirtyState.getWorkspaceName(), m.getNodeId());
                }
            }
        
            client.bulkAsync( bulk, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse response) {
                    logger.info( "Indexed nodes in bulk.");
                }

                @Override
                public void onFailure(Exception e) {
                    logger.warn( "Could not index nodes.", e);
                }
            });
        }
        catch( IOException e) {
            logger.warn( "Error indexing with bulk operation", e);
        }
    }

    @Override
    public void clearIndex(String workspaceName) {
        try {
            DeleteByQueryRequest r = new DeleteByQueryRequest( getEsIndexName( workspaceName));

            r.setQuery( QueryBuilders.matchAllQuery());
            r.setConflicts( "proceed");

            client.deleteByQuery( r, RequestOptions.DEFAULT);
        }
        catch( Exception e) {
            logger.warn( "Could not clear index <" + getEsIndexName( workspaceName) + ">.", e);
        }
    }

    @Override
    public <T extends Query> T createQuery(Session session, Class<T> type) {
        if( QueryByExample.class == type) {
            return (T)new ElasticQueryByExample( this, session);
        }
        else if( FulltextQuery.class == type) {
            return (T)new ElasticFulltextQuery( this, session);
        }
        else {
            throw new RepositoryException( "Unknown query type <" + type.getName() + ">.");
        }
    }
    
    private void removeSingleNodeBulk( BulkRequest bulk, String workspaceName, String nodeId) {
        bulk.add( new DeleteRequest( getEsIndexName( workspaceName), nodeId));
    }
    
    private void indexNode( Node node) throws IOException {
        indexSingleNode( node);
        
        for( Node n : node.getNodes()) {
            indexNode( n);
        }
    }
    
    private void indexSingleNode( Node node) throws IOException {
        XContentBuilder builder = createIndexEntry( node);
        IndexRequest ir = new IndexRequest( getEsIndexName( node.getSession().getWorkspace().getName()));
        
        ir.id( node.getNodeId().getUUID());
        ir.source( builder);
        
        IndexResponse response = client.index( ir, RequestOptions.DEFAULT);
        
        if( response.status().getStatus() >= 300) {
            logger.warn( "Could not index node <{}>. Error Code : <{}>.", node.getNodeId(), response.status());
        }
    }
    
    private void indexSingleNodeBulk( BulkRequest bulk, Node node) throws IOException {
        XContentBuilder builder = createIndexEntry( node);

        bulk.add( new IndexRequest( getEsIndexName( node.getSession().getWorkspace().getName())).id( node.getNodeId().getUUID()).source( builder));
    }
    
    public String getEsIndexName( String workspaceName) {
        return "ovata-cr-" + this.repositoryName + "-" + workspaceName;
    }
    
    private XContentBuilder createIndexEntry( Node node) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        
        builder.startObject();
        
        builder.field( "@revision", node.getNodeId().getRevision());
        builder.field( "@path", node.getPath());
        builder.field( "@name", node.getName());
        builder.field( "@type", node.getType());
        
        for( Property p : node.getProperties()) {
            addProperty( node, builder, p.getName(), p.getValue());
        }
        
        builder.endObject();
        
        return builder;
    }
    
    private void addProperty( Node node, XContentBuilder builder, String name, Value v) throws IOException {
        switch( v.getType()) {
            case STRING:
                if( name != null) {
                    builder.field( name, v.getString());
                }
                else {
                    builder.value( v.getString());
                }
                break;
            case BOOLEAN:
                if( name != null) {
                    builder.field( name, v.getBoolean());
                }
                else {
                    builder.value( v.getBoolean());
                }
                break;
            case DECIMAL:
                if( name != null) {
                    builder.field( name, v.getDecimal());
                }
                else {
                    builder.value( v.getDecimal());
                }
                break;
            case DOUBLE:
                if( name != null) {
                    builder.field( name, v.getDouble());
                }
                else {
                    builder.value( v.getDouble());
                }
                break;
            case LONG:
                if( name != null) {
                    builder.field( name, v.getLong());
                }
                else {
                    builder.value( v.getLong());
                }
                break;
            case LOCAL_DATE:
                if( name != null) {
                    builder.timeField( name, v.getDate());
                }
                else {
                    builder.timeValue( v.getDate());
                }
                break;
            case ZONED_DATETIME:
                if( name != null) {
                    builder.timeField( name, v.getTime().toInstant().toEpochMilli());
                }
                else {
                    builder.timeValue( v.getTime().toInstant().toEpochMilli());
                }
                break;
            case BINARY:
                indexer.index( new FulltextIndexingRequest( node.getSession().getRepository().getName(), node.getSession().getWorkspace().getName(), node.getNodeId(), v.getBinary().getId()));
                break;
            case ARRAY:
                if( name != null) {
                    builder.startArray( name);
                }
                else {
                    builder.startObject();
                }
                addArray( node, builder, v.getArray());
                builder.endArray();
                break;
            case MAP:
                if( name != null) {
                    builder.startObject( name);
                }
                else {
                    builder.startObject();
                }
                addMap( node, builder, v.getMap());
                builder.endObject();
                break;
        }
    }
    
    private void addArray( Node node, XContentBuilder builder, Value[] array) throws IOException {
        for( Value v : array) {
            addProperty( node, builder, null, v);
        }
    }
    
    private void addMap( Node node, XContentBuilder builder, Map<String, Value> map) throws IOException {
        for( Map.Entry<String, Value> e : map.entrySet()) {
            addProperty( node, builder, e.getKey(), e.getValue());
        }
    }
}

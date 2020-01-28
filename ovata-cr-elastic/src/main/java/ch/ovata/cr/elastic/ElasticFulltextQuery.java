/*
 * $Id: ElasticFulltextQuery.java 2845 2019-11-22 10:20:25Z dani $
 * Created on 13.09.2019, 16:00:00
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
package ch.ovata.cr.elastic;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.Query;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 *
 * @author dani
 */
public class ElasticFulltextQuery implements FulltextQuery {

    private final ElasticSearchProvider provider;
    private final Session session;
    
    private String term;
    private String childOf;
    private String sortProperty;
    private String[] nodeTypes = null;
    private int skip = 0;
    private int limit = 100;
    
    public ElasticFulltextQuery( ElasticSearchProvider provider, Session session) {
        this.provider = provider;
        this.session = session;
    }
    
    @Override
    public FulltextQuery term(String term) {
        this.term = term;
        
        return this;
    }

    @Override
    public FulltextQuery childOf(String path) {
        this.childOf = path;
        
        return this;
    }

    private Optional<Node> findNode( String id) {
        return this.session.findNodeByIdentifier( id);
    }
    
    @Override
    public Query sortBy(String propertyName) {
        this.sortProperty = propertyName;
        
        return this;
    }

    @Override
    public Query skip(int skip) {
        this.skip = skip;
        
        return this;
    }

    @Override
    public Query limit(int limit) {
        this.limit = limit;
        
        return this;
    }

    @Override
    public Query types(String... types) {
        this.nodeTypes = types;
        
        return this;
    }

    @Override
    public List<Node> execute() {
        try {
            RestHighLevelClient client = provider.getClient();
            SearchRequest request = new SearchRequest( provider.getEsIndexName( session.getWorkspace().getName()));
            SearchSourceBuilder builder = new SearchSourceBuilder();
            
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            
            if( this.childOf != null) {
                queryBuilder.filter( QueryBuilders.prefixQuery( "@path", this.childOf));
            }
            
            if( this.nodeTypes != null) {
                queryBuilder.filter( QueryBuilders.termsQuery( "@type", this.nodeTypes));
            }
            
            queryBuilder.must( QueryBuilders.multiMatchQuery( this.term));

            builder.query( queryBuilder);
            request.source(builder);
            builder.from( skip);
            builder.size( limit);
            
            if( this.sortProperty != null) {
                builder.sort( this.sortProperty);
            }
            
            SearchResponse response = client.search( request, RequestOptions.DEFAULT);
            Stream<SearchHit> hits = StreamSupport.stream( Spliterators.spliteratorUnknownSize( response.getHits().iterator(), Spliterator.ORDERED), false);            
            
            return hits.map( h -> (String)h.getId()).map( this::findNode).filter( Optional::isPresent).map( Optional::get).collect( Collectors.toList());
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not execute query.", e);
        }
    }
}

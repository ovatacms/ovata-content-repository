/*
 * $Id: MongoDbQueryByExample.java 679 2017-02-25 10:28:00Z dani $
 * Created on 07.03.2017, 12:00:00
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
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.util.PropertyComparatorIfAvailable;
import ch.ovata.cr.spi.store.StoreDocument;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author dani
 */
public class MongoDbFulltextQuery implements FulltextQuery {

    private final MongoDbSearchProvider provider;
    private final Session session;
    private String term;
    private String childOf = "";
    private String aggregateTo;
    private String sortProperty;
    private String[] nodeTypes = null;
    private int limit = Integer.MAX_VALUE;
    private int skip = 0;
    
    public MongoDbFulltextQuery( MongoDbSearchProvider provider, Session session) {
        this.provider = provider;
        this.session = session;
    }
    
    @Override
    public FulltextQuery term( String term) {
        this.term = term;
        
        return this;
    }

    @Override
    public FulltextQuery types( String... types) {
        this.nodeTypes = types;
        
        return this;
    }
    
    @Override
    public FulltextQuery childOf(String path) {
        this.childOf = path;
        
        return this;
    }
    
    @Override
    public FulltextQuery aggregateTo( String type) {
        this.aggregateTo = type;
        
        return this;
    }

    @Override
    public FulltextQuery sortBy(String propertyName) {
        this.sortProperty = propertyName;
        
        return this;
    }

    @Override
    public FulltextQuery skip( int skip) {
        this.skip = skip;
        
        return this;
    }

    @Override
    public FulltextQuery limit(int limit) {
        this.limit = limit;
        
        return this;
    }

    @Override
    public List<Node> execute() {
        StoreDocument query = session.getValueFactory().newDocument();
        StoreDocument filter = session.getValueFactory().newDocument();
        
        filter.append( "$search", this.term);
        query.append( "$text", filter);

        List<NodeId> ids = provider.executeQuery( session.getWorkspace().getName(), query);
        
        return ids.stream().map( this::findNode)
                           .filter( Optional::isPresent)
                           .map( Optional::get)
                           .filter( n -> n.getPath().startsWith( this.childOf))
                           .sorted( new PropertyComparatorIfAvailable( this.sortProperty))
                           .skip( this.skip)
                           .limit( this.limit)
                           .collect( Collectors.toList());
    }

    private Optional<Node> findNode( NodeId id) {
        return this.session.findNodeByIdentifier( id.getUUID()).filter( n -> n.getRevision() == id.getRevision());
    }
}

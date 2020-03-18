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
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.util.PropertyComparatorIfAvailable;
import ch.ovata.cr.spi.store.StoreDocument;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author dani
 */
public class MongoDbQueryByExample implements QueryByExample {

    private final MongoDbSearchProvider provider;
    private final Session session;
    private StoreDocument document;
    private String childOf = "";
    private String aggregateTo;
    private String sortProperty;
    private int limit = Integer.MAX_VALUE;
    private int skip = 0;
    
    public MongoDbQueryByExample( MongoDbSearchProvider provider, Session session) {
        this.provider = provider;
        this.session = session;
    }
    
    @Override
    public QueryByExample document(StoreDocument document) {
        this.document = document;
        
        return this;
    }

    @Override
    public QueryByExample childOf(String path) {
        this.childOf = path;
        
        return this;
    }
    
    @Override
    public QueryByExample aggregateTo( String type) {
        this.aggregateTo = type;
        
        return this;
    }
    
    public StoreDocument getDocument() {
        return this.document;
    }

    @Override
    public QueryByExample sortBy(String propertyName) {
        this.sortProperty = propertyName;
        
        return this;
    }

    @Override
    public QueryByExample skip( int skip) {
        this.skip = skip;
        
        return this;
    }

    @Override
    public QueryByExample limit(int limit) {
        this.limit = limit;
        
        return this;
    }

    @Override
    public Query types(String... types) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Node> execute() {
        List<NodeId> ids = provider.executeQuery( session.getWorkspace().getName(), this.document);
        
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
        Optional<Node> node = this.session.findNodeByIdentifier( id.getUUID());
        
        return node.filter( n -> n.getRevision() == id.getRevision());
    }
}

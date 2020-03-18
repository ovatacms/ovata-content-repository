/*
 * $Id: MySqlFulltextQuery.java 697 2017-03-06 18:12:59Z dani $
 * Created on 29.01.2018, 16:00:00
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

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.util.PropertyComparatorIfAvailable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author dani
 */
public class MySqlSimpleFulltextQuery extends MySqlBaseQuery implements FulltextQuery {
    
    private String term;
    private String childOf;
    private String aggregateTo;
    private String sortBy;
    private int skip = 0;
    private int limit = Integer.MAX_VALUE;
    private final List<String> types = new ArrayList<>();
    
    public MySqlSimpleFulltextQuery( Session session, MySqlSearchProvider provider) {
        super( session, provider);
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
    
    @Override
    public FulltextQuery aggregateTo( String type) {
        this.aggregateTo = type;
        
        return this;
    }

    @Override
    public Query sortBy(String propertyName) {
        this.sortBy = propertyName;
        
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
        this.types.clear();
        this.types.addAll( Arrays.asList( types));
        
        return this;
    }

    @Override
    public List<Node> execute() {
        try( Connection connection = this.provider.getDataSource().getConnection()) {
            try( PreparedStatement stmt = connection.prepareStatement( "SELECT NODE_ID, REVISION FROM " + getTableName() + " WHERE PAYLOAD LIKE ?")) {
                stmt.setString( 1, "%" + term + "%");
                
                Stream<Node> stream = this.fetchNodeIds( stmt).stream().map( this::findNode).filter( Optional::isPresent).map( Optional::get);

                if( this.aggregateTo != null) {
                    stream = stream.map( n -> aggregate( n, aggregateTo)).filter( Optional::isPresent).map( Optional::get);
                }
                
                if( !types.isEmpty()) {
                    stream = stream.filter( n -> types.contains( n.getType()));
                }

                if( this.childOf != null) {
                    stream.filter( n -> n.getPath().startsWith( this.childOf));
                }
                
                if( this.sortBy != null) {
                    stream = stream.sorted( new PropertyComparatorIfAvailable( this.sortBy));
                }

                return stream.skip( this.skip).limit( this.limit).collect( Collectors.toList());
            }
        }
        catch( SQLException e) {
            throw new RepositoryException( "Could not execute fulltext query.", e);
        }
    }
    
    private String getTableName() {
        return this.provider.getDatabaseName() + "_" + this.session.getWorkspace().getName();
    }
}

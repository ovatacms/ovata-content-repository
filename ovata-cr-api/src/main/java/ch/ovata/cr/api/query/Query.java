/*
 * $Id: Query.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.api.query;

import ch.ovata.cr.api.Node;
import java.util.List;

/**
 *
 * @author dani
 */
public interface Query {
    /**
     * Executes the query
     * @return list of nodes
     */
    List<Node> execute();
    
    /**
     * The path to the parent of the node
     * @param path parent path
     * @return the query itself
     */
    Query childOf( String path);
    
    /**
     * Aggregate the result to a specific node type
     * @param type the node type to aggregate to
     * @return the query itself
     */
    Query aggregateTo( String type);
    
    /**
     * The node types to select
     * @param types the node types to filter
     * @return the query itself for chaining
     */
    Query types( String... types);
    
    /**
     * Order the result by the given property
     * @param propertyName name of the property to sort by
     * @return the query itself
     */
    Query sortBy( String propertyName);

    /**
     * Skip the first n entries of the result set (use for paging)
     * @param skip number of entries to skip
     * @return the query itself
     */
    Query skip( int skip);
    
    /**
     * Limit the search result to the specified count
     * @param limit the limit
     * @return the query itself
     */
    Query limit( int limit);
}

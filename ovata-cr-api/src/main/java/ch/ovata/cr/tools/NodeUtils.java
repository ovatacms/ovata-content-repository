/*
 * $Id: NodeUtils.java 2616 2019-07-26 11:29:29Z dani $
 * Created on 15.12.2016, 12:00:00
 * 
 * Copyright (c) 2016 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.tools;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.ValueFactory;
import ch.ovata.cr.api.util.PropertyComparator;
import java.util.Optional;

/**
 *
 * @author dani
 */
public final class NodeUtils {
    
    private static final String PROP_ORDER = "order";
    
    private NodeUtils() {
    }
    
    public static Node addNextNode( Node parent, String name, String nodeType) {
        int i = 0;
        while( parent.hasNode( name + i)) {
            i++;
        }

        return parent.addNode( name + i, nodeType);
    }
    
    public static Node addNextNodeWithOrder( Node parent, String name, String nodeType) {
        ValueFactory vf = parent.getSession().getValueFactory();
        long order = getMaxOrder( parent);
        Node node = addNextNode( parent, name, nodeType);
        
        node.setProperty( PROP_ORDER, vf.of( order + 1));
        
        return node;
    }
    
    public static long getMaxOrder( Node parent) {
        Optional<Node> optional = parent.getNodes().stream().filter(n -> n.hasProperty( PROP_ORDER)).max( PropertyComparator.ByOrder);
        
        return optional.map( n -> n.getLong( PROP_ORDER)).orElse( 0l);
    }
}

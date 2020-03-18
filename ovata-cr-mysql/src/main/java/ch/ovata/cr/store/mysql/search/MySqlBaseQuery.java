/*
 * $Id: MySqlBaseQuery.java 697 2017-03-06 18:12:59Z dani $
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
import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.api.Session;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author dani
 */
public class MySqlBaseQuery {
    
    protected final Session session;
    protected final MySqlSearchProvider provider;
    
    protected MySqlBaseQuery( Session session, MySqlSearchProvider provider) {
        this.session = session;
        this.provider = provider;
    }
    
    protected List<NodeId> fetchNodeIds( PreparedStatement stmt) throws SQLException {
        List<NodeId> ids = new ArrayList<>();

        try( ResultSet r = stmt.executeQuery()) {
            while( r.next()) {
                ids.add( new NodeId( r.getString( "NODE_ID"), r.getLong( "REVISION")));
            }
        }

        return ids;
    }

    protected Optional<Node> aggregate( Node node, String type) {
        Node n = node;
        
        while( (n != null) && !n.isOfType( type)) {
            n = n.getParent();
        }
        
        return Optional.ofNullable( n);
    }
    
    protected Optional<Node> findNode( NodeId id) {
        return this.session.findNodeByIdentifier( id.getUUID()).filter( n -> n.getRevision() == id.getRevision());
    }
}

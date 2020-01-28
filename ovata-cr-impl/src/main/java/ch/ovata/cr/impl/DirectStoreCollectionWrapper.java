/*
 * $Id: DirectStoreCollectionWrapper.java 2858 2019-12-02 13:24:29Z dani $
 * Created on 21.04.2017, 18:00:00
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeId;
import ch.ovata.cr.spi.store.StoreCollection;
import ch.ovata.cr.spi.store.StoreDocument;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author dani
 */
public class DirectStoreCollectionWrapper implements StoreCollectionWrapper {
    
    private final StoreCollection collection;
    
    public DirectStoreCollectionWrapper( StoreCollection collection) {
        this.collection = collection;
    }
    
    @Override
    public StoreDocument getById( String id, long revision) {
        StoreDocument doc = this.collection.findById( id, revision);

        return ( (doc == null) || doc.containsKey( Node.REMOVED_FIELD)) ? null : doc;
    }
    
    @Override
    public StoreDocument getChildByName( String parentUUID, String name, long revision) {
        StoreDocument document = this.collection.findByParentIdAndName( parentUUID, name, revision);

        return (document == null) || document.containsKey( Node.REMOVED_FIELD) ? null : document;
    }
    
    @Override
    public Collection<StoreDocument> getChildren( String parentUUID, long revision) {
        Iterator<StoreDocument> doci = this.collection.findByParentId( parentUUID, revision).iterator();
        List<StoreDocument> result = new LinkedList<>();
        
        if( doci.hasNext()) {
            StoreDocument document = doci.next();
            NodeId nodeId = new NodeId( document.getDocument( Node.NODE_ID_FIELD));
            
            while( document != null) {
                if( !document.containsKey( Node.REMOVED_FIELD)) {
                    result.add( document);
                }
                
                document = null;
                
                while( doci.hasNext()) {
                    StoreDocument document2 = doci.next();
                    NodeId nodeId2 = new NodeId( document2.getDocument( Node.NODE_ID_FIELD));
                    
                    if( !nodeId2.getUUID().equals( nodeId.getUUID())) {
                        nodeId = nodeId2;
                        document = document2;
                        break;
                    }
                }
            }
        }
        
        return result;
    }
}

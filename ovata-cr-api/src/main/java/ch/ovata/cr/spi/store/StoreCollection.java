/*
 * $Id: StoreCollection.java 679 2017-02-25 10:28:00Z dani $
 * Created on 12.01.2017, 19:00:00
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
package ch.ovata.cr.spi.store;

import java.util.Collection;

/**
 *
 * @author dani
 */
public interface StoreCollection extends StoreDocumentFactory {
    
    /**
     * Insert single document into this collection
     * @param document the document to insert
     */
    void init( StoreDocument document);
    
    /**
     * Insert a set of documents in bulk into this collection
     * @param trx the currently active transaction
     * @param documents the documents to insert
     */
    void insertMany( Transaction trx, Collection<StoreDocument> documents);
    
    /**
     * Find single document representing a node by primary key and revision
     * @param nodeId the primary key of the node
     * @param revision the revision to pick
     * @return the node document
     */
    StoreDocument findById( String nodeId, long revision);
    
    /**
     * Find a child node's document by name and revision
     * @param parentId the primary key of the parent node
     * @param name the child node's name
     * @param revision the revision to pick
     * @return the node document
     */
    StoreDocument findByParentIdAndName( String parentId, String name, long revision);
    
    /**
     * Find all child nodes by parent id and revision
     * @param parentId the primary key of the parent node
     * @param revision the revision to pick
     * @return the collection of node documents
     */
    StoreDocumentIterable findByParentId( String parentId, long revision);
}

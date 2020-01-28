/*
 * $Id: StoreCollectionWrapper.java 821 2017-04-21 21:49:06Z dani $
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

import ch.ovata.cr.spi.store.StoreDocument;
import java.util.Collection;

/**
 *
 * @author dani
 */
public interface StoreCollectionWrapper {

    /**
     * Retrieves the lates node document for the given id and revision
     * @param id the id of the node
     * @param revision the revision to lookup
     * @return the document if available or null
     */
    StoreDocument getById( String id, long revision);

    /**
     * Retrieves the latest node document for the given parent node, name and revision
     * @param parentUUID the id of the parent node
     * @param name the name of the child node
     * @param revision the revision to lookup
     * @return the document if available or null
     */
    StoreDocument getChildByName( String parentUUID, String name, long revision);

    /**
     * Retrieves all child documents for a given parent node
     * @param parentUUID the id of the parent node
     * @param revision the revision to lookup
     * @return the list of child documents that are not marked as deleted
     */
    Collection<StoreDocument> getChildren( String parentUUID, long revision);
}

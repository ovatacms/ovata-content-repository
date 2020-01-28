/*
 * $Id: StoreDocumentList.java 679 2017-02-25 10:28:00Z dani $
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

import java.io.Serializable;
import java.util.stream.Stream;

/**
 *
 * @author dani
 */
public interface StoreDocumentList extends Iterable<StoreDocument>, Serializable {

    /**
     * Add a single document to this list
     * @param document the document to add
     */
    void add( StoreDocument document);
    
    /**
     * Retrieve a stream over the documents in this list
     * @return the stream
     */
    Stream<StoreDocument> stream();
    
    /**
     * Retrieve the size of the list
     * @return number of documents in this list
     */
    int size();
}

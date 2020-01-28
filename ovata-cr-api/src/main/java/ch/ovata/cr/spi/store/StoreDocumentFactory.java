/*
 * $Id: StoreDocumentFactory.java 679 2017-02-25 10:28:00Z dani $
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

/**
 *
 * @author dani
 */
public interface StoreDocumentFactory {
    /**
     * Creates a new document.
     * @return the new document
     */
    StoreDocument newDocument();
    
    /**
     * Creates a new list of documents.
     * @return the new list
     */
    StoreDocumentList newList();
}

/*
 * $Id: MySqlDocumentIterable.java 2641 2019-08-19 12:11:25Z dani $
 * Created on 19.08.2019, 18:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.mysql;

import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentIterable;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author dani
 */
public class MySqlDocumentIterable implements StoreDocumentIterable {

    private final Iterable<StoreDocument> iterable;
    
    public MySqlDocumentIterable( List<StoreDocument> i) {
        this.iterable = i;
    }
    
    @Override
    public Iterator<StoreDocument> iterator() {
        return this.iterable.iterator();
    }
}

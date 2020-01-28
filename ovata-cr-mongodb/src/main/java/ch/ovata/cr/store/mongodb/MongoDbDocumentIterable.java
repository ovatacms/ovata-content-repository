/*
 * $Id: MongoDbDocumentIterable.java 2787 2019-11-11 09:00:35Z dani $
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
package ch.ovata.cr.store.mongodb;

import ch.ovata.cr.bson.MongoDbDocument;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentIterable;
import com.mongodb.client.FindIterable;
import java.util.Iterator;
import org.bson.Document;

/**
 *
 * @author dani
 */
public class MongoDbDocumentIterable implements StoreDocumentIterable {

    private final FindIterable<Document> iterable;
    
    public MongoDbDocumentIterable( FindIterable<Document> i) {
        this.iterable = i;
    }
    
    @Override
    public Iterator<StoreDocument> iterator() {
        return new MongoDbDocumentIterator( this.iterable.iterator());
    }

    private static class MongoDbDocumentIterator implements Iterator<StoreDocument> {

        private final Iterator<Document> i;
        
        private MongoDbDocumentIterator( Iterator<Document> i) {
            this.i = i;
        }
        
        @Override
        public boolean hasNext() {
            return this.i.hasNext();
        }

        @Override
        public MongoDbDocument next() {
            return new MongoDbDocument( i.next());
        }
    }
}

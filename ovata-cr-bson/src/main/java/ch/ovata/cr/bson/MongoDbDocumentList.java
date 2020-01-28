/*
 * $Id: MongoDbDocumentList.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.bson;

import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.bson.Document;

/**
 *
 * @author dani
 */
public class MongoDbDocumentList implements StoreDocumentList {

    private final List<Document> list;
    
    public MongoDbDocumentList() {
        this.list = new ArrayList<>();
    }
    
    public MongoDbDocumentList( List<Document> list) {
        this.list = list;
    }

    public List<Document> unwrap() {
        return this.list;
    }
    
    @Override
    public int size() {
        return this.list.size();
    }
    
    @Override
    public Iterator<StoreDocument> iterator() {
        return new IteratorImpl();
    }
    
    @Override
    public Stream<StoreDocument> stream() {
        return list.stream().map( MongoDbDocument::new);
    }
    
    @Override
    public void add( StoreDocument document) {
        this.list.add( ((MongoDbDocument)document).unwrap());
    }
    
    private class IteratorImpl implements Iterator<StoreDocument> {

        private final Iterator<Document> i;
        
        public IteratorImpl() {
            i = MongoDbDocumentList.this.list.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return i.hasNext();
        }

        @Override
        public StoreDocument next() {
            Document document = i.next();
            
            return new MongoDbDocument( document);
        }
    }
}

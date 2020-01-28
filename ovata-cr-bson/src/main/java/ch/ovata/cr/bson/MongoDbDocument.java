/*
 * $Id: MongoDbDocument.java 679 2017-02-25 10:28:00Z dani $
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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;

/**
 *
 * @author dani
 */
public class MongoDbDocument implements StoreDocument {
    
    private final Document document;
    
    public MongoDbDocument() {
        this.document = new Document();
    }
    
    public MongoDbDocument( Document document) {
        this.document = document;
    }
    
    public Document toDocument() {
        return document;
    }

    public Document unwrap() {
        return this.document;
    }
    
    @Override
    public StoreDocument append(String name, String value) {
        this.document.put( name, value);
        
        return this;
    }

    @Override
    public StoreDocument append(String name, Date value) {
        this.document.put( name, value);
        
        return this;
    }

    @Override
    public StoreDocument append(String name, Boolean value) {
        this.document.put( name, value);
        
        return this;
    }

    @Override
    public StoreDocument append(String name, Double value) {
        this.document.put( name, value);
        
        return this;
    }

    @Override
    public StoreDocument append(String name, Long value) {
        this.document.put( name, value);
        
        return this;
    }

    @Override
    public StoreDocument append(String name, BigDecimal value) {
        document.put( name, value);
        
        return this;
    }

    @Override
    public StoreDocument append(String name, StoreDocument value) {
        this.document.put(name, ((MongoDbDocument)value).unwrap());
        
        return this;
    }
    
    @Override
    public StoreDocument append( String name, StoreDocumentList list) {
        this.document.put( name, ((MongoDbDocumentList)list).unwrap());
        
        return this;
    }

    @Override
    public StoreDocument append(String name, List<?> values) {
        this.document.put( name, values);
        
        return this;
    }

    @Override
    public String getString(String name) {
        return (String)this.document.get( name);
    }

    @Override
    public Date getDate(String name) {
        return this.document.get( name, Date.class);
    }

    @Override
    public Long getLong(String name) {
        return this.document.get( name, Long.class);
    }

    @Override
    public BigDecimal getDecimal(String name) {
        return this.document.get( name, BigDecimal.class);
    }

    @Override
    public StoreDocument getDocument(String name) {
        Document map = this.document.get( name, Document.class);
        
        return (map != null) ? new MongoDbDocument( map) : null;
    }
    
    @Override
    public StoreDocumentList getDocumentList( String name) {
        List<Document> list = (List<Document>)this.document.get( name);
        
        return (list != null) ? new MongoDbDocumentList( list) : null;
    }

    @Override
    public List<?> getList(String name) {
        return (List<?>)this.document.get( name);
    }

    @Override
    public Boolean getBoolean(String name) {
        return (Boolean)this.document.get( name);
    }

    @Override
    public Double getDouble(String name) {
        return (Double)this.document.get( name);
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        Map<String, Object> map = new HashMap<>();
        
        for( Map.Entry<String, Object> e : this.document.entrySet()) {
            if( e.getValue() instanceof Document) {
                map.put( e.getKey(), new MongoDbDocument( (Document)e.getValue()));
            }
            else if( e.getValue() instanceof List) {
                List list = (List)e.getValue();
                
                if( !list.isEmpty() && list.get( 0) instanceof Document) {
                    map.put( e.getKey(), new MongoDbDocumentList( list));
                }
                else {
                    map.put( e.getKey(), list);
                }
            }
            else {
                map.put( e.getKey(), e.getValue());
            }
        }
        
        return map.entrySet();
    }

    @Override
    public void remove(String name) {
        this.document.remove( name);
    }

    @Override
    public boolean containsKey(String name) {
        return this.document.containsKey( name);
    }
    
    @Override
    public StoreDocument cloneDocument() {
        return new MongoDbDocument( new Document( this.document));
    }
}

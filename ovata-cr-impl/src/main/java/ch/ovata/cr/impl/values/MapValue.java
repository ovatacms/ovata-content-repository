/*
 * $Id: MapValue.java 679 2017-02-25 10:28:00Z dani $
 * Created on 22.11.2016, 12:00:00
 * 
 * Copyright (c) 2016 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.impl.values;

import ch.ovata.cr.api.Value;
import ch.ovata.cr.impl.NodeImpl;
import ch.ovata.cr.spi.store.StoreDocument;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dani
 */
public class MapValue extends AbstractValue {
            
    private final StoreDocument document;
    
    public MapValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public MapValue( ValueFactoryImpl vf, Map<String, Value> map) {
        super( vf);
        
        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.MAP.name());
        
        StoreDocument mapDocument = vf.newDocument();
        
        for( Map.Entry<String, Value> entry : map.entrySet()) {
            mapDocument.append( entry.getKey(), ((AbstractValue)entry.getValue()).toDocument());
        }
        
        this.document.append( NodeImpl.VALUE_FIELD, mapDocument);
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Value.Type getType() {
        return Value.Type.MAP;
    }

    @Override
    public Map<String, Value> getMap() {
        Map<String, Value> map = new HashMap<>();
        
        for( Map.Entry entry : this.document.getDocument(NodeImpl.VALUE_FIELD).entrySet()) {
            map.put((String)entry.getKey(), this.getValueFactory().of((StoreDocument)entry.getValue()));
        }
        
        return map;
    }

    @Override
    public int compareTo(Value o) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals( Object o) {
        return super.equals( o);
    }
}

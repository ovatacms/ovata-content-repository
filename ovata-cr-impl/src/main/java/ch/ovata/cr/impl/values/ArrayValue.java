/*
 * $Id: ArrayValue.java 679 2017-02-25 10:28:00Z dani $
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
import ch.ovata.cr.spi.store.StoreDocumentList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author dani
 */
public class ArrayValue extends AbstractValue {
            
    private final StoreDocument document;
    
    public ArrayValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public ArrayValue( ValueFactoryImpl vf, Collection<Value> values) {
        super( vf);
        
        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.ARRAY.name());
        
        StoreDocumentList listOfDocuments = vf.newList();
        
        for( Value value : values) {
            listOfDocuments.add( ((AbstractValue)value).toDocument());
        }
        
        this.document.append( NodeImpl.VALUE_FIELD, listOfDocuments);
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Value.Type getType() {
        return Value.Type.ARRAY;
    }

    @Override
    public Value[] getArray() {
        StoreDocumentList documents = this.document.getDocumentList( NodeImpl.VALUE_FIELD);
        List<Value> values = new ArrayList<>( documents.size());
        
        for( StoreDocument d : documents) {
            values.add( this.getValueFactory().of( d));
        }
        
        return values.toArray( new Value[ values.size()]);
    }

    @Override
    public int compareTo(Value o) {
        throw new UnsupportedOperationException("Comapre not supported on this type.");
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

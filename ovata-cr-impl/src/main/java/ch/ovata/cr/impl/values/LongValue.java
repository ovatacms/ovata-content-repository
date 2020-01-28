/*
 * $Id: LongValue.java 679 2017-02-25 10:28:00Z dani $
 * Created on 14.11.2016, 12:00:00
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
import java.util.Objects;

/**
 *
 * @author dani
 */
public class LongValue extends AbstractValue {

    private final StoreDocument document;
    
    public LongValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public LongValue( ValueFactoryImpl vf, long value) {
        super( vf);

        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.LONG.name()).append( NodeImpl.VALUE_FIELD, value);
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Type getType() {
        return Type.LONG;
    }

    @Override
    public Long getLong() {
        return this.document.getLong( NodeImpl.VALUE_FIELD);
    }

    @Override
    public int compareTo(Value o) {
        if( o.getType() != Value.Type.LONG) {
            throw new ClassCastException( "Cannot compare <" + this.getType() + "> to <" + o.getType() + ">.");
        }
        
        return this.getLong().compareTo( o.getLong());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( this.getLong());
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof LongValue) {
            LongValue v = (LongValue)o;
            
            return this.getLong().equals( v.getLong());
        }
        
        return false;
    }
}

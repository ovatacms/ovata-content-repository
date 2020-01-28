/*
 * $Id: DoubleValue.java 679 2017-02-25 10:28:00Z dani $
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
public class DoubleValue extends AbstractValue {

    private final StoreDocument document;
    
    public DoubleValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public DoubleValue( ValueFactoryImpl vf, double value) {
        super( vf);

        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.DOUBLE.name()).append( NodeImpl.VALUE_FIELD, value);
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Type getType() {
        return Type.DOUBLE;
    }

    @Override
    public Double getDouble() {
        return this.document.getDouble(NodeImpl.VALUE_FIELD);
    }

    @Override
    public int compareTo(Value o) {
        if( o.getType() != Value.Type.DOUBLE) {
            throw new ClassCastException( "Cannot compare <" + this.getType() + "> to <" + o.getType() + ">.");
        }
        
        return this.getDouble().compareTo( o.getDouble());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( this.getDouble());
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof DoubleValue) {
            DoubleValue v = (DoubleValue)o;
            
            return this.getDouble().equals( v.getDouble());
        }
        
        return false;
    }
}

/*
 * $Id: BooleanValue.java 679 2017-02-25 10:28:00Z dani $
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
public class BooleanValue extends AbstractValue {

    private final StoreDocument document;
    
    public BooleanValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public BooleanValue( ValueFactoryImpl vf, boolean value) {
        super( vf);

        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.BOOLEAN.name()).append( NodeImpl.VALUE_FIELD, value);
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public Boolean getBoolean() {
        return this.document.getBoolean( NodeImpl.VALUE_FIELD);
    }

    @Override
    public int compareTo( Value o) {
        if( o.getType() != Value.Type.BOOLEAN) {
            throw new ClassCastException( "Cannot compare <" + this.getType() + "> to <" + o.getType() + ">.");
        }
        
        return this.getBoolean().compareTo( o.getBoolean());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( this.getBoolean());
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof BooleanValue) {
            BooleanValue v = (BooleanValue)o;
            
            return this.getBoolean().equals( v.getBoolean());
        }
        
        return false;
    }
}

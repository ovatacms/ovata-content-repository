/*
 * $Id: DecimalValue.java 679 2017-02-25 10:28:00Z dani $
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
import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author dani
 */
public class DecimalValue extends AbstractValue {

    private final StoreDocument document;
    
    public DecimalValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public DecimalValue( ValueFactoryImpl vf, BigDecimal value) {
        super( vf);

        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.DECIMAL.name()).append( NodeImpl.VALUE_FIELD, value.toPlainString());
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Type getType() {
        return Type.DECIMAL;
    }

    @Override
    public BigDecimal getDecimal() {
        return new BigDecimal( this.document.getString( NodeImpl.VALUE_FIELD));
    }

    @Override
    public int compareTo(Value o) {
        if( o.getType() != Value.Type.DECIMAL) {
            throw new ClassCastException( "Cannot compare <" + this.getType() + "> to <" + o.getType() + ">.");
        }
        
        return this.getDecimal().compareTo( o.getDecimal());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( this.getDecimal());
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof DecimalValue) {
            DecimalValue v = (DecimalValue)o;
            
            return this.getDecimal().compareTo( v.getDecimal()) == 0;
        }
        
        return false;
    }
}

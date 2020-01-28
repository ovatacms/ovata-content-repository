/*
 * $Id: LocalDateValue.java 679 2017-02-25 10:28:00Z dani $
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author dani
 */
public class LocalDateValue extends AbstractValue {

    private final StoreDocument document;
    
    public LocalDateValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public LocalDateValue( ValueFactoryImpl vf, LocalDate value) {
        super( vf);

        Date date = Date.from( value.atStartOfDay().toInstant( ZoneOffset.UTC));
        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.LOCAL_DATE.name()).append( NodeImpl.VALUE_FIELD, date);
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Type getType() {
        return Type.LOCAL_DATE;
    }

    @Override
    public LocalDate getDate() {
        Date date = this.document.getDate( NodeImpl.VALUE_FIELD);
        
        return date.toInstant().atZone( ZoneOffset.UTC).toLocalDate();
    }

    @Override
    public int compareTo(Value o) {
        if( o.getType() != Value.Type.LOCAL_DATE) {
            throw new ClassCastException( "Cannot compare <" + this.getType() + "> to <" + o.getType() + ">.");
        }
        
        return this.getDate().compareTo( o.getDate());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( this.getDate());
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof LocalDateValue) {
            LocalDateValue v = (LocalDateValue)o;
            
            return this.getDate().equals( v.getDate());
        }
        
        return false;
    }
}

/*
 * $Id: ZonedDateTimeValue.java 679 2017-02-25 10:28:00Z dani $
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author dani
 */
public class ZonedDateTimeValue extends AbstractValue {

    private static final String ZONEID_FIELD = "zoneid";
    
    private final StoreDocument document;
    
    public ZonedDateTimeValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public ZonedDateTimeValue( ValueFactoryImpl vf, ZonedDateTime value) {
        super( vf);

        Date date = Date.from( value.toInstant());
        ZoneId zone = value.getZone();
        
        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.ZONED_DATETIME.name()).append( NodeImpl.VALUE_FIELD, date).append( ZONEID_FIELD, zone.getId());
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Type getType() {
        return Type.ZONED_DATETIME;
    }

    @Override
    public ZonedDateTime getTime() {
        Date date = this.document.getDate( NodeImpl.VALUE_FIELD);
        String zoneid = this.document.getString( ZONEID_FIELD);
        
        return ZonedDateTime.ofInstant( date.toInstant(), ZoneId.of( zoneid));
    }

    @Override
    public int compareTo(Value o) {
        if( o.getType() != Value.Type.ZONED_DATETIME) {
            throw new ClassCastException( "Cannot compare <" + this.getType() + "> to <" + o.getType() + ">.");
        }
        
        return this.getTime().compareTo( o.getTime());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( this.getTime());
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof ZonedDateTimeValue) {
            ZonedDateTimeValue v = (ZonedDateTimeValue)o;
            
            return this.getTime().equals( v.getTime());
        }
        
        return false;
    }
}

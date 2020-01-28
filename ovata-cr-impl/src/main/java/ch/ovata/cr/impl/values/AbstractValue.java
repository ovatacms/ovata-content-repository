/*
 * $Id: AbstractValue.java 679 2017-02-25 10:28:00Z dani $
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

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.Reference;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.spi.store.StoreDocument;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import javax.json.JsonObject;

/**
 *
 * @author dani
 */
public abstract class AbstractValue implements Value {
    
    private final ValueFactoryImpl vf;
    
    protected AbstractValue( ValueFactoryImpl vf) {
        this.vf = vf;
    }
    
    protected ValueFactoryImpl getValueFactory() {
        return this.vf;
    }
    
    public abstract StoreDocument toDocument();


    @Override
    public JsonObject getGeoJSON() {
        throw createWrongTypeException();
    }

    @Override
    public Binary getBinary() {
        throw createWrongTypeException();
    }

    @Override
    public Boolean getBoolean() {
        throw createWrongTypeException();
    }

    @Override
    public LocalDate getDate() {
        throw createWrongTypeException();
    }

    @Override
    public ZonedDateTime getTime() {
        throw createWrongTypeException();
    }

    @Override
    public BigDecimal getDecimal() {
        throw createWrongTypeException();
    }

    @Override
    public Double getDouble() {
        throw createWrongTypeException();
    }

    @Override
    public Long getLong() {
        throw createWrongTypeException();
    }

    @Override
    public String getString() {
        throw createWrongTypeException();
    }

    @Override
    public Reference getReference() {
        throw createWrongTypeException();
    }

    @Override
    public Value[] getArray() {
        throw createWrongTypeException();
    }

    @Override
    public Map<String, Value> getMap() {
        throw createWrongTypeException();
    }
    
    private IllegalStateException createWrongTypeException() {
        return new IllegalStateException( "Actual type: <" + this.getType() + ">.");
    }

    @Override
    public boolean isGeoJSON() {
        return this.getType() == Value.Type.GEOJSON;
    }
    
    @Override
    public boolean isBinary() {
        return this.getType() == Value.Type.BINARY;
    }
    
    @Override
    public boolean isBoolean() {
        return this.getType() == Value.Type.BOOLEAN;
    }
    
    @Override
    public boolean  isDate() {
        return this.getType() == Value.Type.LOCAL_DATE;
    }
    
    @Override
    public boolean isTime() {
        return this.getType() == Value.Type.ZONED_DATETIME;
    }
    
    @Override
    public boolean isDecimal() {
        return this.getType() == Value.Type.DECIMAL;
    }
    
    @Override
    public boolean isDouble() {
        return this.getType() == Value.Type.DOUBLE;
    }
    
    @Override
    public boolean isLong() {
        return this.getType() == Value.Type.LONG;
    }
    
    @Override
    public boolean isString() {
        return this.getType() == Value.Type.STRING;
    }
    
    @Override
    public boolean isReference() {
        return this.getType() == Value.Type.REFERENCE;
    }
    
    @Override
    public boolean isArray() {
        return this.getType() == Value.Type.ARRAY;
    }
    
    @Override
    public boolean isMap() {
        return this.getType() == Value.Type.MAP;
    }
}

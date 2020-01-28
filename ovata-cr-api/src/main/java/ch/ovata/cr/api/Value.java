/*
 * $Id: Value.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import javax.json.JsonObject;

/**
 * Represents a value saved as a property on a node.
 * @author dani
 */
public interface Value extends Comparable<Value>, Serializable {
    
    enum Type { BINARY, BOOLEAN, LOCAL_DATE, ZONED_DATETIME, DECIMAL, DOUBLE, LONG, STRING, GEOJSON, REFERENCE, ARRAY, MAP }
    
    /**
     * Value representing a GeoJSON value according to rfc7946
     * @return GeoJSON value
     */
    JsonObject getGeoJSON();
    
    /**
     * Binary representation of this value
     * @return binary value
     */
    Binary getBinary();
    
    /**
     * Boolean representation of this value
     * @return boolean value
     */
    Boolean getBoolean();
    
    /**
     * LocalDate representation of this value
     * @return LocalDate value
     */
    LocalDate getDate();
    
    /**
     * ZonedDateTime representation of this value
     * @return ZonedDateTime value
     */
    ZonedDateTime getTime();
    
    /**
     * BigDecimal representation of this value
     * @return BigDecimal value
     */
    BigDecimal getDecimal();
    
    /**
     * Double represenation of this value
     * @return Double value
     */
    Double getDouble();
    
    /**
     * Long representation of this value
     * @return Long value
     */
    Long getLong();
    
    /**
     * String representation of this value
     * @return String value
     */
    String getString();
    
    /**
     * Retrieves a reference to another node
     * @return the node reference
     */
    Reference getReference();
    
    /**
     * Retrieves an array of values
     * @return the array of values
     */
    Value[] getArray();
    
    /**
     * Retrieves a map of values
     * @return the map of values
     */
    Map<String, Value> getMap();
    
    /**
     * Type of the value
     * @return the type
     */
    Type getType();
    
    /**
     * Checks if the value is a GeoJSON value according to rfc7946
     * @return GeoJSON value
     */
    boolean isGeoJSON();
    
    /**
     * Checks if the value is a binary
     * @return binary value
     */
    boolean isBinary();
    
    /**
     * Checks if the value is a boolean
     * @return boolean value
     */
    boolean isBoolean();
    
    /**
     * Checks if the value is a date
     * @return LocalDate value
     */
    boolean  isDate();
    
    /**
     * Checks if the value is a date/time value
     * @return ZonedDateTime value
     */
    boolean isTime();
    
    /**
     * Checks if the value is a decimal
     * @return BigDecimal value
     */
    boolean isDecimal();
    
    /**
     * Checks if the value is a double
     * @return Double value
     */
    boolean isDouble();
    
    /**
     * Checks if the value is a long
     * @return Long value
     */
    boolean isLong();
    
    /**
     * Checks if the value is a string
     * @return String value
     */
    boolean isString();
    
    /**
     * Checks if the value is a reference
     * @return the node reference
     */
    boolean isReference();
    
    /**
     * Checks if the value is an array
     * @return the array of values
     */
    boolean isArray();
    
    /**
     * Checks if the value is a map
     * @return the map of values
     */
    boolean isMap();
}

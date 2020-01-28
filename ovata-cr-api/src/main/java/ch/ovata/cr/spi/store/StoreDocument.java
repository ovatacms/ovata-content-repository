/*
 * $Id: StoreDocument.java 679 2017-02-25 10:28:00Z dani $
 * Created on 12.01.2017, 19:00:00
 * 
 * Copyright (c) 2017 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.spi.store;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a document that is stored in the backend store.
 * @author dani
 */
public interface StoreDocument extends Serializable, Cloneable {
    StoreDocument append( String name, String value);
    StoreDocument append( String name, Boolean value);
    StoreDocument append( String name, Double value);
    StoreDocument append( String name, Date value);
    StoreDocument append( String name, Long value);
    StoreDocument append( String name, BigDecimal value);
    StoreDocument append( String name, StoreDocument document);
    StoreDocument append( String name, List<?> values);
    StoreDocument append( String name, StoreDocumentList list);

    String getString( String name);
    Boolean getBoolean( String name);
    Double getDouble( String name);
    Date getDate( String name);
    Long getLong( String name);
    BigDecimal getDecimal( String name);
    StoreDocument getDocument( String name);
    List<?> getList( String name);
    StoreDocumentList getDocumentList( String name);
    
    Set<Map.Entry<String, Object>> entrySet();
    
    void remove( String name);
    
    boolean containsKey( String name);
    
    StoreDocument cloneDocument();
}

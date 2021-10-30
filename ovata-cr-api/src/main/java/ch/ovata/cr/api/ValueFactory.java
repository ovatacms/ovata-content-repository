/*
 * $Id: ValueFactory.java 679 2017-02-25 10:28:00Z dani $
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

import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author dani
 */
public interface ValueFactory {
    
    /**
     * Retrieves the session this value factory belongs to
     * @return session the current session
     */
    Session getSession();
    
    /**
     * Creates a new document for the underlying document store
     * @return the new document
     */
    StoreDocument newDocument();
    
    /**
     * Creates a new document list for the unerlying document store
     * @return new document list
     */
    StoreDocumentList newList();
    
    /**
     * Creates an new value from as String
     * @param value the string value
     * @return the new value object
     */
    Value of( String value);
    
    /**
     * Creates a new value from a LocalDate
     * @param value the LocalDate value
     * @return the new value object
     */
    Value of( LocalDate value);
    
    /**
     * Creates a new value from a ZonedDateTime
     * @param value the ZonedDateTime value
     * @return the new value object
     */
    Value of( ZonedDateTime value);
    
    /**
     * Creates a new value from a long
     * @param value the long value
     * @return the new value object
     */
    Value of( long value);
    
    /**
     * Creates a new value from a double
     * @param value the double value
     * @return the new value object
     */
    Value of( double value);
    
    /**
     * Creates a new value from a BigDecimal
     * @param value the BigDecimal value
     * @return the new value object
     */
    Value of( BigDecimal value);
    
    /**
     * Creates a new value from a boolean
     * @param value the boolean value
     * @return the new value object
     */
    Value of( boolean value);

    /**
     * Creates an array of values
     * @param array the array of values to assign
     * @return the new value object
     */    
    Value of( Collection<Value> array);

    /**
     * Creates a map of values
     * @param map the map of values to assign
     * @return the new value object
     */
    Value of( Map<String, Value> map);
    
    /**
     * Creates a new binary value
     * @param in InputStream providing the data for the binary
     * @param filename the filename
     * @param contentType the content type
     * @return the new value object
     */
    Value binary( InputStream in, String filename, String contentType);
    
    /**
     * Creates a new binary value
     * @param in InputStream providing the data for the binary
     * @param contentLength size of the binary
     * @param filename the filename
     * @param contentType the content type
     * @return the new value object
     */
    Value binary( InputStream in, long contentLength, String filename, String contentType);
    
    /**
     * Creates a new binary value
     * @param bytes byte array providing the data for the binary
     * @param filename the filename to assign
     * @param contentType the content type
     * @return the new value object
     */
    Value binary( byte[] bytes, String filename, String contentType);
    
    /**
     * Creates a new node reference
     * @param workspaceName the name of the workspace to reference
     * @param path the path to the node to reference
     * @return the new value object
     */
    Value referenceByPath( String workspaceName, String path);
    
    /**
     * Creates a new node reference
     * @param workspaceName the name of the workspace to reference
     * @param nodeId the id of the node to reference
     * @return the new value object
     */
    Value referenceById( String workspaceName, String nodeId);
}

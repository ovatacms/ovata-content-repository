/*
 * $Id: StoreConnection.java 715 2017-03-14 22:13:40Z dani $
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

/**
 *
 * @author dani
 */
public interface StoreConnection {
    /**
     * Retrieves a database from the current connection.
     * @param name name of the database
     * @return the database
     */
    StoreDatabase getDatabase( String name);
    
    /**
     * Checks if a database exists
     * @param name name of the database
     * @return true if database exists
     */
    boolean checkIfDatabaseExists( String name);
    
    /**
     * Creates a new database with given name
     * @param name name of the database to create
     * @return the database
     */
    StoreDatabase createDatabase( String name);
    
    /**
     * Returns information about the connection
     * @return connection information
     */
    String getInfo();
    
    /**
     * Unwrap an implementation specific object (e.g. underlying database connection)
     * @param <T> type of the object to unwrap
     * @param c class of the type to unwrap
     * @return  the unwrapped object or null if unavailable
     */
    <T> T unwrap( Class<T> c);
    
    /**
     * Shut down of this connection and frees all resources associated with this connection.
     */
    void shutdown();
}

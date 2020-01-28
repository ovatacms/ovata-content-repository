/*
 * $Id: RepositoryConnectionDataSource.java 679 2017-02-25 10:28:00Z dani $
 * Created on 03.01.2017, 12:00:00
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
package ch.ovata.cr.api;

/**
 *
 * @author isc-has
 */
public interface RepositoryConnectionDataSource {
    
    /**
     * Retrieves a connection to the repository
     * @return the repository connection
     */
    RepositoryConnection getConnection();
    
    /**
     * Closes the data source and frees associated resources
     */
    void close();
}

/*
 * $Id: ConcurrencyControlFactory.java 2858 2019-12-02 13:24:29Z dani $
 * Created on 30.03.2017, 12:00:00
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

import java.util.function.LongSupplier;

/**
 *
 * @author dani
 */
public interface ConcurrencyControlFactory {
    
    /**
     * Initializes the <code>ConcurrencyControl</code> for the given repository
     * @param repositoryName name of the repository
     * @param initialRevisionGenerator generator the initial revision if required
     * @return ConcurrencyControl the created concurrency control
     * @throws InterruptedException if something goes wrong
     */
    ConcurrencyControl setupRepository( String repositoryName, LongSupplier initialRevisionGenerator) throws InterruptedException;
    
    /**
     * Shutdown of the <code>ConcurrencyControlFactory</code>
     */
    void shutdown();

    /**
     * Retrieve an implementation specific object
     * @param <T> type of the object to unwrap
     * @param c class of the object to unwrap
     * @return the unwrapped object or null
     */
    <T> T unwrap( Class<T> c);
}

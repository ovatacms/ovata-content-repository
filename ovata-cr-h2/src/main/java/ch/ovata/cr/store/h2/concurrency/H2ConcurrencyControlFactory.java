/*
 * $Id: PostgresqlConcurrencyControlFactory.java 2918 2020-01-08 14:58:39Z dani $
 * Created on 07.11.2019, 17:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.h2.concurrency;

import ch.ovata.cr.spi.store.ConcurrencyControl;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import java.util.function.LongSupplier;
import javax.sql.DataSource;

/**
 *
 * @author dani
 */
public class H2ConcurrencyControlFactory implements ConcurrencyControlFactory {

    private final DataSource ds;
    
    public H2ConcurrencyControlFactory( DataSource ds) {
        this.ds = ds;
    }
    
    @Override
    public ConcurrencyControl setupRepository(String repositoryName, LongSupplier initialRevisionGenerator) throws InterruptedException {
        return new H2ConcurrencyControl( this.ds, repositoryName);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public <T> T unwrap(Class<T> c) {
        return null;
    }
}

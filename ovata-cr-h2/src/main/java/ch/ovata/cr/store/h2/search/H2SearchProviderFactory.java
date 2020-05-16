/*
 * $Id: PostgresqlSearchProviderFactory.java 2662 2019-09-11 12:27:35Z dani $
 * Created on 22.01.2018, 16:00:00
 * 
 * Copyright (c) 2018 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.h2.search;

import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.search.SearchProviderFactory;

/**
 *
 * @author dani
 */
public class H2SearchProviderFactory implements SearchProviderFactory {

    @Override
    public SearchProvider createSearchProvider(String databaseName) {
        return new H2SearchProvider( databaseName);
    }

    @Override
    public void shutdown() {
        // nothing to release
    }
}

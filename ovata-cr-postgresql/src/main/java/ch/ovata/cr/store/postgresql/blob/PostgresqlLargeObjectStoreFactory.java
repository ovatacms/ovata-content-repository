/*
 * $Id: PostgresqlBlobStoreFactory.java 2662 2019-09-11 12:27:35Z dani $
 * Created on 25.01.2018, 12:00:00
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
package ch.ovata.cr.store.postgresql.blob;

import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import javax.sql.DataSource;

/**
 *
 * @author dani
 */
public class PostgresqlLargeObjectStoreFactory implements BlobStoreFactory {

    private final DataSource ds;
    
    public PostgresqlLargeObjectStoreFactory( DataSource ds) {
        this.ds = ds;
    }
    
    @Override
    public BlobStore createBlobStore(String databaseName) {
        return new PostgresqlLargeObjectStore( this.ds, databaseName);
    }

    @Override
    public void shutdown() {
        // nothing to release
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if( DataSource.class.isAssignableFrom( type)) {
            return (T)this.ds;
        }
        
        return null;
    }
}

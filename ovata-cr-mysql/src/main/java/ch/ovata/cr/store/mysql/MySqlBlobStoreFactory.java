/*
 * $Id: MySqlBlobStoreFactory.java 690 2017-03-02 22:03:30Z dani $
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
package ch.ovata.cr.store.mysql;

import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import javax.sql.DataSource;

/**
 *
 * @author dani
 */
public class MySqlBlobStoreFactory implements BlobStoreFactory {

    private final DataSource ds;
    
    public MySqlBlobStoreFactory( DataSource ds) {
        this.ds = ds;
    }
    
    @Override
    public BlobStore createBlobStore(String databaseName) {
        return new MySqlBlobStore( this.ds, databaseName);
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

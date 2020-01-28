/*
 * $Id: FSBlobStoreFactory.java 1079 2017-10-17 19:46:11Z dani $
 * Created on 19.01.2018, 12:00:00
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
package ch.ovata.cr.store.fs;

import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class FSBlobStoreFactory implements BlobStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger( FSBlobStoreFactory.class);
    
    private final File directory;
    
    public FSBlobStoreFactory( File directory) {
        this.directory = directory;
    }
    
    @Override
    public BlobStore createBlobStore(String databaseName) {
        logger.debug( "Creating Blob store for database <{}>.", databaseName);
        
        File storeRoot = new File( directory, databaseName);
        
        if( !storeRoot.exists() && !storeRoot.mkdirs()) {
            throw new RepositoryException( "Could not create blob store root directory <" + storeRoot.getAbsolutePath() + ">.");
        }
        
        return new FSBlobStore( storeRoot);
    }

    @Override
    public void shutdown() {
        // nothing to release
    }

    @Override
    public <T> T unwrap( Class<T> type) {
        return null;
    }
}

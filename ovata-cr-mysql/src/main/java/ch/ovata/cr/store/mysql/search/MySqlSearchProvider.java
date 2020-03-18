/*
 * $Id: MySqlSearchProvider.java 2668 2019-09-13 12:24:59Z dani $
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
package ch.ovata.cr.store.mysql.search;

import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import ch.ovata.cr.store.mysql.MySqlConnection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class MySqlSearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(MySqlSearchProvider.class);
    
    protected final BlobStoreFactory blobStoreFactory;
    protected final MySqlConnection connection;
    protected final String databaseName;
    
    public MySqlSearchProvider( BlobStoreFactory blobStoreFactory, MySqlConnection connection, String databaseName) {
        this.blobStoreFactory = blobStoreFactory;
        this.connection = connection;
        this.databaseName = databaseName;
    }

    @Override
    public void clearIndex(String workspaceName) {
        logger.info( "No index to clear.");
    }

    @Override
    public void reindexWorkspace( Repository repository, String workspaceName) {
        logger.info( "Re-Indexation for this provider not supported.");
    }

    @Override
    public void indexNodes( DirtyState dirtyState, long revision) {
    }

    public BlobStore getBlobStore() {
        return this.blobStoreFactory.createBlobStore( this.databaseName);
    }
    
    public DataSource getDataSource() throws SQLException {
        return this.connection.unwrap( DataSource.class);
    }
    
    public String getDatabaseName() {
        return this.databaseName;
    }

    @Override
    public <T extends Query> T createQuery(Session session, Class<T> type) {
        if( QueryByExample.class == type) {
            return (T)new MySqlSimpleQueryByExample( this, session);
        }
        else if( FulltextQuery.class == type) {
            return (T)new MySqlSimpleFulltextQuery( session, this);
        }
        else {
            throw new RepositoryException( "Unknown query type <" + type.getName() + ">.");
        }
    }
}

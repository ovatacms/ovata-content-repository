/*
 * $Id: PostgresqlSearchProvider.java 2668 2019-09-13 12:24:59Z dani $
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
package ch.ovata.cr.store.postgresql.search;

import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.spi.base.DirtyState;
import ch.ovata.cr.spi.search.SearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class PostgresqlSearchProvider implements SearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(PostgresqlSearchProvider.class);
    
    private final String databaseName;
    
    public PostgresqlSearchProvider( String databaseName) {
        this.databaseName = databaseName;
    }
    
    @Override
    public void reindexWorkspace(Repository repository, String workspaceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearIndex(String workspaceName) {
        logger.info( "Dummy clearing index.");
    }

    @Override
    public void indexNodes( DirtyState dirtyState, long revision) {
        logger.info( "Dummy indexing nodes.");
    }

    @Override
    public <T extends Query> T createQuery(Session session, Class<T> type) {
        if( QueryByExample.class == type) {
            return (T)new PostgresqlQueryByExample( this, session);
        }
        else if( FulltextQuery.class == type) {
            return (T)new PostgresqlFulltextQuery( this, session);
        }
        else {
            throw new RepositoryException( "Unknown query type <" + type.getName() + ">.");
        }
    }
}

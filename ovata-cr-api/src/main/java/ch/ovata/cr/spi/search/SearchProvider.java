/*
 * $Id: SearchProvider.java 2668 2019-09-13 12:24:59Z dani $
 * Created on 02.03.2017, 16:00:00
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
package ch.ovata.cr.spi.search;

import ch.ovata.cr.api.Repository;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.spi.base.DirtyState;

/**
 *
 * @author dani
 */
public interface SearchProvider {
    /**
     * Clears the search index. Most often used before a re-indexation
     * @param workspaceName name of the workspace to remove from the index
     */
    void clearIndex( String workspaceName);
    
    /**
     * Reindex complete workspace
     * @param repository the repository the workspace belongs to
     * @param workspaceName name of the workspace to reindex
     */
    void reindexWorkspace( Repository repository, String workspaceName);
    
    /**
     * Bulk ingest list of nodes into search index
     * @param dirtyState contains the modifications applied in the transaction
     * @param revision the trx revision the dirty state describes
     */
    void indexNodes( DirtyState dirtyState, long revision);

    /**
     * Create a query of given type
     * @param <T> type of query
     * @param type class of the type of query
     * @param session the session used to resolve the nodes found
     * @return new query of given type
     */
    <T extends Query> T createQuery( Session session, Class<T> type);
}

/*
 * $Id: PostgresqlQueryByExample.java 2845 2019-11-22 10:20:25Z dani $
 * Created on 29.01.2018, 16:00:00
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

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.Query;
import ch.ovata.cr.api.query.QueryByExample;
import ch.ovata.cr.spi.store.StoreDocument;
import java.util.List;

/**
 *
 * @author dani
 */
public class PostgresqlQueryByExample implements QueryByExample {

    private final PostgresqlSearchProvider provider;
    private final Session session;
    
    public PostgresqlQueryByExample( PostgresqlSearchProvider provider, Session session) {
        this.provider = provider;
        this.session = session;
    }
    
    @Override
    public QueryByExample document(StoreDocument document) {
        return this;
    }

    @Override
    public QueryByExample childOf(String path) {
        return this;
    }

    @Override
    public List<Node> execute() {
        throw new UnsupportedOperationException( "Not implemented.");
    }

    @Override
    public Query sortBy(String propertyName) {
        return this;
    }

    @Override
    public Query skip(int skip) {
        return this;
    }

    @Override
    public Query limit(int limit) {
        return this;
    }

    @Override
    public Query types(String... types) {
        return this;
    }
}

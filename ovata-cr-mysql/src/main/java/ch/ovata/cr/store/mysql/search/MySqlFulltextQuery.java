/*
 * $Id: MySqlFulltextQuery.java 697 2017-03-06 18:12:59Z dani $
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
package ch.ovata.cr.store.mysql.search;

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.query.FulltextQuery;
import ch.ovata.cr.api.query.Query;
import java.util.List;

/**
 *
 * @author dani
 */
public class MySqlFulltextQuery implements FulltextQuery {

    private final MySqlSearchProvider provider;
    private final Session session;
    
    public MySqlFulltextQuery( MySqlSearchProvider provider, Session session) {
        this.provider = provider;
        this.session = session;
    }
    
    @Override
    public FulltextQuery term(String term) {
        return this;
    }

    @Override
    public FulltextQuery childOf(String path) {
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

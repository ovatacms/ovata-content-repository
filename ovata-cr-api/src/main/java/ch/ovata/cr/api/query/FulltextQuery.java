/*
 * $Id: FulltextQuery.java 679 2017-02-25 10:28:00Z dani $
 * Created on 07.03.2017, 16:00:00
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
package ch.ovata.cr.api.query;

/**
 *
 * @author dani
 */
public interface FulltextQuery extends Query {
    /**
     * The query term to search for
     * @param term the query term
     * @return the query itself
     */
    FulltextQuery term( String term);
}

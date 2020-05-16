/*
 * $Id: SqlUtils.java 2662 2019-09-11 12:27:35Z dani $
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
package ch.ovata.cr.store.h2;

import ch.ovata.cr.api.RepositoryException;

/**
 *
 * @author dani
 */
public final class SqlUtils {
    
    private SqlUtils() {
    }

    public static String createStatement(String sql, String tableName) {
        if (tableName.indexOf('`') != -1) {
            throw new RepositoryException("Illegal character in table name.");
        }
        return String.format(sql, tableName);
    }
}

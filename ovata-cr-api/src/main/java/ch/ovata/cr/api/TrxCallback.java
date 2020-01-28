/*
 * $Id: TrxCallback.java 2944 2020-01-27 14:14:20Z dani $
 * Created on 21.01.2020, 19:00:00
 * 
 * Copyright (c) 2020 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.api;

import ch.ovata.cr.spi.store.Transaction;

/**
 *
 * @author dani
 */
public interface TrxCallback {
    /**
     * Perform work in the same (database) transaction as the content repository changes are written.
     * @param trx the transaction committed
     */
    void doWork( Transaction trx);
}

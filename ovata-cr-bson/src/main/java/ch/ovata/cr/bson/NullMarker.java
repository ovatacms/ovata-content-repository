/*
 * $Id: NullMarker.java 2824 2019-11-17 13:12:32Z dani $
 * Created on 13.11.2019, 19:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.bson;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 *
 * @author dani
 */
public class NullMarker implements Serializable {

    public static final NullMarker EMPTY_RESULT = new NullMarker();
    
    protected Object readResolve() throws ObjectStreamException {
        return EMPTY_RESULT;
    }
}

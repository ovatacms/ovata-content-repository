/*
 * $Id: AbstractYamlBase.java 2712 2019-09-28 09:40:17Z dani $
 * Created on 20.01.2017, 12:00:00
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
package ch.ovata.cr.utils;

/**
 *
 * @author dani
 */
public abstract class AbstractYamlBase {
    protected static final String REFERENCE_PREFIX = "@@Reference:";
    protected static final String LOCAL_DATE_PREFIX = "@@LocalDate:";
    protected static final String ZONED_DATETIME_PREFIX = "@@ZonedDate:";
    protected static final String DECIMAL_PREFIX = "@@Decimal:";
    
    protected AbstractYamlBase() {
    }
}

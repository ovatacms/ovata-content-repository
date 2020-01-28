/*
 * $Id: CoreNodeTypes.java 679 2017-02-25 10:28:00Z dani $
 * Created on 10.12.2016, 12:00:00
 * 
 * Copyright (c) 2016 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.api;

/**
 *
 * @author dani
 */
public interface CoreNodeTypes {
    String UNSTRUCTURED = "ovata:unstructured";
    String FOLDER = "ovata:folder";
    String APPLICATION = "ovata:app";
    String FORM = "ovata:form";
    String RESOURCE = "ovata:resource";
    String JAAS_ENTRY = "ovata:jaas:entry";
    String USER = "ovata:user";
    String ROLE = "ovata:role";
}

/*
 * $Id: Reference.java 679 2017-02-25 10:28:00Z dani $
 * Created on 14.11.2016, 12:00:00
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

import java.util.Optional;

/**
 *
 * @author dani
 */
public interface Reference {
    /**
     * The workspace referenced
     * @return the name of the workspace referenced
     */
    String getWorkspace();
    
    /**
     * The path to the node referenced in the specified worksapce
     * @return the path to the node
     */
    String getPath();
    
    /**
     * Retrieves the node referenced
     * @return the node referenced
     */
    Node getNode();
    
    /**
     * Retrieves the node referenced
     * @return the node referenced wrapped in an <code>Optional</code>
     */
    Optional<Node> findNode();
}

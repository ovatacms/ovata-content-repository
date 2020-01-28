/*
 * $Id: DirtyState.java 2894 2019-12-16 14:20:34Z dani $
 * Created on 04.01.2017, 12:00:00
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
package ch.ovata.cr.spi.base;

import ch.ovata.cr.api.Node;
import java.util.Collection;

/**
 *
 * @author dani
 */
public interface DirtyState {
    
    /**
     * The name of the workspace this dirty state belongs to
     * @return name of a workspace
     */
    String getWorkspaceName();
    
    /**
     * Retrieve a modified node by it's id
     * @param id the key of the node
     * @return the modified node as contained in the dirty state or null
     */
    Node findNode( String id);
    
    /**
     * Returns a collection containing the node id's of the nodes modified in this dirty state.
     * @return collection with node ids
     */
    Collection<String> getModifiedNodeIds();
    
    /**
     * Set of modifications that may be applied to the data store
     * @return the collection of modifications
     */
    Collection<Modification> getModifications();

    interface Modification {

        public enum Type { MODIFY, ADD, REMOVE, MOVE, RENAME }
    
        Type getType();
        String getNodeId();
        Node getNode();
    }
}

/*
 * $Id: Item.java 2944 2020-01-27 14:14:20Z dani $
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

/**
 *
 * @author dani
 */
public interface Item {
    
    /**
     * Retrieves the name of the current item
     * @return name of the item
     */
    String getName();
    
    /**
     * Retrieves the absolute path of the current node within the workspace
     * @return the absolute path
     */
    String getPath();
    
    /**
     * Retrieves the depth of the node in the node tree
     * @return the depth value
     */
    int getDepth();
    
    /**
     * Renames the item
     * @param name 
     */
    void rename( String name);
    
    /**
     * Retrieves the parent node of this property
     * @return 
     */
    Node getParent();
    
    /**
     * Removes this item and its subgraph
     */
    void remove();
    
    /**
     * Accepts a visitor
     * @param visitor the vistor to call
     */
    void accept( Visitor visitor);
    
    /**
     * Implementation of the visitor pattern
     */
    interface Visitor {
        /**
         * Called when visiting a node
         * @param node the node visited
         */
        void visit( Node node);
        
        /**
         * Called when visiting a property
         * @param property the property visited
         */
        void visit( Property property);
    }
}

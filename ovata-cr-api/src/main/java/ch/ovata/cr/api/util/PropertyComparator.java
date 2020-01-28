/*
 * $Id: PropertyComparator.java 692 2017-03-06 09:45:19Z dani $
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
package ch.ovata.cr.api.util;

import ch.ovata.cr.api.Node;
import java.util.Comparator;

/**
 *
 * @author dani
 */
public class PropertyComparator implements Comparator<Node> {

    public static final Comparator<Node> ByOrder = new PropertyComparator( "order");
    public static final Comparator<Node> ByName =  (n1,n2) -> n1.getName().compareTo( n2.getName());
    public static final Comparator<Node> ByType = (n1,n2) -> n1.getType().compareTo( n2.getType());

    
    private final String propertyName;
    
    public PropertyComparator( String propertyName) {
        this.propertyName = propertyName;
    }
    
    @Override
    public int compare(Node n1, Node n2) {
        return n1.getProperty( propertyName).getValue().compareTo( n2.getProperty( propertyName).getValue());
    }
}

/*
 * $Id: PropertyComparatorIfAvailable.java 692 2017-03-06 09:45:19Z dani $
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
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Value;
import java.util.Comparator;
import java.util.Optional;

/**
 *
 * @author dani
 */
public class PropertyComparatorIfAvailable implements Comparator<Node> {

    public static final PropertyComparatorIfAvailable ByOrder = new PropertyComparatorIfAvailable( "order");
    
    private final String propertyName;
    private boolean reverse;
    
    public PropertyComparatorIfAvailable( String propertyName) {
        this( propertyName, false);
    }
    
    public PropertyComparatorIfAvailable( String propertyName, boolean reverse) {
        this.propertyName = propertyName;
        this.reverse = reverse;
    }
    
    @Override
    public int compare(Node n1, Node n2) {
        if( this.propertyName == null) {
            return 0;
        }
        
        int result = 0;
        Optional<Property> p1 = n1.findProperty( propertyName);
        Optional<Property> p2 = n2.findProperty( propertyName);
        
        if( !p1.isPresent() && !p2.isPresent()) {
            result = 0;
        }
        else if( !p1.isPresent()) {
            result = 1;
        }
        else if( !p2.isPresent()) {
            result = -1;
        }
        else {
            Value v1 = p1.get().getValue();
            Value v2 = p2.get().getValue();
            
            result = v1.compareTo( v2);
        }
        
        return reverse ? (-1) * result : result;
    }
}

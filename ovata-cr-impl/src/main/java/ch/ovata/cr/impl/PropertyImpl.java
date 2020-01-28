/*
 * $Id: PropertyImpl.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.impl;

import ch.ovata.cr.impl.values.AbstractValue;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Value;
import static ch.ovata.cr.impl.NodeImpl.PATH_DELIMITER;
import java.util.Objects;

/**
 *
 * @author dani
 */
public class PropertyImpl implements Property {

    private NodeImpl node;
    private String name;
    
    public PropertyImpl( NodeImpl node, String name) {
        this.node = node;
        this.name = name;
    }
    
    @Override
    public Node getParent() {
        return this.node;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    public void setName( String name) {
        this.name = name;
    }
    
    @Override
    public void rename(String name) {
        this.node.renameProperty( this, name);
    }

    @Override
    public void remove() {
        this.node.removeProperty( this);
    }

    @Override
    public String getPath() {
        String path = getParent().getPath();
        
        if( path.endsWith( PATH_DELIMITER) ) {
            return path + this.getName();
        }
        else {
            return path + PATH_DELIMITER + this.getName();
        }
    }

    @Override
    public int getDepth() {
        return this.getParent().getDepth() + 1;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit( this);
    }

    @Override
    public Value getValue() {
        return this.node.getValue( this);
    }
    
    @Override
    public void setValue( Value value) {
        this.node.updateValue( this, (AbstractValue)value);
    }
    
    @Override
    public boolean equals( Object other) {
        if( other instanceof PropertyImpl) {
            PropertyImpl p2 = (PropertyImpl)other;
            
            if( this.node.equals( p2.node)) {
                return this.name.equals( p2.name);
            }
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.node);
        hash = 59 * hash + Objects.hashCode(this.name);
        return hash;
    }
}

/*
 * $Id: RootNodeImpl.java 679 2017-02-25 10:28:00Z dani $
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

import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.security.Permission;
import ch.ovata.cr.api.security.Policy;
import ch.ovata.cr.spi.store.StoreDocument;

/**
 *
 * @author dani
 */
public class RootNodeImpl extends NodeImpl {
    
    public RootNodeImpl( SessionImpl session, StoreDocument document) {
        super( session, document, false);
    }
    
    @Override
    public int getDepth() {
        return 0;
    }
    
    @Override
    public String getPath() {
        return "/";
    }
    
    @Override
    public boolean isRoot() {
        return true;
    }
    
    @Override
    public Node getParent() {
        return null;
    }
    
    @Override
    public String getName() {
        return "/";
    }

    @Override
    public void remove() {
        throw new IllegalStateException( "Not allowed to remove root node.");
    }
    
    @Override
    public void rename( String name) {
        if( !name.equals( this.getName())) {
            throw new IllegalStateException( "Not allowed to rename root node.");
        }
    }

    @Override
    protected boolean hasPermission( Permission permission) {
        return (this.getPolicy().check( permission) == Policy.Result.GRANTED);
    }
}

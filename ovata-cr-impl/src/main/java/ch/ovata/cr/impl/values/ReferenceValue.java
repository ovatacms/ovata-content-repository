/*
 * $Id: ReferenceValue.java 679 2017-02-25 10:28:00Z dani $
 * Created on 22.11.2016, 12:00:00
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
package ch.ovata.cr.impl.values;

import ch.ovata.cr.api.Reference;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.impl.NodeImpl;
import ch.ovata.cr.spi.store.StoreDocument;

/**
 *
 * @author dani
 */
public class ReferenceValue extends AbstractValue {

    private static final String WORKSPACE_NAME_FIELD = "workspace_name";
    private static final String PATH_FIELD = "path";
            
    private final StoreDocument document;
    
    public ReferenceValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public ReferenceValue( ValueFactoryImpl vf, Reference reference) {
        super( vf);
        
         this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.REFERENCE.name())
                                         .append(NodeImpl.VALUE_FIELD, vf.newDocument().append( WORKSPACE_NAME_FIELD, reference.getWorkspace())
                                                                                       .append( PATH_FIELD, reference.getPath()));
    }
    
    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Value.Type getType() {
        return Value.Type.REFERENCE;
    }

    @Override
    public Reference getReference() {
        StoreDocument refDoc = this.document.getDocument(NodeImpl.VALUE_FIELD);
        
        return new ReferenceImpl( this.getValueFactory().getSession(), refDoc.getString( WORKSPACE_NAME_FIELD), refDoc.getString( PATH_FIELD));
    }

    @Override
    public int compareTo(Value o) {
        throw new UnsupportedOperationException("Compare not defined on this type.");
    }
    
    @Override
    public int hashCode() {
        return this.getReference().hashCode();
    }
    
    @Override
    public boolean equals( Object o) {
        if( o instanceof ReferenceValue) {
            ReferenceValue other = (ReferenceValue)o;
            
            return this.getReference().equals( other.getReference());
        }
        
        return false;
    }
}

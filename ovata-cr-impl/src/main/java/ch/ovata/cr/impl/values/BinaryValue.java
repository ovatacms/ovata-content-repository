/*
 * $Id: BinaryValue.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.impl.values;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.impl.NodeImpl;
import ch.ovata.cr.impl.RepositoryImpl;
import ch.ovata.cr.spi.store.StoreDocument;

/**
 *
 * @author dani
 */
public class BinaryValue extends AbstractValue {

    private static final String FILENAME_FIELD = "@filename";
    private static final String CONTENTTYPE_FIELD = "@contenttype";
    private static final String CONTENTLENGTH_FIELD = "@contentlength";
    
    private final StoreDocument document;
    
    public BinaryValue( ValueFactoryImpl vf, StoreDocument document) {
        super( vf);

        this.document = document;
    }
    
    public BinaryValue( ValueFactoryImpl vf, Binary value) {
        super( vf);

        this.document = vf.newDocument().append( NodeImpl.VALUE_TYPE_FIELD, Value.Type.BINARY.name())
                                        .append( NodeImpl.VALUE_FIELD, value.getId())
                                        .append( FILENAME_FIELD, value.getFilename())
                                        .append( CONTENTTYPE_FIELD, value.getContentType())
                                        .append( CONTENTLENGTH_FIELD, value.getLength());
    }

    @Override
    public StoreDocument toDocument() {
        return this.document;
    }

    @Override
    public Type getType() {
        return Value.Type.BINARY;
    }

    @Override
    public Binary getBinary() {
        String id = this.document.getString( NodeImpl.VALUE_FIELD);
        String filename = this.document.getString( FILENAME_FIELD);
        String contentType = this.document.getString( CONTENTTYPE_FIELD);
        Long length = this.document.getLong( CONTENTLENGTH_FIELD);
        
        BlobStore store = ((RepositoryImpl)this.getValueFactory().getSession().getRepository()).getBlobStore();
        
        return new BinaryWrapper( id, filename, contentType, length, store);
    }

    @Override
    public int compareTo(Value o) {
        throw new UnsupportedOperationException("Compare not supported on this type.");
    }
}

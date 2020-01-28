/*
 * $Id: ValueFactoryImpl.java 707 2017-03-09 13:31:29Z dani $
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
import ch.ovata.cr.api.ValueFactory;
import ch.ovata.cr.api.ValueFormatException;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.impl.NodeImpl;
import ch.ovata.cr.impl.RepositoryImpl;
import ch.ovata.cr.impl.SessionImpl;
import ch.ovata.cr.spi.store.StoreDocument;
import ch.ovata.cr.spi.store.StoreDocumentList;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author dani
 */
public class ValueFactoryImpl implements ValueFactory {
    
    private final EnumMap<Value.Type, Function<StoreDocument, Value>> factories = new EnumMap<>( Value.Type.class);
    
    private final SessionImpl session;

    public ValueFactoryImpl( SessionImpl session) {
        this.session = session;
        
        factories.put( Value.Type.STRING, d -> new StringValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.BOOLEAN, d -> new BooleanValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.LONG, d -> new LongValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.DOUBLE, d -> new DoubleValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.DECIMAL, d -> new DecimalValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.LOCAL_DATE, d -> new LocalDateValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.ZONED_DATETIME, d -> new ZonedDateTimeValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.BINARY, d -> new BinaryValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.REFERENCE, d -> new ReferenceValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.MAP, d -> new MapValue( ValueFactoryImpl.this, d));
        factories.put( Value.Type.ARRAY, d -> new ArrayValue( ValueFactoryImpl.this, d));
    }
    
    @Override
    public SessionImpl getSession() {
        return this.session;
    }
    
    public Value of( StoreDocument document) {
        Value.Type type = Value.Type.valueOf( document.getString( NodeImpl.VALUE_TYPE_FIELD));
        
        Function<StoreDocument, Value> f = factories.get( type);

        if( f == null) {
            throw new ValueFormatException( "Unsupported Type <" + type.name() + ">.");
        }
        
        return f.apply( document);
    }
    
    @Override
    public Value of(String value) {
        if( value == null) {
            return null;
        }

        return new StringValue( this, value);
    }

    @Override
    public Value of( LocalDate value) {
        if( value == null) {
            return null;
        }

        return new LocalDateValue( this, value);
    }

    @Override
    public Value of(ZonedDateTime value) {
        if( value == null) {
            return null;
        }

        return new ZonedDateTimeValue( this, value);
    }

    @Override
    public Value of( long value) {
        return new LongValue( this, value);
    }

    @Override
    public Value of( double value) {
        return new DoubleValue( this, value);
    }

    @Override
    public Value of(BigDecimal value) {
        if( value == null) {
            return null;
        }

        return new DecimalValue( this, value);
    }

    @Override
    public Value of( boolean value) {
        return new BooleanValue( this, value);
    }

    @Override
    public Value of( Map<String, Value> map) {
        if( map == null) {
            return null;
        }

        return new MapValue( this, map);
    }

    @Override
    public Value of( Collection<Value> array) {
        if( array == null) {
            return null;
        }

        return new ArrayValue( this, array);
    }

    @Override
    public Value binary( InputStream in, String filename, String contentType) {
        if( in == null) {
            return null;
        }
        
        BlobStore store = ((RepositoryImpl)this.session.getRepository()).getBlobStore();
        Binary binary = store.createBlob(in, filename, contentType);

        this.session.registerBlob( binary);
        
        return new BinaryValue( this, binary);
    }
    
    @Override
    public Value binary( InputStream in, long contentLength, String filename, String contentType) {
        if( in == null) {
            return null;
        }
        
        BlobStore store = ((RepositoryImpl)this.session.getRepository()).getBlobStore();
        Binary binary = store.createBlob(in, contentLength, filename, contentType);

        this.session.registerBlob( binary);
        
        return new BinaryValue( this, binary);
    }

    @Override
    public Value binary( byte[] bytes, String filename, String contentType) {
        if( bytes == null) {
            return null;
        }
        
        BlobStore store = ((RepositoryImpl)this.session.getRepository()).getBlobStore();
        Binary binary = store.createBlob( bytes, filename, contentType);
        
        this.session.registerBlob( binary);
        
        return new BinaryValue( this, binary);
    }

    @Override
    public Value referenceByPath( String workspaceName, String path) {
        if( (workspaceName == null) || (path == null)) {
            return null;
        }
        
        return new ReferenceValue( this, new ReferenceImpl( this.getSession(), workspaceName, path));
    }

    @Override
    public Value referenceById( String workspaceName, String nodeId) {
        if( (workspaceName == null) || (nodeId == null)) {
            return null;
        }
        
        return new ReferenceValue( this, new ReferenceImpl( this.getSession(), workspaceName, nodeId));
    }

    @Override
    public StoreDocument newDocument() {
        return ((RepositoryImpl)this.session.getRepository()).getDatabase().newDocument();
    }
    
    @Override
    public StoreDocumentList newList() {
        return ((RepositoryImpl)this.session.getRepository()).getDatabase().newList();
    }
}

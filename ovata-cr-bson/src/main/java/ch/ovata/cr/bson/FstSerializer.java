/*
 * $Id: FstSerializer.java 2858 2019-12-02 13:24:29Z dani $
 * Created on 13.11.2019, 19:00:00
 * 
 * Copyright (c) 2019 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.bson;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.ehcache.core.util.ByteBufferInputStream;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import org.nustaq.serialization.FSTConfiguration;

/**
 *
 * @author dani
 */
public class FstSerializer implements Serializer {
    
    private FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    
    @Override
    public ByteBuffer serialize(Object object) {
        return ByteBuffer.wrap( conf.asByteArray( object));
    }

    @Override
    public Object read(ByteBuffer binary) throws ClassNotFoundException {
        if( binary.hasArray()) {
            return conf.asObject( binary.array());
        }
        else {
            try {
                ByteBufferInputStream in = new ByteBufferInputStream( binary);

                return conf.getObjectInput( in).readObject();
            }
            catch( IOException e) {
                throw new SerializerException( "Could not deserialize.", e);
            }
        }
    }

    @Override
    public boolean equals(Object object, ByteBuffer binary) throws ClassNotFoundException {
        return object.equals( read( binary));
    }
}

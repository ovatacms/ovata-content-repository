/*
 * $Id: JsonExporter.java 2859 2019-12-02 13:34:08Z dani $
 * Created on 05.07.2019, 12:00:00
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
package ch.ovata.cr.utils;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.Property;
import ch.ovata.cr.api.Session;
import ch.ovata.cr.api.Value;
import ch.ovata.cr.tools.IOUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.json.stream.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author dani
 */
public class JsonExporter {
    
    private static final String LABEL_NODE_TYPE = "@type";
    private static final String LABEL_PROPERTIES = "properties";
    private static final String LABEL_NODES = "nodes";
    private static final String LABEL_VALUE = "value";
    private static final String LABEL_VALUE_TYPE = "type";
    private static final String LABEL_BINARY = "binary";
    
    private static final Logger logger = LoggerFactory.getLogger( JsonExporter.class);
    
    public void exportJson( JsonGenerator writer, Node node) {
        writer.writeStartObject();
        
        writeProperties( writer, node);
        writeNodes( writer, node);
        
        writer.writeEnd();
    }
    
    private void writeNode( JsonGenerator writer, Node node) {
        writer.writeStartObject( node.getName());

        writer.write( LABEL_NODE_TYPE, node.getType());
        
        writeProperties( writer, node);
        writeNodes( writer, node);
        
        writer.writeEnd();
    }
    
    private void writeProperties( JsonGenerator writer, Node node) {
        writer.writeStartObject( LABEL_PROPERTIES);

        node.getProperties().stream().forEach( p -> writeProperty( writer, p));

        writer.writeEnd();
    }
    
    private void writeNodes( JsonGenerator writer, Node node) {
        writer.writeStartObject( LABEL_NODES);
        
        node.getNodes().forEach( n -> writeNode( writer, n));
        
        writer.writeEnd();
    }
    
    private void writeProperty( JsonGenerator writer, Property p) {
        writer.writeStartObject( p.getName());

        writeValue( writer, p.getParent().getSession(), p.getValue());
        
        writer.writeEnd();
    }
    
    private void writeValue( JsonGenerator writer, Session session, Value v) {
        writer.write( LABEL_VALUE_TYPE, v.getType().name());
        
        switch( v.getType()) {
            case STRING:
                writer.write( LABEL_VALUE, v.getString());
                break;
            case LONG:
                writer.write( LABEL_VALUE, v.getLong());
                break;
            case DOUBLE:
                writer.write( LABEL_VALUE, v.getDouble());
                break;
            case DECIMAL:
                writer.write( LABEL_VALUE, v.getDecimal());
                break;
            case BOOLEAN:
                writer.write( LABEL_VALUE, v.getBoolean());
                break;
            case REFERENCE:
                if( session.getWorkspace().getName().equals( v.getReference().getWorkspace())) {
                    writer.write( LABEL_VALUE, "<this>:" + v.getReference().getPath());
                }
                else {
                    writer.write( LABEL_VALUE, v.getReference().getWorkspace() + ":" + v.getReference().getPath());
                }
                break;
            case LOCAL_DATE:
                writer.write( LABEL_VALUE, DateTimeFormatter.ISO_LOCAL_DATE.format( v.getDate()));
                break;
            case ZONED_DATETIME:
                writer.write( LABEL_VALUE, DateTimeFormatter.ISO_ZONED_DATE_TIME.format( v.getTime()));
                break;
            case BINARY:
                Binary binary = v.getBinary();
                
                writer.writeStartObject( LABEL_VALUE);
                
                writer.write( "contenttype", binary.getContentType());
                writer.write( "filename", binary.getFilename());
                writer.write( "length", binary.getLength());
                
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                
                try( OutputStream out = Base64.getEncoder().wrap( buffer))  {
                    try( InputStream in = v.getBinary().getInputStream()) {
                        IOUtils.copy( in, out);
                    }
                }
                catch( Exception e) {
                    logger.warn( "Could not read binary.", e);
                }
                
                writer.write( LABEL_BINARY, new String( buffer.toByteArray(), StandardCharsets.UTF_8));
                
                writer.writeEnd();
                break;
            case MAP:
                writer.writeStartObject( LABEL_VALUE);

                v.getMap().entrySet().stream().forEach( entry  -> {
                    writer.writeStartObject( entry.getKey());
                    
                    writeValue( writer, session, entry.getValue());
                    
                    writer.writeEnd();
                });
                
                writer.writeEnd();
                break;
            case ARRAY:
                writer.writeStartArray( LABEL_VALUE);
                
                for( Value mv : v.getArray()) {
                    writer.writeStartObject();
                    
                    writeValue( writer, session, mv);
                    
                    writer.writeEnd();
                }
                
                writer.writeEnd();
                break;
            case GEOJSON:
                // Not supported as by now
                break;
        }
    }
}

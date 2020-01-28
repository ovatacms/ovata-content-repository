/*
 * $Id: TextExtractor.java 2858 2019-12-02 13:24:29Z dani $
 * Created on 21.03.2018, 12:00:00
 * 
 * Copyright (c) 2018 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.mongodb.fulltext;

import ch.ovata.cr.api.Binary;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author dani
 */
public final class TextExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(TextExtractor.class);
    
    private TextExtractor() {
    }
    
    public static String extract( Binary binary) throws IOException, SAXException, TikaException {
        BodyContentHandler handler = new BodyContentHandler();
        
        logger.debug( "Extracting text from <{}> with content-type <{}>.", binary.getFilename(), binary.getContentType());
        
        try( InputStream in = binary.getInputStream()) {
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();

            metadata.set( HttpHeaders.CONTENT_TYPE, binary.getContentType());
        
            parser.parse( in, handler, metadata);
        }
        catch( SAXException e) {
            logger.info( "Could not (fully) parse document.", e);
        }
        
        String text = handler.toString();

        if( logger.isDebugEnabled()) {
            logger.debug( "Text : {}.", text.substring( 0, Math.min( 100, text.length() - 1)));
        }

        return handler.toString();
    }
}

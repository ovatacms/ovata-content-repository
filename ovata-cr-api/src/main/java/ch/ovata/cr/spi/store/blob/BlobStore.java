/*
 * $Id: BlobStore.java 707 2017-03-09 13:31:29Z dani $
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
package ch.ovata.cr.spi.store.blob;

import ch.ovata.cr.api.Binary;
import java.io.InputStream;
import java.util.Collection;

/**
 *
 * @author dani
 */
public interface BlobStore {
    
    /**
     * Find the <code>Binary</code> with the given id
     * @param id id of the binary
     * @return the binary or null if no binary found
     */
    Binary findBlob( String id);
    
    /**
     * Creates a new binary with the given content and metadata
     * @param in stream providing the content of the binary
     * @param filename the name of the file
     * @param contentType the type of content
     * @return the newly created <code>Binary</code>
     */
    Binary createBlob( InputStream in, String filename, String contentType);
    
    /**
     * Creates a new binary with the given content and metadata
     * @param in stream providing the content of the binary
     * @param contentLength size of the binary
     * @param filename the name of the file
     * @param contentType the type of content
     * @return the newly created <code>Binary</code>
     */
    Binary createBlob( InputStream in, long contentLength, String filename, String contentType);
    
    /**
     * Creates a new binary with the given content and metadata
     * @param data the content of the new binary
     * @param filename the name of the file
     * @param contentType the type of content
     * @return the newly created <code>Binary</code>
     */
    Binary createBlob( byte[] data, String filename, String contentType);
    
    /**
     * Removes the <code>Binary</code> with the given id
     * @param ids list of ids of binaries to remove
     */
    void deleteBlobs( Collection<String> ids);
}

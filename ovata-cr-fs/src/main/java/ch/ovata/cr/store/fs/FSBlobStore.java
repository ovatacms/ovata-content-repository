/*
 * $Id: FSBlobStore.java 1079 2017-10-17 19:46:11Z dani $
 * Created on 19.01.2018, 12:00:00
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
package ch.ovata.cr.store.fs;

import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.NotFoundException;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.spi.store.blob.BlobStore;
import ch.ovata.cr.tools.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class FSBlobStore implements BlobStore {

    private static final Logger logger = LoggerFactory.getLogger( FSBlobStore.class);
    private static final String META_SUFFIX = ".meta";
    
    private final File rootDirectory;
    
    public FSBlobStore( File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }
    
    @Override
    public Binary findBlob(String id) {
        File file = calculateFilePath( id);
        
        if( !file.exists()) {
            throw new NotFoundException( "Could not find blob with id <" + id + ">.");
        }
        
        return new FSBlob( id, file);
    }

    @Override
    public Binary createBlob(InputStream in, String filename, String contentType) {
        String id = createId();
        File contentFile = calculateFilePath( id);
        File metaFile = new File( contentFile.getParentFile(), id + META_SUFFIX);
        File dir = contentFile.getParentFile();
        
        if( !dir.exists() && !dir.mkdirs()) {
            throw new RepositoryException( "Could not create directory for blob with id <" + id + ">.");
        }
        
        try( ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( metaFile))) {
            out.writeUTF( filename);
            out.writeUTF( contentType);
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not write meta-data for blob with id <" + id + ">.", e);
        }
        
        try( OutputStream out = new FileOutputStream( contentFile)) {
            IOUtils.copy( in, out);
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not copy content for blob with id <" + id + ">.", e);
        }
        
        return new FSBlob( id, contentFile);
    }

    @Override
    public Binary createBlob(InputStream in, long contentLength, String filename, String contentType) {
        return createBlob( in, filename, contentType);
    }

    @Override
    public Binary createBlob(byte[] data, String filename, String contentType) {
        return createBlob( new ByteArrayInputStream( data), filename, contentType);
    }

    @Override
    public void deleteBlobs(Collection<String> ids) {
        ids.stream().forEach( this::deleteFile);
    }
    
    private String createId() {
        return UUID.randomUUID().toString();
    }
    
    private void deleteFile( String id) {
        File file = calculateFilePath( id);
        File metaFile = new File( file.getAbsolutePath() + META_SUFFIX);

        if( !file.delete()) {
            logger.warn( "Could not delete file for Blob with id <{}>.", id);
        }
        
        if( !metaFile.delete()) {
            logger.warn( "Could not delete meta-file for Blob with id <{}>. Path : <{}>.", id, metaFile.getAbsolutePath());
        }
    }
    
    private File calculateFilePath( String id) {
        String[] parts = id.split( "-");
        
        if( parts.length != 5) {
            throw new RepositoryException( "Unrecognized blob id <" + id + ">.");
        }
        
        File dir = rootDirectory;
        
        for( int i = 0; i < 4; i++) {
            dir = new File( dir, parts[ i].substring( 0, 2));
        }
        
        return new File( dir, id);
    }
}

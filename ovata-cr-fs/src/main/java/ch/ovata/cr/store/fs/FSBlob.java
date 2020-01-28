/*
 * $Id: FSBlob.java 1079 2017-10-17 19:46:11Z dani $
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

import ch.ovata.cr.tools.LimitedInputStream;
import ch.ovata.cr.api.Binary;
import ch.ovata.cr.api.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 *
 * @author dani
 */
public class FSBlob implements Binary {

    private final String id;
    private final File file;
    private String filename;
    private String contentType;
    
    public FSBlob( String id, File file) {
        this.id = id;
        this.file = file;
    }
    
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream( file);
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not access file <" + file + ">.", e);
        }
    }

    @Override
    public InputStream getInputStream(long start, Long end) {
        try {
            return new LimitedInputStream( new FileInputStream( file), start, end);
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not retrieve data fro blob with id <" + this.id + ">", e);
        }
    }

    @Override
    public String getFilename() {
        if( this.filename == null) {
            readMetadata();
        }
        
        return this.filename;
    }

    @Override
    public String getContentType() {
        if( this.contentType == null) {
            readMetadata();
        }
        
        return this.contentType;
    }

    @Override
    public long getLength() {
        return file.length();
    }
    
    private void readMetadata() {
        File metaFile = new File( file.getParentFile(), id + ".meta");
        
        try( ObjectInputStream in = new ObjectInputStream( new FileInputStream( metaFile))) {
            this.filename = in.readUTF();
            this.contentType = in.readUTF();
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not read meta-data from <" + metaFile + ">.", e);
        }
    }
}

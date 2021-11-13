/*
 * $Id: MongoDbConnection.java 715 2017-03-14 22:13:40Z dani $
 * Created on 12.01.2017, 19:00:00
 * 
 * Copyright (c) 2017 by Ovata GmbH,
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Ovata GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Ovata GmbH.
 */
package ch.ovata.cr.store.mongodb;

import ch.ovata.cr.spi.store.StoreConnection;
import ch.ovata.cr.spi.store.StoreDatabase;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoIterable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author dani
 */
public class MongoDbConnection implements StoreConnection {

    private final MongoClient mongoClient;
    private final BlobStoreFactory blobStoreFactory;
    
    private final Map<String, StoreDatabase> databases = new ConcurrentHashMap<>();
    
    public MongoDbConnection( MongoClient client, BlobStoreFactory blobStoreFactory) {
        this.mongoClient = client;
        this.blobStoreFactory = blobStoreFactory;
    }
    
    @Override
    public String getInfo() {
        return "<not available>";
    }

    @Override
    public <T> T unwrap(Class<T> c) {
        if( MongoClient.class.equals( c)) {
            return (T)this.mongoClient;
        }
        
        return blobStoreFactory.unwrap( c);
    }
    
    public MongoClient unwrap() {
        return this.mongoClient;
    }
    
    @Override
    public StoreDatabase getDatabase(String name) {
        return this.databases.computeIfAbsent( name, key -> new  MongoDbDatabase( mongoClient, mongoClient.getDatabase( key), blobStoreFactory.createBlobStore( key)));
    }
    
    @Override
    public StoreDatabase createDatabase( String name) {
        return getDatabase( name);
    }

    @Override
    public boolean checkIfDatabaseExists( String name) {
        MongoIterable<String> dbNames = mongoClient.listDatabaseNames();
        
        for( String dbName : dbNames) {
            if( dbName.equals( name)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void shutdown() {
        this.databases.clear();
        this.blobStoreFactory.shutdown();
    }
}

/*
 * $Id: RCMongoDbS3.java 2674 2019-09-18 11:45:20Z dani $
 * Created on 25.01.2018, 12:00:00
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
package ch.ovata.cr.impl;

import ch.ovata.cr.api.RepositoryConnection;
import ch.ovata.cr.api.RepositoryConnectionDataSource;
import ch.ovata.cr.impl.concurrency.LocalConcurrencyControlFactory;
import ch.ovata.cr.spi.search.SearchProviderFactory;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import ch.ovata.cr.store.aws.S3BlobStoreFactory;
import ch.ovata.cr.store.mongodb.MongoDbConnection;
import ch.ovata.cr.store.mongodb.search.MongoDbFulltextSearchProviderFactory;
import com.amazonaws.regions.Regions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 *
 * @author dani
 */
public class RCMongoDbS3 implements RepositoryConnectionDataSource {

//    private static final String mongouri = "mongodb://ovatacms:33Ta3TYDZInudadU@ovatacms-cluster-shard-00-00-dffel.mongodb.net:27017,ovatacms-cluster-shard-00-01-dffel.mongodb.net:27017,ovatacms-cluster-shard-00-02-dffel.mongodb.net:27017/admin?ssl=true&replicaSet=ovatacms-cluster-shard-0&authSource=admin&minPoolSize=1&maxPoolSize=50&maxIdleTimeMS=120000";
    private static final String mongouri = "mongodb://administrator:b1972Adm12@ovatacms-dev-shard-00-00-rm9zl.mongodb.net:27017,ovatacms-dev-shard-00-01-rm9zl.mongodb.net:27017,ovatacms-dev-shard-00-02-rm9zl.mongodb.net:27017/admin?ssl=true&replicaSet=ovatacms-dev-shard-0&authSource=admin";
//    private static final String elasticuri = "https://elastic:VNswJQBBJoicHIA8OnWcuNMZ@980b666e5aaa340cd6945cd40e96a5fe.eu-west-1.aws.found.io:9343";
    private static final String region = "eu-west-1";
    private static final String bucketName = "ovata-cr-bucket-dev";
    private static final String clusterName = "ovata-dev";
    private static final String queueUrl = "https://sqs.eu-west-1.amazonaws.com/504647414493/ovata-indexing-dev";
    
    @Override
    public RepositoryConnection getConnection() {
        MongoClient mongodb = MongoClients.create( mongouri);
        BlobStoreFactory blobStoreFactory = new S3BlobStoreFactory( Regions.fromName( region), bucketName);
        ConcurrencyControlFactory ccontrol = new LocalConcurrencyControlFactory();
        MongoDbConnection mongoConnection = new MongoDbConnection(mongodb, blobStoreFactory, ccontrol);
        
        SearchProviderFactory searchProviderFactory = new MongoDbFulltextSearchProviderFactory( blobStoreFactory, mongoConnection);
        
        return new RepositoryConnectionImpl( mongoConnection, searchProviderFactory);
    }

    @Override
    public void close() {
    }
}

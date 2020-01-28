/*
 * $Id: ElasticSearchProviderFactory.java 679 2017-02-25 10:28:00Z dani $
 * Created on 02.03.2017, 16:00:00
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
package ch.ovata.cr.elastic;

import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.elastic.fulltext.FulltextIndexer;
import ch.ovata.cr.spi.search.SearchProvider;
import ch.ovata.cr.spi.search.SearchProviderFactory;
import ch.ovata.cr.spi.store.blob.BlobStoreFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 *
 * @author dani
 */
public class ElasticSearchProviderFactory implements SearchProviderFactory {

    protected final RestHighLevelClient client;
    protected final FulltextIndexer indexer;
    
    public ElasticSearchProviderFactory( BlobStoreFactory blobStoreFactory, String host, String uidpwd) {
        String token = Base64.getEncoder().encodeToString( uidpwd.getBytes( StandardCharsets.UTF_8));
        RestClientBuilder builder = RestClient.builder( HttpHost.create( host))
                                            .setDefaultHeaders( new Header[] { new BasicHeader( "Authorization", "Basic " + token)});
        
        client = new RestHighLevelClient( builder);
        indexer = new FulltextIndexer( blobStoreFactory, client);
    }
    
    @Override
    public SearchProvider createSearchProvider(String repositoryName) {
        return new ElasticSearchProvider( client, repositoryName, indexer);
    }

    @Override
    public void shutdown() {
        try {
            indexer.shutdown();
            client.close();
        }
        catch( IOException e) {
            throw new RepositoryException( "Could not shutdown elastic search provider.", e);
        }
    }
}

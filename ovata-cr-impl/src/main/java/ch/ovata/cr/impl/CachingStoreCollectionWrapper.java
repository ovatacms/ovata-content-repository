/*
 * $Id: CachingStoreCollectionWrapper.java 2918 2020-01-08 14:58:39Z dani $
 * Created on 21.04.2017, 18:00:00
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

import ch.ovata.cr.bson.FstSerializer;
import ch.ovata.cr.bson.NullMarker;
import ch.ovata.cr.spi.store.StoreDocument;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.core.statistics.DefaultStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class CachingStoreCollectionWrapper implements StoreCollectionWrapper {

    private static final Logger logger = LoggerFactory.getLogger( CachingStoreCollectionWrapper.class);
    
    private static final String CACHE_NAME= "ovatacr";
    
    private final String repositoryName;
    private final String workspaceName;
    private final StoreCollectionWrapper internal;

    private static final Cache<QueryKey, Serializable> cache;
    private static final DefaultStatisticsService statistics = new DefaultStatisticsService();
    
    static {
        int onheapEntries = Integer.parseInt( System.getProperty( "ovatacr.onheap.cache.size", "512"));
        int offheapSize = Integer.parseInt( System.getProperty( "ovatacr.offheap.cache.size", "256"));
        
        ResourcePoolsBuilder rpb = ResourcePoolsBuilder.newResourcePoolsBuilder().heap( onheapEntries, EntryUnit.ENTRIES).offheap( offheapSize, MemoryUnit.MB);
        CacheConfigurationBuilder ccb = CacheConfigurationBuilder.newCacheConfigurationBuilder( QueryKey.class, Serializable.class, rpb).withValueSerializer( new FstSerializer()).withKeySerializer( new FstSerializer());        
        CacheManager cm = CacheManagerBuilder.newCacheManagerBuilder().withCache( CACHE_NAME, ccb).using( statistics).build( true);

        cache = cm.getCache( CACHE_NAME, QueryKey.class, Serializable.class);
    }
    
    public CachingStoreCollectionWrapper( String repositoryName, String workspaceName, StoreCollectionWrapper wrapper) {
        this.repositoryName = repositoryName;
        this.workspaceName = workspaceName;
        this.internal = wrapper;
    }
    
    public static void clearCache() {
        logger.debug( "Clearing cache.");
        cache.clear();
    }
    
    @Override
    public StoreDocument getById( String id, long revision) {
        QueryKey key = new GetByIdQueryKey( repositoryName, workspaceName, id, revision);
        
        return fromCache( key, () -> this.internal.getById( id, revision));
    }

    @Override
    public StoreDocument getChildByName( String parentUUID, String name, long revision) {
        QueryKey key = new GetChildByNameQueryKey( repositoryName, workspaceName, parentUUID, name, revision);
        
        return fromCache( key, () -> this.internal.getChildByName( parentUUID, name, revision));
    }

    @Override
    public Collection<StoreDocument> getChildren( String parentUUID, long revision) {
        QueryKey key = new GetChildrenQueryKey( repositoryName, workspaceName, parentUUID, revision);
        
        return fromCache( key, () -> this.internal.getChildren( parentUUID, revision));
    }
    
    private <T> T fromCache( QueryKey key, Supplier<T> loader) {
        T result = (T)cache.get( key);
        
        printStats();
        
        if( result == null) {
            result = loader.get();
            
            if( result != null) {
                cache.put( key, (Serializable)result);
            }
            else {
                cache.put( key, NullMarker.EMPTY_RESULT);
            }
            
            return result;
        }
        else {
            return (result == NullMarker.EMPTY_RESULT) ? null : result;
        }
    }

    private static long counter = 0;
    
    private void printStats() {
        counter++;
        
        if( counter % 10000 == 0) {
            CacheStatistics stats = statistics.getCacheStatistics( CACHE_NAME);
        
            logger.info( "Cache hit ratio : {}%, {}/{}.", stats.getCacheHitPercentage(), stats.getCacheHits(), stats.getCacheGets());
        }
    }
   
    public abstract static class QueryKey implements Serializable {
        protected final String repositoryName;
        protected final String workspaceName;
        protected final String id;
        protected final long revision;
        
        public QueryKey( String repositoryName, String workspaceName, String id, long revision) {
            this.repositoryName = repositoryName;
            this.workspaceName = workspaceName;
            this.id = id;
            this.revision = revision;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Objects.hashCode( this.repositoryName);
            hash = 71 * hash + Objects.hashCode(this.workspaceName);
            hash = 71 * hash + Objects.hashCode(this.id);
            hash = 71 * hash + (int) (this.revision ^ (this.revision >>> 32));
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final QueryKey other = (QueryKey) obj;
            if (this.revision != other.revision) {
                return false;
            }
            if (!Objects.equals(this.workspaceName, other.workspaceName)) {
                return false;
            }
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if(!Objects.equals( this.repositoryName, other.repositoryName)) {
                return false;
            }
            return true;
        }
    }
    
    public static class GetChildrenQueryKey extends QueryKey {
        
        public GetChildrenQueryKey( String repositoryName, String workspaceName, String id, long revision) {
            super( repositoryName, workspaceName, id, revision);
        }
        
        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" + "repositoryName=" + repositoryName + ", workspaceName=" + workspaceName + ", id=" + id + ", revision=" + revision + '}';
        }
    }
    
    public static class GetByIdQueryKey extends QueryKey {
        
        public GetByIdQueryKey( String repositoryName, String workspaceName, String id, long revision) {
            super( repositoryName, workspaceName, id, revision);
        }
    }
    
    public static class GetChildByNameQueryKey extends QueryKey {
        
        private final String name;
        
        public GetChildByNameQueryKey( String repositoryName, String workspaceName, String id, String name, long revision) {
            super( repositoryName, workspaceName, id, revision);
            
            this.name = name;
        }
        
        @Override
        public int hashCode() {
            int hash = super.hashCode();
            
            hash = 71 * hash + Objects.hashCode(this.name);

            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if( !super.equals( obj)) {
                return false;
            }
            
            return Objects.equals(this.name, ((GetChildByNameQueryKey)obj).name);
        }
    }
}

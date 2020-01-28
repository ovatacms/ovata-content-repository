/*
 * $Id: LocalConcurrencyControl.java 679 2017-02-25 10:28:00Z dani $
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
package ch.ovata.cr.impl.concurrency;

import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeLockedException;
import ch.ovata.cr.api.RepositoryException;
import ch.ovata.cr.api.security.UserPrincipal;
import ch.ovata.cr.impl.CachingStoreCollectionWrapper;
import ch.ovata.cr.spi.store.ConcurrencyControl;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class LocalConcurrencyControlFactory implements ConcurrencyControlFactory {

    private final Map<String, AtomicLong> generators = new ConcurrentHashMap<>();
    private final Map<String, Lock> locks = new HashMap<>();
    
    @Override
    public ConcurrencyControl setupRepository( String repositoryName, LongSupplier initialRevisionGenerator) throws InterruptedException {
        return new LocalConcurrencyControl( getLock( repositoryName), getRevisionGenerator( repositoryName, initialRevisionGenerator));
    }
    
    @Override
    public void shutdown() {
        // nothing to release
    }

    @Override
    public <T> T unwrap(Class<T> c) {
        return null;
    }

    public static class LocalConcurrencyControl implements ConcurrencyControl {
        
        private static final Logger logger = LoggerFactory.getLogger( LocalConcurrencyControl.class);
        
        private final Lock lock;
        private final AtomicLong revisionGenerator;
        private final Map<String, LockInfo> locks = new ConcurrentHashMap<>();
        
        public LocalConcurrencyControl( Lock lock, AtomicLong revisionGenerator) {
            this.lock = lock;
            this.revisionGenerator = revisionGenerator;
        }
        
        @Override
        public long nextRevision() {
            return this.revisionGenerator.addAndGet( 2);
        }

        @Override
        public long currentRevision() {
            return this.revisionGenerator.get();
        }

        @Override
        public boolean acquireLock() throws InterruptedException {
            return this.lock.tryLock( 5000, TimeUnit.MILLISECONDS);
        }

        @Override
        public void releaseLock() {
            this.lock.unlock();
        }

        @Override
        public void signalClearCache() {
            logger.info( "Clearing caches.");
            CachingStoreCollectionWrapper.clearCache();
        }

        @Override
        public void lockNode(Node node) {
            String key = LockInfoImpl.getKey( node);
            LockInfo info = locks.get( key);
            UserPrincipal user = (UserPrincipal)node.getSession().getRepository().getPrincipal();
            
            if( (info != null) && !info.getPrincipal().equals( user)) {
                throw new NodeLockedException( "Node <" + node.getId() + "> is already locked by <" + user.getName() + ">.", info.getPrincipal().getName());
            }
            
            locks.put( key, new LockInfoImpl( node));
        }

        @Override
        public void releaseNodeLock(Node node) {
            String key = LockInfoImpl.getKey( node);
            LockInfo info = locks.get( key);
            UserPrincipal user = (UserPrincipal)node.getSession().getRepository().getPrincipal();
            
            if( (info != null) && user.equals( info.getPrincipal())) {
                locks.remove( key);
            }
            else {
                throw new RepositoryException( "You are not the owner of the lock.");
            }
        }

        @Override
        public Optional<LockInfo> getNodeLockInfo(Node node) {
            String key = LockInfoImpl.getKey( node);
            
            return Optional.ofNullable( this.locks.get( key));
        }

        @Override
        public void breakNodeLock(Node node) {
            String key = LockInfoImpl.getKey( node);
            
            this.locks.remove( key);
        }
    }

    private synchronized Lock getLock( String name) {
        return this.locks.computeIfAbsent( name, k -> new ReentrantLock());
    }
    
    private AtomicLong getRevisionGenerator( String repositoryName, LongSupplier initialRevisionGenerator) {
        return this.generators.computeIfAbsent( repositoryName, k -> this.createRevisionGenerator( initialRevisionGenerator));
    }
    
    private AtomicLong createRevisionGenerator( LongSupplier initialRevisionGenerator) {
        AtomicLong generator = new AtomicLong();
        
        generator.set( initialRevisionGenerator.getAsLong());
        
        return generator;
    }
}

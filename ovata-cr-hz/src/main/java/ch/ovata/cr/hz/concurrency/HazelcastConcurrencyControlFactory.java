/*
 * $Id: HazelcastConcurrencyControlFactory.java 2858 2019-12-02 13:24:29Z dani $
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
package ch.ovata.cr.hz.concurrency;

import ch.ovata.cr.api.LockInfo;
import ch.ovata.cr.api.Node;
import ch.ovata.cr.api.NodeLockedException;
import ch.ovata.cr.impl.CachingStoreCollectionWrapper;
import ch.ovata.cr.impl.concurrency.LockInfoImpl;
import ch.ovata.cr.spi.store.ConcurrencyControl;
import ch.ovata.cr.spi.store.ConcurrencyControlFactory;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dani
 */
public class HazelcastConcurrencyControlFactory implements ConcurrencyControlFactory {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastConcurrencyControlFactory.class);

    private static final String REVISION_COUNTER_NAME = "ovata-repo-revision-counter";
    private static final String OVATA_SIGNALING_TOPIC = "ovata-signaling";
    private static final String OVATA_CLEAR_CACHE_SIGNAL = "ovata-clear-cache";
    
    private final HazelcastInstance hazelcast;

    public HazelcastConcurrencyControlFactory( String clusterName) {
        logger.info( "Creating hazelcast instance for cluster {}.", clusterName);
        
        hazelcast = Hazelcast.newHazelcastInstance();
    }
    
    @Override
    public ConcurrencyControl setupRepository( String repositoryName, LongSupplier initialRevisionGenerator) throws InterruptedException {
        ConcurrencyControl cc = new HazelcastConcurrencyControl( getLock( repositoryName), getRevisionGenerator( repositoryName), initialRevisionGenerator, getSignalingTopic(), getNodeLockMap( repositoryName));
        
        logger.info( "Initialized concurrency control for repository {}. At Revision : {}.", repositoryName, cc.currentRevision());
        
        return cc;
    }
    
    @Override
    public void shutdown() {
        hazelcast.shutdown();
    }

    private ILock getLock( String name) {
        return this.hazelcast.getLock( name);
    }

    private IMap getNodeLockMap( String repositoryName) {
        return this.hazelcast.getMap( "node-locks-" + repositoryName);
    }
    
    private ITopic<String> getSignalingTopic() {
        return this.hazelcast.getTopic( OVATA_SIGNALING_TOPIC);
    }
    
    private IAtomicLong getRevisionGenerator( String repositoryName) {
        return this.hazelcast.getAtomicLong( repositoryName + "#" + REVISION_COUNTER_NAME);    
    }

    @Override
    public <T> T unwrap(Class<T> c) {
        if( HazelcastInstance.class.equals( c)) {
            return (T)hazelcast;
        }
        
        return null;
    }

    public static class HazelcastConcurrencyControl implements ConcurrencyControl {
        
        private static final Logger logger = LoggerFactory.getLogger( HazelcastConcurrencyControl.class);
        
        private final ILock lock;
        private final IAtomicLong revisionGenerator;
        private final ITopic<String> signaling;
        private final IMap<String, LockInfo> nodeLocks;
        
        public HazelcastConcurrencyControl( ILock lock, IAtomicLong revisionGenerator, LongSupplier initialRevisionGenerator, ITopic<String> signaling, IMap<String, LockInfo> nodeLocks) {
            this.lock = lock;
            this.revisionGenerator = revisionGenerator;
            this.signaling = signaling;
            this.revisionGenerator.compareAndSet( 0, initialRevisionGenerator.getAsLong());
            this.signaling.addMessageListener( this::onSignal);
            this.nodeLocks = nodeLocks;
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
            logger.info( "Publishing clear cache signal.");
            
            signaling.publish( OVATA_CLEAR_CACHE_SIGNAL);
        }
        
        private void onSignal( Message<String> signal) {
            logger.info( "Received signal <{}>.", signal.getMessageObject());
            
            if( OVATA_CLEAR_CACHE_SIGNAL.equals( signal.getMessageObject())) {
                CachingStoreCollectionWrapper.clearCache();
            }
        }

        @Override
        public void lockNode(Node node) {
            String key = LockInfoImpl.getKey( node);
            LockInfo info = this.nodeLocks.get( key);
            
            if( (info != null) &&  !info.getPrincipal().equals( node.getSession().getRepository().getPrincipal())) {
                throw new NodeLockedException( "Node <" + node.getId() + "> is already locked by <" + info.getPrincipal().getName() + ">.", info.getPrincipal().getName());
            }
            
            this.nodeLocks.put( key, new LockInfoImpl( node), 12, TimeUnit.HOURS);
        }

        @Override
        public void releaseNodeLock( Node node) {
            String key = LockInfoImpl.getKey( node);

            this.nodeLocks.remove( key);
        }

        @Override
        public Optional<LockInfo> getNodeLockInfo(Node node) {
            String key = LockInfoImpl.getKey( node);
            
            return Optional.ofNullable( this.nodeLocks.get( key));
        }

        @Override
        public void breakNodeLock(Node node) {
            String key = LockInfoImpl.getKey( node);
            
            this.nodeLocks.remove( key);
        }
    }
}

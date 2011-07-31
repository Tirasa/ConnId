/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.     
 * 
 * The contents of this file are subject to the terms of the Common Development 
 * and Distribution License("CDDL") (the "License").  You may not use this file 
 * except in compliance with the License.
 * 
 * You can obtain a copy of the License at 
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api.local;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;


public class ObjectPool<T> {

    private static final Log _log = Log.getLog(ObjectPool.class);
    
    /**
     * Statistics bean
     */
    public static final class Statistics {
        private final int _numIdle;
        private final int _numActive;
        
        private Statistics(int numIdle, int numActive) {
            _numIdle = numIdle;
            _numActive = numActive;
        }
        
        /**
         * Returns the number of idle objects
         */
        public int getNumIdle() {
            return _numIdle;
        }
        
        /**
         * Returns the number of active objects
         */
        public int getNumActive() {
            return _numActive;
        }
    }
    
    /**
     * An object plus additional book-keeping
     * information about the object
     */
    private static class PooledObject<T> {
        /**
         * The underlying object 
         */
        private final T _object;
        
        /**
         * True if this is currently active, false if
         * it is idle
         */
        private boolean _isActive;
        
        /**
         * Last state change (change from active to
         * idle or vice-versa)
         */
        private long _lastStateChangeTimestamp;
        
        /**
         * Is this a freshly created object (never been pooled)?
         */
        private boolean _isNew;
        
        public PooledObject(T object) {
            _object = object;
            _isNew = true;
            touch();
        }
        
        public T getObject() {
            return _object;
        }
        
        public boolean isNew() {
            return _isNew;
        }
        
        public void setNew(boolean n) {
            _isNew = n;
        }
        
        public void setActive(boolean v) {
            if (_isActive != v) {
                touch();
                _isActive = v;
            }
        }
        
        private void touch() {
            _lastStateChangeTimestamp = System.currentTimeMillis();
        }
        
        public long getLastStateChangeTimestamp() {
            return _lastStateChangeTimestamp;
        }
    }
    
    /**
     * The lock object we use for everything
     */
    private final Object LOCK = new Object();
    
    /**
     * Map from the object to the
     * PooledObject (use IdentityHashMap so it's
     * always object equality)
     */
    private final Map<T,PooledObject<T>>
        _activeObjects = new IdentityHashMap<T, PooledObject<T>>();
    
    /**
     * Queue of idle objects. The one that has
     * been idle for the longest comes first in the queue
     */
    private final LinkedList<PooledObject<T>>
        _idleObjects = new LinkedList<PooledObject<T>>();
    
    /**
     * ObjectPoolHandler we use for managing object lifecycle
     */
    private final ObjectPoolHandler<T> _handler;
    
    /**
     * Configuration for this pool.
     */
    private final ObjectPoolConfiguration _config;
    
    /**
     * Is the pool shutdown
     */
    private boolean _isShutdown;
    
    /**
     * Create a new ObjectPool
     * @param handler Handler for objects
     * @param config Configuration for the pool
     */
    public ObjectPool(ObjectPoolHandler<T> handler,
            ObjectPoolConfiguration config) {
        
        Assertions.nullCheck(handler, "handler");
        Assertions.nullCheck(config, "config");
        
        _handler = handler;
        //clone it
        _config = 
            (ObjectPoolConfiguration)SerializerUtil.cloneObject(config);
        //validate it
        _config.validate();
        
    }
    
    /**
     * Return an object to the pool
     * @param object
     */
    public void returnObject(T object) {
        Assertions.nullCheck(object, "object");
        synchronized (LOCK) {
            //remove it from the active list
            PooledObject<T> pooled =
                _activeObjects.remove(object);
            
            //they are attempting to return something
            //we haven't allocated (or that they've
            //already returned)
            if ( pooled == null ) {
                throw new IllegalStateException("Attempt to return an object not in the pool: "+object);
            }
            
            //set it to idle and add to idle list
            //(this might get evicted right away
            //by evictIdleObjects if we're over the
            //limit or if we're shutdown)
            pooled.setActive(false);
            pooled.setNew(false);
            _idleObjects.add(pooled);
            
            //finally evict idle objects
            evictIdleObjects();
            
            //wake anyone up who was waiting on a object
            LOCK.notifyAll();
        }
    }
    
    /**
     * Borrow an object from the pool.
     * @return An object
     */
    public T borrowObject() {
        while ( true ) {
            PooledObject<T> rv = borrowObjectNoTest();
            try {
                //make sure we are testing it outside
                //of synchronization. otherwise this
                //can create an IO bottleneck
                assert !Thread.holdsLock(LOCK);
                _handler.testObject(rv.getObject());
                return rv.getObject();
            }
            catch (Exception e) {
                //it's bad - remove from active objects
                synchronized (LOCK) {
                    _activeObjects.remove(rv.getObject());
                }
                disposeNoException(rv.getObject());
                //if it's a new object, break out of the loop
                //immediately
                if ( rv.isNew() ) {
                    throw ConnectorException.wrap(e);
                }
            }
        }
    }
    
    /**
     * Borrow an object from the pool, but don't test
     * it (it gets tested by the caller *outside* of
     * synchronization)
     * @return the object
     */
    private PooledObject<T> borrowObjectNoTest() {        
        //time when the call began
        final long startTime = System.currentTimeMillis();
        
        synchronized (LOCK) {
            evictIdleObjects();
            while ( true ) {
                if (_isShutdown) {
                    throw new IllegalStateException("Object pool already shutdown");
                }
                
                PooledObject<T> pooledConn = null;
                
                //first try to recycle an idle object
                if (_idleObjects.size() > 0) {
                    pooledConn = _idleObjects.removeFirst();
                }
                //otherwise, allocate a new object if
                //below the limit
                else if (_activeObjects.size() < _config.getMaxObjects()) {
                    pooledConn =
                        new PooledObject<T>(_handler.newObject());
                }
                
                //if there's an object available, return it
                //and break out of the loop
                if ( pooledConn != null ) {
                    pooledConn.setActive(true);
                    _activeObjects.put(pooledConn.getObject(), 
                            pooledConn);
                    return pooledConn;
                }
                
                //see if we haven't timed-out yet
                final long elapsed =
                    System.currentTimeMillis() - startTime;
                final long remaining = _config.getMaxWait() - elapsed;

                //wait if we haven't timed out
                if (remaining > 0) {
                    try {
                        LOCK.wait(remaining);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ConnectorException(e);
                    }
                }
                else {
                    //otherwise throw
                    throw new ConnectorException("Max objects exceeded");
                }
            }
        }
    }
    
    /**
     * Closes any idle objects in the pool.
     * Existing active objects will remain alive and
     * be allowed to shutdown gracefully, but no more 
     * objects will be allocated.
     */
    public void shutdown() {
        synchronized(LOCK) {
            _isShutdown = true;
            //just evict idle objects
            //if there are any active objects still
            //going, leave them alone so they can return
            //gracefully
            evictIdleObjects();
            //wake anyone up who was waiting on an object
            LOCK.notifyAll();
        }
    }
    
    /**
     * Gets a snapshot of the pool's stats at a point in time.
     * @return The statistics
     */
    public Statistics getStatistics() {
        synchronized(LOCK) {
            return new Statistics(_idleObjects.size(),
                    _activeObjects.size());
        }
    }
    
    /**
     * Evicts idle objects as needed (evicts
     * all idle objects if we're shutdown)
     */
    private void evictIdleObjects() {      
        assert Thread.holdsLock(LOCK);
        while (tooManyIdleObjects()) {
            PooledObject<T> conn = _idleObjects.removeFirst();
            disposeNoException(conn.getObject());
        }
    }
    
    /**
     * Returns true if any of the following are true:
     * <ol>
     *    <li>We're shutdown and there are idle objects</li>
     *    <li>Max idle objects exceeded</li>
     *    <li>Min idle objects exceeded and there are old objects</li>
     * </ol>
     */
    private boolean tooManyIdleObjects() {
        assert Thread.holdsLock(LOCK);
        
        if (_isShutdown && _idleObjects.size() > 0) {
            return true;
        }
        
        if (_config.getMaxIdle() < _idleObjects.size()) {
            return true;
        }
        if (_config.getMinIdle() >= _idleObjects.size()) {
            return false;
        }
        
        PooledObject<T> oldest =
            _idleObjects.getFirst();
        
        long age = 
            ( System.currentTimeMillis()-oldest.getLastStateChangeTimestamp() );
        

        return age > _config.getMinEvictableIdleTimeMillis();
    }
    
    /**
     * Dispose of an object, but don't throw any exceptions
     * @param object
     */
    private void disposeNoException(T object) {
        try {
            _handler.disposeObject(object);
        }
        catch (Exception e) {
            _log.warn(e, "disposeObject() is not supposed to throw");
        }
    }
}

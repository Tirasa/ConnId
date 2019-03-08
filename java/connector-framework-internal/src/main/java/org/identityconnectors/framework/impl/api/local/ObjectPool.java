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
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2015-2019 Evolveum
 */

package org.identityconnectors.framework.impl.api.local;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class ObjectPool<T> {

    private static final Log LOG = Log.getLog(ObjectPool.class);

    /**
     * Statistics bean
     */
    public static final class Statistics {
        private final int numIdle;
        private final int numActive;

        private Statistics(final int numIdle, final int numActive) {
            this.numIdle = numIdle;
            this.numActive = numActive;
        }

        /**
         * Returns the number of idle objects
         */
        public int getNumIdle() {
            return numIdle;
        }

        /**
         * Returns the number of active objects
         */
        public int getNumActive() {
            return numActive - numIdle;
        }

        @Override
        public String toString() {
            return "Statistics(numIdle=" + numIdle + ", numActive=" + numActive + ")";
        }
        
    }

    /**
     * An object plus additional book-keeping information about the object
     */
    private class PooledObject implements ObjectPoolEntry<T> {
        /**
         * The underlying object
         */
        private final T object;

        /**
         * True if this is currently active, false if it is idle
         */
        private boolean isActive;

        /**
         * Last state change (change from active to idle or vice-versa)
         */
        private long lastStateChangeTimestamp;

        /**
         * Is this a freshly created object (never been pooled)?
         */
        private boolean isNew;

        public PooledObject(final T object) {
            this.object = object;
            isNew = true;
            touch();
        }

        @Override
        public T getPooledObject() {
            return object;
        }

        @Override
        public void close() throws IOException {
            try {
                returnObject(this);
            } catch (InterruptedException e) {
                LOG.error(e, "Failed to close/dispose PooledObject object {0} from pool {1}: {2}", this, getPoolName(), e.getMessage());
            }
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(final boolean n) {
            isNew = n;
        }

        public void setActive(final boolean v) {
            if (isActive != v) {
                touch();
                isActive = v;
            }
        }

        private void touch() {
            lastStateChangeTimestamp = System.currentTimeMillis();
        }

        public boolean isOlderThan(long maxAge) {
            return maxAge < (System.currentTimeMillis() - lastStateChangeTimestamp);
        }

        @Override
        public String toString() {
            return "PooledObject(object=" + object + ", isActive=" + isActive + ", lastStateChangeTimestamp="
                + lastStateChangeTimestamp + ", isNew=" + isNew + ")";
        }

    }

    /**
     * Set contains all the PooledObject was made by this pool. It contains all
     * idle and borrowed(active) objects.
     */
    private Set<PooledObject> activeObjects;

    /**
     * Queue of idle objects. The one that has been idle for the longest comes
     * first in the queue
     */
    private final ConcurrentLinkedQueue<PooledObject> idleObjects =
            new ConcurrentLinkedQueue<PooledObject>();

    /**
     * Limits the maximum available pooled object in the pool.
     */
    private Semaphore totalPermit;

    /**
     * Lock to maintain the state changes of the pool.
     */
    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * ObjectPoolHandler we use for managing object lifecycle
     */
    private final ObjectPoolHandler<T> handler;

    /**
     * Configuration for this pool.
     */
    private final ObjectPoolConfiguration poolConfiguration;
    
    /**
     * Human readable name for the pool. Used for diagnostics.
     */
    private String poolName;

    /**
     * Is the pool shutdown
     */
    private volatile boolean isShutdown = false;
    
    private volatile boolean isDisposing = false;

    /**
     * Create a new ObjectPool
     *
     * @param handler
     *            Handler for objects
     * @param config
     *            Configuration for the pool
     */
    public ObjectPool(final ObjectPoolHandler<T> handler, final ObjectPoolConfiguration config) {

        Assertions.nullCheck(handler, "handler");
        Assertions.nullCheck(config, "config");

        this.handler = handler;
        // clone it
        poolConfiguration = this.handler.validate(config);
        activeObjects = new HashSet<PooledObject>(poolConfiguration.getMaxObjects());
        totalPermit = new Semaphore(poolConfiguration.getMaxObjects());
    }

    /**
     * Get the state of the pool.
     *
     * @return true if the {@link #shutdown()} method was called before.
     */
    public boolean isShutdown() {
        return isShutdown;
    }

    public boolean isDisposing() {
        return isDisposing;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

	/**
     * Return an object to the pool
     *
     * @param pooled
     */
    private void returnObject(PooledObject pooled) throws InterruptedException {
        if (isShutdown() || isDisposing() || poolConfiguration.getMaxIdle() < 1) {
            dispose(pooled);
            LOG.ok("Returned object to pool {0}, disposing immediately: {1}", getPoolName(), pooled);
        } else {
            try {
                for (PooledObject entry : idleObjects) {
                    if ((poolConfiguration.getMaxIdle() <= idleObjects.size())
                            || entry.isOlderThan(poolConfiguration.getMinEvictableIdleTimeMillis())) {
                        if (idleObjects.remove(entry)) {
                            dispose(entry);
                            LOG.ok("Disposed pool {0} entry (expired): {1}", getPoolName(), entry);
                        }
                    }
                }
            } finally {
                pooled.setActive(false);
                pooled.setNew(false);
                idleObjects.add(pooled);
                signalNotEmpty();
            }
        }
        LOG.ok("returned object to {0}: {1}", this, pooled);
    }

    /**
     * Borrow an object from the pool.
     *
     * @return An object
     */
    public ObjectPoolEntry<T> borrowObject() {
        PooledObject rv = null;
        try {
            do {
                rv = borrowObjectNoTest();
                try {
                    handler.testObject(rv.getPooledObject());
                } catch (Exception e) {
                    if (null != rv) {
                        dispose(rv);
                        // if it's a new object, break out of the loop
                        // immediately
                        if (rv.isNew()) {
                            throw ConnectorException.wrap(e);
                        }
                        rv = null;
                    }
                }
            } while (null == rv);
            rv.setActive(true);
        } catch (InterruptedException e) {
            LOG.error(e, "Failed to borrow object from pool.");
            throw ConnectorException.wrap(e);
        }
        LOG.ok("Borrowed object from pool {0}: {1}", this, rv);
        return rv;
    }

    /**
     * Borrow an object from the pool, but don't test it (it gets tested by the
     * caller *outside* of synchronization)
     *
     * @return the object
     */
    private PooledObject borrowObjectNoTest() throws InterruptedException {
        if (isShutdown()) {
            throw new IllegalStateException("Object pool already shutdown");
        }

        // First borrow from the idle pool
        PooledObject pooledConn = borrowIdleObject();
        if (null == pooledConn) {
            long nanos = TimeUnit.SECONDS.toNanos(poolConfiguration.getMaxWait());
            final ReentrantLock lock = this.takeLock;
            lock.lockInterruptibly();
            try {
                do {
                    if (totalPermit.tryAcquire()) {
                    	try {
	                        // If the pool is empty and there are available permits
	                        // then create a new instance.
	                        return makeObject();
                    	} catch (RuntimeException e) {
                    		totalPermit.release();
                    		throw e;
                    	} catch (Error e) {
                    		totalPermit.release();
                    		throw e;
                    	}
                    } else {
                        // Wait for permit or object to became available
                        try {
                            nanos = notEmpty.awaitNanos(nanos);
                        } catch (InterruptedException ie) {
                            notEmpty.signal(); // propagate to non-interrupted
                                               // thread
                            throw ConnectorException.wrap(ie);
                        }

                        if (nanos <= 0) {
                            throw new ConnectorException("TimeOut");
                        }
                        // Try to borrow from the idle pool
                        pooledConn = borrowIdleObject();
                        if (null != pooledConn) {
                            return pooledConn;
                        }
                    }
                } while (nanos > 0);
            } finally {
                lock.unlock();
            }
        }
        return pooledConn;
    }

    /**
     * Polls the head object from the queue.
     * <p/>
     * Polls the head object and before it returns it checks the {@code MaxIdle}
     * size and the {@code MinEvictableIdleTime} before accepts the object.
     *
     * @return null if there was no fresh/new object in the queue.
     * @throws InterruptedException
     */
    private PooledObject borrowIdleObject() throws InterruptedException {
        for (PooledObject pooledConn = idleObjects.poll(); pooledConn != null; pooledConn =
                idleObjects.poll()) {
            int size = idleObjects.size();
            if (poolConfiguration.getMinIdle() < size + 1
                    && ((poolConfiguration.getMaxIdle() < size) || pooledConn
                            .isOlderThan(poolConfiguration.getMinEvictableIdleTimeMillis()))) {
                dispose(pooledConn);
            } else {
                return pooledConn;
            }
        }
        return null;
    }
    
    /**
     * Disposes all objects in the pool.
     * <p/>
     * Existing active objects will remain alive and be allowed to shutdown
     * gracefully. Unlike shutdown, the pool will be able to work on and create
     * new objects.
     */
    public void disposeAllObjects() {
    	final ReentrantLock lock = this.takeLock;
        try {
        	lock.lockInterruptibly();
        	
        	isDisposing = true;
    		LOG.ok("Disposing all objects from {0}", this);
	        // just evict idle objects
	        // if there are any active objects still
	        // going, leave them alone so they can return
	        // gracefully
	        for (PooledObject entry = idleObjects.poll(); entry != null; entry = idleObjects.poll()) {
	            try {
	                dispose(entry);
	            } catch (InterruptedException e) {
	                LOG.error(e, "Interrupted disposal of PooledObject object {0}", entry);
	            }
	        }
	        
        } catch (Exception e) {
            LOG.warn(e, "Error disposing of all objects from pool {0}: {1}", this, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Closes any idle objects in the pool.
     * <p/>
     * Existing active objects will remain alive and be allowed to shutdown
     * gracefully, but no more objects will be allocated.
     */
    public void shutdown() {
        isShutdown = true;
        try {
        	disposeAllObjects();
        } finally {
            handler.shutdown();
        }
    }

    /**
     * Gets a snapshot of the pool's stats at a point in time.
     *
     * @return The statistics
     */
    public Statistics getStatistics() {
        return new Statistics(idleObjects.size(), activeObjects.size());
    }

    /**
     * This is a long running process to create and init the connector instance.
     * <p/>
     *
     * @throws ConnectorException
     *             if something happens.
     */
    private PooledObject makeObject() {
        synchronized (activeObjects) {
            PooledObject pooledConn =
                    new PooledObject((activeObjects.size() > 0) ? handler.makeObject() : handler
                            .makeObject());
            activeObjects.add(pooledConn);
            return pooledConn;
        }
    }

    /**
     * Dispose of an object, but don't throw any exceptions
     *
     * @param entry
     */
    private void dispose(final PooledObject entry) throws InterruptedException {
        final ReentrantLock lock = this.takeLock;
        lock.lockInterruptibly();
        try {
            synchronized (activeObjects) {
                // Make sure the disposed object was the last item in the
                // activeObjects
                if (activeObjects.remove(entry) && activeObjects.isEmpty()) {
                    handler.disposeObject(entry.getPooledObject());
                } else {
                    handler.disposeObject(entry.getPooledObject());
                }
            }
        } catch (Exception e) {
            LOG.warn(e, "Unexpected error from disposeObject() method: {0}", e.getMessage());
        } finally {
            totalPermit.release();
            notEmpty.signal();
            lock.unlock();
        }
    }

    /**
     * Signals a waiting take. Called only from borrowObjectNoTest
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ObjectPool(");
		sb.append(poolName);
		if (isDisposing) {
			sb.append(", DISPOSING");
		}
		if (isShutdown) {
			sb.append(", SHUTTING DOWN");
		}
		sb.append(", idle=").append(idleObjects.size());
		sb.append(", active=").append(activeObjects.size());
		sb.append(")");
		return sb.toString();
	}
    
}

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
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.framework.impl.api.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.local.ObjectPool.Statistics;
import org.junit.jupiter.api.Test;

public class ObjectPoolTests {

    private class MyTestConnection {

        private boolean _isGood = true;

        @Test
        public void test() {
            if (!_isGood) {
                throw new ConnectorException("Connection is bad");
            }
        }

        public void dispose() {
            _isGood = false;
        }

        public boolean isGood() {
            return _isGood;
        }
    }

    private class MyTestConnectionFactory implements ObjectPoolHandler<MyTestConnection> {

        private boolean _createBadConnection = false;

        private int _totalCreatedConnections = 0;

        @Override
        public ObjectPoolConfiguration validate(ObjectPoolConfiguration original) {
            ObjectPoolConfiguration configuration = (ObjectPoolConfiguration) SerializerUtil.cloneObject(original);
            //validate it
            configuration.validate();
            return configuration;
        }

        @Override
        public MyTestConnection makeObject() {
            _totalCreatedConnections++;
            MyTestConnection rv = new MyTestConnection();
            if (_createBadConnection) {
                rv.dispose();
            }
            return rv;
        }

        @Test
        @Override
        public void testObject(MyTestConnection object) {
            object.test();
        }

        @Override
        public void disposeObject(MyTestConnection object) {
            object.dispose();
        }

        public int getTotalCreatedConnections() {
            return _totalCreatedConnections;
        }

        public void setCreateBadConnection(boolean v) {
            _createBadConnection = v;
        }

        @Override
        public void shutdown() {
        }
    }

    private class MyTestThread extends Thread {

        private final ObjectPool<MyTestConnection> _pool;

        private final int _numIterations;

        private Exception _exception;

        public MyTestThread(ObjectPool<MyTestConnection> pool,
                int numIterations) {
            _pool = pool;
            _numIterations = numIterations;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < _numIterations; i++) {
                    try (ObjectPoolEntry<MyTestConnection> con = _pool.borrowObject()) {
                        Thread.sleep(300);
                    }
                }
            } catch (Exception e) {
                _exception = e;
            }
        }

        public void shutdown() throws Exception {
            join();
            if (_exception != null) {
                throw _exception;
            }
        }

    }

    @Test
    public void testWithManyThreads() throws Exception {
        final int NUM_ITERATIONS = 10;
        final int NUM_THREADS = 10;
        final int MAX_CONNECTIONS = NUM_THREADS - 3; //make sure we get some waiting
        ObjectPoolConfiguration config = new ObjectPoolConfiguration();
        config.setMaxObjects(MAX_CONNECTIONS);
        config.setMaxIdle(MAX_CONNECTIONS);
        config.setMinIdle(MAX_CONNECTIONS);
        config.setMinEvictableIdleTimeMillis(60 * 1000);
        config.setMaxWait(60 * 1000);
        MyTestConnectionFactory fact = new MyTestConnectionFactory();

        ObjectPool<MyTestConnection> pool = new ObjectPool<>(fact, config);

        MyTestThread[] threads = new MyTestThread[NUM_THREADS];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MyTestThread(pool, NUM_ITERATIONS);
            threads[i].start();
        }

        for (MyTestThread thread : threads) {
            thread.shutdown();
        }

        //these should be the same since we never
        //should have disposed anything
        assertEquals(MAX_CONNECTIONS, fact.getTotalCreatedConnections());
        Statistics stats = pool.getStatistics();
        assertEquals(stats.getNumActive(), 0);
        assertEquals(MAX_CONNECTIONS, stats.getNumIdle());

        pool.shutdown();
        stats = pool.getStatistics();
        assertEquals(0, stats.getNumActive());
        assertEquals(0, stats.getNumIdle());

    }

    @Test
    public void testBadConnection() throws Exception {
        final int MAX_CONNECTIONS = 3;
        ObjectPoolConfiguration config = new ObjectPoolConfiguration();
        config.setMaxObjects(MAX_CONNECTIONS);
        config.setMaxIdle(MAX_CONNECTIONS);
        config.setMinIdle(MAX_CONNECTIONS);
        config.setMinEvictableIdleTimeMillis(60 * 1000);
        config.setMaxWait(60 * 1000);
        MyTestConnectionFactory fact = new MyTestConnectionFactory();

        ObjectPool<MyTestConnection> pool = new ObjectPool<>(fact, config);

        //borrow first connection and return
        ObjectPoolEntry<MyTestConnection> conn = pool.borrowObject();
        assertEquals(1, fact.getTotalCreatedConnections());
        conn.close();
        assertEquals(1, fact.getTotalCreatedConnections());

        //re-borrow same connection and return
        conn = pool.borrowObject();
        assertEquals(1, fact.getTotalCreatedConnections());
        conn.close();
        assertEquals(1, fact.getTotalCreatedConnections());

        //dispose and make sure we get a new connection
        conn.getPooledObject().dispose();
        conn = pool.borrowObject();
        assertEquals(2, fact.getTotalCreatedConnections());
        conn.close();
        assertEquals(2, fact.getTotalCreatedConnections());
    }

    @Test
    public void testIdleCleanup() throws Exception {
        ObjectPoolConfiguration config = new ObjectPoolConfiguration();
        config.setMaxObjects(3);
        config.setMaxIdle(2);
        config.setMinIdle(1);
        config.setMinEvictableIdleTimeMillis(1000);
        config.setMaxWait(60 * 1000);
        config.setMaxIdleTimeMillis(2500);
        MyTestConnectionFactory fact = new MyTestConnectionFactory();

        ObjectPool<MyTestConnection> pool = new ObjectPool<>(fact, config);

        ObjectPoolEntry<MyTestConnection> conn1 = pool.borrowObject();
        ObjectPoolEntry<MyTestConnection> conn2 = pool.borrowObject();
        ObjectPoolEntry<MyTestConnection> conn3 = pool.borrowObject();

        assertEquals(3, fact.getTotalCreatedConnections());
        conn1.close();
        assertEquals(1, pool.getStatistics().getNumIdle());
        conn2.close();
        assertEquals(2, pool.getStatistics().getNumIdle());
        conn3.close();
        assertEquals(2, pool.getStatistics().getNumIdle());
        assertEquals(false, conn1.getPooledObject().isGood());
        assertEquals(true, conn2.getPooledObject().isGood());
        assertEquals(true, conn3.getPooledObject().isGood());
        Thread.sleep(config.getMinEvictableIdleTimeMillis() + 500);
        ObjectPoolEntry<MyTestConnection> conn4 = pool.borrowObject();
        assertSame(conn3, conn4);
        assertEquals(0, pool.getStatistics().getNumIdle());
        assertEquals(1, pool.getStatistics().getNumActive());
        assertEquals(false, conn1.getPooledObject().isGood());
        assertEquals(false, conn2.getPooledObject().isGood());
        assertEquals(true, conn3.getPooledObject().isGood());
        assertEquals(true, conn4.getPooledObject().isGood());
        conn4.close();
        assertEquals(1, pool.getStatistics().getNumIdle());
        assertEquals(0, pool.getStatistics().getNumActive());
        // BASE-80
        Thread.sleep(config.getMaxIdleTimeMillis() + 500);
        ObjectPoolEntry<MyTestConnection> conn5 = pool.borrowObject();
        assertNotSame(conn4, conn5);
        assertEquals(false, conn1.getPooledObject().isGood());
        assertEquals(false, conn2.getPooledObject().isGood());
        assertEquals(false, conn3.getPooledObject().isGood());
        assertEquals(false, conn4.getPooledObject().isGood());
        assertEquals(true, conn5.getPooledObject().isGood());
        assertEquals(0, pool.getStatistics().getNumIdle());
        assertEquals(1, pool.getStatistics().getNumActive());
        conn5.close();
        assertEquals(1, pool.getStatistics().getNumIdle());
        assertEquals(0, pool.getStatistics().getNumActive());
    }

    @Test
    public void testCreateBadConnection()
            throws Exception {
        MyTestConnectionFactory fact = new MyTestConnectionFactory();
        fact.setCreateBadConnection(true);

        ObjectPool<MyTestConnection> pool = new ObjectPool<>(fact, new ObjectPoolConfiguration());
        try {
            pool.borrowObject();
            fail("expected exception");
        } catch (ConnectorException e) {
            assertEquals("Connection is bad", e.getMessage());
        }
    }
}

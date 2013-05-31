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
 */
package org.identityconnectors.framework.impl.api.local;

import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.api.local.ObjectPool.Statistics;


public class ObjectPoolTests {

    private class MyTestConnection  {

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

        public ObjectPoolConfiguration validate(ObjectPoolConfiguration original) {
            ObjectPoolConfiguration configuration = (ObjectPoolConfiguration) SerializerUtil.cloneObject(original);
            //validate it
            configuration.validate();
            return configuration;
        }

        public MyTestConnection makeFirstObject() {
            return makeObject();
        }

        public MyTestConnection makeObject() {
            _totalCreatedConnections++;
            MyTestConnection rv = new MyTestConnection();
            if (_createBadConnection) {
                rv.dispose();
            }
            return rv;
        }
        @Test
		public void testObject(MyTestConnection object) {
            object.test();
        }
        public void disposeObject(MyTestConnection object) {
            object.dispose();
        }

        public void disposeLastObject(MyTestConnection object) {
            disposeObject(object);
        }

        public int getTotalCreatedConnections() {
            return _totalCreatedConnections;
        }


        public void setCreateBadConnection(boolean v) {
            _createBadConnection = v;
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
        public void run() {
            try {
                for ( int i = 0; i < _numIterations; i++ ) {
                    ObjectPoolEntry<MyTestConnection> con =
                        _pool.borrowObject();
                    Thread.sleep(300);
                    con.close();
                }
            }
            catch (Exception e) {
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
        final int MAX_CONNECTIONS = NUM_THREADS-3; //make sure we get some waiting
        ObjectPoolConfiguration config = new ObjectPoolConfiguration();
        config.setMaxObjects(MAX_CONNECTIONS);
        config.setMaxIdle(MAX_CONNECTIONS);
        config.setMinIdle(MAX_CONNECTIONS);
        config.setMinEvictableIdleTimeMillis(60*1000);
        config.setMaxWait(60*1000);
        MyTestConnectionFactory fact = new MyTestConnectionFactory();

        ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact,config);

        MyTestThread [] threads = new MyTestThread[NUM_THREADS];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MyTestThread(pool,NUM_ITERATIONS);
            threads[i].start();
        }

        for (MyTestThread thread : threads) {
            thread.shutdown();
        }

        //these should be the same since we never
        //should have disposed anything
        Assert.assertEquals(MAX_CONNECTIONS, fact.getTotalCreatedConnections());
        Statistics stats = pool.getStatistics();
        Assert.assertEquals(stats.getNumActive(), 0);
        Assert.assertEquals(MAX_CONNECTIONS, stats.getNumIdle());

        pool.shutdown();
        stats = pool.getStatistics();
        Assert.assertEquals(0, stats.getNumActive());
        Assert.assertEquals(0, stats.getNumIdle());

    }

    @Test
    public void testBadConnection() throws Exception {
        final int MAX_CONNECTIONS = 3;
        ObjectPoolConfiguration config = new ObjectPoolConfiguration();
        config.setMaxObjects(MAX_CONNECTIONS);
        config.setMaxIdle(MAX_CONNECTIONS);
        config.setMinIdle(MAX_CONNECTIONS);
        config.setMinEvictableIdleTimeMillis(60*1000);
        config.setMaxWait(60*1000);
        MyTestConnectionFactory fact = new MyTestConnectionFactory();

        ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact,config);

        //borrow first connection and return
        ObjectPoolEntry<MyTestConnection> conn = pool.borrowObject();
        Assert.assertEquals(1, fact.getTotalCreatedConnections());
        conn.close();
        Assert.assertEquals(1, fact.getTotalCreatedConnections());

        //re-borrow same connection and return
        conn = pool.borrowObject();
        Assert.assertEquals(1, fact.getTotalCreatedConnections());
        conn.close();
        Assert.assertEquals(1, fact.getTotalCreatedConnections());

        //dispose and make sure we get a new connection
        conn.getPooledObject().dispose();
        conn = pool.borrowObject();
        Assert.assertEquals(2, fact.getTotalCreatedConnections());
        conn.close();
        Assert.assertEquals(2, fact.getTotalCreatedConnections());
    }

    @Test
    public void testIdleCleanup() throws Exception {
        ObjectPoolConfiguration config = new ObjectPoolConfiguration();
        config.setMaxObjects(3);
        config.setMaxIdle(2);
        config.setMinIdle(1);
        config.setMinEvictableIdleTimeMillis(3000);
        config.setMaxWait(60*1000);
        MyTestConnectionFactory fact = new MyTestConnectionFactory();

        ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact,config);

        ObjectPoolEntry<MyTestConnection> conn1 = pool.borrowObject();
        ObjectPoolEntry<MyTestConnection> conn2 = pool.borrowObject();
        ObjectPoolEntry<MyTestConnection> conn3 = pool.borrowObject();

        Assert.assertEquals(3, fact.getTotalCreatedConnections());
        conn1.close();
        Assert.assertEquals(1, pool.getStatistics().getNumIdle());
        conn2.close();
        Assert.assertEquals(pool.getStatistics().getNumIdle(), 2);
        conn3.close();
        Assert.assertEquals(pool.getStatistics().getNumIdle(), 2);
        Assert.assertEquals(false, conn1.getPooledObject().isGood());
        Assert.assertEquals(true, conn2.getPooledObject().isGood());
        Assert.assertEquals(true, conn3.getPooledObject().isGood());
        Thread.sleep(config.getMinEvictableIdleTimeMillis()+1000);
        ObjectPoolEntry<MyTestConnection> conn4 = pool.borrowObject();
        Assert.assertSame(conn3, conn4);
        Assert.assertEquals(false, conn1.getPooledObject().isGood());
        Assert.assertEquals(false, conn2.getPooledObject().isGood());
        Assert.assertEquals(true, conn3.getPooledObject().isGood());
        Assert.assertEquals(true, conn4.getPooledObject().isGood());
    }

    @Test
    public void testCreateBadConnection()
        throws Exception
    {
        MyTestConnectionFactory fact = new MyTestConnectionFactory();
        fact.setCreateBadConnection(true);

        ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact,new ObjectPoolConfiguration());
        try {
            pool.borrowObject();
            Assert.fail("expected exception");
        }
        catch (ConnectorException e) {
            Assert.assertEquals("Connection is bad", e.getMessage());
        }
    }

}

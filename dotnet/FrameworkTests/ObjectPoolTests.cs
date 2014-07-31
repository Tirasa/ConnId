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
 * Portions Copyrighted 2014 ForgeRock AS.
 */
using System;
using System.Threading;
using NUnit.Framework;

using Org.IdentityConnectors.Common.Pooling;
using Org.IdentityConnectors.Framework.Common.Exceptions;
using Org.IdentityConnectors.Framework.Impl.Api.Local;

namespace FrameworkTests
{
    [TestFixture]
    public class ObjectPoolTests
    {
        private class MyTestConnection
        {
            private bool _isGood = true;

            public void Test()
            {
                if (!_isGood)
                {
                    throw new ConnectorException("Connection is bad");
                }
            }

            public void Dispose()
            {
                _isGood = false;
            }

            public bool IsGood
            {
                get
                {
                    return _isGood;
                }
            }
        }

        private class MyTestConnectionFactory : ObjectPoolHandler<MyTestConnection>
        {

            private bool _createBadConnection = false;
            private int _totalCreatedConnections = 0;

            public MyTestConnection MakeObject()
            {
                _totalCreatedConnections++;
                MyTestConnection rv = new MyTestConnection();
                if (_createBadConnection)
                {
                    rv.Dispose();
                }
                return rv;
            }
            public void TestObject(MyTestConnection obj)
            {
                obj.Test();
            }
            public void DisposeObject(MyTestConnection obj)
            {
                obj.Dispose();
            }

            public int TotalCreatedConnections
            {
                get
                {
                    return _totalCreatedConnections;
                }
            }

            public bool CreateBadConnection
            {
                set
                {
                    _createBadConnection = value;
                }
            }

            public ObjectPoolConfiguration Validate(ObjectPoolConfiguration original)
            {
                return original;
            }

            public void Shutdown()
            {
            }
        }

        private class MyTestThread
        {
            private readonly ObjectPool<MyTestConnection> _pool;
            private readonly int _numIterations;
            private Exception _exception;
            private Thread _thisThread;
            public MyTestThread(ObjectPool<MyTestConnection> pool,
                    int numIterations)
            {
                _pool = pool;
                _numIterations = numIterations;
                _thisThread = new Thread(Run);
            }

            public void Start()
            {
                _thisThread.Start();
            }

            public void Run()
            {
                try
                {
                    for (int i = 0; i < _numIterations; i++)
                    {
                        MyTestConnection con =
                            _pool.BorrowObject();
                        Thread.Sleep(300);
                        _pool.ReturnObject(con);
                    }
                }
                catch (Exception e)
                {
                    _exception = e;
                }
            }
            public void Shutdown()
            {
                _thisThread.Join();
                if (_exception != null)
                {
                    throw _exception;
                }
            }

        }

        [Test]
        public void TestWithManyThreads()
        {
            int NUM_ITERATIONS = 10;
            int NUM_THREADS = 10;
            int MAX_CONNECTIONS = NUM_THREADS - 3; //make sure we get some waiting
            ObjectPoolConfiguration config = new ObjectPoolConfiguration();
            config.MaxObjects = (MAX_CONNECTIONS);
            config.MaxIdle = (MAX_CONNECTIONS);
            config.MinIdle = (MAX_CONNECTIONS);
            config.MinEvictableIdleTimeMillis = (60 * 1000);
            config.MaxWait = (60 * 1000);
            MyTestConnectionFactory fact = new MyTestConnectionFactory();

            ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact, config);

            MyTestThread[] threads = new MyTestThread[NUM_THREADS];
            for (int i = 0; i < threads.Length; i++)
            {
                threads[i] = new MyTestThread(pool, NUM_ITERATIONS);
                threads[i].Start();
            }

            foreach (MyTestThread thread in threads)
            {
                thread.Shutdown();
            }

            //these should be the same since we never 
            //should have disposed anything
            Assert.AreEqual(MAX_CONNECTIONS, fact.TotalCreatedConnections);
            ObjectPool<MyTestConnection>.Statistics stats = pool.GetStatistics();
            Assert.AreEqual(0, stats.NumActive);
            Assert.AreEqual(MAX_CONNECTIONS, stats.NumIdle);

            pool.Shutdown();
            stats = pool.GetStatistics();
            Assert.AreEqual(0, stats.NumActive);
            Assert.AreEqual(0, stats.NumIdle);

        }

        [Test]
        public void TestBadConnection()
        {
            int MAX_CONNECTIONS = 3;
            ObjectPoolConfiguration config = new ObjectPoolConfiguration();
            config.MaxObjects = (MAX_CONNECTIONS);
            config.MaxIdle = (MAX_CONNECTIONS);
            config.MinIdle = (MAX_CONNECTIONS);
            config.MinEvictableIdleTimeMillis = (60 * 1000);
            config.MaxWait = (60 * 1000);
            MyTestConnectionFactory fact = new MyTestConnectionFactory();

            ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact, config);

            //borrow first connection and return
            MyTestConnection conn = pool.BorrowObject();
            Assert.AreEqual(1, fact.TotalCreatedConnections);
            pool.ReturnObject(conn);
            Assert.AreEqual(1, fact.TotalCreatedConnections);

            //re-borrow same connection and return
            conn = pool.BorrowObject();
            Assert.AreEqual(1, fact.TotalCreatedConnections);
            pool.ReturnObject(conn);
            Assert.AreEqual(1, fact.TotalCreatedConnections);

            //dispose and make sure we get a new connection
            conn.Dispose();
            conn = pool.BorrowObject();
            Assert.AreEqual(2, fact.TotalCreatedConnections);
            pool.ReturnObject(conn);
            Assert.AreEqual(2, fact.TotalCreatedConnections);
        }

        [Test]
        public void TestIdleCleanup()
        {
            ObjectPoolConfiguration config = new ObjectPoolConfiguration();
            config.MaxObjects = (3);
            config.MaxIdle = (2);
            config.MinIdle = (1);
            config.MinEvictableIdleTimeMillis = (3000);
            config.MaxWait = (60 * 1000);
            MyTestConnectionFactory fact = new MyTestConnectionFactory();

            ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact, config);

            MyTestConnection conn1 = (MyTestConnection)pool.BorrowObject();
            MyTestConnection conn2 = (MyTestConnection)pool.BorrowObject();
            MyTestConnection conn3 = (MyTestConnection)pool.BorrowObject();

            Assert.AreEqual(3, fact.TotalCreatedConnections);
            pool.ReturnObject(conn1);
            Assert.AreEqual(1, pool.GetStatistics().NumIdle);
            pool.ReturnObject(conn2);
            Assert.AreEqual(2, pool.GetStatistics().NumIdle);
            pool.ReturnObject(conn3);
            Assert.AreEqual(2, pool.GetStatistics().NumIdle);
            Assert.AreEqual(false, conn1.IsGood);
            Assert.AreEqual(true, conn2.IsGood);
            Assert.AreEqual(true, conn3.IsGood);
            Thread.Sleep(((int)(config.MinEvictableIdleTimeMillis + 1000)));
            MyTestConnection conn4 = (MyTestConnection)pool.BorrowObject();
            Assert.AreSame(conn3, conn4);
            Assert.AreEqual(false, conn1.IsGood);
            Assert.AreEqual(false, conn2.IsGood);
            Assert.AreEqual(true, conn3.IsGood);
            Assert.AreEqual(true, conn4.IsGood);
        }

        [Test]
        public void TestCreateBadConnection()
        {
            MyTestConnectionFactory fact = new MyTestConnectionFactory();
            fact.CreateBadConnection = (true);

            ObjectPool<MyTestConnection> pool = new ObjectPool<MyTestConnection>(fact, new ObjectPoolConfiguration());
            try
            {
                pool.BorrowObject();
                Assert.Fail("expected exception");
            }
            catch (ConnectorException e)
            {
                Assert.AreEqual("Connection is bad", e.Message);
            }
        }
    }
}

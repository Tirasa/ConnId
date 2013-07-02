/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
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

package org.identityconnectors.common.logging.slf4j;

import org.identityconnectors.common.logging.slf4j.SLF4JLog;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;

import org.identityconnectors.common.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class SLF4JLogTests {

    private static final Logger logger = LoggerFactory.getLogger(SLF4JLogTests.class);

    @Test
    public void testMultithreaded() {
        final SLF4JLog log = new SLF4JLog();
        int size = 100;
        CyclicBarrier barier = new CyclicBarrier(size);
        List<Thread> threads = new ArrayList<Thread>(size);
        Set<String> keys = new HashSet<String>();
        keys.add(SLF4JLogTests.class.getName());
        for (int i = 0; i < 100; i++) {
            String key = "" + i % 10;
            keys.add(key);
            Thread thread = new Thread(new CreateLogger(barier, log, key));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                /* ignore */
            }
        }
        assertEquals(keys, log.getMap().keySet());
    }

    private static class CreateLogger implements Runnable {

        final CyclicBarrier barier;
        final SLF4JLog logger;
        final String key;

        private CreateLogger(CyclicBarrier barier, SLF4JLog logger, String key) {
            this.barier = barier;
            this.logger = logger;
            this.key = key;
        }

        @Override
        public void run() {
            try {
                logger.log(SLF4JLogTests.class, "run", Log.Level.INFO, "Failed with:",
                        new IllegalArgumentException(new RuntimeException("Cause")));
                barier.await();
            } catch (Exception e1) {
                /* ignore */
            }
            logger.getSLF4JLogger(key);
        }
    }
}

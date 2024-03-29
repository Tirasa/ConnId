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
 * Portions Copyrighted 2021 Evolveum
 */
package org.identityconnectors.common.pooling;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for pooling objects.
 */
public final class ObjectPoolConfiguration {

    public ObjectPoolConfiguration() {
    }

    public ObjectPoolConfiguration(ObjectPoolConfiguration other) {
        this.setMaxObjects(other.getMaxObjects());
        this.setMaxIdle(other.getMaxIdle());
        this.setMaxWait(other.getMaxWait());
        this.setMinEvictableIdleTimeMillis(other.getMinEvictableIdleTimeMillis());
        this.setMaxIdleTimeMillis(other.getMaxIdleTimeMillis());
        this.setMinIdle(other.getMinIdle());
    }

    /**
     * Max objects (idle+active).
     */
    private int maxObjects = 10;

    /**
     * Max idle objects.
     */
    private int maxIdle = 10;

    /**
     * Max time to wait if the pool is waiting for a free object to become
     * available before failing. Zero means don't wait.
     */
    private long maxWait = 150 * 1000;

    /**
     * Minimum time to wait before evicting an idle object. Zero means don't
     * wait
     */
    private long minEvictableIdleTimeMillis = 120 * 1000;

    /**
     * Maximum time that an idle object will be kept in the pool (in milliseconds).
     * Connectors will not be re-used if they are kept idle in the pool for longer than this interval.
     * Zero means no time limitation.
     */
    private long maxIdleTimeMillis = 0;

    /**
     * Minimum number of idle objects.
     */
    private int minIdle = 1;

    /**
     * Get the set number of maximum objects (idle+active).
     */
    public int getMaxObjects() {
        return maxObjects;
    }

    /**
     * Sets the maximum number of objects (idle+active).
     */
    public void setMaxObjects(int maxObjects) {
        this.maxObjects = maxObjects;
    }

    /**
     * Get the maximum number of idle objects.
     */
    public int getMaxIdle() {
        return maxIdle;
    }

    /**
     * Sets the maximum number of objects that can sit idle in the pool at any
     * time.
     */
    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    /**
     * Max time to wait if the pool is waiting for a free object to become
     * available before failing. Zero means don't wait.
     */
    public long getMaxWait() {
        return maxWait;
    }

    /**
     * Max time to wait if the pool is waiting for a free object to become
     * available before failing. Zero means don't wait.
     */
    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    /**
     * Minimum time to wait before evicting an idle object. Zero means don't
     * wait.
     */
    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    /**
     * Minimum time to wait before evicting an idle object. Zero means don't
     * wait.
     */
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    /**
     * Maximum time that an idle object will be kept in the pool (in milliseconds).
     * Connectors will not be re-used if they are kept idle in the pool for longer than this interval.
     * Zero means no time limitation.
     */
    public long getMaxIdleTimeMillis() {
        return maxIdleTimeMillis;
    }

    /**
     * Maximum time that an idle object will be kept in the pool (in milliseconds).
     * Connectors will not be re-used if they are kept idle in the pool for longer than this interval.
     * Zero means no time limitation.
     */
    public void setMaxIdleTimeMillis(long maxIdleTimeMillis) {
        this.maxIdleTimeMillis = maxIdleTimeMillis;
    }

    /**
     * Minimum number of idle objects.
     */
    public int getMinIdle() {
        return minIdle;
    }

    /**
     * Minimum number of idle objects.
     */
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public void validate() {
        if (minIdle < 0) {
            throw new IllegalArgumentException("Min idle is less than zero.");
        }
        if (maxObjects < 0) {
            throw new IllegalArgumentException("Max active is less than zero.");
        }
        if (maxIdle < 0) {
            throw new IllegalArgumentException("Max idle is less than zero.");
        }
        if (maxWait < 0) {
            throw new IllegalArgumentException("Max wait is less than zero.");
        }
        if (minEvictableIdleTimeMillis < 0) {
            throw new IllegalArgumentException("Min evictable idle time millis less than zero.");
        }
        if (maxIdleTimeMillis < 0) {
            throw new IllegalArgumentException("Max idle time millis less than zero.");
        }
        if (maxIdleTimeMillis > 0 && maxIdleTimeMillis < minEvictableIdleTimeMillis) {
            throw new IllegalArgumentException("Max idle time millis less than min evictable idle time millis.");
        }
        if (minIdle > maxIdle) {
            throw new IllegalArgumentException("Min idle is greater than max idle.");
        }
        if (maxIdle > maxObjects) {
            throw new IllegalArgumentException("Max idle is greater than max objects.");
        }
    }

    @Override
    public int hashCode() {
        return (int) (getMaxObjects() + getMaxIdle() + getMaxWait()
                + getMinEvictableIdleTimeMillis() + getMinIdle() + getMaxIdleTimeMillis());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObjectPoolConfiguration) {
            ObjectPoolConfiguration other = (ObjectPoolConfiguration) obj;

            if (getMaxObjects() != other.getMaxObjects()) {
                return false;
            }
            if (getMaxIdle() != other.getMaxIdle()) {
                return false;
            }
            if (getMaxWait() != other.getMaxWait()) {
                return false;
            }
            if (getMinEvictableIdleTimeMillis() != other.getMinEvictableIdleTimeMillis()) {
                return false;
            }
            if (getMinIdle() != other.getMinIdle()) {
                return false;
            }
            if (getMaxIdleTimeMillis() != other.getMaxIdleTimeMillis()) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        // poor man's toString()
        Map<String, Object> bld = new LinkedHashMap<String, Object>();
        bld.put("MaxObjects", getMaxObjects());
        bld.put("MaxIdle", getMaxIdle());
        bld.put("MaxWait", getMaxWait());
        bld.put("MinEvictableIdleTimeMillis", getMinEvictableIdleTimeMillis());
        bld.put("MinIdle", getMinIdle());
        bld.put("MaxLifetimeMillis", getMaxIdleTimeMillis());
        return bld.toString();
    }
}

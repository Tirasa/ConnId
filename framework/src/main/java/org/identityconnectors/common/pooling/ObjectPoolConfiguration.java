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
package org.identityconnectors.common.pooling;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for pooling objects
 */
public final class ObjectPoolConfiguration {

    /**
     * Max objects (idle+active). 
     */
    private int _maxObjects = 10;

    /**
     * Max idle objects.
     */
    private int _maxIdle = 10;

    /**
     * Max time to wait if the pool is waiting for a free object to become
     * available before failing. Zero means don't wait
     */
    private long _maxWait = 150 * 1000;

    /**
     * Minimum time to wait before evicting an idle object.
     * Zero means don't wait
     */
    private long _minEvictableIdleTimeMillis = 120 * 1000;

    /**
     * Minimum number of idle objects.
     */
    private int _minIdle = 1;


    /**
     * Get the set number of maximum objects (idle+active)
     */
    public int getMaxObjects() {
        return _maxObjects;
    }

    /**
     * Sets the maximum number of objects (idle+active)
     */
    public void setMaxObjects(int maxObjects) {
        this._maxObjects = maxObjects;
    }

    /**
     * Get the maximum number of idle objects.
     */
    public int getMaxIdle() {
        return _maxIdle;
    }

    /**
     * Sets the maximum number of objects that can sit idle in the pool at any
     * time. 
     */
    public void setMaxIdle(int maxIdle) {
        this._maxIdle = maxIdle;
    }

    /**
     * Max time to wait if the pool is waiting for a free object to become
     * available before failing. Zero means don't wait
     */
    public long getMaxWait() {
        return _maxWait;
    }

    /**
     * Max time to wait if the pool is waiting for a free object to become
     * available before failing. Zero means don't wait
     */
    public void setMaxWait(long maxWait) {
        this._maxWait = maxWait;
    }

    /**
     * Minimum time to wait before evicting an idle object.
     * Zero means don't wait
     */
    public long getMinEvictableIdleTimeMillis() {
        return _minEvictableIdleTimeMillis;
    }

    /**
     * Minimum time to wait before evicting an idle object.
     * Zero means don't wait
     */
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    /**
     * Minimum number of idle objects.
     */
    public int getMinIdle() {
        return _minIdle;
    }

    /**
     * Minimum number of idle objects.
     */
    public void setMinIdle(int minIdle) {
        this._minIdle = minIdle;
    }
    
    public void validate() {
        if (_minIdle < 0) {
            throw new IllegalStateException("Min idle is less than zero.");
        }
        if (_maxObjects < 0) {
            throw new IllegalStateException("Max active is less than zero.");
        }
        if (_maxIdle < 0) {
            throw new IllegalStateException("Max idle is less than zero.");
        }
        if (_maxWait < 0) {
            throw new IllegalStateException("Max wait is less than zero.");
        }
        if (_minEvictableIdleTimeMillis < 0) {
            throw new IllegalStateException("Min evictable idle time millis less than zero.");
        }
        if ( _minIdle > _maxIdle ) {
            throw new IllegalStateException("Min idle is greater than max idle.");            
        }
        if ( _maxIdle > _maxObjects ) {
            throw new IllegalStateException("Max idle is greater than max objects.");                        
        }
    }

    @Override
    public int hashCode() {
        return (int)(getMaxObjects()+getMaxIdle()+getMaxWait()+getMinEvictableIdleTimeMillis()+getMinIdle());        
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof ObjectPoolConfiguration) {
            ObjectPoolConfiguration other = (ObjectPoolConfiguration)obj;
            
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
        return bld.toString();
    }
}

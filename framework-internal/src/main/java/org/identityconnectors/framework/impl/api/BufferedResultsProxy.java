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
package org.identityconnectors.framework.impl.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;


public class BufferedResultsProxy implements InvocationHandler {

    private final static Log _log = Log.getLog(BufferedResultsProxy.class);
    
    private final Object _target;
    private final int _bufferSize;
    private final long _timeoutMillis;
    
    public BufferedResultsProxy(Object target, 
            int bufferSize,
            long timeoutMillis) {
        
        if (target == null) {
            final String ERR = "Target argument must not be null!";
            throw new IllegalArgumentException(ERR);
        }
        
        _target = target;
        
        if ( timeoutMillis == APIOperation.NO_TIMEOUT ) {
            _timeoutMillis = Long.MAX_VALUE;
        }
        else if ( timeoutMillis == 0 ) {
            _timeoutMillis = 60 * 1000;
        }
        else {
            _timeoutMillis = timeoutMillis;
        }
        // create the pipe between the consumer thread an caller..
        _bufferSize = (bufferSize < 1) ? 100 : bufferSize;
    }
    
    private static class BufferedResultsHandler extends Thread implements ObjectStreamHandler {
        private static final Object DONE = new Object();
        private boolean _stopped = false;
        private final Method _method;
        private final Object _target;
        private final Object [] _arguments;
        private final long _timeoutMillis;
        private final ArrayBlockingQueue<Object> _buffer; 
        
        public BufferedResultsHandler(
                Method method,
                Object target,
                Object [] arguments,
                int bufferSize, 
                long timeoutMillis) {
            _method = method;
            _target = target;
            _arguments = arguments;
            _buffer = new ArrayBlockingQueue<Object>(bufferSize);
            _timeoutMillis = timeoutMillis;
        }
                
        public boolean handle(final Object obj) {
            Assertions.nullCheck(obj, "obj");
            try {
                _buffer.put(obj);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectorException.wrap(e);
            }
            return !isStopped();
        }
        
        /**
         * Stops the thread and optionally waits for it to finish.
         * @param wait True if we should wait for the thread to finish
         * @throws OperationTimeoutException If we said to wait and we
         *  timed out.
         */
        public void stop(boolean wait) {
            if ( wait && Thread.currentThread() == this ) {
                throw new IllegalStateException("A thread cannot wait on itself");
            }
            synchronized (this) {
                _stopped = true;
            }
            //clear out the queue - this will cause the thread to
            //wakeup so that it can exit
            _buffer.clear();
            if ( wait ) {
                try {
                    //join with a time-limit. this may timeout
                    //if we are blocked in the producer
                    join(_timeoutMillis);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectorException.wrap(e);
                }
                //if we're still alive, we've timed out
                if (isAlive()) {
                    throw new OperationTimeoutException();
                }
            }
        }
        public synchronized boolean isStopped() {
            return _stopped;
        }
        
        private Object [] createActualArguments()
        {
            Object [] actualArguments = new Object[_arguments.length];
            Class<?> [] paramTypes =
                _method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                if (StreamHandlerUtil.isAdaptableToObjectStreamHandler(paramType)) {
                    actualArguments[i] =
                        StreamHandlerUtil.adaptFromObjectStreamHandler(paramType, this);
                }
                else {
                    actualArguments[i] =
                        _arguments[i];
                }
            }
            return actualArguments;
        }
        
        public void run() {
            try {
                try {                    
                    _method.invoke(_target, createActualArguments());
                    _buffer.put(DONE);
                }
                catch (RuntimeException e) {
                    _buffer.put(e);
                }
                catch (InvocationTargetException e) {
                    _buffer.put(e.getTargetException());
                }
                catch (InterruptedException e) {
                    throw e;
                }
                catch (Exception e) {
                    _buffer.put(ConnectorException.wrap(e));
                }
            }
            catch (InterruptedException e) {
                _log.error(e,null);
            }
        }
        /**
         * Returns the next object from the stream. Returns null if
         * done. 
         * @return The next object from the stream or null if done
         * @throws OperationTimeoutException If we timed out
         * @throws RuntimeException If the search threw an exception
         */
        public Object getNextObject() {
            if (isStopped()) {
                return null;
            }
            Object obj;
            try {
                obj = 
                    _buffer.poll(_timeoutMillis,TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectorException.wrap(e);
            }
            if ( obj == null ) {
                stop(false); //stop, but don't wait since we've already timed out
                throw new OperationTimeoutException();
            }
            else if ( obj == DONE ) {
                stop(true); //stop and wait
                return null;
            }
            else if ( obj instanceof RuntimeException ) {
                stop(true); //stop and wait
                throw (RuntimeException)obj;
            }
            else {
                return obj;
            }
        }
    }
    
    
    public Object invoke(final Object proxy, final Method method, Object [] arguments) 
    throws Throwable {
        //do not buffer/timeout equals, hashCode, toString
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(_target, arguments);
        }

        if (method.getReturnType() != void.class) {
            throw new UnsupportedOperationException("We only support operations that return void "+method);
        }
        
        BufferedResultsHandler bufHandler = new BufferedResultsHandler(
                method,
                _target,
                arguments,
                _bufferSize,
                _timeoutMillis);
        
        ObjectStreamHandler handler = null;
        
        Class<?> [] paramTypes =
            method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (StreamHandlerUtil.isAdaptableToObjectStreamHandler(paramType)) {
                if ( handler != null ) {
                    throw new UnsupportedOperationException("We only support operations that have a single stream handler "+method);
                }
                handler = StreamHandlerUtil.adaptToObjectStreamHandler(paramType, arguments[i]);
            }            
        }
        
        if ( handler == null ) {
            throw new UnsupportedOperationException("We only support operations that have a single stream handler "+method);
        }

        //this guy will automatically inherit
        //CurrentLocale since we are using a new thread
        //NOTE: if we ever introduce thread pooling
        //here, it needs to explicitly propagate
        bufHandler.setDaemon(true);
        bufHandler.start();
        while (!bufHandler.isStopped())
        {
            Object obj = bufHandler.getNextObject();
            if ( obj != null ) {
                try {
                    boolean keepGoing = handler.handle(obj);
                    if (!keepGoing) {
                        //stop and wait 
                        bufHandler.stop(true);
                    }
                }
                catch (RuntimeException e) {
                    //handler threw an exception
                    try {
                        //stop the buf handler thread
                        bufHandler.stop(true);
                    }
                    catch (RuntimeException e2) {
                        //log timeout if it happens, but don't mask
                        //original exception
                        _log.error(e2, null);
                    }
                    //throw the exception the handler threw
                    throw e;
                }
            }
        }
        
        return null;
    } 
}

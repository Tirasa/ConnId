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
package org.identityconnectors.framework.spi.operations;

import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;

/**
 * Implement this interface to allow the Connector to search for resource
 * objects.
 * @param T The result type of the translator. 
 * @see AbstractFilterTranslator For more information
 */
public interface SearchOp<T> extends SPIOperation {
    
    /**
     * Creates a filter translator that will translate a specified 
     * {@link org.identityconnectors.framework.common.objects.filter.Filter filter} 
     * into one or more native queries.
     * Each of these native queries will be passed subsequently into
     * <code>executeQuery()</code>.
     * 
     * @param oclass
     *            The object class for the search. Will never be null.
     * @param options
     *            additional options that impact the way this operation is run.
     *            If the caller passes null, the framework will convert this
     *            into an empty set of options, so SPI need not worry about this
     *            ever being null.
     * @return A filter translator. This must not be <code>null</code>. 
     *  	   A <code>null</code> return value will cause the API 
     *         (<code>SearchApiOp</code>) to throw {@link NullPointerException}.
     */
    public FilterTranslator<T> createFilterTranslator(ObjectClass oclass, OperationOptions options);
    
    /**
     * ConnectorFacade calls this method once for each native query 
     * that the {@linkplain #createFilterTranslator(ObjectClass, OperationOptions) FilterTranslator} 
     * produces in response to the <code>Filter</code> passed into 
     * {@link org.identityconnectors.framework.api.operations.SearchApiOp#search SearchApiOp}.
     * If the <code>FilterTranslator</code> produces more than one native query, then ConnectorFacade
     * will automatically merge the results from each query and eliminate any duplicates.
     * NOTE that this implies an in-memory data structure that holds a set of
     * Uid values, so memory usage in the event of multiple queries will be O(N)
     * where N is the number of results. This is why it is important that
     * the FilterTranslator for each Connector implement OR if possible.
     * 
     * @param oclass The object class for the search. Will never be null.
     * @param query The native query to run. A value of null means 
     * 			  "return every instance of the given object class".
     * @param handler
     *            Results should be returned to this handler
     * @param options
     *            Additional options that impact the way this operation is run.
     *            If the caller passes null, the framework will convert this into
     *            an empty set of options, so SPI need not guard against options being null.
     */
    public void executeQuery(ObjectClass oclass, T query, ResultsHandler handler, OperationOptions options);

}

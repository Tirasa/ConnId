/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.identityconnectors.framework.api;

/**
 * SPI search operation has a default
 * {@link org.identityconnectors.framework.common.objects.ResultsHandler}-chain
 * as:
 * <ul>
 * <li>NormalizingResultsHandler</li>
 * <li>FilteredResultsHandler</li>
 * <li>AttributesToGetSearchResultsHandler</li>
 * </ul>
 * This configuration allow to overconfigure the chain.
 *
 * @author $author$
 * @since 1.1
 */
public class ResultsHandlerConfiguration {

    /**
     * Enables the
     * {@link org.identityconnectors.framework.impl.api.local.operations.NormalizingResultsHandler}
     * in the handler chain.
     */
    boolean enableNormalizingResultsHandler = true;
    /**
     * Enables the
     * {@link org.identityconnectors.framework.impl.api.local.operations.FilteredResultsHandler}
     * in the handler chain.
     */
    boolean enableFilteredResultsHandler = true;
    /**
     * Enables the case insensitive filtering.
     */
    boolean enableCaseInsensitiveFilter = false;
    /**
     * Enables the
     * {@link org.identityconnectors.framework.impl.api.local.operations.SearchImpl.AttributesToGetSearchResultsHandler}
     * in the handler chain.
     */
    boolean enableAttributesToGetSearchResultsHandler = true;

    /**
     * default empty constructor.
     */
    public ResultsHandlerConfiguration() {
    }

    /**
     * Copy constructor.
     *
     * @param source
     *            configuration that copied to.
     */
    public ResultsHandlerConfiguration(ResultsHandlerConfiguration source) {
        this.enableNormalizingResultsHandler = source.isEnableNormalizingResultsHandler();
        this.enableFilteredResultsHandler = source.isEnableFilteredResultsHandler();
        this.enableCaseInsensitiveFilter = source.isEnableCaseInsensitiveFilter();
        this.enableAttributesToGetSearchResultsHandler =
                source.isEnableAttributesToGetSearchResultsHandler();
    }

    public boolean isEnableAttributesToGetSearchResultsHandler() {
        return enableAttributesToGetSearchResultsHandler;
    }

    public void setEnableAttributesToGetSearchResultsHandler(
            boolean enableAttributesToGetSearchResultsHandler) {
        this.enableAttributesToGetSearchResultsHandler = enableAttributesToGetSearchResultsHandler;
    }

    public boolean isEnableCaseInsensitiveFilter() {
        return enableCaseInsensitiveFilter;
    }

    public void setEnableCaseInsensitiveFilter(boolean enableCaseInsensitiveFilter) {
        this.enableCaseInsensitiveFilter = enableCaseInsensitiveFilter;
    }

    public boolean isEnableFilteredResultsHandler() {
        return enableFilteredResultsHandler;
    }

    public void setEnableFilteredResultsHandler(boolean enableFilteredResultsHandler) {
        this.enableFilteredResultsHandler = enableFilteredResultsHandler;
    }

    public boolean isEnableNormalizingResultsHandler() {
        return enableNormalizingResultsHandler;
    }

    public void setEnableNormalizingResultsHandler(boolean enableNormalizingResultsHandler) {
        this.enableNormalizingResultsHandler = enableNormalizingResultsHandler;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResultsHandlerConfiguration other = (ResultsHandlerConfiguration) obj;
        if (this.enableNormalizingResultsHandler != other.enableNormalizingResultsHandler) {
            return false;
        }
        if (this.enableFilteredResultsHandler != other.enableFilteredResultsHandler) {
            return false;
        }
        if (this.enableCaseInsensitiveFilter != other.enableCaseInsensitiveFilter) {
            return false;
        }
        if (this.enableAttributesToGetSearchResultsHandler != other.enableAttributesToGetSearchResultsHandler) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.enableNormalizingResultsHandler ? 1 : 0);
        hash = 79 * hash + (this.enableFilteredResultsHandler ? 1 : 0);
        hash = 79 * hash + (this.enableCaseInsensitiveFilter ? 1 : 0);
        hash = 79 * hash + (this.enableAttributesToGetSearchResultsHandler ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ResultsHandlerConfiguration{" + "enableNormalizingResultsHandler="
                + enableNormalizingResultsHandler + "\nenableFilteredResultsHandler="
                + enableFilteredResultsHandler + "\nenableCaseInsensitiveFilter="
                + enableCaseInsensitiveFilter + "\nenableAttributesToGetSearchResultsHandler="
                + enableAttributesToGetSearchResultsHandler + '}';
    }
}

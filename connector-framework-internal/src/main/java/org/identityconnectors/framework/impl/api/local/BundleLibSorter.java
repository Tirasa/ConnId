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
 * Portions Copyrighted 2013 ConnId
 */
package org.identityconnectors.framework.impl.api.local;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BundleLibSorter implements Comparator<File>, Serializable {

    private static final long serialVersionUID = 1885450684185821535L;

    @Override
    public int compare(final File arg0, final File arg1) {
        final String name1 = arg0.getName();
        final String name2 = arg1.getName();
        return name1.compareTo(name2);
    }

    /**
     * Returns the sorted libs from the given bundle directory.
     */
    public static File[] getSortedFiles(final File dir) {
        final File[] files = dir.listFiles();
        Arrays.sort(files, new BundleLibSorter());
        return files;
    }

    public static List<URL> getSortedURLs(final File dir) throws IOException {
        final File[] files = getSortedFiles(dir);
        final List<URL> sortedURLs = new ArrayList<URL>();
        for (File file : files) {
            sortedURLs.add(file.toURI().toURL());
        }
        return sortedURLs;
    }
}

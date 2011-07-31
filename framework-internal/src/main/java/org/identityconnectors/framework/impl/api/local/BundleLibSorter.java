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
package org.identityconnectors.framework.impl.api.local;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BundleLibSorter implements Comparator<File> {

    public int compare(File arg0, File arg1) {
        String name1 = arg0.getName();
        String name2 = arg1.getName();
        int comp = name1.compareTo(name2);
        return comp;
    }

    /**
     * Returns the sorted libs from the given bundle directory.
     */
    public static File [] getSortedFiles(File dir) {
        File [] files = dir.listFiles();
        Arrays.sort(files,new BundleLibSorter());
        return files;
    }
    
    public static List<URL> getSortedURLs(File dir) throws IOException {
        File [] files = getSortedFiles(dir);
        List<URL> rv = new ArrayList<URL>();
        for (File file : files) {
            rv.add(file.toURL());
        }
        return rv;
    }
}

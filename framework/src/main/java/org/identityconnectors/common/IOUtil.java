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
package org.identityconnectors.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;

/**
 * IO Utilities
 */
public class IOUtil {
    /**
     * Never allow this to be instantiated.
     */
    private IOUtil() {
        throw new AssertionError();
    }

    /**
     * Quietly closes the reader. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * reader and catch any {@link IOException} which may be thrown and ignore
     * it.
     * 
     * @param reader -
     *            Reader to close
     */
    public static void quietClose(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (java.io.IOException ex) {
            // ignore
        }
    }

    /**
     * Quietly closes the stream. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * stream and catch any {@link IOException} which may be thrown.
     * 
     * @param stream -
     *            Stream to close
     */
    public static void quietClose(InputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (java.io.IOException ex) {
            // ignore
        }
    }

    /**
     * Quietly closes the Writer. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * Writer and catch any ioexceptions which may be thrown.
     * 
     * @param writer -
     *            Writer to close
     */
    public static void quietClose(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (java.io.IOException ex) {
            // ignore
        }
    }

    /**
     * Quietly closes the stream. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * stream and catch any ioexceptions which may be thrown.
     * 
     * @param stream -
     *            Stream to close
     */
    public static void quietClose(OutputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (java.io.IOException ex) {
            // ignore
        }
    }

    // =======================================================================
    // Resource Utility Methods
    // =======================================================================
    /**
     * Get the path to a resource base on the package of given class.
     * 
     * @param c
     *            Class to get the package path too.
     * @param res
     *            Name of the resource to get the path of.
     * @return Returns the fully quilified path to a resource.
     */
    public static String getResourcePath(Class<?> c, String res) {
        assert c != null && StringUtil.isNotBlank(res);
        String classname = c.getName();
        String result;
        int dot = classname.lastIndexOf('.');
        if (dot >= 0) {
            String pkg = classname.substring(0, dot);
            result = pkg.replace('.', '/') + '/' + res;
        } else {
            result = res;
        }
        return result;
    }

    /**
     * Returns an input stream of the resource specified.
     * 
     * @return Returns an InputStream to the resource.
     */
    public static InputStream getResourceAsStream(Class<?> clazz, String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        InputStream ret = null;
        ClassLoader classLoader = clazz.getClassLoader();
        String name[] = { res, getResourcePath(clazz, res),
                "/" + getResourcePath(clazz, res) };
        for (int i = 0; ret == null && i < name.length; i++) {
            ret = classLoader.getResourceAsStream(name[i]);
        }
        return ret;
    }

    /**
     * Get the resource as a byte array.
     */
    public static byte[] getResourceAsBytes(Class<?> clazz, String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        // copy bytes from the stream to an array..
        InputStream ins = getResourceAsStream(clazz, res);
        if (ins == null) {
            throw new IllegalStateException("Resource not found: " + res);
        }
        byte[] ret = inputStreamToBytes(ins);
        quietClose(ins);
        return ret;
    }

    /**
     * Read the entire stream into a String and return it.
     */
    public static String getResourceAsString(Class<?> clazz, String res,
            Charset charset) {
        assert clazz != null && StringUtil.isNotBlank(res);
        String ret = null;
        InputStream ins = getResourceAsStream(clazz, res);
        if (ins != null) {
            try {
                InputStreamReader rdr = new InputStreamReader(ins, charset);
                ret = readerToString(rdr);
            } finally {
                quietClose(ins);
            }
        }
        return ret;
    }

    /**
     * Read the entire stream into a String and return it.
     */
    public static String getResourceAsString(Class<?> clazz, String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        return getResourceAsString(clazz, res, Charset.forName("UTF-8"));
    }

    /**
     * Takes a 'InputStream' and returns a byte array.
     */
    public static byte[] inputStreamToBytes(InputStream ins) {
        byte[] ret = null;
        try {
            // copy bytes from the stream to an array..
            int len = 0;
            byte[] buf = new byte[2048];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((len = ins.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            // get byte array..
            ret = out.toByteArray();
        } catch (IOException e) {
            // ignore and return null..
        }
        return ret;
    }

    /**
     * Takes a 'Reader' and returns the contents as a string.
     * 
     * @param rdr
     *            Producer for the string data.
     * @return Null if the 'Reader' is broken or empty otherwise the contents as
     *         a string.
     */
    public static String readerToString(Reader rdr) {
        String ret = null;
        try {
            int len = 0;
            char[] buf = new char[2048];
            StringWriter wrt = new StringWriter();
            while ((len = rdr.read(buf)) != -1) {
                wrt.write(buf, 0, len);
            }
            ret = wrt.toString();
        } catch (UnsupportedEncodingException e) {
            // ignore exception simply return null..
        } catch (IOException e) {
            // ignore exception simply return null..
        }
        return ret;
    }

    /**
     * Copies a file to a destination.
     * 
     * @param src
     *            The source must be a file
     * @param dest
     *            This can be a directory or a file.
     * @return True if succeeded otherwise false.
     */
    public static boolean copyFile(File src, File dest) throws IOException {
        boolean ret = true;
        // quick exit if this is bogus
        if (src == null || dest == null || !src.isFile()) {
            throw new FileNotFoundException();
        }
        // check for directory
        if (dest.isDirectory()) {
            String name = src.getName();
            dest = new File(dest, name);
        }
        FileInputStream fis = null;
        FileOutputStream fout = null;
        try {
            // get source stream
            fis = new FileInputStream(src);
            // get destination stream
            fout = new FileOutputStream(dest);
            // copy source to destination..
            ret = copyFile(fis, fout) > 0;
        } finally {
            // close open streams..
            quietClose(fis);
            quietClose(fout);
        }
        return ret;
    }

    /**
     * Copies one file to another.
     * <p>
     * NOTE: does not close streams.
     * 
     * @return total bytes copied.
     */
    public static long copyFile(InputStream fis, OutputStream fos)
            throws IOException {
        long ret = 0;
        byte[] bytes = new byte[8 * 1024];
        for (int rd = fis.read(bytes); rd != -1; rd = fis.read(bytes)) {
            fos.write(bytes, 0, rd);
            ret += rd;
        }
        return ret;
    }

    /**
     * Calculates the CRC32 checksum of the specified file.
     * 
     * @param fileName -
     *            the path to the file on which to calculate the checksum
     */
    public static long checksum(String fileName) throws IOException,
            FileNotFoundException {
        return (checksum(new File(fileName)));
    }

    public static long checksum(File file) throws java.io.IOException,
            FileNotFoundException {
        FileInputStream fis = null;
        byte[] bytes = new byte[16384];
        int len;
        try {
            fis = new FileInputStream(file);
            CRC32 chkSum = new CRC32();
            len = fis.read(bytes);
            while (len != -1) {
                chkSum.update(bytes, 0, len);
                len = fis.read(bytes);
            }
            return chkSum.getValue();
        } finally {
            quietClose(fis);
        }
    }

    /**
     * Reads an entire file and returns the bytes.
     * 
     * @param close
     *            if true, close when finished reading.
     * @return file bytes.
     */
    public static byte[] readInputStreamBytes(InputStream is, boolean close)
            throws IOException {
        byte[] bytes = null;
        if (is != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            try {
                int bytesRead = 0;
                byte[] buf = new byte[1024];
                while ((bytesRead = is.read(buf)) != -1) {
                    bout.write(buf, 0, bytesRead);
                } // end of while ((read(buf) != -1)
                bytes = bout.toByteArray();
            } finally {
                if (close) {
                    quietClose(is);
                } // end of if (is != null)
            }
        }
        return bytes;
    }

    /**
     * Recursively delete all the files in a directory and the directory.
     * 
     * @throws RuntimeException
     *             iff there is file that can not be deleted.
     */
    public static void delete(File f) throws IOException {
        // determine if the file/directory exists..
        if (f.exists()) {
            final String msg = "Failed to delete: " + f;
            // determine if this is a directory or file..
            if (f.isDirectory()) {
                // delete all the files/directories
                File[] fs = f.listFiles();
                for (int x = 0; x < fs.length; x++) {
                    delete(fs[x]);
                }
                // delete the directory
                if (!f.delete()) {
                    throw new IOException(msg);
                }
            } else if (f.isFile() && !f.delete()) {
                // failed to delete file..
                throw new IOException(msg);
            }
        }
    }

    /**
     * Loads the given file as a Properties file.
     */
    public static Properties loadPropertiesFile(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        try {
            Properties rv = new Properties();
            rv.load(in);
            return rv;
        } finally {
            in.close();
        }
    }
    
    /**
     * Stores the given file as a Properties file.
     */
    public static void storePropertiesFile(File f, Properties properties) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        try {
            properties.store(out, null);
        } finally {
            out.close();
        }
    }

    /**
     * Loads the given resource as a properties object.
     * 
     * @param loader
     *            The class loader
     * @param path
     *            The path to the resource
     * @return The properties or null if not found
     * @throws IOException
     *             If an error occurs reading it
     */
    public static Properties getResourceAsProperties(ClassLoader loader,
            String path) throws IOException {
        InputStream in = loader.getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        try {
            Properties rv = new Properties();
            rv.load(in);
            return rv;
        } finally {
            in.close();
        }
    }
    /**
     * Extracts the resource to a file.
     * 
     * @param clazz
     *            The class, relative to which path is resolved
     * @param path
     *            The path to the resource
     * @param file
     *            The file to extract to
     * @throws IOException
     *             If an error occurs reading it
     */
    public static void extractResourceToFile(Class<?> clazz,
            String path,
            File file) throws IOException {
        InputStream in = getResourceAsStream(clazz, path);
        if ( in == null ) {
            throw new IOException("Missing resource: "+path);
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            IOUtil.copyFile(in, out);
        } finally {
            if ( out != null ) {
                out.close();
            }
            if ( in != null ) {
                in.close();
            }
            
        }
    }

    /**
     * Unjars the given file to the given directory. Does not close the JarFile
     * when finished.
     * 
     * @param jarFile
     *            The file to unjar.
     * @param toDir
     *            The directory to unjar to.
     */
    public static void unjar(JarFile jarFile, File toDir) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            File outFile = new File(toDir, entry.getName());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(outFile);
                InputStream in = jarFile.getInputStream(entry);
                IOUtil.copyFile(in, fos);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    public static final String UTF8 = "UTF-8";

    /**
     * Reads the given file as UTF-8
     * 
     * @param file
     *            The file to read
     * @return The contents of the file
     * @throws IOException
     *             iff there is an issue reading the file.
     */
    public static String readFileUTF8(File file) throws IOException {
        byte[] bytes = IOUtil.readFileBytes(file);
        return new String(bytes, UTF8);
    }

    /**
     * Reads the given file as bytes
     * 
     * @param file
     *            The file to read
     * @return The contents of the file
     * @throws IOException
     *             iff there is an issue reading the file.
     */
    public static byte[] readFileBytes(File file) throws IOException {
        InputStream ins = new FileInputStream(file);
        byte[] bytes = IOUtil.readInputStreamBytes(ins, true);
        return bytes;
    }

    /**
     * Write the contents of the string out to a file in UTF-8 format.
     * 
     * @param file
     *            the file to write to.
     * @param contents
     *            the contents of the file to write to.
     * @throws IOException
     *             iff there is an issue writing the file.
     * @throws NullPointerException
     *             iff the file parameter is null.
     */
    public static void writeFileUTF8(File file, String contents)
            throws IOException {
        Writer w = new OutputStreamWriter(new FileOutputStream(file), UTF8);
        try {
            w.write(contents);
        } finally {
            w.close();
        }
    }

    /**
     * Make a URL from a directory and path.
     * 
     * @param dir
     *            directory to start from.
     * @param path
     *            file or path to create the url.
     * @return URL based on the parameter provided.
     * @throws IOException
     *             iff the URL create from the parameters does not specify a
     *             file.
     */
    public static URL makeURL(File dir, String path) throws IOException {
        File file = new File(dir, path);
        if (!file.isFile()) {
            throw new IOException(file.getPath() + " does not exist");
        }
        return file.toURL();
    }

    /**
     * Attempt to load file based on a string base filename.
     * 
     * @param string
     *            represents the file.
     * @return a loaded properties file.
     * @throws IOException
     *             if there is an issue.
     */
    public static Properties loadPropertiesFile(String string)
            throws IOException {
        return loadPropertiesFile(new File(string));
    }
}

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
package org.identityconnectors.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;

/**
 * IO Utilities.
 */
public final class IOUtil {

    /**
     * Never allow this to be instantiated.
     */
    private IOUtil() {
        throw new AssertionError();
    }

    /**
     * Quietly closes the reader.
     *
     * This avoids having to handle exceptions, and then inside of the exception
     * handling have a try catch block to close the reader and catch any
     * {@link IOException} which may be thrown and ignore it.
     *
     * @param reader
     * Reader to close
     */
    public static void quietClose(final Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Quietly closes the stream.
     *
     * This avoids having to handle exceptions, and then inside of the exception
     * handling have a try catch block to close the stream and catch any
     * {@link IOException} which may be thrown.
     *
     * @param stream
     * Stream to close
     */
    public static void quietClose(final InputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Quietly closes the writer.
     *
     * This avoids having to handle exceptions, and then inside of the exception
     * handling have a try catch block to close the Writer and catch any
     * {@link IOException} which may be thrown.
     *
     * @param writer
     * Writer to close
     */
    public static void quietClose(final Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Quietly closes the stream.
     *
     * This avoids having to handle exceptions, and then inside of the exception
     * handling have a try catch block to close the stream and catch any
     * {@link IOException} which may be thrown.
     *
     * @param stream
     * Stream to close
     */
    public static void quietClose(final OutputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Quietly closes the statement.
     *
     * This avoids having to handle exceptions, and then inside of the exception
     * handling have a try catch block to close the statement and catch any
     * {@link SQLException} which may be thrown.
     *
     * @param stmt
     * Statement to close
     * @since 1.3
     */
    public static void quietClose(final Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * Quietly closes the connection.
     *
     * This avoids having to handle exceptions, and then inside of the exception
     * handling have a try catch block to close the connection and catch any
     * {@link SQLException} which may be thrown.
     *
     * @param conn
     * Connection to close
     * @since 1.3
     */
    public static void quietClose(final Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }

    /**
     * Quietly closes the resultset.
     *
     * This avoids having to handle exceptions, and then inside of the exception
     * handling have a try catch block to close the connection and catch any
     * {@link SQLException} which may be thrown.
     *
     * @param resultset
     * ResultSet to close
     * @since 1.3
     */
    public static void quietClose(final ResultSet resultset) {
        try {
            if (resultset != null) {
                resultset.close();
            }
        } catch (SQLException e) {
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
     * Class to get the package path too.
     * @param res
     * Name of the resource to get the path of.
     * @return Returns the fully quilified path to a resource.
     */
    public static String getResourcePath(final Class<?> c, final String res) {
        assert c != null && StringUtil.isNotBlank(res);
        final String classname = c.getName();
        String result;
        final int dot = classname.lastIndexOf('.');
        if (dot >= 0) {
            final String pkg = classname.substring(0, dot);
            result = pkg.replace('.', '/') + '/' + res;
        } else {
            result = res;
        }
        return result;
    }

    /**
     * Returns an input stream of the resource specified.
     *
     * @param clazz
     * @param res
     * @return Returns an InputStream to the resource.
     */
    public static InputStream getResourceAsStream(final Class<?> clazz, final String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        InputStream ret = null;
        final ClassLoader classLoader = clazz.getClassLoader();
        final String name[] = { res, getResourcePath(clazz, res), "/" + getResourcePath(clazz, res) };
        for (int i = 0; ret == null && i < name.length; i++) {
            ret = classLoader.getResourceAsStream(name[i]);
        }
        return ret;
    }

    /**
     * Get the resource as a byte array.
     *
     * @param clazz
     * @param res
     * @return
     */
    public static byte[] getResourceAsBytes(final Class<?> clazz, final String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        // copy bytes from the stream to an array..
        final InputStream ins = getResourceAsStream(clazz, res);
        if (ins == null) {
            throw new IllegalStateException("Resource not found: " + res);
        }
        final byte[] ret = inputStreamToBytes(ins);
        quietClose(ins);
        return ret;
    }

    /**
     * Read the entire stream into a String and return it.
     *
     * @param clazz
     * @param res
     * @param charset
     * @return
     */
    public static String getResourceAsString(final Class<?> clazz, final String res, final Charset charset) {
        assert clazz != null && StringUtil.isNotBlank(res);
        String ret = null;
        final InputStream ins = getResourceAsStream(clazz, res);
        if (ins != null) {
            try {
                final InputStreamReader rdr = new InputStreamReader(ins, charset);
                ret = readerToString(rdr);
            } finally {
                quietClose(ins);
            }
        }
        return ret;
    }

    /**
     * Read the entire stream into a String and return it.
     *
     * @param clazz
     * @param res
     * @return
     */
    public static String getResourceAsString(final Class<?> clazz, final String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        return getResourceAsString(clazz, res, Charset.forName("UTF-8"));
    }

    /**
     * Takes a 'InputStream' and returns a byte array.
     *
     * @param ins
     * @return
     */
    public static byte[] inputStreamToBytes(final InputStream ins) {
        byte[] ret = null;
        try {
            // copy bytes from the stream to an array..
            int len;
            final byte[] buf = new byte[2048];
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
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
     * @param rdr Producer for the string data.
     * @return Null if the 'Reader' is broken or empty otherwise the contents as a string.
     */
    public static String readerToString(final Reader rdr) {
        String ret = null;
        try {
            int len;
            final char[] buf = new char[2048];
            final StringWriter wrt = new StringWriter();
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
     * @param src The source must be a file
     * @param dest This can be a directory or a file.
     * @return True if succeeded otherwise false.
     */
    public static boolean copyFile(final File src, final File dest) throws IOException {
        // quick exit if this is bogus
        if (src == null || dest == null || !src.isFile()) {
            throw new FileNotFoundException();
        }

        // check for directory
        Path dst = dest.toPath();
        if (dest.isDirectory()) {
            dst = dst.resolve(src.getName());
        }

        boolean ret;
        try (InputStream fis = Files.newInputStream(src.toPath());
                OutputStream fout = Files.newOutputStream(dst)) {

            // copy source to destination..
            ret = copyFile(fis, fout) > 0;
        }
        return ret;
    }

    /**
     * Copies one file to another.
     *
     * NOTE: does not close streams.
     *
     * @param fis
     * @param fos
     * @return total bytes copied.
     */
    public static long copyFile(final InputStream fis, final OutputStream fos) throws IOException {
        long ret = 0;
        final byte[] bytes = new byte[8 * 1024];
        for (int rd = fis.read(bytes); rd != -1; rd = fis.read(bytes)) {
            fos.write(bytes, 0, rd);
            ret += rd;
        }
        return ret;
    }

    /**
     * Calculates the CRC32 checksum of the specified file.
     *
     * @param fileName the path to the file on which to calculate the checksum
     * @return
     */
    public static long checksum(final String fileName) throws IOException, FileNotFoundException {
        return checksum(Path.of(fileName));
    }

    /**
     * Calculates the CRC32 checksum of the specified file.
     *
     * @param file the file on which to calculate the checksum
     * @return
     */
    public static long checksum(final Path file) throws IOException, FileNotFoundException {
        final byte[] bytes = new byte[16384];
        int len;
        try (InputStream fis = Files.newInputStream(file)) {
            CRC32 chkSum = new CRC32();
            len = fis.read(bytes);
            while (len != -1) {
                chkSum.update(bytes, 0, len);
                len = fis.read(bytes);
            }
            return chkSum.getValue();
        }
    }

    /**
     * Reads an entire file and returns the bytes.
     *
     * @param is
     * @param close if true, close when finished reading.
     * @return file bytes.
     */
    public static byte[] readInputStreamBytes(final InputStream is, final boolean close) throws IOException {
        byte[] bytes = null;
        if (is != null) {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            try {
                int bytesRead = 0;
                final byte[] buf = new byte[1024];
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
     * @param file
     * the file to delete
     * @throws RuntimeException
     * if there is file that can not be deleted.
     */
    public static void delete(final File file) throws IOException {
        // determine if the file/directory exists..
        if (file.exists()) {
            final String msg = "Failed to delete: " + file;
            // determine if this is a directory or file..
            if (file.isDirectory()) {
                // delete all the files/directories
                File[] fs = file.listFiles();
                for (int x = 0; x < fs.length; x++) {
                    delete(fs[x]);
                }
                // delete the directory
                if (!file.delete()) {
                    throw new IOException(msg);
                }
            } else if (file.isFile() && !file.delete()) {
                // failed to delete file..
                throw new IOException(msg);
            }
        }
    }

    /**
     * Loads the given file as a Properties file.
     *
     * @param file
     * the file to load the propertied from
     * @return properties loaded
     */
    public static Properties loadPropertiesFile(final Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            Properties rv = new Properties();
            rv.load(in);
            return rv;
        }
    }

    /**
     * Stores the given file as a Properties file.
     */
    public static void storePropertiesFile(final Path file, final Properties properties) throws IOException {
        try (OutputStream out = Files.newOutputStream(file)) {
            properties.store(out, null);
        }
    }

    /**
     * Loads the given resource as a properties object.
     *
     * @param loader
     * The class loader
     * @param path
     * The path to the resource
     * @return The properties or null if not found
     * @throws IOException
     * If an error occurs reading it
     */
    public static Properties getResourceAsProperties(final ClassLoader loader, final String path)
            throws IOException {
        final InputStream in = loader.getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        try {
            final Properties rv = new Properties();
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
     * The class, relative to which path is resolved
     * @param path
     * The path to the resource
     * @param file
     * The file to extract to
     * @throws IOException
     * If an error occurs reading it
     */
    public static void extractResourceToFile(
            final Class<?> clazz,
            final String path,
            final Path file) throws IOException {

        try (InputStream in = Optional.ofNullable(getResourceAsStream(clazz, path)).
                orElseThrow(() -> new IOException("Missing resource: " + path));
                OutputStream out = Files.newOutputStream(file)) {

            copyFile(in, out);
        }
    }

    /**
     * Unjars the given file to the given directory. Does not close the JarFile
     * when finished.
     *
     * @param jarFile
     * The file to unjar.
     * @param toDir
     * The directory to unjar to.
     */
    public static void unjar(final JarFile jarFile, final Path toDir) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            Path outFile = toDir.resolve(entry.getName());
            try (InputStream in = jarFile.getInputStream(entry);
                    OutputStream fos = Files.newOutputStream(outFile)) {

                copyFile(in, fos);
            }
        }
    }

    /**
     * Reads the given file as UTF-8
     *
     * @param file
     * The file to read
     * @return The contents of the file
     * @throws IOException
     * if there is an issue reading the file.
     */
    public static String readFileUTF8(final Path file) throws IOException {
        final byte[] bytes = readFileBytes(file);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads the given file as bytes
     *
     * @param file
     * The file to read
     * @return The contents of the file
     * @throws IOException
     * if there is an issue reading the file.
     */
    public static byte[] readFileBytes(final Path file) throws IOException {
        return readInputStreamBytes(Files.newInputStream(file), true);
    }

    /**
     * Write the contents of the string out to a file in UTF-8 format.
     *
     * @param file
     * the file to write to.
     * @param contents
     * the contents of the file to write to.
     * @throws IOException
     * if there is an issue writing the file.
     * @throws NullPointerException
     * if the file parameter is null.
     */
    public static void writeFileUTF8(final Path file, final String contents) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(contents);
        }
    }

    /**
     * Make a URL from a directory and path.
     *
     * @param dir
     * directory to start from.
     * @param path
     * file or path to create the url.
     * @return URL based on the parameter provided.
     * @throws IOException
     * if the URL create from the parameters does not specify a
     * file.
     */
    public static URL makeURL(final Path dir, final String path) throws IOException {
        final File file = dir.resolve(path).toFile();
        if (!file.isFile()) {
            throw new IOException(file.getPath() + " does not exist");
        }
        return file.toURI().toURL();
    }

    /**
     * Attempt to load file based on a string base filename.
     *
     * @param string
     * represents the file.
     * @return a loaded properties file.
     * @throws IOException
     * if there is an issue.
     */
    public static Properties loadPropertiesFile(final String string) throws IOException {
        return loadPropertiesFile(Path.of(string));
    }
}

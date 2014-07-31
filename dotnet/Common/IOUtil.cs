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
 * Portions Copyrighted 2014 ForgeRock AS.
 */
using System;
using System.Net;
using System.Collections.Generic;
using System.Text;
using System.Diagnostics;
using System.IO;
using System.Data.SqlClient;
using System.Reflection;

namespace Org.IdentityConnectors.Common
{
    #region IOUtil
    /// <summary>
    /// Description of IOUtil.
    /// </summary>
    public static class IOUtil
    {
        /// <summary>
        /// Given an ip address or host, returns the
        /// IPAddress
        /// </summary>
        /// <param name="hostOrIp"></param>
        /// <returns></returns>
        public static IPAddress GetIPAddress(String hostOrIp)
        {
            if (hostOrIp.Equals("0.0.0.0") ||
                hostOrIp.Equals("::0"))
            {
                return IPAddress.Parse(hostOrIp);
            }
            return Dns.GetHostAddresses(hostOrIp)[0];
        }

        /// <summary>
        /// Quietly closes the reader.
        /// <p/>
        /// This avoids having to handle exceptions, and then inside of the exception
        /// handling have a try catch block to close the reader and catch any
        /// <seealso cref="IOException"/> which may be thrown and ignore it.
        /// </summary>
        /// <param name="reader">
        ///            Reader to close </param>
        public static void QuietClose(TextReader reader)
        {
            try
            {
                if (reader != null)
                {
                    reader.Close();
                }
            }
            catch (IOException)
            {
                // ignore
            }
        }

        /// <summary>
        /// Quietly closes the writer.
        /// <p/>
        /// This avoids having to handle exceptions, and then inside of the exception
        /// handling have a try catch block to close the Writer and catch any
        /// <seealso cref="IOException"/> which may be thrown.
        /// </summary>
        /// <param name="writer">
        ///            Writer to close </param>
        public static void QuietClose(TextWriter writer)
        {
            try
            {
                if (writer != null)
                {
                    writer.Close();
                }
            }
            catch (IOException)
            {
                // ignore
            }
        }

        /// <summary>
        /// Quietly closes the stream.
        /// <p/>
        /// This avoids having to handle exceptions, and then inside of the exception
        /// handling have a try catch block to close the stream and catch any
        /// <seealso cref="IOException"/> which may be thrown.
        /// </summary>
        /// <param name="stream">
        ///            Stream to close </param>
        public static void QuietClose(Stream stream)
        {
            try
            {
                if (stream != null)
                {
                    stream.Close();
                }
            }
            catch (IOException)
            {
                // ignore
            }
        }

        /// <summary>
        /// Quietly dispose the statement.
        /// <p/>
        /// This avoids having to handle exceptions, and then inside of the exception
        /// handling have a try catch block to close the statement and catch any
        /// <seealso cref="SQLException"/> which may be thrown.
        /// </summary>
        /// <param name="stmt">
        ///            Statement to dispose</param>
        /// <remarks>Since 1.3</remarks>
        public static void QuietClose(SqlCommand stmt)
        {
            try
            {
                if (stmt != null)
                {
                    stmt.Dispose();
                }
            }
            catch (SqlException)
            {
                // ignore
            }
        }

        /// <summary>
        /// Quietly closes the connection.
        /// <p/>
        /// This avoids having to handle exceptions, and then inside of the exception
        /// handling have a try catch block to close the connection and catch any
        /// <seealso cref="SQLException"/> which may be thrown.
        /// </summary>
        /// <param name="conn">
        ///            Connection to close</param>
        /// <remarks>Since 1.3</remarks>
        public static void QuietClose(SqlConnection conn)
        {
            try
            {
                if (conn != null)
                {
                    conn.Close();
                }
            }
            catch (SqlException)
            {
                // ignore
            }
        }

        /// <summary>
        /// Quietly closes the resultset.
        /// <p/>
        /// This avoids having to handle exceptions, and then inside of the exception
        /// handling have a try catch block to close the connection and catch any
        /// <seealso cref="SQLException"/> which may be thrown.
        /// </summary>
        /// <param name="resultset">
        ///            ResultSet to close</param>
        /// <remarks>Since 1.3</remarks>
        public static void QuietClose(SqlDataReader resultset)
        {
            try
            {
                if (resultset != null)
                {
                    resultset.Close();
                }
            }
            catch (SqlException)
            {
                // ignore
            }
        }

        // =======================================================================
        // Resource Utility Methods
        // =======================================================================
        /// <summary>
        /// Returns an Assembly contains the typeName.
        /// </summary>
        /// <param name="typeName"> </param>
        /// <returns> Returns an Assembly or null if not found</returns>
        public static Assembly GetAssemblyContainingType(String typeName)
        {
            foreach (Assembly currentassembly in AppDomain.CurrentDomain.GetAssemblies())
            {
                Type t = currentassembly.GetType(typeName, false, true);
                if (t != null)
                {
                    return currentassembly;
                }
            }
            return null;
        }

        /// <summary>
        /// Returns an input stream of the resource specified.
        /// </summary>
        /// <param name="clazz"> </param>
        /// <param name="resourceName"> </param>
        /// <returns> Returns an InputStream to the resource. </returns>
        public static Stream GetResourceAsStream(Type clazz, string resourceName)
        {
            Debug.Assert(clazz != null && StringUtil.IsNotBlank(resourceName));
            return clazz.Assembly.GetManifestResourceStream(resourceName);
        }

        /// <summary>
        /// Get the resource as a byte array.
        /// </summary>
        /// <param name="clazz"> </param>
        /// <param name="res">
        /// @return </param>
        public static byte[] GetResourceAsBytes(Type clazz, string res)
        {
            Debug.Assert(clazz != null && StringUtil.IsNotBlank(res));
            // copy bytes from the stream to an array..
            Stream ins = GetResourceAsStream(clazz, res);
            if (ins == null)
            {
                throw new InvalidOperationException("Resource not found: " + res);
            }

            byte[] buffer = new byte[16 * 1024];
            using (MemoryStream ms = new MemoryStream())
            {
                int read;
                while ((read = ins.Read(buffer, 0, buffer.Length)) > 0)
                {
                    ms.Write(buffer, 0, read);
                }
                QuietClose(ins);
                return ms.ToArray();
            }
        }

        /// <summary>
        /// Read the entire stream into a String and return it.
        /// </summary>
        /// <param name="clazz"> </param>
        /// <param name="res"></param>
        /// <returns></returns>
        public static string GetResourceAsString(Type clazz, string res, Encoding charset)
        {
            Debug.Assert(clazz != null && StringUtil.IsNotBlank(res));
            string ret = null;
            Stream ins = GetResourceAsStream(clazz, res);
            if (ins != null)
            {
                using (StreamReader reader = new StreamReader(ins, charset))
                {
                    ret = reader.ReadToEnd();
                }
                QuietClose(ins);
            }
            return ret;
        }

        /// <summary>
        /// Read the entire stream into a String and return it.
        /// </summary>
        /// <param name="clazz"> </param>
        /// <param name="res"></param>
        /// <returns></returns>
        public static string GetResourceAsString(Type clazz, string res)
        {
            Debug.Assert(clazz != null && StringUtil.IsNotBlank(res));
            return GetResourceAsString(clazz, res, Encoding.UTF8);
        }

        /// <summary>
        /// Copies a file to a destination.
        /// </summary>
        /// <param name="src">
        ///            The source must be a file </param>
        /// <param name="dest">
        ///            This can be a directory or a file. </param>
        /// <returns> True if succeeded otherwise false. </returns>
        public static bool CopyFile(String src, String dest)
        {
            bool ret = true;
            // quick exit if this is bogus
            if (src == null || dest == null || !File.Exists(src))
            {
                throw new FileNotFoundException();
            }
            // check for directory
            if (!Directory.Exists(dest))
            {
                Directory.CreateDirectory(dest);
            }
            File.Copy(src, dest, true);
            return ret;
        }

        /// <summary>
        /// Copies one file to another.
        /// <para>
        /// NOTE: does not close streams.
        /// 
        /// </para>
        /// </summary>
        /// <param name="input"> </param>
        /// <param name="output"> </param>
        /// <returns> total bytes copied. </returns>
        public static void CopyFile(Stream input, Stream output)
        {
            byte[] buffer = new byte[32768];
            int read;
            while ((read = input.Read(buffer, 0, buffer.Length)) > 0)
            {
                output.Write(buffer, 0, read);
            }
        }

        /// <summary>
        /// Reads the given file as UTF-8
        /// </summary>
        /// <param name="file">
        ///            The file to read </param>
        /// <returns> The contents of the file </returns>
        /// <exception cref="IOException">
        ///             if there is an issue reading the file. </exception>
        public static string ReadFileUTF8(String file)
        {
            string content;
            using (StreamReader reader = new StreamReader(file, Encoding.UTF8))
            {
                content = reader.ReadToEnd();
            }
            return content;
        }

        /// <summary>
        /// Write the contents of the string out to a file in UTF-8 format.
        /// </summary>
        /// <param name="file">
        ///            the file to write to. </param>
        /// <param name="contents">
        ///            the contents of the file to write to. </param>
        /// <exception cref="IOException">
        ///             if there is an issue writing the file. </exception>
        /// <exception cref="NullPointerException">
        ///             if the file parameter is null. </exception>
        public static void WriteFileUTF8(String file, string contents)
        {
            using (var sw = new StreamWriter(File.Open(file, FileMode.CreateNew), Encoding.UTF8))
            {
                sw.WriteLine(contents);
            }
        }

        /// 
        /// <param name="collection"> </param>
        /// <param name="separator"> </param>
        /// <remarks>Since 1.3</remarks>
        public static string Join(ICollection<string> collection, char separator)
        {
            if (collection == null)
            {
                return null;
            }

            return Join(new List<String>(collection).ToArray(), separator, 0, collection.Count);
        }

        /// 
        /// <param name="array"> </param>
        /// <param name="separator"> </param>
        /// <remarks>Since 1.3</remarks>
        public static string Join(object[] array, char separator)
        {
            if (array == null)
            {
                return null;
            }

            return Join(array, separator, 0, array.Length);
        }

        /// 
        /// <param name="array"> </param>
        /// <param name="separator"> </param>
        /// <param name="startIndex"> </param>
        /// <param name="endIndex"> </param>
        /// <returns></returns>
        /// <remarks>Since 1.3</remarks>
        public static string Join(object[] array, char separator, int startIndex, int endIndex)
        {
            if (array == null)
            {
                return null;
            }
            int noOfItems = endIndex - startIndex;
            if (noOfItems <= 0)
            {
                return String.Empty;
            }

            StringBuilder buf = new StringBuilder(noOfItems * 16);

            for (int i = startIndex; i < endIndex; i++)
            {
                if (i > startIndex)
                {
                    buf.Append(separator);
                }
                if (array[i] != null)
                {
                    buf.Append(array[i]);
                }
            }
            return buf.ToString();
        }
    }
    #endregion
}

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
using System.Linq;
using System.Data.SqlClient;
using System.Security.Cryptography;
using System.Reflection;
using Org.IdentityConnectors.Common.DamienG.Security.Cryptography;

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
        /// Calculates the CRC32 checksum of the specified file.
        /// </summary>
        /// <param name="file">
        ///            the file on which to calculate the checksum
        /// @return </param>
        public static String Checksum(string fileName)
        {
            Crc32 crc32 = new Crc32();
            String hash = String.Empty;
            using (FileStream fs = File.Open(fileName, FileMode.Open))
            {
                foreach (byte b in crc32.ComputeHash(fs))
                {
                    hash += b.ToString("x2").ToLower();
                }
            }
            return hash;

        }

        /// <summary>
        /// Stores the given file as a Properties file.
        /// </summary>
        public static void StorePropertiesFile(FileInfo f, Dictionary<String, String> properties)
        {
        }

        /// <summary>
        /// Loads the given resource as a properties object.
        /// </summary>
        /// <param name="loader">
        ///            The class loader </param>
        /// <param name="path">
        ///            The path to the resource </param>
        /// <returns> The properties or null if not found </returns>
        /// <exception cref="IOException">
        ///             If an error occurs reading it </exception>
        public static Dictionary<String, String> GetResourceAsProperties(Assembly loader, string path)
        {
            using (Stream input = loader.GetManifestResourceStream(path))
            {
                Dictionary<String, String> rv = null;
                //rv.load(@in);
                return rv;
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

        /// <summary>
        /// Attempt to load file based on a string base filename.
        /// </summary>
        /// <param name="pathToFile">
        ///            represents the file. </param>
        /// <returns> a loaded properties file. </returns>
        /// <exception cref="IOException">
        ///             if there is an issue. </exception>
        public static Dictionary<string, string> LoadPropertiesFile(string pathToFile)
        {
            var data = new Dictionary<string, string>();
            foreach (var row in File.ReadAllLines(pathToFile))
                data.Add(row.Split('=')[0], string.Join("=", row.Split('=').Skip(1).ToArray()));
            return data;
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

    #region Crc32
    // Copyright (c) Damien Guard.  All rights reserved.
    // Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
    // You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    // Originally published at http://damieng.com/blog/2006/08/08/calculating_crc32_in_c_and_net
    namespace DamienG.Security.Cryptography
    {
        /// <summary>
        /// Implements a 32-bit CRC hash algorithm compatible with Zip etc.
        /// </summary>
        /// <remarks>
        /// Crc32 should only be used for backward compatibility with older file formats
        /// and algorithms. It is not secure enough for new applications.
        /// If you need to call multiple times for the same data either use the HashAlgorithm
        /// interface or remember that the result of one Compute call needs to be ~ (XOR) before
        /// being passed in as the seed for the next Compute call.
        /// </remarks>
        public sealed class Crc32 : HashAlgorithm
        {
            public const UInt32 DefaultPolynomial = 0xedb88320u;
            public const UInt32 DefaultSeed = 0xffffffffu;

            private static UInt32[] defaultTable;

            private readonly UInt32 seed;
            private readonly UInt32[] table;
            private UInt32 hash;

            public Crc32()
                : this(DefaultPolynomial, DefaultSeed)
            {
            }

            public Crc32(UInt32 polynomial, UInt32 seed)
            {
                table = InitializeTable(polynomial);
                this.seed = hash = seed;
            }

            public override void Initialize()
            {
                hash = seed;
            }

            protected override void HashCore(byte[] buffer, int start, int length)
            {
                hash = CalculateHash(table, hash, buffer, start, length);
            }

            protected override byte[] HashFinal()
            {
                var hashBuffer = UInt32ToBigEndianBytes(~hash);
                HashValue = hashBuffer;
                return hashBuffer;
            }

            public override int HashSize { get { return 32; } }

            public static UInt32 Compute(byte[] buffer)
            {
                return Compute(DefaultSeed, buffer);
            }

            public static UInt32 Compute(UInt32 seed, byte[] buffer)
            {
                return Compute(DefaultPolynomial, seed, buffer);
            }

            public static UInt32 Compute(UInt32 polynomial, UInt32 seed, byte[] buffer)
            {
                return ~CalculateHash(InitializeTable(polynomial), seed, buffer, 0, buffer.Length);
            }

            private static UInt32[] InitializeTable(UInt32 polynomial)
            {
                if (polynomial == DefaultPolynomial && defaultTable != null)
                    return defaultTable;

                var createTable = new UInt32[256];
                for (var i = 0; i < 256; i++)
                {
                    var entry = (UInt32)i;
                    for (var j = 0; j < 8; j++)
                        if ((entry & 1) == 1)
                            entry = (entry >> 1) ^ polynomial;
                        else
                            entry = entry >> 1;
                    createTable[i] = entry;
                }

                if (polynomial == DefaultPolynomial)
                    defaultTable = createTable;

                return createTable;
            }

            private static UInt32 CalculateHash(UInt32[] table, UInt32 seed, IList<byte> buffer, int start, int size)
            {
                var crc = seed;
                for (var i = start; i < size - start; i++)
                    crc = (crc >> 8) ^ table[buffer[i] ^ crc & 0xff];
                return crc;
            }

            private static byte[] UInt32ToBigEndianBytes(UInt32 uint32)
            {
                var result = BitConverter.GetBytes(uint32);

                if (BitConverter.IsLittleEndian)
                    Array.Reverse(result);

                return result;
            }
        }
    }
    #endregion
}

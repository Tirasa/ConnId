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
using System;
using System.Collections.Generic;
using System.Globalization;
namespace Org.IdentityConnectors.Common
{
    /// <summary>
    /// Represents a Java Locale and has facilities for converting to/from
    /// C# CultureInfo
    /// </summary>
    public sealed class Locale
    {
        private static readonly IDictionary<Locale, String>
            Locale2CultureInfoName = new Dictionary<Locale, String>();
        private static readonly IDictionary<String, Locale>
            CultureInfoName2Locale = new Dictionary<String, Locale>();

        private static void AddMapping(Locale locale, String name)
        {
            Locale2CultureInfoName[locale] = name;
            CultureInfoName2Locale[name] = locale;
        }

        /// <summary>
        /// Special cases
        /// </summary>
        static Locale()
        {
            AddMapping(new Locale(), "");
            AddMapping(new Locale("iw"), "he");
            AddMapping(new Locale("zh"), "zh-CHS");
            AddMapping(new Locale("iw", "IL"), "he-IL");
            AddMapping(new Locale("no", "NO"), "nb-NO");
            AddMapping(new Locale("no", "NO", "NY"), "nn-NO");
        }

        private String _language;
        private String _country;
        private String _variant;

        public Locale()
            : this("")
        {

        }

        public Locale(String language)
            : this(language, "")
        {
        }
        public Locale(String language, String country)
            : this(language, country, "")
        {
        }
        public Locale(String language, String country, String variant)
        {
            _language = language ?? "";
            _country = country ?? "";
            _variant = variant ?? "";
        }

        public string Language
        {
            get
            {
                return _language;
            }
        }

        public string Country
        {
            get
            {
                return _country;
            }
        }

        public string Variant
        {
            get
            {
                return _variant;
            }
        }

        public override bool Equals(Object o)
        {
            Locale other = o as Locale;
            if (other != null)
            {
                if (!Object.Equals(Language, other.Language))
                {
                    return false;
                }
                if (!Object.Equals(Country, other.Country))
                {
                    return false;
                }
                if (!Object.Equals(Variant, other.Variant))
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return _language.GetHashCode() + _country.GetHashCode();
            }
        }

        public override string ToString()
        {
            return Language + "_" + Country + "_" + Variant;
        }

        /// <summary>
        /// Creates the closest CultureInfo that maps to this locale
        /// </summary>
        /// <returns>The culture info</returns>
        public CultureInfo ToCultureInfo()
        {
            CultureInfo rv = null;
            String code = CollectionUtil.GetValue(Locale2CultureInfoName, this, null);
            if (code != null)
            {
                rv = TryCultureCode(code);
            }
            if (rv == null)
            {
                if (Country.Length > 0)
                {
                    rv = TryCultureCode(Language + "-" + Country);
                }
            }
            if (rv == null)
            {
                rv = TryCultureCode(Language);
            }
            if (rv == null)
            {
                rv = CultureInfo.InvariantCulture;
            }
            return rv;
        }

        public static Locale FindLocale(CultureInfo info)
        {
            String code = info.Name;
            Locale rv = CollectionUtil.GetValue(CultureInfoName2Locale, code, null);
            if (rv == null)
            {
                String[] parts = code.Split(new string[] { "-" },
                                             StringSplitOptions.RemoveEmptyEntries);
                String language = "";
                String country = "";
                if (parts.Length > 0)
                {
                    String l = parts[0];
                    if (LooksLikeValidLanguageCode(l))
                    {
                        language = l;
                        if (parts.Length > 1)
                        {
                            String c = parts[1];
                            if (LooksLikeValidCountryCode(c))
                            {
                                country = c;
                            }
                        }
                    }
                }
                rv = new Locale(language, country);
            }
            return rv;
        }

        /// <summary>
        /// Weeds out some junk
        /// </summary>
        /// <param name="l"></param>
        /// <returns></returns>
        private static bool LooksLikeValidLanguageCode(String l)
        {
            char[] chars = l.ToCharArray();
            return (chars.Length == 2 &&
                    Char.IsLower(chars[0]) &&
                    Char.IsLower(chars[1]));
        }
        /// <summary>
        /// Weeds out some junk
        /// </summary>
        /// <param name="l"></param>
        /// <returns></returns>
        private static bool LooksLikeValidCountryCode(String l)
        {
            char[] chars = l.ToCharArray();
            return (chars.Length == 2 &&
                    Char.IsUpper(chars[0]) &&
                    Char.IsUpper(chars[1]));
        }

        private static CultureInfo TryCultureCode(String code)
        {
            try
            {
                return new CultureInfo(code);
            }
            catch (Exception)
            {
                return null;
            }
        }
    }
}

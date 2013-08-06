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
 */
using System;
using NUnit.Framework;
using System.Globalization;
using System.Collections.Generic;
using Org.IdentityConnectors.Common;
namespace FrameworkTests
{
    [TestFixture]
    public class LocaleTests
    {


        [Test]
        public void TestJava2CSharp()
        {
            HashSet<CultureInfo>
                cultures = new HashSet<CultureInfo>(CultureInfo.GetCultures(CultureTypes.AllCultures));

            TestJavaLocale(cultures, new Locale("ar", "", ""), "Arabic");
            TestJavaLocale(cultures, new Locale("be", "", ""), "Belarusian");
            TestJavaLocale(cultures, new Locale("bg", "", ""), "Bulgarian");
            TestJavaLocale(cultures, new Locale("ca", "", ""), "Catalan");
            TestJavaLocale(cultures, new Locale("cs", "", ""), "Czech");
            TestJavaLocale(cultures, new Locale("da", "", ""), "Danish");
            TestJavaLocale(cultures, new Locale("de", "", ""), "German");
            TestJavaLocale(cultures, new Locale("el", "", ""), "Greek");
            TestJavaLocale(cultures, new Locale("en", "", ""), "English");
            TestJavaLocale(cultures, new Locale("es", "", ""), "Spanish");
            TestJavaLocale(cultures, new Locale("et", "", ""), "Estonian");
            TestJavaLocale(cultures, new Locale("fi", "", ""), "Finnish");
            TestJavaLocale(cultures, new Locale("fr", "", ""), "French");
            TestJavaLocale(cultures, new Locale("hr", "", ""), "Croatian");
            TestJavaLocale(cultures, new Locale("hu", "", ""), "Hungarian");
            TestJavaLocale(cultures, new Locale("is", "", ""), "Icelandic");
            TestJavaLocale(cultures, new Locale("it", "", ""), "Italian");
            TestJavaLocale(cultures, new Locale("iw", "", ""), "Hebrew");
            TestJavaLocale(cultures, new Locale("ja", "", ""), "Japanese");
            TestJavaLocale(cultures, new Locale("ko", "", ""), "Korean");
            TestJavaLocale(cultures, new Locale("lt", "", ""), "Lithuanian");
            TestJavaLocale(cultures, new Locale("lv", "", ""), "Latvian");
            TestJavaLocale(cultures, new Locale("mk", "", ""), "Macedonian");
            TestJavaLocale(cultures, new Locale("nl", "", ""), "Dutch");
            TestJavaLocale(cultures, new Locale("no", "", ""), "Norwegian");
            TestJavaLocale(cultures, new Locale("pl", "", ""), "Polish");
            TestJavaLocale(cultures, new Locale("pt", "", ""), "Portuguese");
            TestJavaLocale(cultures, new Locale("ro", "", ""), "Romanian");
            TestJavaLocale(cultures, new Locale("ru", "", ""), "Russian");
            TestJavaLocale(cultures, new Locale("sk", "", ""), "Slovak");
            TestJavaLocale(cultures, new Locale("sl", "", ""), "Slovenian");
            TestJavaLocale(cultures, new Locale("sq", "", ""), "Albanian");
            TestJavaLocale(cultures, new Locale("sr", "", ""), "Serbian");
            TestJavaLocale(cultures, new Locale("sv", "", ""), "Swedish");
            TestJavaLocale(cultures, new Locale("th", "", ""), "Thai");
            TestJavaLocale(cultures, new Locale("tr", "", ""), "Turkish");
            TestJavaLocale(cultures, new Locale("uk", "", ""), "Ukrainian");
            TestJavaLocale(cultures, new Locale("vi", "", ""), "Vietnamese");
            TestJavaLocale(cultures, new Locale("zh", "", ""), "Chinese");
            TestJavaLocale(cultures, new Locale("ar", "AE", ""), "Arabic (United Arab Emirates)");
            TestJavaLocale(cultures, new Locale("ar", "BH", ""), "Arabic (Bahrain)");
            TestJavaLocale(cultures, new Locale("ar", "DZ", ""), "Arabic (Algeria)");
            TestJavaLocale(cultures, new Locale("ar", "EG", ""), "Arabic (Egypt)");
            TestJavaLocale(cultures, new Locale("ar", "IQ", ""), "Arabic (Iraq)");
            TestJavaLocale(cultures, new Locale("ar", "JO", ""), "Arabic (Jordan)");
            TestJavaLocale(cultures, new Locale("ar", "KW", ""), "Arabic (Kuwait)");
            TestJavaLocale(cultures, new Locale("ar", "LB", ""), "Arabic (Lebanon)");
            TestJavaLocale(cultures, new Locale("ar", "LY", ""), "Arabic (Libya)");
            TestJavaLocale(cultures, new Locale("ar", "MA", ""), "Arabic (Morocco)");
            TestJavaLocale(cultures, new Locale("ar", "OM", ""), "Arabic (Oman)");
            TestJavaLocale(cultures, new Locale("ar", "QA", ""), "Arabic (Qatar)");
            TestJavaLocale(cultures, new Locale("ar", "SA", ""), "Arabic (Saudi Arabia)");
            TestJavaLocale(cultures,
                           new Locale("ar", "SD", ""),
                           "Arabic (Sudan)",
                           new Locale("ar"));
            TestJavaLocale(cultures, new Locale("ar", "SY", ""), "Arabic (Syria)");
            TestJavaLocale(cultures, new Locale("ar", "TN", ""), "Arabic (Tunisia)");
            TestJavaLocale(cultures, new Locale("ar", "YE", ""), "Arabic (Yemen)");
            TestJavaLocale(cultures, new Locale("be", "BY", ""), "Belarusian (Belarus)");
            TestJavaLocale(cultures, new Locale("bg", "BG", ""), "Bulgarian (Bulgaria)");
            TestJavaLocale(cultures, new Locale("ca", "ES", ""), "Catalan (Spain)");
            TestJavaLocale(cultures, new Locale("cs", "CZ", ""), "Czech (Czech Republic)");
            TestJavaLocale(cultures, new Locale("da", "DK", ""), "Danish (Denmark)");
            TestJavaLocale(cultures, new Locale("de", "AT", ""), "German (Austria)");
            TestJavaLocale(cultures, new Locale("de", "CH", ""), "German (Switzerland)");
            TestJavaLocale(cultures, new Locale("de", "DE", ""), "German (Germany)");
            TestJavaLocale(cultures, new Locale("de", "LU", ""), "German (Luxembourg)");
            TestJavaLocale(cultures, new Locale("el", "GR", ""), "Greek (Greece)");
            TestJavaLocale(cultures, new Locale("en", "AU", ""), "English (Australia)");
            TestJavaLocale(cultures, new Locale("en", "CA", ""), "English (Canada)");
            TestJavaLocale(cultures, new Locale("en", "GB", ""), "English (United Kingdom)");
            TestJavaLocale(cultures, new Locale("en", "IE", ""), "English (Ireland)");
            TestJavaLocale(cultures, new Locale("en", "IN", ""), "English (India)");
            TestJavaLocale(cultures, new Locale("en", "NZ", ""), "English (New Zealand)");
            TestJavaLocale(cultures, new Locale("en", "US", ""), "English (United States)");
            TestJavaLocale(cultures, new Locale("en", "ZA", ""), "English (South Africa)");
            TestJavaLocale(cultures, new Locale("es", "AR", ""), "Spanish (Argentina)");
            TestJavaLocale(cultures, new Locale("es", "BO", ""), "Spanish (Bolivia)");
            TestJavaLocale(cultures, new Locale("es", "CL", ""), "Spanish (Chile)");
            TestJavaLocale(cultures, new Locale("es", "CO", ""), "Spanish (Colombia)");
            TestJavaLocale(cultures, new Locale("es", "CR", ""), "Spanish (Costa Rica)");
            TestJavaLocale(cultures, new Locale("es", "DO", ""), "Spanish (Dominican Republic)");
            TestJavaLocale(cultures, new Locale("es", "EC", ""), "Spanish (Ecuador)");
            TestJavaLocale(cultures, new Locale("es", "ES", ""), "Spanish (Spain)");
            TestJavaLocale(cultures, new Locale("es", "GT", ""), "Spanish (Guatemala)");
            TestJavaLocale(cultures, new Locale("es", "HN", ""), "Spanish (Honduras)");
            TestJavaLocale(cultures, new Locale("es", "MX", ""), "Spanish (Mexico)");
            TestJavaLocale(cultures, new Locale("es", "NI", ""), "Spanish (Nicaragua)");
            TestJavaLocale(cultures, new Locale("es", "PA", ""), "Spanish (Panama)");
            TestJavaLocale(cultures, new Locale("es", "PE", ""), "Spanish (Peru)");
            TestJavaLocale(cultures, new Locale("es", "PR", ""), "Spanish (Puerto Rico)");
            TestJavaLocale(cultures, new Locale("es", "PY", ""), "Spanish (Paraguay)");
            TestJavaLocale(cultures, new Locale("es", "SV", ""), "Spanish (El Salvador)");
            TestJavaLocale(cultures, new Locale("es", "UY", ""), "Spanish (Uruguay)");
            TestJavaLocale(cultures, new Locale("es", "VE", ""), "Spanish (Venezuela)");
            TestJavaLocale(cultures, new Locale("et", "EE", ""), "Estonian (Estonia)");
            TestJavaLocale(cultures, new Locale("fi", "FI", ""), "Finnish (Finland)");
            TestJavaLocale(cultures, new Locale("fr", "BE", ""), "French (Belgium)");
            TestJavaLocale(cultures, new Locale("fr", "CA", ""), "French (Canada)");
            TestJavaLocale(cultures, new Locale("fr", "CH", ""), "French (Switzerland)");
            TestJavaLocale(cultures, new Locale("fr", "FR", ""), "French (France)");
            TestJavaLocale(cultures, new Locale("fr", "LU", ""), "French (Luxembourg)");
            TestJavaLocale(cultures, new Locale("hi", "IN", ""), "Hindi (India)");
            TestJavaLocale(cultures, new Locale("hr", "HR", ""), "Croatian (Croatia)");
            TestJavaLocale(cultures, new Locale("hu", "HU", ""), "Hungarian (Hungary)");
            TestJavaLocale(cultures, new Locale("is", "IS", ""), "Icelandic (Iceland)");
            TestJavaLocale(cultures, new Locale("it", "CH", ""), "Italian (Switzerland)");
            TestJavaLocale(cultures, new Locale("it", "IT", ""), "Italian (Italy)");
            TestJavaLocale(cultures, new Locale("iw", "IL", ""), "Hebrew (Israel)");
            TestJavaLocale(cultures, new Locale("ja", "JP", ""), "Japanese (Japan)");
            TestJavaLocale(cultures, new Locale("ko", "KR", ""), "Korean (South Korea)");
            TestJavaLocale(cultures, new Locale("lt", "LT", ""), "Lithuanian (Lithuania)");
            TestJavaLocale(cultures, new Locale("lv", "LV", ""), "Latvian (Latvia)");
            TestJavaLocale(cultures, new Locale("mk", "MK", ""), "Macedonian (Macedonia)");
            TestJavaLocale(cultures, new Locale("nl", "BE", ""), "Dutch (Belgium)");
            TestJavaLocale(cultures, new Locale("nl", "NL", ""), "Dutch (Netherlands)");
            TestJavaLocale(cultures, new Locale("no", "NO", ""), "Norwegian (Norway)");
            TestJavaLocale(cultures, new Locale("no", "NO", "NY"), "Norwegian (Norway,Nynorsk)");
            TestJavaLocale(cultures, new Locale("pl", "PL", ""), "Polish (Poland)");
            TestJavaLocale(cultures, new Locale("pt", "BR", ""), "Portuguese (Brazil)");
            TestJavaLocale(cultures, new Locale("pt", "PT", ""), "Portuguese (Portugal)");
            TestJavaLocale(cultures, new Locale("ro", "RO", ""), "Romanian (Romania)");
            TestJavaLocale(cultures, new Locale("ru", "RU", ""), "Russian (Russia)");
            TestJavaLocale(cultures, new Locale("sk", "SK", ""), "Slovak (Slovakia)");
            TestJavaLocale(cultures, new Locale("sl", "SI", ""), "Slovenian (Slovenia)");
            TestJavaLocale(cultures, new Locale("sq", "AL", ""), "Albanian (Albania)");
            TestJavaLocale(cultures, new Locale("sr", "BA", ""),
                           "Serbian (Bosnia and Herzegovina)",
                           new Locale("sr"));
            TestJavaLocale(cultures, new Locale("sr", "CS", ""),
                           "Serbian (Serbia and Montenegro)",
                           new Locale("sr"));
            TestJavaLocale(cultures, new Locale("sv", "SE", ""), "Swedish (Sweden)");
            TestJavaLocale(cultures, new Locale("th", "TH", ""), "Thai (Thailand)");
            TestJavaLocale(cultures, new Locale("th", "TH", "TH"),
                           "Thai (Thailand,TH)",
                           new Locale("th", "TH"));
            TestJavaLocale(cultures, new Locale("tr", "TR", ""), "Turkish (Turkey)");
            TestJavaLocale(cultures, new Locale("uk", "UA", ""), "Ukrainian (Ukraine)");
            TestJavaLocale(cultures, new Locale("vi", "VN", ""), "Vietnamese (Vietnam)");
            TestJavaLocale(cultures, new Locale("zh", "CN", ""), "Chinese (China)");
            TestJavaLocale(cultures, new Locale("zh", "HK", ""), "Chinese (Hong Kong)");
            TestJavaLocale(cultures, new Locale("zh", "TW", ""), "Chinese (Taiwan)");

            //foreach (CultureInfo info in cultures) {
            //    Console.WriteLine("remaining: "+info+" "+info.DisplayName+" "+info.TwoLetterISOLanguageName);
            //}
        }
        private void TestJavaLocale(HashSet<CultureInfo> cultures,
                                    Locale original,
                                    String display)
        {
            TestJavaLocale(cultures, original, display, null);
        }
        private void TestJavaLocale(HashSet<CultureInfo> cultures,
                                    Locale original,
                                    String display,
                                    Locale expected)
        {
            if (expected == null)
            {
                expected = original;
            }
            CultureInfo cinfo = original.ToCultureInfo();
            Locale actual = Locale.FindLocale(cinfo);
            Assert.AreEqual(expected, actual, display + " (" + original + ") " + " didn't map");
        }
    }
}
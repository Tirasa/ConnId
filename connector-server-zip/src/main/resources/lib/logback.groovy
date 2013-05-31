//
// Copyright (c) 2011-2013 ForgeRock Inc. All Rights Reserved

// The contents of this file are subject to the terms
// of the Common Development and Distribution License
// (the License). You may not use this file except in
// compliance with the License.

// You can obtain a copy of the License at
// http://www.opensource.org/licenses/cddl1.php or
// OpenIDM/legal/CDDLv1.0.txt
// See the License for the specific language governing
// permission and limitations under the License.

// When distributing Covered Code, include this CDDL
// Header Notice in each file and include the License file
// at OpenIDM/legal/CDDLv1.0.txt.
// If applicable, add the following below the CDDL Header,
// with the fields enclosed by brackets [] replaced by
// your own identifying information:
// "Portions Copyrighted 2011 [name of copyright owner]"

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.TRACE

appender("STDOUT", ConsoleAppender) {
  layout(PatternLayout) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}
logger("org.identityconnectors", TRACE)
root(DEBUG, ["STDOUT"])
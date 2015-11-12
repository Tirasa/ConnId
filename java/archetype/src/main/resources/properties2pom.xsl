<?xml version="1.0" encoding="UTF-8"?>
<!--

    ====================
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright 2015 ConnId. All rights reserved.

    The contents of this file are subject to the terms of the Common Development
    and Distribution License("CDDL") (the "License").  You may not use this file
    except in compliance with the License.

    You can obtain a copy of the License at
    http://opensource.org/licenses/cddl1.php
    See the License for the specific language governing permissions and limitations
    under the License.

    When distributing the Covered Code, include this CDDL Header Notice in each file
    and include the License file at http://opensource.org/licenses/cddl1.php.
    If applicable, add the following below this CDDL Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyrighted [year] [name of copyright owner]"
    ====================

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:m="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="m"
                version="1.0">
  <xsl:param name="connidVersion"/>
  
  <xsl:template match="/m:project/m:parent/m:version">
    <version>
      <xsl:value-of select="$connidVersion"/>
    </version>
  </xsl:template>

  <xsl:template match="/m:project/m:properties/m:connid.version">
    <connid.version>
      <xsl:value-of select="$connidVersion"/>
    </connid.version>
  </xsl:template>
  
  <xsl:template match="node()|@*|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

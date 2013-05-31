@echo off
rem -- START LICENSE
rem ====================
rem DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
rem
rem Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
rem
rem The contents of this file are subject to the terms of the Common Development
rem and Distribution License("CDDL") (the "License").  You may not use this file
rem except in compliance with the License.
rem
rem You can obtain a copy of the License at
rem http://opensource.org/licenses/cddl1.php
rem See the License for the specific language governing permissions and limitations
rem under the License.
rem
rem When distributing the Covered Code, include this CDDL Header Notice in each file
rem and include the License file at http://opensource.org/licenses/cddl1.php.
rem If applicable, add the following below this CDDL Header, with the fields
rem enclosed by brackets [] replaced by your own identifying information:
rem "Portions Copyrighted [year] [name of copyright owner]"
rem ====================
rem
rem Portions Copyrighted 2012 ForgeRock AS
rem
rem -- END LICENSE

SETLOCAL ENABLEDELAYEDEXPANSION

rem Set Connector Server Home
set CURRENT_DIR=%cd%
cd /d %0\..
set SCRIPT_DIR=%cd%
cd ..
if not "%CONNECTOR_SERVER_HOME%" == "" goto homeSet
set CONNECTOR_SERVER_HOME=%cd%
:homeSet
cd "%CURRENT_DIR%""
if exist "%CONNECTOR_SERVER_HOME%\bin\ConnectorServer.bat" goto homeOk
echo Invalid CONNECTOR_SERVER_HOME environment variable
echo Please set it to correct Connector Server Home
:homeOk

rem Check Java availability
if not "%JAVA_HOME%" == "" goto checkJavaHome
if not "%JRE_HOME%" == "" goto checkJavaHome
echo JAVA_HOME or JRE_HOME not available, Java is needed to run the Connector Server
echo Please install Java and set the JAVA_HOME accordingly
goto exit
:checkJavaHome
if exist "%JAVA_HOME%\bin\java.exe" goto javaHomeOk
if exist "%JRE_HOME%\bin\java.exe" goto jreHomeOk
echo Incorrect JAVA_HOME or JRE_HOME
goto exit
:jreHomeOk
set JAVA="%JRE_HOME%\bin\java.exe"
set JAVA_DLL="%JRE_HOME%\bin\server\jvm.dll"
goto homeOk
:javaHomeOk
set JAVA="%JAVA_HOME%\bin\java.exe"
set JAVA_DLL="%JAVA_HOME%\jre\bin\server\jvm.dll"
:homeOk

rem Set CLASSPATH for starting connector server
rem Only Java 6 supports wildcard (*)
rem set CP="lib/*;lib/framework/*"

rem setup the classpath
set CP=lib\framework\connector-framework.jar
set CP=%CP%;lib\framework\connector-framework-internal.jar
set CP=%CP%;lib\framework\groovy-all.jar
set CP=%CP%;lib\framework\icfl-over-slf4j.jar
set CP=%CP%;lib\framework\slf4j-api.jar
set CP=%CP%;lib\framework\logback-core.jar
set CP=%CP%;lib\framework\logback-classic.jar

echo %CP%

rem SET MISC PROPERTIES
rem Architecture, can be i386 or amd64 or ia64 (it is basically the directory name
rem   where the binaries are stored, if not set this script will try to
rem   find the value automatically based on environment variables)
set ARCH=
rem find out the architecture
if ""%ARCH%"" == """" (
  set ARCH=i386
  if ""%PROCESSOR_ARCHITECTURE%"" == ""AMD64"" set ARCH=amd64
  if ""%PROCESSOR_ARCHITECTURE%"" == ""IA64""  set ARCH=ia64
)

rem Run java options, separated by space
set JAVA_OPTS=-Xmx500m "-Djava.util.logging.config.file=conf\logging.properties" "-Dlogback.configurationFile=lib\logback.xml"

rem Service java options, needs to be separated by ;
set JAVA_OPTS_SERVICE=-Xmx500m;"-Dlogback.configurationFile=lib\logback.xml";
set MAIN_CLASS=org.identityconnectors.framework.server.Main
set SERVER_PROPERTIES_KEY=-properties
set SERVER_PROPERTIES="conf\ConnectorServer.properties"
set JVM_OPTION_IDENTIFIER=-J


if ""%1"" == ""/run"" goto srvRun
if ""%1"" == ""/setkey"" goto srvSetKey
if ""%1"" == ""/install"" goto srvInstall
if ""%1"" == ""/uninstall"" goto srvUninstall

echo Usage: ConnectorServer ^<command^> ^[option^]
echo command:
echo    /install ^[^<serviceName^>^] ^["-J<java option>"^] - Installs the service.
echo    /uninstall ^[^<serviceName^>^] - Uninstalls the service.
echo    /run ^["-J<java option>"^] - Runs the server from the console.
echo    /setkey ^[^<key^>^] - Sets the connector server key.
echo.
echo example:
echo     ConnectorServer.bat /run "-J-Djavax.net.ssl.keyStore=mykeystore.jks" "-J-Djavax.net.ssl.keyStorePassword=changeit"
echo        - this will run connector server with SSL
echo.
echo     ConnectorServer.bat /run "-J-Xdebug" "-J-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
echo        - this will run connector server in debug mode
goto :EOF

:srvRun
rem Run the server main class
shift
set JAVA_OPTS_PARAM=
set JAVA_OPTS_DELIM=
for %%P in (%*) do (
    set T=%%P
    if "!T:~1,2!" == "%JVM_OPTION_IDENTIFIER%" (
      set JAVA_OPTS_PARAM=!JAVA_OPTS_PARAM!!JAVA_OPTS_DELIM!!T:~3,-1!
      set JAVA_OPTS_DELIM=
    )
)
cd "%CONNECTOR_SERVER_HOME%"

%JAVA% %JAVA_OPTS% %JAVA_OPTS_PARAM% -server -classpath %CP% %MAIN_CLASS% -run %SERVER_PROPERTIES_KEY% %SERVER_PROPERTIES%
cd "%CURRENT_DIR%"
goto :EOF

:srvSetKey
rem Set Connector Server key in its properties file
shift
if not ""%1"" == """" goto keyOk
echo Please provide key you want to set.
goto exit
:keyOk
set key=%1
rem Sets key in the Identity Connectors Server properties file
%JAVA% %JAVA_OPTS% -classpath %CP% %MAIN_CLASS% -setkey -key %key% %SERVER_PROPERTIES_KEY% %SERVER_PROPERTIES%
set key=
goto :EOF

:srvInstall
rem Install the Connector Server as Windows service
shift
set SERVICE_NAME=OpenICFConnectorServerJava
if not ""%1"" == """" (
    set T=%1
    if "!T:~1,2!" == "%JVM_OPTION_IDENTIFIER%" goto :noServiceName
    set SERVICE_NAME=%1
)
shift
:noServiceName
set JAVA_OPTS_PARAM=
set JAVA_OPTS_DELIM=
for %%P in (%*) do (
    set T=%%P
    if "!T:~1,2!" == "%JVM_OPTION_IDENTIFIER%" (
      set JAVA_OPTS_PARAM=!JAVA_OPTS_PARAM!!JAVA_OPTS_DELIM!!T:~3,-1!
      set JAVA_OPTS_DELIM=;
    )
)
"%CONNECTOR_SERVER_HOME%\bin\%ARCH%\ConnectorServerJava.exe" //IS//%SERVICE_NAME% --Install="%CONNECTOR_SERVER_HOME%\bin\%ARCH%\ConnectorServerJava.exe" --Description="OpenICF Connectors Java Server" --Jvm=%JAVA_DLL% --Classpath=%CP% --JvmOptions=%JAVA_OPTS_SERVICE%%JAVA_OPTS_PARAM% --StartPath="%CONNECTOR_SERVER_HOME%" --StartMode=jvm --StartClass=%MAIN_CLASS% --StartParams="-run;%SERVER_PROPERTIES_KEY%;%SERVER_PROPERTIES%" --StopMode=jvm --StopClass=%MAIN_CLASS% --StopMethod=stop --StopParams=dummy --LogPath="%CONNECTOR_SERVER_HOME%\logs" --LogPrefix=service --StdOutput=auto --StdError=auto --LogLevel=INFO
echo Connector server successfully installed as "%SERVICE_NAME%" service
goto :EOF

:srvUninstall
shift
if not ""%1"" == """" (
    set SERVICE_NAME=%1
) else (
    set SERVICE_NAME=OpenICFConnectorServerJava
)
"%CONNECTOR_SERVER_HOME%\bin\%ARCH%\ConnectorServerJava.exe" //DS//%SERVICE_NAME%
echo Service "%SERVICE_NAME%" removed successfully
goto :EOF

:exit
exit /b 1

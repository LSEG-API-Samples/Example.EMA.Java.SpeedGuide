@echo off
setlocal

rem
rem The following batch file assumes the following environment:
rem 
rem		JAVA_HOME - Root directory of your JDK 8 environment
rem 	ELEKTRON_JAVA_HOME - Root directory of your (EMA) Elektron Java API installation
rem	
rem Note: The following build file is based on Elektron Java 1.3.0.L1.  You may need to 
rem       update the specific Jar files below as they may be named based on your version
rem       of EMA Java.  For example, ema-3.3.0.0.jar, upa-3.3.0.0.jar, etc.
rem

set EMA_HOME="%ELEKTRON_JAVA_HOME%"\Elektron-SDK-BinaryPack\Java\Ema\Libs
set ETA_HOME="%ELEKTRON_JAVA_HOME%"\Java\Eta\Libs
set JAVA_FX="%JAVA_HOME%"\jre\lib

rem Java runtime.  Default runtime in path.
set JAVA="%JAVA_HOME%\bin\java"

set CLASSPATH=.\bin;"%ELEKTRON_JAVA_HOME%"\Java\Ema\Libs\ema-3.3.0.0.jar;%EMA_HOME%\SLF4J\slf4j-1.7.12\slf4j-api-1.7.12.jar;%EMA_HOME%\SLF4J\slf4j-1.7.12\slf4j-jdk14-1.7.12.jar;%EMA_HOME%\apache\commons-configuration-1.10.jar;%EMA_HOME%\apache\commons-logging-1.2.jar;%EMA_HOME%\apache\commons-lang-2.6.jar;%EMA_HOME%\apache\commons-collections-3.2.2.jar;%ETA_HOME%\upa-3.3.0.0.jar;%ETA_HOME%\upaValueAdd-3.3.0.0.jar;"%JAVA_HOME%"\jre\lib\jfxswt.jar

echo Running Speed Guide...
%JAVA% -cp %CLASSPATH% com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuide %*

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

set SCRIPT=%0
set BINDIR=bin
set "EMA_HOME=%ELEKTRON_JAVA_HOME%\Elektron-SDK-BinaryPack\Java\Ema\Libs"
set "ETA_HOME=%ELEKTRON_JAVA_HOME%\Java\Eta\Libs"

rem Java Compiler.  Default compiler in path.
set JAVAC="%JAVA_HOME%\bin\javac"

set CLASSPATH=.\src;%ELEKTRON_JAVA_HOME%\Java\Ema\Libs\ema-3.3.0.0.jar;%EMA_HOME%\SLF4J\slf4j-1.7.12\slf4j-api-1.7.12.jar;%EMA_HOME%\SLF4J\slf4j-1.7.12\slf4j-jdk14-1.7.12.jar;%EMA_HOME%\apache\commons-configuration-1.10.jar;%EMA_HOME%\apache\commons-logging-1.2.jar;%EMA_HOME%\apache\commons-lang-2.6.jar;%EMA_HOME%\apache\commons-collections-3.2.2.jar;%ETA_HOME%\upa-3.3.0.0.jar;%ETA_HOME%\upaValueAdd-3.3.0.0.jar;%JAVA_HOME%\jre\lib\jfxswt.jar

if not exist %BINDIR% (mkdir %BINDIR%)

echo Building Speed Guide...
%JAVAC% -d %BINDIR% src\com\thomsonreuters\ema\example\gui\SpeedGuide\*.java
%JAVAC% -d %BINDIR% src\com\thomsonreuters\ema\example\gui\SpeedGuide\view\*.java
if %errorlevel% neq 0 goto :ERROR

rem Copy our configuration files over
copy /Y src\com\thomsonreuters\ema\example\gui\SpeedGuide\view\*.fxml bin\com\thomsonreuters\ema\example\gui\SpeedGuide\view\.
copy /Y src\com\thomsonreuters\ema\example\gui\SpeedGuide\view\*.css bin\com\thomsonreuters\ema\example\gui\SpeedGuide\view\.
goto :DONE

:ERROR
echo.
echo Build failed.  Exiting.
goto :EOF

:DONE
echo.
echo Done.

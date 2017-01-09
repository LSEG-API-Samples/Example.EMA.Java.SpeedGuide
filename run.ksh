#!/bin/ksh

#
# The following batch file assumes the following environment:
# 
#	JAVA_HOME - Root directory of your JDK 8 environment
# 	ELEKTRON_JAVA_HOME - Root directory of your (EMA) Elektron Java API installation
#

JAVA=$JAVA_HOME/bin/java
EMA_HOME=$ELEKTRON_JAVA_HOME/Ema
ETA_HOME=$ELEKTRON_JAVA_HOME/Eta

CLASSPATH=./bin:$EMA_HOME/Libs/ema.jar:$EMA_HOME/Libs/SLF4J/slf4j-1.7.12/slf4j-api-1.7.12.jar:$EMA_HOME/Libs/SLF4J/slf4j-1.7.12/slf4j-jdk14-1.7.12.jar:$EMA_HOME/Libs/apache/commons-configuration-1.10.jar:$EMA_HOME/Libs/apache/commons-logging-1.2.jar:$EMA_HOME/Libs/apache/commons-lang-2.6.jar:$ETA_HOME/Libs/upa.jar:$ETA_HOME/Libs/upaValueAdd.jar:$JAVA_HOME/jre/lib/jfxswt.jar

function run
{
   printf "Running SpeedGuide...\n"
   $JAVA -cp $CLASSPATH com.thomsonreuters.ema.example.gui.SpeedGuide.SpeedGuide $*
}

run


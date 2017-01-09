#!/bin/ksh

#
# The following batch file assumes the following environment:
# 
#	JAVA_HOME - Root directory of your JDK 8 environment
# 	ELEKTRON_JAVA_HOME - Root directory of your (EMA) Elektron Java API installation
#

BINDIR=bin
if [ ! -d $BINDIR ]; then
  mkdir $BINDIR
fi

JAVAC=$JAVA_HOME/bin/javac
EMA_HOME=$ELEKTRON_JAVA_HOME/Ema
ETA_HOME=$ELEKTRON_JAVA_HOME/Eta

CLASSPATH=./src:$EMA_HOME/Libs/ema.jar:$EMA_HOME/Libs/SLF4J/slf4j-1.7.12/slf4j-api-1.7.12.jar:$EMA_HOME/Libs/SLF4J/slf4j-1.7.12/slf4j-jdk14-1.7.12.jar:$EMA_HOME/Libs/apache/commons-configuration-1.10.jar:$EMA_HOME/Libs/apache/commons-logging-1.2.jar:$EMA_HOME/Libs/apache/commons-lang-2.6.jar:$ETA_HOME/Libs/upa.jar:$ETA_HOME/Libs/upaValueAdd.jar:$JAVA_HOME/jre/lib/jfxswt.jar

function build
{
   printf "Building Speed Guide...\n"
   $JAVAC -d $BINDIR src/com/thomsonreuters/ema/example/gui/SpeedGuide/*.java; ret=$?

   if [ $ret != 0 ]; then
      printf "Build failed.  Exiting\n"
      exit $ret
   fi
}

build

# Copy our configuration files over
cp src/com/thomsonreuters/ema/example/gui/SpeedGuide/view/*.fxml bin/com/thomsonreuters/ema/example/gui/SpeedGuide/view/.
cp src/com/thomsonreuters/ema/example/gui/SpeedGuide/view/*.css bin/com/thomsonreuters/ema/example/gui/SpeedGuide/view/.
printf "\nDone.\n"

#!/usr/bin/env bash

JAVA_OPTS="$JAVA_OTPS -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M"
JAR_FILENAME="zenith-ui-2.1.4-SNAPSHOT.jar"
JAR_PATH=

if [ ! -e $JAR_FILENAME ]; then
  JAR_PATH="zenith-ui/target/"
fi

java $JAVA_OPTS -jar $JAR_PATH$JAR_FILENAME
#!/usr/bin/env bash

JAVA_OPTS="$JAVA_OTPS -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M"

if [ "$1" = "--debug" ]; then
  JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
  shift
fi

JAR_FILENAME="zenith-ui-2026.1.jar"
JAR_PATH=

if [ ! -e $JAR_FILENAME ]; then
  JAR_PATH="zenith-ui/target/"
fi

java $JAVA_OPTS -jar $JAR_PATH$JAR_FILENAME

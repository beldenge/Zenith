#!/usr/bin/env bash

JAVA_OPTS="$JAVA_OTPS -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M"

java $JAVA_OPTS -jar zenith-ui-2.1.4-SNAPSHOT.jar
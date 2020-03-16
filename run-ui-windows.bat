set JAVA_OPTS=%JAVA_OPTS% -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M

java %JAVA_OPTS% -jar zenith-ui-2.0.1-SNAPSHOT.jar
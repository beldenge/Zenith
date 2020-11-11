set JAVA_OPTS=%JAVA_OPTS% -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M
set JAR_FILENAME=zenith-ui-2.1.4-SNAPSHOT.jar
set JAR_PATH=

if not exist %JAR_FILENAME% (
    set JAR_PATH=zenith-ui\target\
)

java %JAVA_OPTS% -jar %JAR_PATH%%JAR_FILENAME%
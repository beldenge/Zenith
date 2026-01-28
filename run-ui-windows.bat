set JAVA_OPTS=%JAVA_OPTS% -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M

if "%1"=="--debug" (
    set JAVA_OPTS=%JAVA_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
    shift
)

set JAR_FILENAME=zenith-ui-2026.1.1-SNAPSHOT.jar
set JAR_PATH=

if not exist %JAR_FILENAME% (
    set JAR_PATH=zenith-ui\target\
)

java %JAVA_OPTS% -jar %JAR_PATH%%JAR_FILENAME%

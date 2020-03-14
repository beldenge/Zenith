# Description
This module exposes the functionality of Zenith as a REST API.  It is used as a dependency by the zenith-ui module and therefore does not need to be started independently if being used with the web UI.

# Running Standalone
1. Download and install Java 8 or later: [AdoptOpenJDK](https://adoptopenjdk.net/)
2. Download zenith-api-2.0.0-SNAPSHOT-exec.jar
3. Issue the command `java -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M -jar zenith-api-2.0.0-SNAPSHOT-exec.jar`

Note: You must run the *-exec.jar and not the vanilla jar file, as this module is used both as a dependency and as a runnable application on its own.

# API Documentation
After starting the application, you can view the API documentation at the following URL, which will allow you to explore the endpoints in a web UI:
http://localhost:8080/swagger-ui.html
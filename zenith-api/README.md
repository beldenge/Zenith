# Description
This module exposes the functionality of Zenith as a GraphQL API.  It is used as a dependency by the zenith-ui module and therefore does not need to be started independently if being used with the web UI.

# Running Standalone
1. Download and install Java 25 or later: [Amazon Corretto](https://aws.amazon.com/corretto/)
2. Download zenith-api-2.1.4-SNAPSHOT-exec.jar
3. Issue the command `java -XX:+UseParallelGC -XX:ParallelGCThreads=2 -Xms2G -Xmx2G -XX:MaxMetaspaceSize=512M -jar zenith-api-2.1.4-SNAPSHOT-exec.jar`

Note: You must run the *-exec.jar and not the vanilla jar file, as this module is used both as a dependency and as a runnable application on its own.

# API Documentation
After starting the application, you can explore the schema in GraphiQL at:
http://localhost:8080/graphiql

The GraphQL endpoint is available at:
http://localhost:8080/graphql

Subscriptions use the same `/graphql` endpoint over WebSocket.
# DB Example

This repository contains a DB based sample application to illustrate
a possibility how to integrate a relational database in AEM 6.

To build run

    mvn clean install

To install on a local AEM 6 instance running on port `4502`

    mvn clean install -P installPackage

For development it is also useful to only update the bundle

    mvn clean install -P installBundle -pl resource-provider
    
Testing CRUD with curl:

    curl -X POST -u admin:admin -F"./userid=julian -F"./n -F"./name=Julian Sedding" "http://localhost:4502/examples/db/accounts/*"

By default an in-memory H2 database is created due to the connection url configured in

    content/src/main/content/jcr_root/apps/db-example/config/org.apache.sling.datasource.DataSourceFactory-db-example.config

To configure a persistent database, adjust the `url` property in this file, e.g.

    url="jdbc:h2:~/db-example-h2"
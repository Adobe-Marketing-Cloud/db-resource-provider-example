# DB Example

This repository contains a DB based sample application to illustrate
a possibility how to integrate a relational database in AEM 6.

To build run

    mvn clean install

To install on a local AEM 6 instance running on port `4502`

    mvn clean install -P installPackage

For development it is also useful to only update the bundle

    mvn clean install -P installBundle -pl resource-provider

Testing CRUD with curl

Create:

    curl -X POST -u admin:admin -F"./userid=john" -F"./name=John Doe" -F"./email=jdoe@example.org" -F":nameHint=john" "http://localhost:4502/examples/db/accounts/*"

Read (in the browser):

    http://localhost:4502/examples/db/accounts.tidy.-1.json
    or
    http://localhost:4502/examples/db/accounts/john.tidy.-1.json

Update:

    curl -X POST -u admin:admin -F"./balance=100" "http://localhost:4502/examples/db/accounts/john"

Delete:

    curl -X POST -u admin:admin -F":operation=delete" "http://localhost:4502/examples/db/accounts/john"

Rendering the result in a browser:

    http://localhost:4502/examples/db/accounts.tidy.-1.json

By default an in-memory H2 database is created due to the connection url configured in

    content/src/main/content/jcr_root/apps/db-example/config/org.apache.sling.datasource.DataSourceFactory-db-example.config

To configure a persistent database, adjust the `url` property in this file, e.g.

    url="jdbc:h2:~/db-example-h2"

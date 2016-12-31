# Light Oauth2 Enterprise Edition - Oracle 

An OAuth2 service provider based on [light-java](https://github.com/networknt/light-java)
and Oracle database. 

The codebase can be found at branch oracle at [light-oauth2](https://github.com/networknt/light-oauth2)

If you want to use ouath2 server for development, it is recommended to use development edition
which doesn't have any database or other dependencies. It can be found in dev branch.

# Architecture

### Microservices

It has 6 microservices and each serivce has several endpoints to support user login, 
access token, user registration, service registration, client registration and public 
key certificate distribution. It can support millions users and thousands of clients 
and services with scopes. It should be easily handle thousands of concurrent users per
instance and each service can be scaled individually if necessary.

### In-Memory Data Grid

Hazelcast is used as Data Grid across multiple services and majority of operations
won't hit database server for best performance.

### Multiple Databases

Currently, it supports Oracle, Mysql and Postgres for production and H2 for End-to-End
testing. Other databases can be easily plugged in if there are demands.

Due to licensing issue, we are using Oracle XE 11g for development and expect end user
to have licensed Oracle database installed in their data center.

Mysql and Postgres are open sourced and free for commercial use and they are highly
recommended.

Database script can be found at db folder.

# Specifications

OAuth2 services specifications can be found at https://github.com/networknt/swagger
in folders start with oauth2.


# Start Services

To start services and test them in development mode. 

```
git clone git@github.com:networknt/light-oauth2.git
cd light-oauth2
git checkout oracle
mvn clean install -DskipTests
docker-compose up
```

It will take about 30 seconds to have all services and database up and running. 

If you have modified source code, please follow the steps to restart services. 
```
docker-compose down
mvn clean install
./cleanup.sh
docker-compose up
```


# Test Services

By default, the security is partially disabled on these services so that users
can easily test these services to learn how to use them. 

### Code

This is the service that takes user's credentials and redirect back authorization
code to webserver. 

There are two endpoints: /oauth2/code@get and /oauth2/code@post

The GET endpoint uses Basic Authorization and POST endpoint uses Form Authorization.

In most of the cases, you should use GET endpoint as it provides popup window on
the browser to ask username and password. 

POST endpoint is usually used with existing web server that provides login form and
post the user credentials to this endpoint to get authorization code indirectly. It
requires customization most of the time.

There is only one admin user after the system is installed and the default password
is "123456". The password needs to be reset immediately with User Service for
production.

To get authorization code put the following url into your browser.

```
http://localhost:6881/oauth2/code?response_type=code&client_id=f7d42348-c647-4efb-a52d-4c5787421e72&redirect_uri=http://localhost:8080/authorization
```

If this is the first time on this browser, you will have a popup window for user
credentials. Now let's use admin/123456 to login given you haven't reset the password
yet for admin user.

Once authentication is completed, an authorization code will be redirect to your
browser. Something like the following.

```
http://localhost:8080/authorization?code=pVk10fdsTiiJ1HdUlV4y1g
```

### Token

This service has only one endpoint to get access token. It supports authorization
code grant type and client credentials grant type. 

Authorization Grant with authorization code redirected in the previous step.
 
```

```


### Service



### Client



### User



### Key




# Production

For production, we have created docker images on docker hub for every service
with different database support.

Here is an example of docker-compose.yml that use Oracle database. Please note
that you need to have service.json externalized to point to your Oracle DB.

```

```

# Key Generation
You can use keytool or openssl to generate key pairs to sign and verify JWT tokens.
Here is an example of keytool. 

```
keytool -genkey -keyalg RSA -alias selfsigned -keystore primary.jks -storepass password -validity 3600 -keysize 2048

keytool -export -alias selfsigned -keystore primary.jks -rfc -file primary.crt

```

Please note that you can use the default key pairs for development, but cannot
use them on production. You must buy certificates from CA or generate key pairs
with above tool.


# Long live JWT token for testing

The light-oauth2 contains two testing key pairs for testing only. Both private keys and public key certificates
can be found in resources/config/oauth folder. The same public key certificates are included in light-java so that
the server can verify any token issued by this oauth server.

Important note:
For your official deployment, please create key pair of your own or buy certificate from one of
the CAs.

The following is a token generated for petstore api with scope write:pets and read:pets

```
Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5NDgwMDYzOSwianRpIjoiWFhlQmpJYXUwUk5ZSTl3dVF0MWxtUSIsImlhdCI6MTQ3OTQ0MDYzOSwibmJmIjoxNDc5NDQwNTE5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.f5XdkmhOoHT2lgTobqVGPp2aWUv_ItA0tqyLHC_CeMbmwzPvREqb5-oJ9T_m3VwRcJlPTh8xTdSjrLITXClaQFE4Y0bT8C-u6bb38uT-NQ5mjUjLrFQYHCF6GqwL7YkwQt_rshEqtrDFe1T4HoEL_9FHbOxf3MSJ39UKq0Ef_9mHXkn4Y-SHfdapeuUWc_4dDPdxzEdzbqmf1WSOOgTuM5O5F2fK4p_ix8LQl0H3AnMZIhIDyygQEnYPxEG-u35gwh503wfxio6buIf0b2Kku2PXPE36lethZwIVaPTncEcY5OPxfBxXuy-Wq-YQizd7NnpJTteHYbdQXupjK7NDvQ
```

# Build server

The codebase has dependencies with Oracle database and you have to manually download
oracle driver and install it in your local .m2 repo. 

to download the driver go to [here](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html)

to install it.



# Start a standalone server

Given you have JDK8 and Maven 3 installed.

```
git clone https://github.com/networknt/light-oauth2.git
cd light-oauth2
mvn install exec:exec

```

In order to start the server with externalized configuration.

```
java -jar -D target/oauth2
```

# Start a docker container

with default configuration

```
docker run -d -p 8888:8888 networknt/oauth2-server
```

with externalized configuration in /home/steve/tmp/config/oauth2 folder
```
docker run -d -v /home/steve/tmp/config/oauth2:/config -p 8888:8888 networknt/oauth2-server
```

# Token endpoint
/oauth2/token can be used to get JWT access token. Here is one of the responses.


```
{"access_token":"eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTQ3MjgzNTE0NiwianRpIjoidko5NnZVWFVoTmd3a29OWkhHWnZHdyIsImlhdCI6MTQ3MjgzNDU0NiwibmJmIjoxNDcyODM0NDI2LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJhYWFhYWFhYS0xMjM0LTEyMzQtMTIzNC1iYmJiYmJiYiIsInNjb3BlIjpbImFwaS5yIiwiYXBpLnciXX0.ZAIUYASDUO_4g9hmWFNYy4Zg1oDg-m3nvIGJAU7zUaWs8wt_a8FSCfwsfzhEe1EBjajnvTzGkSYOi2gwkyDVLoXN0tAfgrbFCFrR-LtNV9KWy82-HF1sYzIgx6M0-7PigVHqIacjdKmPgsA4GmNiG5AoMjoCYllJaISOmdSu6z6SD2APhHBlJcZFuMDjCaX-TNfesW7cHzLrcppGIwwGSCMlt8KEvmQBOKizpWcsj2MhvQmvjhFr7v6yU1h6o1So3w1NCFDK421Qwx4Pcbew912dJ9dOOOdQ4IbmI3757VF88QeJbI8SgjzlMX3t6KPLtyBkGs9geAU40Ui7pjzROQ"}
```


# Code endpoint
/oauth2/code can be used to get authorization code. The code is redirect to the uri specified by the
client. Here is an example of redirected uri.

```
http://localhost:8080/oauth?code=Gp6GHT02SJ6G_-wyvaMNPw
```

# User login

When using authorization flow, the client application will redirect to authorization code endpoint on
OAuth2 server, the server will authenticate the user by poping up a login page. Please use the
following builtin credentials:

username: stevehu

password: 123456


# Admin interface

Not implemented yet. If you want to add new client or new user, please update clients.json and users.json
in config folder. Also, the config folder can be externalized for you standalone instance or docker
container instance.

# Further info

[Wiki - OAuth2 Introduction](https://github.com/networknt/light-oauth2/wiki/OAuth2-Introduction)


How to start the oauth2 server as standalone Java application

https://youtu.be/MZfRH-AAzWU

How to start the oauth2 server in docker container

https://youtu.be/w0a8f0hJVmU

How to customize the oauth2 server

https://youtu.be/eq1BxjDFg6o


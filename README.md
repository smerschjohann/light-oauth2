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

Authorization Grant with authorization code redirected in the previous step. Please
replace code with the newly retrieved one as it is only valid for 10 minutes.
 
```
curl -H "Authorization: Basic f7d42348-c647-4efb-a52d-4c5787421e72:f6h1FTI8Q3-7UScPZDzfXA" -H "Content-Type: application/x-www-form-urlencoded" -X POST -d "grant_type=authorization_code&code=c0iAfPAeTk2BpiPWj-CYPQ" http://localhost:6882/oauth2/token
```

The above command will have the following output.

```
{"access_token":"eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTQ4MzIwOTQ4NiwianRpIjoib0dtdXEzSl85d0tlOUVIT2RWM21PUSIsImlhdCI6MTQ4MzIwODg4NiwibmJmIjoxNDgzMjA4NzY2LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6ImFkbWluIiwidXNlcl90eXBlIjoiYWRtaW4iLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJwZXRzdG9yZS5yIiwicGV0c3RvcmUudyJdfQ.gQ5HI2drObxorsQvz86RYT5tgk7QCnEBm9zNod7SbC--v8s4OfFIM4FQbxGqlMzbU3_dDXiyMSGzOFD_ShZ5se9W2FLxLjbMmBJwQG89peymcdY2mTgQoKJMYxL602a7cloyuoDZ_l-OQSj6RMdgRw4FKmMdOqMKWauoh58faZqvHgGxk43hlKW4bBy4vqg2IhNsUm_vIf-SVAUAMqp0Birt94FfjM3QSCQfwHXfK1nCWjFvfRIoN6w7XrPDQtnZq_8Mhdv8dNwowDLoYayKoUpr7i84gFA11-J1gocJOALj1kYody6kU5CfMwGOSX90PUEmdVy_3WnyEAp3blC-Iw","token_type":"bearer","expires_in":600} 
```

Client Credentials grant doesn't need authorization code but only client_id and
client_secret. Here is the curl command line to get access token.

```
curl -H "Authorization: Basic f7d42348-c647-4efb-a52d-4c5787421e72:f6h1FTI8Q3-7UScPZDzfXA" -H "Content-Type: application/x-www-form-urlencoded" -X POST -d "grant_type=client_credentials" http://localhost:6882/oauth2/token
```

The above command will have the following output.

```
{"access_token":"eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTQ4MzIwOTc1NywianRpIjoiOVhWdGV2dXZ2cjMwQ0lnZVFuUTFUUSIsImlhdCI6MTQ4MzIwOTE1NywibmJmIjoxNDgzMjA5MDM3LCJ2ZXJzaW9uIjoiMS4wIiwiY2xpZW50X2lkIjoiZjdkNDIzNDgtYzY0Ny00ZWZiLWE1MmQtNGM1Nzg3NDIxZTcyIiwic2NvcGUiOlsicGV0c3RvcmUuciIsInBldHN0b3JlLnciXX0.C8oHgjKpaKWAYJvSqZ4_VT2sw8XXpABFq-aXgNUN2mCEKZJN7AkA6qio0fK4ZCTn5lT9bLou6SOEDV-uXvcU1_XlvKTTnbMO2g-s_7-O-xXxSCAXiLZ-5C7ieGt7enQrxrESUEsgr0Kow4a34GjxAod5j0vcKzhZ6vrcQcuCecPKaeovV0nkBZH2cGPhaLvK346RA9VjxITcR1DgzPWIO3AYJGaIrF8-mCA6Ad8LNi8mB0T5pHIST5fpVTsDYF3KjQJKYiwEhVMbfErBrsmiUUHJ7fYNi5ntLvT-61rupqrQeudl54gg4onct6rT9A2HmuV0iucECkwm9urJ2QxO-A","token_type":"bearer","expires_in":600}
```

If you are interested, you can compare the claims of above tokens at https://jwt.io/

### Service

OAuth2 is used to protect services and each service must register itself with scope in
order to have fine-grained access control. This microservice provides endpoint to add,
update, remove and query services. 

To add a new service.

```
curl -H "Content-Type: application/json" -X POST -d '{"serviceId":"AACT0003","serviceType":"ms","serviceName":"Retail Account","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","ownerId":"admin"}' http://localhost:6883/oauth2/service
```

To query all services.

```
curl http://localhost:6883/oauth2/service

```
And here is the result.

```
[{"serviceType":"ms","serviceDesc":"A microservice that serves account information","scope":"a.r b.r","serviceId":"AACT0001","serviceName":"Account Service","ownerId":"admin","updateDt":null,"createDt":"2016-12-31"},{"serviceType":"ms","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","serviceId":"AACT0003","serviceName":"Retail Account","ownerId":"admin","updateDt":null,"createDt":"2016-12-31"}]
```

To query a service with service id.

```
curl http://localhost:6883/oauth2/service/AACT0003

```
And here is the result.
```
{"serviceType":"ms","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","serviceId":"AACT0003","serviceName":"Retail Account","ownerId":"admin"}
```

To update above service type to "api".

```
curl -H "Content-Type: application/json" -X PUT -d '{"serviceType":"api","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","serviceId":"AACT0003","serviceName":"Retail Account","ownerId":"admin"}' http://localhost:6883/oauth2/service
```

To delete above service with service id.

```
curl -X DELETE http://localhost:6883/oauth2/service/AACT0003

```

### Client

Every client that accesses service(s) must register itself in order to get
access token during runtime. An entity might be a client and service at the
same time and in this case, it must register twice as client and service.

To add a new client.

```
curl -H "Content-Type: application/json" -X POST -d '{"clientType":"mobile","clientName":"AccountViewer","clientDesc":"Retail Online Banking Account Viewer","scope":"act.r act.w","redirectUrl": "http://localhost:8080/authorization","ownerId":"admin"}' http://localhost:6884/oauth2/client
```

And here is the result with client_id and client_secret.

```
{"clientDesc":"Retail Online Banking Account Viewer","clientType":"mobile","redirectUrl":"http://localhost:8080/authorization","clientId":"e24e7110-c39f-49f1-85eb-8434cb577482","clientName":"AccountViewer","scope":"act.r act.w","clientSecret":"YDJLse8SQRapHyoMsdPUig","ownerId":"admin","createDt":"2016-12-31"}
```

To query all clients.

```
curl http://localhost:6884/oauth2/client

```
And here is the result.

```
[{"clientDesc":"PetStore Web Server that calls PetStore API","clientId":"f7d42348-c647-4efb-a52d-4c5787421e72","clientType":"server","redirectUrl":"http://localhost:8080/authorization","clientName":"PetStore Web Server","scope":"petstore.r petstore.w","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null},{"clientDesc":"Retail Online Banking Account Viewer","clientId":"9ef89c7b-f17b-4a64-a24b-ce539ed80641","clientType":"mobile","redirectUrl":"http://localhost:8080/authorization","clientName":"AccountViewer","scope":"act.r act.w","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null}]
```

To query a client by id.

```
curl http://localhost:6884/oauth2/client/f7d42348-c647-4efb-a52d-4c5787421e72
```

And here is the result.

```
{"clientDesc":"PetStore Web Server that calls PetStore API","clientId":"f7d42348-c647-4efb-a52d-4c5787421e72","clientType":"server","redirectUrl":"http://localhost:8080/authorization","clientName":"PetStore Web Server","scope":"petstore.r petstore.w","clientSecret":"f6h1FTI8Q3-7UScPZDzfXA","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null}
```

To update a client with a shorter clientDesc.

```
curl -H "Content-Type: application/json" -X PUT -d '{"clientDesc":"PetStore Web Server","clientId":"f7d42348-c647-4efb-a52d-4c5787421e72","clientType":"server","redirectUrl":"http://localhost:8080/authorization","clientName":"PetStore Web Server","scope":"petstore.r petstore.w","clientSecret":"f6h1FTI8Q3-7UScPZDzfXA","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null}' http://localhost:6884/oauth2/client
```

To delete a client with client id.

```
curl -X DELETE http://localhost:6884/oauth2/client/9ef89c7b-f17b-4a64-a24b-ce539ed80641

```


### User

The OAuth2 services can be integrated with existing Active Directory, LDAP or customer
database for authentication. If there is no existing authentication service, you can
register users into database.

To add a new user.

```
curl -H "Content-Type: application/json" -X POST -d '{"userId":"stevehu","userType":"employee","firstName":"Steve","lastName":"Hu","email":"stevehu@gmail.com","password":"123456","passwordConfirm":"123456"}' http://localhost:6885/oauth2/user
```

To query a user.

```
curl http://localhost:6885/oauth2/user/stevehu

```

And here is the result.

```
{"firstName":"Steve","lastName":"Hu","userType":"employee","userId":"stevehu","email":"stevehu@gmail.com"}
```

To update the user type to partner.

```
curl -H "Content-Type: application/json" -X PUT -d '{"firstName":"Steve","lastName":"Hu","userType":"partner","userId":"stevehu","email":"stevehu@gmail.com"}' http://localhost:6885/oauth2/user
```

To reset the password.

```
curl -H "Content-Type: application/json" -X POST -d '{"password":"123456","newPassword":"stevehu","newPasswordConfirm":"stevehu"}' http://localhost:6885/oauth2/password/stevehu
```

To remove a user.

```
curl -X DELETE http://localhost:6885/oauth2/user/stevehu

```


### Key

Light-Java and Light-OAuth2 support distributed security verification and this
requires the JWT public key certificate to be distributed to all services. By
default, all services built on top of Light-Java will include a set of 
certificates. But how to distributed new certificates to thousands of running
services if certificates are renewed? There is no way we can copy certificates
to all the running containers as they are dynamic and new containers can be
started anytime by container orchestration tool. 

The traditional push approach is not working and a new way of pull certificates
from OAuth2 key service is implemented in Light-Java and Light-OAuth2.

This feature is tightly integrated with Light-Java and it should work seamlessly.

The first step to get certificate is to encode client_id:client_secret pair for
basic authentication. 

Here is the client_id:client_secret

```
f7d42348-c647-4efb-a52d-4c5787421e72:f6h1FTI8Q3-7UScPZDzfXA
```

Go to https://www.base64encode.org/ to encode it to

```
ZjdkNDIzNDgtYzY0Ny00ZWZiLWE1MmQtNGM1Nzg3NDIxZTcyOmY2aDFGVEk4UTMtN1VTY1BaRHpmWEE=
```

To get certificate by a key id.

```
curl -H "Authorization: Basic ZjdkNDIzNDgtYzY0Ny00ZWZiLWE1MmQtNGM1Nzg3NDIxZTcyOmY2aDFGVEk4UTMtN1VTY1BaRHpmWEE=" http://localhost:6886/oauth2/key/101
```

And here is the result.

```
-----BEGIN CERTIFICATE-----
MIIDkzCCAnugAwIBAgIEUBGbJDANBgkqhkiG9w0BAQsFADB6MQswCQYDVQQGEwJDQTEQMA4GA1UE
CBMHT250YXJpbzEQMA4GA1UEBxMHVG9yb250bzEmMCQGA1UEChMdTmV0d29yayBOZXcgVGVjaG5v
bG9naWVzIEluYy4xDDAKBgNVBAsTA0FQSTERMA8GA1UEAxMIU3RldmUgSHUwHhcNMTYwOTIyMjI1
OTIxWhcNMjYwODAxMjI1OTIxWjB6MQswCQYDVQQGEwJDQTEQMA4GA1UECBMHT250YXJpbzEQMA4G
A1UEBxMHVG9yb250bzEmMCQGA1UEChMdTmV0d29yayBOZXcgVGVjaG5vbG9naWVzIEluYy4xDDAK
BgNVBAsTA0FQSTERMA8GA1UEAxMIU3RldmUgSHUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK
AoIBAQCqYfarFwug2DwpG/mmcW77OluaHVNsKEVJ/BptLp5suJAH/Z70SS5pwM4x2QwMOVO2ke8U
rsAws8allxcuKXrbpVt4evpO1Ly2sFwqB1bjN3+VMp6wcT+tSjzYdVGFpQAYHpeA+OLuoHtQyfpB
0KCveTEe3KAG33zXDNfGKTGmupZ3ZfmBLINoey/X13rY71ITt67AY78VHUKb+D53MBahCcjJ9YpJ
UHG+Sd3d4oeXiQcqJCBCVpD97awWARf8WYRIgU1xfCe06wQ3CzH3+GyfozLeu76Ni5PwE1tm7Dhg
EDSSZo5khmzVzo4G0T2sOeshePc5weZBNRHdHlJA0L0fAgMBAAGjITAfMB0GA1UdDgQWBBT9rnek
spnrFus5wTszjdzYgKll9TANBgkqhkiG9w0BAQsFAAOCAQEAT8udTfUGBgeWbN6ZAXRI64VsSJj5
1sNUN1GPDADLxZF6jArKU7LjBNXn9bG5VjJqlx8hQ1SNvi/t7FqBRCUt/3MxDmGZrVZqLY1kZ2e7
x+5RykbspA8neEUtU8sOr/NP3O5jBjU77EVec9hNNT5zwKLevZNL/Q5mfHoc4GrIAolQvi/5fEqC
8OMdOIWS6sERgjaeI4tXxQtHDcMo5PeLW0/7t5sgEsadZ+pkdeEMVTmLfgf97bpNNI7KF5uEbYnQ
NpwCT+NNC5ACmJmKidrfW23kml1C7vr7YzTevw9QuH/hN8l/Rh0fr+iPEVpgN6Zv00ymoKGmjuuW
owVmdKg/0w==
-----END CERTIFICATE-----
```


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


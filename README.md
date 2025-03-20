# Cambridge Digital Library: Content Loader

This is a web application providing a GUI for editing and loading data into the Cambridge Digital Library Platform.
It edits data in the cudl-data-source s3 buckets (prefixed with dev-, staging- and production-).
Once this data is edited it triggers processing, see https://github.com/cambridge-collection/data-lambda-transform which has
details on how the data is processed.

## Want to just start the content loader on a folder of data?
If you are new to the content loader, start by using the sample
data set instead of using data ina s3 bucket. You can run the following commands to
bring in the sample data, and run the client:

    git submodule init

You should see some sample data in the directory `docker/dl-data-samples/source-data`

Then you can build the client and run it using the commands:

    mvn clean package

This should produce a target directory with a war file in.

Then you can run the command:

    docker-compose  --env-file example.env --file docker-compose-sample-data.yml up

and the content loader should start on http://localhost:8081 and you will need to log in using the following:

    username: test-all
    password: password

This is set in the file `docker/dl-loading-db/resources/example_user_data.sql` and can be changed through the user management section.

### Ready for getting the edited data into the viewer?

The content loader works on the "source data"
(see `docker/dl-data-samples/source-data` for an example) and it needs to be transformed into the processed data
(see `docker/dl-data-samples/processed-data` for an example).  In order to do this we have a Terraform script to create a
pipeline in AWS to transform the data in real-time here: https://github.com/cambridge-collection/cudl-terraform.  If you prefer to
do this transformation yourself we have XSLT here: https://github.com/cambridge-collection/cudl-data-processing-xslt which
transforms the TEI into the JSON format we use for the viewer.

### Cambridge developer?

Use:
        `docker-compose --env-file <your env file> --file docker-compose-cudl-data.yml`

## Running with S3 buckets

For this you will need to make sure the env file you are using points to a valid s3 bucket which you have access to
and have created an access key, with permission to write to that bucket.
The bucket should contain the data in the same format as shown in the `docker/dl-data-samples/source-data`.

1. Build the project:

        $ mvn clean package
        $ docker build --network=host .

2. Copy the `example.env` file and customise by adding specific s3 bucket details and access keys.

3. Run using the command:

       docker-compose --env-file <your_.env_file> up


alternatively you can enable hot reloading (for html) using the command:

  `docker-compose --file docker-compose-hot.yml --env-file <your_.env_file> up`


4. The application will start an HTTP server listening on http://localhost:8081. (or an alternative port if config was changed)


NOTE: It currently takes AGES for the servers to start.  This is when SSL is enabled. Needs investigating. Be prepared
when deploying to staging/dev.

[Externalized Configuration]: https://docs.spring.io/spring-boot/docs/2.1.8.RELEASE/reference/html/boot-features-external-config.html

ALSO: If you're deploying for CUDL, the env files for staging and dev (there is no production deployment) are in the KeePass file.



## Developing

The Maven build is the definitive way to compile/test the project. Your IDE should be able to import the project from the `pom.xml`.

In addition, compile-time annotation processing is used by the [Immutables] library. This works automatically with Maven, but **your IDE may need some configuration to enable & configure annotation processing feature of the java compiler**.

[Immutables]: https://immutables.github.io/

* Instructions for enabling compile-time annotation processing in IDEs are available here: https://immutables.github.io/apt.html
* The annotation processor is expected to generate code at:
    * `target/generated-sources/annotations`
    * `target/generated-test-sources/test-annotations`
* These locations are added to the build path via the `build-helper-maven-plugin` and should be picked up by IDEs automatically

## Configuration

Configuration is set through .env file which defines a number of environment variables that
are passed to the application on building and running.

Copy the `example.env` file and make your own adjustments to the variables.

This is passed into docker-compose when the application is run/

> #### Note
>
> The application can be configured using the methods described in the [Externalized Configuration] section of the  Spring Boot docs.

## Authentication / Authorisation

The `auth.methods` configuration property controls which authentication method(s) are enabled. It's a comma-separated list of method names. Available methods are:

* `basic` — HTTP basic authentication; suitable for development/testing only
* `saml` — SAML 2.0 authentication

### SAML 2.0 Authentication Guide

#### Setting up your Idp
This application can use a SAML IdP to provide user authentication.
I have used the standalone version of Keycloak from https://www.keycloak.org/
(tested with Keycloak 9.0).

Getting started guide for Keycloak: https://www.keycloak.org/docs/latest/getting_started/

After installing keycloak you need to create a new Realm.

It will default to the 'Master' realm and if you click on the word Master it will
allow you to 'Add Realm'.  Add the following:

    Name: demo
    Import file: select the config file 'keycloak-realm-export.json' in the root of this project.

This will be setup the Client 'com:demo:spring:sp', Role 'DEMO_USER' and connect using the to use the keystore in src
/main/resources/saml/samlKeystore.jks.

For more info, here's a nice guide on setting up a Spring Boot app with SAML and Keycloak:
https://blog.codecentric.de/en/2019/03/secure-spring-boot-app-saml-keycloak/

### Create a test user

You them need to go into keycloak and add a user to test with.  To do this select Manage -> Users -> Add User.
You should then ensure the user has the DEMO_USER role in the 'Role Mappings' tab.

You then need to set the users password so you can log in.  You do this by going back to the Users screen and select
 'impersonate'.  You can  then select the password tab and supply a password for your user.

### Adding a Client Scope

You should now see the Client com:demo:spring:sp in your list of clients and
DEMO_USER in your list of roles.
Now select Client scopes -> Create, to create a new scope for our client to use.

    Settings:
        Name: saml_profile
        Protocol: saml

    Mappers:
        Add Builtin:
            X500 givenName
            X500 surname
            X500 email

    Scope:
        Assigned Roles: DEMO_USER

Now you can add this to your client by going selecting com:demo:spring:sp from
the list of clients, and going to the Client Scopes tab.  You can then make sure saml_profile
is in 'Assigned Default Client Scopes'.

If you goto: http://your-keycloak-host/auth/realms/demo/protocol/saml/descriptor you should see the XML descriptor
for the Idp that the loading-ui will connect to.

You can now update the application.properties to point to this installation e.g.
auth.saml.keycloak.auth-server-url=http://your-keycloak-host/auth/realms/demo

You can now start the loading ui and log in using the user account you have created.

## Database

A database server is required to run the application. Postgres 12.3 is recommended. Docker Compose can be used to create one, see the Docker Compose section.

The database schema is managed by [Flyway]. Flyway applies any required migrations on application startup, so the app's user needs permission to create/modify the database structure.
Alternatively, the [Flyway CLI] can be used stand-alone to apply the [migration files].

[Flyway]: https://flywaydb.org/
[Flyway CLI]: https://flywaydb.org/documentation/commandline/
[migration files]: src/main/resources/db/migration

### Example Data

The database can be populated with sample users and workspaces by executing [example_user_data.sql].

[example_user_data.sql]: src/main/docs/example_data.sql

## Docker Compose

The repository contains a Docker Compose file which will run a suitable database, plus a web-based DB admin UI to manage it.

Run `$ docker-compose --env-file example.env up --build` in the repository to start the services with appropriate values set in the example.env

### Build and Publish to Docker Images AWS ECR Repository

First build the project if you have not already.
    mvn clean package

Then publish to ECR
    source <env file>

Build the image and upload to sandbox:

```sh
source sandbox-cudl-loader.env
cd docker/dl-loading-db
docker image build --no-cache --build-arg LOADING_DB_PASSWORD=$LOADING_DB_PASSWORD --build-arg LOADING_DB_USER_SETUP_SQL=$LOADING_DB_USER_SETUP_SQL -t dl-loader-db .
docker tag dl-loader-db:latest 563181399728.dkr.ecr.eu-west-1.amazonaws.com/dl-loader-db:latest
aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 563181399728.dkr.ecr.eu-west-1.amazonaws.com
docker push 563181399728.dkr.ecr.eu-west-1.amazonaws.com/dl-loader-db:latest
cd ../..
docker image build --no-cache --network=host --build-arg LOADING_UI_HARDCODED_USERS_FILE=$LOADING_UI_HARDCODED_USERS_FILE -t dl-loader-ui .
docker tag dl-loader-ui:latest 563181399728.dkr.ecr.eu-west-1.amazonaws.com/dl-loader-ui:latest
docker push 563181399728.dkr.ecr.eu-west-1.amazonaws.com/dl-loader-ui:latest
```

Upload to cul-cudl account:
```sh
source cul-staging-cudl-loader.env
cd docker/dl-loading-db
docker image build --no-cache --build-arg LOADING_DB_PASSWORD=$LOADING_DB_PASSWORD --build-arg LOADING_DB_USER_SETUP_SQL=$LOADING_DB_USER_SETUP_SQL -t dl-loader-db .
docker tag dl-loader-db:latest 438117829123.dkr.ecr.eu-west-1.amazonaws.com/cudl/content-loader-db:latest
aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 438117829123.dkr.ecr.eu-west-1.amazonaws.com
docker push 438117829123.dkr.ecr.eu-west-1.amazonaws.com/cudl/content-loader-db
cd ../..
docker image build --no-cache --network=host --build-arg LOADING_UI_HARDCODED_USERS_FILE=$LOADING_UI_HARDCODED_USERS_FILE -t dl-loader-ui .
docker tag dl-loader-ui:latest 438117829123.dkr.ecr.eu-west-1.amazonaws.com/cudl/content-loader-ui:latest
docker push 438117829123.dkr.ecr.eu-west-1.amazonaws.com/cudl/content-loader-ui:latest
```

NOTE THE SHA VALUES WILL BE DIFFERENT ON SANDBOX AND ON STAGING

Then use the repository 'cudl-terraform' to update the ECR image used and deploy the new version by using the new image sha.
*Note at the moment it can be very slow to deploy*

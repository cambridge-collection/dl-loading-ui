# Cambridge Digital Library: Content Loader

This is a project for editing and loading data into the Cambridge Digital Library Platform.

## Configure:

Look at properties in **src/main/resources/application.properties**
In particular set **'git.sourcedata.checkout.path'** to a local dir to
checkout the source data.

## Authentication:

### Setting up your Idp
This application requires a SAML IdP to provide user authentication.
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

## Setting up the Development API

The application requires a development API (https://bitbucket.org/CUDL/dl-deployment-api/) to be contactable in order
to make requests to get the instance -> data-version mappings and deploy new data packages.

If you want to run a local version, use the git link above to download and then see instructions within to run.
Update the 'deployment.api.url' property to point to the installation.

Without setting this up the 'deploy' section will not function.

## Setting up the database


## Further Details

More details can be found in `src/main/docs/Overview.md`

## Package:

    mvn clean package

## Run:

    mvn spring-boot:run

This will run the application on:

    http://localhost:8081

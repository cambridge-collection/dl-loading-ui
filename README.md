#Cambridge Digital Library: Content Loader

This is a project for editing and loading data into the Cambridge Digital Library Platform.

##Configure:

Look at properties in **src/main/resources/application.properties**
In particular set **'git.sourcedata.checkout.path'** to a local dir to
checkout the source data.

##Package:

    mvn clean package

##Run:

    mvn spring-boot:run

This will run the application on:

    http://localhost:8081

##Authentication:

### Setting up your Idp
This application requires a SAML IdP to login.  I can recommend Keycloak https://www.keycloak.org/
Getting started guide for Keycloak: https://www.keycloak.org/docs/latest/getting_started/

Note I have tested with Keycloak 9.0.

After installing keycloak you can load the config file 'keycloak-realm-export.json' which will setup the
demo client to connect to.

For more info, here's a nice guide on setting up a Spring Boot app with SAML and Keycloak:
https://blog.codecentric.de/en/2019/03/secure-spring-boot-app-saml-keycloak/

###Setting up the Keystore
You'll also need to setup the keystore, in keycloak by doing the following:

In the SAML Keys tab you need to import the Keystore of the example app.

    Click on import
    Archive Format JKS
    Key-Alias dl-loading-ui
    Store pass *see keepass*
    Select configure the path to src/main/resources/saml/samlKeystore.jks

You then need to update the application properties saml section to point to your Keycloak client.

### Create a test user

You them need to go into keycloak and add a user to test with.  To do this select Manage -> Users -> Add User.



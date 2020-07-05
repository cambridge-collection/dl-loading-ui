FROM openjdk:11.0.7

# TODO hardcoded version
COPY ./target/ui-0.1.0-SNAPSHOT.war /usr/local/dl-loading-ui.war
COPY ./conf/application.properties /etc/dl-loading-ui/application.properties
COPY ./conf/users.properties /etc/dl-loading-ui/users.properties

CMD java -jar -debug /usr/local/dl-loading-ui.war  -spring.config.additional-location=/etc/dl-loading-ui/

FROM openjdk:11.0.14

ARG LOADING_UI_HARDCODED_USERS_FILE

# TODO hardcoded version
COPY ./target/ui-0.1.0-SNAPSHOT.war /usr/local/dl-loading-ui.war
#COPY ./conf/application.properties /etc/dl-loading-ui/application.properties
COPY ./${LOADING_UI_HARDCODED_USERS_FILE} /etc/dl-loading-ui/users.properties

# Install req
RUN apt update -y && apt upgrade -y
RUN apt-get install python-is-python3
RUN apt install apt-utils
RUN apt install curl -y
RUN apt-get install zip -y
RUN apt-get install unzip -y
RUN apt-get install python3 -y
RUN apt install python3-venv -y

# install aws cli
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install

CMD java -jar -debug /usr/local/dl-loading-ui.war --spring.config.additional-location=/etc/dl-loading-ui/

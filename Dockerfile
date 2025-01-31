#FROM adoptopenjdk/openjdk11:jdk-11.0.11_9
FROM ibmjava:11-jdk

ARG LOADING_UI_HARDCODED_USERS_FILE

# TODO hardcoded version
COPY ./target/ui-0.1.0-SNAPSHOT.war /usr/local/dl-loading-ui.war
#COPY ./conf/application.properties /etc/dl-loading-ui/application.properties
COPY ./${LOADING_UI_HARDCODED_USERS_FILE} /etc/dl-loading-ui/users.properties

# Install req
RUN apt-get update -y && apt-get install curl unzip fuse libfuse2 -y

# install aws cli
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install

# install the s3 mountpoint.
# See https://docs.aws.amazon.com/AmazonS3/latest/userguide/mountpoint-installation.html
#COPY ./docker/mountpoint-s3/mount-s3.deb mount-s3.deb
RUN wget https://s3.amazonaws.com/mountpoint-s3-release/1.10.0/x86_64/mount-s3-1.10.0-x86_64.deb
RUN apt-get install ./mount-s3-1.10.0-x86_64.deb -y

CMD java -jar -debug /usr/local/dl-loading-ui.war --spring.config.additional-location=/etc/dl-loading-ui/

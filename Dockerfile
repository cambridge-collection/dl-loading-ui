FROM --platform=linux/amd64 ibmjava:11-jdk

# Install system dependencies and tools first — cached until these change
RUN apt-get update -y && apt-get install curl unzip wget fuse libfuse2 -y

# Install AWS CLI
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip \
    && ./aws/install \
    && rm -rf awscliv2.zip aws

# Install mountpoint-s3
# See https://docs.aws.amazon.com/AmazonS3/latest/userguide/mountpoint-installation.html
RUN wget https://s3.amazonaws.com/mountpoint-s3-release/1.10.0/x86_64/mount-s3-1.10.0-x86_64.deb \
    && apt-get install ./mount-s3-1.10.0-x86_64.deb -y \
    && rm mount-s3-1.10.0-x86_64.deb

# Copy application files last — these change on every build
ARG LOADING_UI_HARDCODED_USERS_FILE
COPY ./target/ui-0.1.0-SNAPSHOT.war /usr/local/dl-loading-ui.war
COPY ./${LOADING_UI_HARDCODED_USERS_FILE} /etc/dl-loading-ui/users.properties

CMD java -jar -debug /usr/local/dl-loading-ui.war --spring.config.additional-location=/etc/dl-loading-ui/

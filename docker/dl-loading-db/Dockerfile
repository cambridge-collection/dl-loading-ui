FROM postgres:12.1-alpine

ARG LOADING_DB_PASSWORD
ARG LOADING_DB_USER_SETUP_SQL

ENV POSTGRES_PASSWORD ${LOADING_DB_PASSWORD}

COPY ./resources/${LOADING_DB_USER_SETUP_SQL} /docker-entrypoint-initdb.d/users_database_setup.sql
COPY ./resources/workspaces_database_setup.sql /docker-entrypoint-initdb.d/workspaces_database_setup.sql
COPY ./resources/errorlog_database_setup.sql /docker-entrypoint-initdb.d/errorlog_database_setup.sql

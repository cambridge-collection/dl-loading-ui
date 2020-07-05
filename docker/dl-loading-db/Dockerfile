FROM postgres:12.1-alpine

ENV POSTGRES_PASSWORD password
COPY ./resources/example_data.sql /docker-entrypoint-initdb.d/users_database_setup.sql
COPY ./resources/workspaces_database_setup.sql /docker-entrypoint-initdb.d/workspaces_database_setup.sql


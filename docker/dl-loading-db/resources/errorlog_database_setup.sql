DROP table IF EXISTS data_processing_errors;

create table data_processing_errors (
    message_id varchar not null,
    topic_arn varchar not null,
    timestamp timestamp not null,
    subject varchar not null,
    message varchar not null,
    unsubscribe_url varchar not null,
    log_type int not null,
    log_group varchar not null,
    log_stream varchar not null,
    json_event varchar,
    error varchar,
    stacktrace varchar,
    primary key (message_id)
);


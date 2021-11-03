DROP table IF EXISTS data_processing_errors;

create table data_processing_errors(
    message_id varchar not null,
	topic_arn varchar not null,
	timestamp timestamp not null,
	subject varchar not null,
	message varchar not null,
	unsubscribe_url varchar not null,
	primary key (message_id)
);


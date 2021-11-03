create table users(
    id bigserial not null,
	username varchar unique not null,
	password varchar,
	firstName varchar not null,
	lastName varchar not null,
	email varchar not null,
	enabled boolean not null,
	primary key (id)
);

create table authorities (
    id bigint not null,
	authority varchar not null,
	constraint fk_authorities_users foreign key(id) references users(id)
);
create unique index ix_auth_username on authorities (id,authority);

create table persistent_logins (
	id bigint not null,
	series varchar primary key,
	token varchar not null,
	last_used timestamp not null
);

create table workspaces(
    workspace_id bigserial not null,
	name varchar unique not null,
	primary key (workspace_id)
);

create table items_in_workspaces (
    item_id varchar not null,
	workspace_id bigint not null,
	constraint fk_items_workspaces foreign key(workspace_id) references workspaces(workspace_id),
    primary key (item_id, workspace_id)
);
create unique index ix_items_workspaces on items_in_workspaces (item_id,workspace_id);

create table collections_in_workspaces (
    collection_id varchar not null,
	workspace_id bigint not null,
	constraint fk_collections_workspaces foreign key(workspace_id) references workspaces(workspace_id),
    primary key (collection_id, workspace_id)
);
create unique index ix_collections_workspaces on collections_in_workspaces (collection_id, workspace_id);

create table data_processing_errors (
    message_id varchar not null,
	topic_arn varchar not null,
	timestamp timestamp not null,
	subject varchar not null,
	message varchar not null,
	unsubscribe_url varchar not null,
	primary key (message_id)
);

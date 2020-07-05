DROP table IF EXISTS items_in_workspaces;
DROP table IF EXISTS collections_in_workspaces;
DROP table IF EXISTS workspaces;

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


-- Insert default workspaces for testing.
insert into workspaces(name) VALUES ('Test Workspace1');
insert into workspaces(name) VALUES ('Test Workspace2');
insert into items_in_workspaces (item_id, workspace_id) VALUES ('MS-TEST-00001-00001',
(SELECT workspace_id FROM workspaces WHERE name='Test Workspace1'));
insert into items_in_workspaces (item_id, workspace_id) VALUES ('MS-TEST-00001-00002',
(SELECT workspace_id FROM workspaces WHERE name='Test Workspace1'));
insert into items_in_workspaces (item_id, workspace_id) VALUES ('MS-TEST-00001-00003',
(SELECT workspace_id FROM workspaces WHERE name='Test Workspace2'));
insert into collections_in_workspaces (collection_id, workspace_id)VALUES ('collections/test.collection.json',
(SELECT workspace_id FROM workspaces WHERE name='Test Workspace1'));


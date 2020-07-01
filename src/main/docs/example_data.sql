BEGIN;

-- Insert test users(please change / remove before putting into production!!)
insert into users (username, firstname, lastname, password, email, enabled)
values ('test-workspace-member1', 'Workspace', 'Member', 'password', 'wm@test.com', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('test-workspace-member2', 'Workspace', 'Member', 'password', 'wm@test.com', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('test-workspace-manager1', 'Workspace', 'Manager', 'password', 'wm2@test.com', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('test-workspace-manager2', 'Workspace', 'Manager', 'password', 'wm2@test.com', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('test-deployment-manager', 'Deployment', 'Manager', 'password', 'dm@test.com', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('test-site-manager', 'Site', 'Manager', 'password', 'dm@test.com', true);

insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='test-workspace-member1'),
                                                'ROLE_WORKSPACE_MEMBER1');

--- test-workspace-member2 is a member of workspace 1 and 2
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='test-workspace-member2'),
                                                'ROLE_WORKSPACE_MEMBER1');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='test-workspace-member2'),
                                                'ROLE_WORKSPACE_MEMBER2');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='test-workspace-manager1'),
                                                'ROLE_WORKSPACE_MANAGER1');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='test-workspace-manager2'),
                                                'ROLE_WORKSPACE_MANAGER2');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='test-deployment-manager'),
                                                'ROLE_DEPLOYMENT_MANAGER');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='test-site-manager'),
                                                'ROLE_SITE_MANAGER');

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

COMMIT;

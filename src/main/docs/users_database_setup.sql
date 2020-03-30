DROP table authorities;
DROP table users;
DROP table persistent_logins;

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

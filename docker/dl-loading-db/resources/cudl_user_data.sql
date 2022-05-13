DROP table IF EXISTS authorities;
DROP table IF EXISTS users;
DROP table IF EXISTS persistent_logins;

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

-- IMPORTANT PASSWORDS NEED TO BE CHANGED AFTER SETUP
-- Passwords default to 'changeme' and need to be updated (this can be done in the user management section)
insert into users (username, firstname, lastname, password, email, enabled)
values ('hej23', 'Huw', 'Jones', '{bcrypt}$2a$10$Ek34zfVy7QaJTqf42ITxZew6/.ZJlBqffOEQ5f.C0cZWbfrf3PAy.', 'hej23@cam.ac.uk', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('acc60', 'Andy', 'Corrigan', '{bcrypt}$2a$10$Ek34zfVy7QaJTqf42ITxZew6/.ZJlBqffOEQ5f.C0cZWbfrf3PAy.', 'acc60@cam.ac.uk', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('jlf44', 'Jennie', 'Fletcher', '{bcrypt}$2a$10$Ek34zfVy7QaJTqf42ITxZew6/.ZJlBqffOEQ5f.C0cZWbfrf3PAy.', 'jlf44@cam.ac.uk', true);

insert into users (username, firstname, lastname, password, email, enabled)
values ('sp510', 'Suzanne', 'Paul', '{bcrypt}$2a$10$Ek34zfVy7QaJTqf42ITxZew6/.ZJlBqffOEQ5f.C0cZWbfrf3PAy.', 'sp510@cam.ac.uk', true);

-- test all user has access to everything
-- Huw
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='hej23'),
                                                'ROLE_SITE_MANAGER');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='hej23'),
                                                'ROLE_WORKSPACE_MANAGER1');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='hej23'),
                                                'ROLE_WORKSPACE_MANAGER2');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='hej23'),
                                                'ROLE_DEPLOYMENT_MANAGER');
-- Andy
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='acc60'),
                                                'ROLE_SITE_MANAGER');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='acc60'),
                                                'ROLE_WORKSPACE_MANAGER1');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='acc60'),
                                                'ROLE_WORKSPACE_MANAGER2');
-- Jennie
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='jlf44'),
                                                'ROLE_SITE_MANAGER');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='jlf44'),
                                                'ROLE_WORKSPACE_MANAGER1');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='jlf44'),
                                                'ROLE_WORKSPACE_MANAGER2');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='jlf44'),
                                                'ROLE_DEPLOYMENT_MANAGER');
-- Suzanne
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='sp510'),
                                                'ROLE_WORKSPACE_MEMBER1');
insert into authorities (id, authority) values ((SELECT id FROM users WHERE username='sp510'),
                                                'ROLE_WORKSPACE_MEMBER2');

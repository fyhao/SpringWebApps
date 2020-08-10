create sequence hibernate_sequence start with 1 increment by 1
create table contact (id bigint not null, created_time timestamp, email varchar(255), mobileno varchar(255), name varchar(255), primary key (id))
create table context (id bigint not null, key varchar(255), value varchar(255), conversation_id bigint, primary key (id))
create table conversation (id bigint not null, channel varchar(255), end_time timestamp, start_time timestamp, contact_id bigint, primary key (id))
create table message (id bigint not null, content varchar(255), created_time timestamp, fromparty varchar(255), toparty varchar(255), conversation_id bigint, primary key (id))
create table user_account (id bigint not null, email varchar(255), status varchar(255), username varchar(255), primary key (id))
alter table context add constraint FKp09yjex53pg62upen2rc5ryqt foreign key (conversation_id) references conversation
alter table conversation add constraint FK35bcp3sctl6f3sfnk2nkhlnh1 foreign key (contact_id) references contact
alter table message add constraint FK6yskk3hxw5sklwgi25y6d5u1l foreign key (conversation_id) references conversation
create sequence hibernate_sequence start with 1 increment by 1
create table contact (id bigint not null, created_time timestamp, email varchar(255), mobileno varchar(255), name varchar(255), primary key (id))
create table context (id bigint not null, key varchar(255), value varchar(255), conversation_id bigint, primary key (id))
create table conversation (id bigint not null, channel varchar(255), end_time timestamp, start_time timestamp, contact_id bigint, primary key (id))
create table message (id bigint not null, content varchar(255), created_time timestamp, fromparty varchar(255), toparty varchar(255), conversation_id bigint, primary key (id))
create table user_account (id bigint not null, email varchar(255), status varchar(255), username varchar(255), primary key (id))
alter table context add constraint FKp09yjex53pg62upen2rc5ryqt foreign key (conversation_id) references conversation
alter table conversation add constraint FK35bcp3sctl6f3sfnk2nkhlnh1 foreign key (contact_id) references contact
alter table message add constraint FK6yskk3hxw5sklwgi25y6d5u1l foreign key (conversation_id) references conversation

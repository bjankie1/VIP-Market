# --- First database schema

# --- !Ups

create table user (
  id                        int(11)      not null primary key auto_increment,
  email                     varchar(255) not null,
  name                      varchar(255) not null,
  password                  varchar(50)  not null,
  seed                      varchar(10)  not null,
  active                    boolean      not null default true,
  date_created              datetime     not null,
  permission                varchar(20)  not null
);

ALTER TABLE user ADD CONSTRAINT user_email_unique UNIQUE(email);

ALTER TABLE user ADD CONSTRAINT user_name_unique UNIQUE(name);


create table event (
  id                        int(11)      not null primary key auto_increment,
  name                      varchar(255) not null,
  description               varchar(255) not null,
  start_date                datetime     not null,
  event_type_id             int(11)      not null,
  venue_id                  int(11)      not null,
  active                    boolean      not null,
  date_created              datetime     not null
);

create table venue (
  id                        int(11)      not null primary key auto_increment,
  name                      varchar(255) not null,
  description               varchar(255) not null,
  created_date              datetime     not null,
  active                    boolean      not null,
  street                    varchar(255),
  city                      varchar(100),
  country                   varchar(100)
);

create table vip_lounge (
  id                        int(11)      not null primary key auto_increment,
  name                      varchar(255) not null,
  base_price                NUMERIC,
  description               varchar(255) not null,
  venue_id                  int(11)      not null,
  location_code             varchar(255) not null,
  seats_number              int(11)      not null,
  active                    boolean      not null,
  date_created              datetime     not null
);

create table file_info (
  id                        varchar(255) not null primary key,
  name                      varchar(255) not null,
  owner                     varchar(255) not null,
  storage                   varchar(20)  not null,
  original_name             varchar(255) not null,
  size                      int(11)      not null
);

create table vip_lounge_event (
  event_id                  int(11),
  vip_lounge_id             int(11)      not null,
  base_price                NUMERIC,
  active                    boolean      not null,
  primary key(event_id,vip_lounge_id)
);

create table event_type (
  id                        int(11) not null primary key auto_increment,
  name                      varchar(255)  not null
);

ALTER TABLE event_type ADD CONSTRAINT event_type_name_unique UNIQUE(name);

create table event_approver (
  event_id                  int(11) not null,
  user_id                   int(11) not null
);

ALTER TABLE event_approver ADD CONSTRAINT event_approver_unique UNIQUE(event_id, user_id);


# --- !Downs

drop table if exists user;
drop table if exists event;
drop table if exists event_type;
drop table if exists venue;
drop table if exists vip_lounge;
drop table if exists vip_lounge_event;
drop table if exists file_info;
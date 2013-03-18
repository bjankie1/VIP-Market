# --- First database schema

# --- !Ups

create table user_account (
  id                        serial primary key,
  email                     varchar(255) not null,
  name                      varchar(255) not null,
  password                  varchar(50)  not null,
  seed                      varchar(10)  not null,
  active                    boolean      not null default true,
  date_created              timestamp with time zone not null,
  permission                varchar(20)  not null
);

ALTER TABLE user_account ADD CONSTRAINT user_email_unique UNIQUE(email);

ALTER TABLE user_account ADD CONSTRAINT user_name_unique UNIQUE(name);


create table event (
  id                        serial primary key,
  name                      varchar(255) not null,
  description               varchar(255) not null,
  start_date                timestamp with time zone     not null,
  event_type_id             integer      not null,
  venue_id                  integer      not null,
  active                    boolean      not null,
  date_created              timestamp with time zone     not null
);

create table venue (
  id                        serial primary key,
  name                      varchar(255) not null,
  description               varchar(255) not null,
  created_date              timestamp with time zone     not null,
  active                    boolean      not null,
  street                    varchar(255),
  city                      varchar(100),
  country                   varchar(100)
);

create table vip_lounge (
  id                        serial primary key,
  name                      varchar(255) not null,
  base_price                NUMERIC,
  description               varchar(255) not null,
  venue_id                  integer      not null,
  location_code             varchar(255) not null,
  seats_number              integer      not null,
  active                    boolean      not null,
  date_created              timestamp with time zone     not null
);

create table file_info (
  id                        varchar(255) not null primary key,
  name                      varchar(255) not null,
  owner                     varchar(255) not null,
  storage                   varchar(20)  not null,
  original_name             varchar(255) not null,
  size                      numeric(11)      not null
);

create table vip_lounge_event (
  event_id                  integer      not null ,
  vip_lounge_id             integer      not null,
  base_price                NUMERIC,
  active                    boolean      not null,
  primary key(event_id,vip_lounge_id)
);

create table event_type (
  id                        serial primary key,
  name                      varchar(255)  not null
);

ALTER TABLE event_type ADD CONSTRAINT event_type_name_unique UNIQUE(name);

create table event_approver (
  event_id                  integer not null,
  user_id                   integer not null
);

ALTER TABLE event_approver ADD CONSTRAINT event_approver_unique UNIQUE(event_id, user_id);


create table business_sector (
  id                        varchar(20),
  venue_id                  integer not null,
  row_scheme                varchar(20) not null,
  primary key(id, venue_id)
);


# --- !Downs

drop table if exists user_account;
drop table if exists event;
drop table if exists event_type;
drop table if exists event_approver;
drop table if exists venue;
drop table if exists vip_lounge;
drop table if exists vip_lounge_event;
drop table if exists file_info;
drop table if exists business_sector;

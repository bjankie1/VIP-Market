# --- First database schema

# --- !Ups

create table user (
  email                     varchar(255) not null primary key,
  name                      varchar(255) not null,
  password                  varchar(255) not null
);

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



# --- !Downs

drop table if exists user;
drop table if exists event;
drop table if exists venue;
drop table if exists vip_lounge;
drop table if exists file_info;
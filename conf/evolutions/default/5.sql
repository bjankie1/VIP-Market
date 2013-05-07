# --- First database schema

# --- !Ups

create table booking_order (
  id                 serial primary key,
  customer_id        integer  not null,
  date_created       timestamp with time zone not null,
  date_confirmed     timestamp with time zone not null,
  confirmed_by       integer,
  date_paid          timestamp with time zone not null,
  paid_by            integer,
  order_status       varchar(50) not null
);

create table vip_lounge_order (
  order_id            integer not null,
  price               numeric,
  date_created        timestamp with time zone not null, 
  vip_lounge_at_event integer not null
);

create table business_seat_order (
  order_id            integer not null,
  price               numeric,
  date_created        timestamp with time zone not null, 
  event_id            int not null
);

create table business_seat( 
  seat_order_id      integer not null,
  sector_id          varchar(20) not null, 
  row                integer not null,
  seat               integer not null,
  primary key(seat_order_id, sector_id, row, seat)
);

# --- !Downs

drop table if exists booking_order;
drop table if exists vip_lounge_order;
drop table if exists business_seat_order;
drop table if exists business_seat;
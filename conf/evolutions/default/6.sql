# --- First database schema

# --- !Ups

alter table vip_lounge_order add column id serial primary key;

alter table business_seat_order add column id serial primary key;

# --- !Downs

alter table vip_lounge_order drop column id;

alter table business_seat_order drop column id;
# --- Sample dataset

# --- !Ups

--- Venue
insert into venue (id,name, description, created_date, active, street, city, country) 
values ( 1, 'Stadion miejski', 'Nic sie tam nie dzieje', '2013-01-27', true, 'Królewiecka', 'Wrocław', 'Polska');
insert into venue (id,name, description, created_date, active, street, city, country) 
values ( 2, 'Stadion narodowy', 'Chyba zarabia na siebie', '2013-01-27', true, 'Murszowska', 'Warszawa', 'Polska');
insert into venue (id,name, description, created_date, active, street, city, country) 
values ( 666, 'Arena', 'Leje sie krew', '2013-01-27', true, 'Zodiak', 'Warszawa', 'Polska');

--- Event
insert into event (id,name, description, start_date, event_type_id, venue_id, active, date_created) 
values ( 1, 'Koncert', 'test 1', '2012-12-11', 1, 1, true, '2013-1-1');
insert into event (id,name, description, start_date, event_type_id, venue_id, active, date_created) 
values ( 2, 'Siatka', 'test 2', '2012-12-12', 1, 1, true, '2013-1-1');
insert into event (id,name, description, start_date, event_type_id, venue_id, active, date_created) 
values ( 3, 'Bania', 'test 3', '2013-01-01', 1, 1, true, '2013-1-1');

--- VIP lounge
insert into vip_lounge (id, name, description, venue_id, seats_number, location_code, active, date_created) 
values ( 1, 'Loża szyderców', 'Wielka zabawa', 666, 1, 'VL_1', true, '2013-01-01');

# --- !Downs

delete from event;
delete from venue;
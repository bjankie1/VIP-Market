# --- Sample dataset

# --- !Ups

#--- Venue
insert into venue (id,name, description, created_date, active, street, city, country) 
values ( 1, 'Stadion miejski', 'Nic sie tam nie dzieje', '2013-01-27', true, 'Królewiecka', 'Wrocław', 'Polska'),
       ( 2, 'Stadion narodowy', 'Chyba zarabia na siebie', '2013-01-27', true, 'Murszowska', 'Warszawa', 'Polska'),
       ( 666, 'Arena', 'Leje sie krew', '2013-01-27', true, 'Zodiak', 'Warszawa', 'Polska');

#--- Event
insert into event (id,name,description, start_date, event_type_id, venue_id, active, date_created) 
values ( 1, 'Koncert', 'test 1 some word', '2012-12-11', 1, 1, true, '2013-1-1'),
       ( 2, 'Siatka', 'test 2 some word','2012-12-12', 1, 1, true, '2013-1-1'),
       ( 3, 'Bania', 'test 3 some word', '2013-01-01', 1, 1, true, '2013-1-1');

#--- EventType
insert into event_type (id,name)
values ( 1, 'Piłka nożna'),
       ( 2, 'Siatkówka'),
       ( 3, 'Skoki narciarskie');

#--- VIP lounge
insert into vip_lounge (id, name,base_price, description, venue_id, seats_number, location_code, active, date_created) 
values ( 1, 'Loża szyderców', 666, 'Wielka zabawa', 1, 1, 'VL_1', true, '2013-01-01'),
       ( 2, 'Loża szyderców', 666, 'Wielka zabawa', 1, 1, 'VL_1', true, '2013-01-01');

insert into user(id, email,                  password,                             seed,         name,    active, date_created, permission)
values           (1,'admin@sportsmarket.pl', '87e82ac94bbab66454f9b09f991c6282',   'mec0R7KBlu', 'admin', true,   '2013-02-12', 'Administrator'),
                 (2,'user@sportsmarket.pl',  '87e82ac94bbab66454f9b09f991c6282',   'mec0R7KBlu', 'tester',true,   '2013-02-12', 'NormalUser');

insert into business_sector(id, venue_id, row_scheme)
values ('A', 1, 'Roman'),
       ('B', 1, 'Roman');

# --- !Downs

delete from event;
delete from venue;
delete from vip_lounge;
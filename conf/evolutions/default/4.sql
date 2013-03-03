# --- Sample dataset

# --- !Ups


insert into business_sector_row(sector_id, venue_id, row, seats)
values ('A', 1, 1, 10),
       ('A', 1, 2, 11),
       ('B', 1, 1, 20),
       ('B', 1, 2, 21);

# --- !Downs

delete from business_sector_row;
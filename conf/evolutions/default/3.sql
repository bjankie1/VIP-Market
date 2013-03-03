# --- First database schema

# --- !Ups


create table business_sector_row (
  sector_id                 varchar(20),
  venue_id                  int(11) not null,
  row                       int(3)  not null,
  seats                     int(3)  not null,
  primary key(sector_id, venue_id, row)
);


# --- !Downs

drop table if exists business_sector_row;

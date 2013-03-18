# --- First database schema

# --- !Ups


create table business_sector_row (
  sector_id                 varchar(20),
  venue_id                  integer  not null,
  row                       integer  not null,
  seats                     integer  not null,
  primary key(sector_id, venue_id, row)
);


# --- !Downs

drop table if exists business_sector_row;

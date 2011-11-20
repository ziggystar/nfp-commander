#Version 1
First database version contains no cycle table.

##Schema
    -- table declarations :
    create table Day (
        measureTime varchar(128),
        ausklammern boolean not null,
        temperature real,
        sex varchar(128),
        id date not null primary key,
        comment binary not null,
        blutung varchar(128),
        mumuFest varchar(128),
        schleim varchar(128),
        mumuPosition varchar(128),
        mumuOpen varchar(128)
      );
    create table KeyValue (
        id varchar(128) not null primary key,
        value varchar(128) not null
      );
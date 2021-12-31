-- apply changes
create table url_checks (
  id                            bigint generated by default as identity not null,
  status_code                   integer not null,
  title                         varchar(255),
  h1                            varchar(255),
  description                   text,
  url_id                        bigint,
  created_at                    timestamp,
  constraint pk_url_checks primary key (id)
);

create table urls (
  id                            bigint generated by default as identity not null,
  name                          varchar(255),
  created_at                    timestamp,
  constraint pk_urls primary key (id)
);

create index ix_url_checks_url_id on url_checks (url_id);
alter table url_checks add constraint fk_url_checks_url_id foreign key (url_id) references urls (id) on delete restrict on update restrict;


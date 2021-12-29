-- apply changes
create table urls (
  id                            bigint generated by default as identity not null,
  name                          clob,
  created_at                    timestamp,
  response_code                 integer not null,
  constraint pk_urls primary key (id)
);


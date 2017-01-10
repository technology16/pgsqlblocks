SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

DROP TABLE IF EXISTS pgsqlblocks_testing;
CREATE TABLE pgsqlblocks_testing
(
  id serial NOT NULL,
  name character varying(25),
  count integer NOT NULL DEFAULT 0,
  description character varying(60),
  CONSTRAINT pkey_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE pgsqlblocks_testing
  OWNER TO pgsqlblocks_test;


INSERT INTO pgsqlblocks_testing(name, description) VALUES('val1', 'value 1'), ('val2' , 'value 2'), ('val3', 'value 3');
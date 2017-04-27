---
-- ========================LICENSE_START=================================
-- pgSqlBlocks
-- *
-- Copyright (C) 2017 "Technology" LLC
-- *
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- =========================LICENSE_END==================================
---
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
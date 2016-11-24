--то запрос в другой сессии
CREATE INDEX CONCURRENTLY ix_pgsqlblocks_testing_name
  ON public.pgsqlblocks_testing
  USING btree
  (name COLLATE pg_catalog."default");
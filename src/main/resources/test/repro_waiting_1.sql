--Например если в отдельной сессии выполнить без автокоммита
BEGIN;
    SELECT pg_sleep(300);
COMMIT;

--будет дожидаться окончания транзакции, а это выполнится сразу:
--CREATE INDEX ix_pgsqlblocks_testing_description ON public.pgsqlblocks_testing USING btree (description);

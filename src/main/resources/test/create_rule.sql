-- 0 транзакция
CREATE OR REPLACE RULE rule_pgsqlblocks_testing AS
    ON INSERT TO public.pgsqlblocks_testing
   WHERE new.count >= 0 DO INSTEAD INSERT INTO public.pgsqlblocks_testing (name, count, description)
  VALUES (new.name, new.count, new.description);
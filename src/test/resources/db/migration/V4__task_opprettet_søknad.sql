ALTER TABLE soknad ADD task_opprettet BOOLEAN      NOT NULL DEFAULT FALSE;          -- annerledes (splittet opp i separata alter table)
ALTER TABLE soknad ADD opprettet_tid  TIMESTAMP(3) NOT NULL DEFAULT localtimestamp; -- annerledes

UPDATE soknad
SET task_opprettet = TRUE
WHERE EXISTS(SELECT payload
             FROM task t
             WHERE t.payload = soknad.id);

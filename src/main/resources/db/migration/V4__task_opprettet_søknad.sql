ALTER TABLE soknad
    ADD
        task_opprettet BOOLEAN NOT NULL DEFAULT FALSE,
        opprettet_tid TIMESTAMP(3) NOT NULL DEFAULT localtimestamp;

UPDATE soknad
SET task_opprettet = TRUE
WHERE EXISTS(SELECT payload
             FROM task t
             WHERE t.payload = soknad.id);

CREATE INDEX ON hendelseslogg(kafka_offset);

DELETE FROM vedlegg v WHERE soknad_id IN (SELECT id FROM soknad WHERE opprettet_tid < TO_DATE('2021-10-01','YYYY-MM-DD'));
DELETE FROM hendelseslogg WHERE opprettet_tid < TO_DATE('2021-10-01','YYYY-MM-DD');

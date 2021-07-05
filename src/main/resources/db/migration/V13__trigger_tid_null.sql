UPDATE task SET trigger_tid=opprettet_tid WHERE trigger_tid IS NULL;
ALTER TABLE task ALTER COLUMN trigger_tid SET NOT NULL;
ALTER TABLE ettersending DROP COLUMN vedlegg;
ALTER TABLE ettersending DROP COLUMN dokumenttype;
ALTER TABLE ettersending DROP COLUMN saksnummer;
ALTER TABLE ettersending
    ADD COLUMN stønad_type VARCHAR NOT NULL;



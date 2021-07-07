CREATE TABLE ettersending_vedlegg (
     id       UUID    NOT NULL PRIMARY KEY,
     ettersending_id VARCHAR NOT NULL REFERENCES ettersending,
     navn     VARCHAR NOT NULL,
     tittel   VARCHAR NOT NULL,
     innhold  BYTEA   NOT NULL
 )

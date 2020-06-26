CREATE TABLE vedlegg (
    id       UUID    NOT NULL PRIMARY KEY,
    soknad_id VARCHAR NOT NULL REFERENCES soknad,
    navn     VARCHAR NOT NULL,
    tittel   VARCHAR NOT NULL,
    innhold  BYTEA   NOT NULL
)

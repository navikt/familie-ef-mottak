DROP TABLE ettersending_vedlegg;
DROP TABLE ettersending;

CREATE TABLE ettersending (
    id                UUID                                NOT NULL PRIMARY KEY,
    ettersending_json BYTEA                               NOT NULL,
    ettersending_pdf  BYTEA,
    journalpost_id    VARCHAR,
    fnr               VARCHAR(50),
    task_opprettet    BOOLEAN      DEFAULT FALSE          NOT NULL,
    opprettet_tid     TIMESTAMP(3) DEFAULT LOCALTIMESTAMP NOT NULL,
    stonad_type       VARCHAR                             NOT NULL
);

CREATE TABLE ettersending_vedlegg (
    id              UUID    NOT NULL PRIMARY KEY,
    ettersending_id UUID    NOT NULL REFERENCES ettersending,
    navn            VARCHAR NOT NULL,
    tittel          VARCHAR NOT NULL,
    innhold         BYTEA   NOT NULL
);


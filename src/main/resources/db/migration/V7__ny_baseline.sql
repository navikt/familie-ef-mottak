CREATE TABLE IF NOT EXISTS task (
    id            BIGINT                                               NOT NULL PRIMARY KEY,
    payload       VARCHAR(100)                                         NOT NULL,
    STATUS        VARCHAR(15)  DEFAULT 'UBEHANDLET'::CHARACTER VARYING NOT NULL,
    versjon       BIGINT       DEFAULT 0,
    opprettet_tid TIMESTAMP(3) DEFAULT LOCALTIMESTAMP,
    TYPE          VARCHAR(100)                                         NOT NULL,
    metadata      VARCHAR(4000),
    trigger_tid   TIMESTAMP    DEFAULT LOCALTIMESTAMP,
    avvikstype    VARCHAR(50)
);

CREATE SEQUENCE IF NOT EXISTS task_seq INCREMENT BY 50;
CREATE UNIQUE INDEX IF NOT EXISTS task_payload_type_idx ON task (payload, TYPE);
CREATE INDEX IF NOT EXISTS task_status_idx ON task (STATUS);

CREATE TABLE IF NOT EXISTS task_logg (
    id            BIGINT       NOT NULL PRIMARY KEY,
    task_id       BIGINT       NOT NULL REFERENCES task,
    type          VARCHAR(15)  NOT NULL,
    node          VARCHAR(100) NOT NULL,
    opprettet_tid TIMESTAMP(3) DEFAULT LOCALTIMESTAMP,
    melding       TEXT,
    endret_av     VARCHAR(100) DEFAULT 'VL'::CHARACTER VARYING
);

CREATE SEQUENCE IF NOT EXISTS task_logg_seq INCREMENT BY 50;;

CREATE INDEX IF NOT EXISTS task_logg_task_id_idx ON task_logg (task_id);

CREATE TABLE IF NOT EXISTS soknad (
    id             VARCHAR(100)                                   NOT NULL PRIMARY KEY,
    soknad_json    BYTEA                                          NOT NULL,
    soknad_pdf     BYTEA,
    journalpost_id VARCHAR,
    saksnummer     VARCHAR,
    fnr            VARCHAR(50),
    dokumenttype   VARCHAR(256) DEFAULT N'OVERGANGSSTØNAD_SØKNAD' NOT NULL,
    task_opprettet BOOLEAN      DEFAULT FALSE                     NOT NULL,
    opprettet_tid  TIMESTAMP(3) DEFAULT LOCALTIMESTAMP            NOT NULL,
    vedlegg        BYTEA
);

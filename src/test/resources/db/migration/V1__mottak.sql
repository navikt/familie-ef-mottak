CREATE TABLE task (
    id            bigint                                               NOT NULL PRIMARY KEY,
    payload       VARCHAR(4000)                                        NOT NULL, -- annerledes varchar i stedet for text
    status        varchar(15)  DEFAULT 'UBEHANDLET'::character varying NOT NULL,
    versjon       bigint       DEFAULT 0,
    opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
    type          varchar(100)                                         NOT NULL,
    metadata      varchar(4000),
    trigger_tid   timestamp    DEFAULT LOCALTIMESTAMP,
    avvikstype    varchar(50)
);

CREATE SEQUENCE task_seq INCREMENT BY 50;
CREATE UNIQUE INDEX ON task (payload, type);

CREATE INDEX ON task (status);

CREATE TABLE task_logg (
    id            bigint       NOT NULL PRIMARY KEY,
    task_id       bigint       NOT NULL REFERENCES task,
    type          varchar(15)  NOT NULL,
    node          varchar(100) NOT NULL,
    opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
    melding       text,
    endret_av     varchar(100) DEFAULT 'VL'::character varying
);

CREATE SEQUENCE task_logg_seq INCREMENT BY 50;;

CREATE INDEX ON task_logg (task_id);

CREATE TABLE soknad (
    id             uuid  NOT NULL PRIMARY KEY, -- annerledes UUID i stedet for text
    soknad_json    bytea NOT NULL,
    soknad_pdf     bytea,
    journalpost_id varchar,
    saksnummer     varchar,
    fnr            varchar(50)
);

CREATE TABLE vedlegg (
    id        bigserial NOT NULL PRIMARY KEY,
    soknad_id UUID      NOT NULL REFERENCES soknad, -- annerledes UUID i stedet for text
    data      bytea     NOT NULL,
    filnavn   varchar,
    tittel    varchar
);

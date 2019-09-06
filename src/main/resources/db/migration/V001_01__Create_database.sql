SET DATABASE SQL SYNTAX PGS true;

CREATE TABLE henvendelse (
    id                  bigserial PRIMARY KEY,
    payload             text         NOT NULL,
    status              varchar(150) NOT NULL DEFAULT 'UBEHANDLET',
    VERSJON             bigint       NOT NULL DEFAULT 1,
    opprettet_tidspunkt timestamp(3) NOT NULL DEFAULT localtimestamp
);

CREATE INDEX status_index ON henvendelse (status);


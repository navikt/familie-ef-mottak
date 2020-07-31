CREATE TABLE hendelseslogg (
    id            UUID                                NOT NULL PRIMARY KEY,
    kafka_offset  BIGINT                              NOT NULL,
    hendelse_id   VARCHAR(50)                         NOT NULL,
    opprettet_tid TIMESTAMP(3) DEFAULT LOCALTIMESTAMP NOT NULL,
    metadata      VARCHAR(4000),
    ident         VARCHAR(20),
    CONSTRAINT hendelseslogg_hendelse_id_consumer_key
        UNIQUE (hendelse_id)
);

CREATE INDEX hendelseslogg_hendelse_id_idx
    ON hendelseslogg (hendelse_id);

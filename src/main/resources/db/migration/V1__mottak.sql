CREATE TABLE MOTTAK (
    id bigint PRIMARY KEY,
    payload json NOT NULL,
    OPPRETTET_TID TIMESTAMP(3) DEFAULT localtimestamp
);

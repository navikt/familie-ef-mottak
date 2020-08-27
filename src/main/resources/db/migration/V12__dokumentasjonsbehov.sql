DROP TABLE dokumentasjonsbehov;

CREATE TABLE dokumentasjonsbehov (
     soknad_id   VARCHAR NOT NULL PRIMARY KEY REFERENCES soknad,
     data        TEXT    NOT NULL
);

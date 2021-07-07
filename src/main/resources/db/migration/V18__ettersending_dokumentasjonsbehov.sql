CREATE TABLE ettersending_dokumentasjonsbehov (
     ettersending_id VARCHAR NOT NULL PRIMARY KEY REFERENCES ettersending,
     data        TEXT    NOT NULL
);

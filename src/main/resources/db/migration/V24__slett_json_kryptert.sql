create table json_kryptert_backup as (
    select id, soknad_json from soknad
);

ALTER TABLE soknad
    ALTER COLUMN soknad_json DROP NOT NULL;
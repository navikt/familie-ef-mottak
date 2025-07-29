create table json_kryptert_backup as (
    select id, soknad_json from soknad
);

ALTER TABLE soknad
    DROP COLUMN soknad_json;
${ignoreIfLocal}DO $$
${ignoreIfLocal}    BEGIN
${ignoreIfLocal}        IF EXISTS
${ignoreIfLocal}            ( SELECT 1 from pg_roles where rolname='cloudsqliamuser')
${ignoreIfLocal}        THEN
${ignoreIfLocal}            GRANT SELECT ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
${ignoreIfLocal}            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cloudsqliamuser;
${ignoreIfLocal}        END IF ;
${ignoreIfLocal}    END
${ignoreIfLocal}$$ ;

# familie-ef-mottak
Mottaksapplikasjon for søknader om overgangsstønad.

## Kjøre appen
`ApplicationLocal` launcher appen med Spring-profil `local` og appen blir tilgjengelig på port 8092.

Kjør applikasjon familie-oidc-test-support på port 8080 
https://github.com/navikt/familie-oidc-test
f.eks: 
docker build -t local:oidc-test-support . 
docker run -it -p 8080:8080 local:oidc-test-support 
Token kan da genereres ved å gå til url: http://localhost:8080/jwt

## Database

For å sette opp Postgres-database lokalt med Docker:
```
docker run --name familie-ef-mottak -e POSTGRES_PASSWORD=<PASSWORD> -d -p 5432:5432 postgres
docker ps (finn container id)
docker exec -it <container_id> bash
psql -U postgres
CREATE DATABASE "familie-ef-mottak";
```

For å kjøre med denne lokalt må følgende miljøvariabler settes i `application-local.yml`:
```
spring.datasource.url=jdbc:postgresql://<HOST>:<PORT>/familie-ef-mottak
spring.datasource.username=<USERNAME>
spring.datasource.password=<PASSWORD>
```
f.eks. 
```
spring.datasource.url=jdbc:postgresql://0.0.0.0:5432/familie-ef-mottak
spring.datasource.username=postgres
spring.datasource.password=test
``` 

Les mer om postgres på nav [her](https://github.com/navikt/utvikling/blob/master/PostgreSQL.md). For å hente credentials manuelt, 
se [her](https://github.com/navikt/utvikling/blob/master/Vault.md). 

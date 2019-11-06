# familie-ef-mottak
Mottaksapplikasjon for søknader om overgangsstønad.

## Kjøre appen
`ApplicationLocal` launcher appen med Spring-profil `local` og appen blir tilgjengelig på port 8092.

## Database

For å sette opp Postgres-database lokalt med Docker:
```
docker run --name familie-ef-mottak -e POSTGRES_PASSWORD=<PASSWORD> -d -p 5432:5432 postgres
docker ps (finn container id)
docker exec -it <container_id> bash
psql -U postgres
CREATE DATABASE familie-ef-mottak;
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

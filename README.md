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

## Kommunisere med ef-sak lokalt
Dersom man ønsker å sende søknader til ef-sak, må man sette opp noen miljøvariabler fordi ef-sak krever preprod-autentisering
også lokalt. Følgende miljøvariabler må hentes fra azure secrets injectet i pod, og secrets-filen med navn familie-ef-mottak i dev-gcp:
* `AZURE_APP_CLIENT_ID`
* `AZURE_APP_CLIENT_SECRET`
* `EF_SAK_SCOPE`

Variablene legges inn under ApplicationLocal -> Edit Configurations -> Environment Variables. 

## Kafka
Lokalt må man kjøre serveren sammen med [navkafka-docker-compose](https://github.com/navikt/navkafka-docker-compose). Topicene vi lytter på og publiserer til må da opprettes via deres api med følgende data:

```
{
  "topics": [
    {
      "topicName": "aapen-brukernotifikasjon-nyBeskjed-v1",
      "members": [
        {"member":"srvc01", "role":"PRODUCER"}
      ],
      "numPartitions": 3
    },
    {
      "topicName": "aapen-dok-journalfoering-v1-q1",
      "members": [
        {"member":"srvc01", "role":"CONSUMER"}
      ],
      "numPartitions": 3
    },
  ]
}
```
Dette kan enkelt gjøres via følgende kommandoer:

```
curl -X POST "http://igroup:itest@localhost:8840/api/v1/topics" -H "Accept: application/json" -H "Content-Type: application/json" --data "{"name": "aapen-brukernotifikasjon-nyBeskjed-v1", "members": [{ "member": "srvc01", "role": "PRODUCER" }], "numPartitions": 3 }"

curl -X POST "http://igroup:itest@localhost:8840/api/v1/topics" -H "Accept: application/json" -H "Content-Type: application/json" --data "{"name": "aapen-dok-journalfoering-v1-q1", "members": [{ "member": "srvc01", "role": "CONSUMER" }], "numPartitions": 3 }"
```
Se README i navkafka-docker-compose for mer info om hvordan man kjører den og kaller apiet.

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

## Swagger 
f.eks. http://localhost:8092/swagger-ui/index.html#
FROM ghcr.io/navikt/baseimages/temurin:17
COPY ./target/familie-ef-mottak.jar "app.jar"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

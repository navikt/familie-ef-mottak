FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=TRUE
EXPOSE 8000
COPY ./target/familie-ef-mottak.jar "app.jar"

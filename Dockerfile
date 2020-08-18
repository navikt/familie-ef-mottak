FROM navikt/java:11
COPY init.sh /init-scripts/init.sh
RUN chmod +x /init-scripts/init.sh
COPY ./target/familie-ef-mottak.jar "app.jar"

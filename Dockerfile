FROM navikt/java:11
COPY srvInit.sh /init-scripts/srvInit.sh
RUN chmod +x /init-scripts/srvInit.sh
COPY ./target/familie-ef-mottak.jar "app.jar"

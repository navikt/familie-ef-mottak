FROM navikt/java:11
COPY srvinit.sh /init-scripts/srvinit.sh
RUN chmod +x /init-scripts/srvinit.sh
COPY ./target/familie-ef-mottak.jar "app.jar"

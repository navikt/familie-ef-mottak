FROM navikt/java:11
COPY init.sh /init-scripts/init.sh
COPY ./target/familie-ef-mottak.jar "app.jar"
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

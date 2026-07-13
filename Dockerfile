FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21
WORKDIR /app
ENV TZ="Europe/Oslo"
COPY target/familie-ef-mottak.jar app.jar
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"
CMD ["-jar", "app.jar"]
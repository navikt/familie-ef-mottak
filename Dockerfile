FROM gcr.io/distroless/java21-debian12:nonroot
ENV TZ="Europe/Oslo"
COPY target/familie-ef-mottak-jar-with-dependencies.jar /app/app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
CMD ["-jar", "/app/app.jar"]



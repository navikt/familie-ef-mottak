FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
ENV TZ="Europe/Oslo"
COPY target/familie-ef-mottak.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
CMD ["app.jar"]
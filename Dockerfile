FROM gcr.io/distroless/java21-debian12:nonroot
ENV TZ="Europe/Oslo"
COPY ./target/familie-ef-mottak.jar /app/app.jar
CMD ["-jar", "-XX:MaxRAMPercentage=75", "/app/app.jar"]


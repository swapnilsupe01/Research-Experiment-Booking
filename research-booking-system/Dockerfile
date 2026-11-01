FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/research-booking-system-0.0.1-SNAPSHOT.jar app.jar
# App listens on 8081 (matches application.properties server.port=8081)
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

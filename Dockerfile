# Simple runtime-only image — JAR is built locally with: mvn package -DskipTests
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY research-booking-system/target/research-booking-system-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

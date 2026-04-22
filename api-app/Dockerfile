# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY ../pom.xml .
COPY pom.xml .
COPY ../agent-app ./agent-app
COPY ../api-app ./api-app
COPY ../sdk-app ./sdk-app
RUN mvn -q -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/api-app/target/api-app-1.0.0-SNAPSHOT.jar .

EXPOSE 8082

CMD ["java", "-jar", "api-app-1.0.0-SNAPSHOT.jar"]

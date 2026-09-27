# syntax=docker/dockerfile:1

# --- Stage 1: compilación con Maven ---
FROM eclipse-temurin:21-jdk AS builder
ARG MODULE
WORKDIR /build
COPY . .
RUN chmod +x mvnw && ./mvnw -pl ${MODULE} -am package -DskipTests

# --- Stage 2: runtime con JRE ---
FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
COPY --from=builder /build/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
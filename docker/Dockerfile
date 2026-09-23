# syntax=docker/dockerfile:1
# Dockerfile genérico para los servicios del monorepo.
# Uso directo: docker build --build-arg MODULE=services/course-service -t paideia/course-service .
# En docker compose se pasa el ARG por servicio (perfil "full").
ARG MODULE=services/course-service

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
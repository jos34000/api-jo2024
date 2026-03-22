FROM maven:3.9-eclipse-temurin-21-alpine AS builder
LABEL maintainer="jos@jo2024.dev"
LABEL description="JO2024 Ticketing System - Backend API"

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 3003 -S appuser && \
    adduser -u 3001 -S appuser -G appuser

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app
USER appuser
EXPOSE 8000
EXPOSE 8001

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8001/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
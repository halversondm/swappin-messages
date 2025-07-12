FROM eclipse-temurin:21
LABEL authors="halversondm"
ARG JAVA_OPTS
RUN mkdir -p /app/logs
WORKDIR /app
ADD target/swappin-messages-0.0.1-SNAPSHOT.jar /app/lib/service.jar
EXPOSE 8080
CMD ["java", "${JAVA_OPTS}", "-jar", "/app/lib/service.jar"]
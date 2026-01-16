FROM eclipse-temurin:17-jre
WORKDIR /app

COPY build/libs/*.jar navik-backend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/navik-backend.jar"]
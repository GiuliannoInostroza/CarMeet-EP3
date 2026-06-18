FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8777
ENTRYPOINT ["java", "-jar", "app.jar"]

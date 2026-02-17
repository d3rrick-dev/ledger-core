FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/*.jar fintech-app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","fintech-app.jar"]
# Build stage
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /projectSem4
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run stage
FROM openjdk:21-jdk-slim
WORKDIR /projectSem4
COPY --from=build /projectSem4/target/*-0.0.1.jar app.jar
EXPOSE 8989
ENTRYPOINT ["java", "-jar", "app.jar"]
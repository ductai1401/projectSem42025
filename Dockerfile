
# Build stage với JDK 21
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN chmod +x mvnw
RUN mvn clean package -DskipTests

# Run stage với JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8989
ENTRYPOINT ["java", "-jar", "app.jar"]
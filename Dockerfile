# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy JAR thực tế (Spring Boot executable JAR)
COPY --from=build /app/target/projectSem4-0.0.1.jar app.jar
EXPOSE 8989
ENTRYPOINT ["java", "-jar", "app.jar"]
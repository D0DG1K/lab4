# Шаг 1: Сборка
FROM maven:3.8.7-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Шаг 2: Запуск
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/lab4-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
# Неделя 4
# Двухэтапная сборка: в первом этапе собираем JAR, во втором — только JRE + JAR (образ меньше и безопаснее).

# Этап 1: сборка. Нужны Maven и JDK, чтобы скомпилировать проект.
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn package -B -q -DskipTests

# Этап 2: запуск. Только JRE и готовый JAR — без исходников и Maven.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN adduser -D -s /bin/sh appuser
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
# Порт, на котором будет работать приложение
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

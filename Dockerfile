# syntax=docker/dockerfile:1

# ---------- 1) build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copia só o pom primeiro, para reaproveitar o cache de dependências
COPY pom.xml ./
RUN mvn -B -q -Dmaven.test.skip=true dependency:go-offline

COPY src ./src
RUN mvn -B -q -Dmaven.test.skip=true package

# ---------- 2) runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# o Render injeta PORT; o default serve para rodar local
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]

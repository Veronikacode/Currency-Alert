# syntax=docker/dockerfile:1

FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# Pre-fetch dependencies to leverage Docker layer caching
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY data-gatherer/pom.xml data-gatherer/pom.xml
COPY data-provider/pom.xml data-provider/pom.xml
COPY integration/pom.xml integration/pom.xml
RUN mvn -pl data-gatherer -am dependency:go-offline -B

# Copy the full project and build the service
COPY . .
RUN mvn -pl data-gatherer -am package -DskipTests -B

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /workspace/data-gatherer/target/data-gatherer-*-exec.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
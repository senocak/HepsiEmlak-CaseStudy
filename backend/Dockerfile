# Build stage
FROM maven:3.8-openjdk-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean install -DskipITs=true

# Package stage
FROM openjdk:17-jdk-slim
COPY --from=build /home/app/target/senocak-0.0.1.jar /usr/local/lib/app.jar
ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
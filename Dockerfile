FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system blog && useradd --system --gid blog blog
COPY --from=build --chown=blog:blog /build/target/MSA_blog-0.0.1-SNAPSHOT.jar app.jar
USER blog
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

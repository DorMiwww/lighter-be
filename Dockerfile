FROM gradle:jdk21 AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src src
RUN ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S lighter && adduser -S lighter -G lighter
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
USER lighter
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

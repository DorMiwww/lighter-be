FROM gradle:jdk21 AS build
WORKDIR /app

# 1. Copy only build files — this layer is cached until dependencies change
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle gradle

# 2. Download dependencies (cached layer — rebuilds only when build files change)
RUN ./gradlew dependencies --no-daemon

# 3. Copy source code (changes often)
COPY src src

# 4. Build
RUN ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S lighter && adduser -S lighter -G lighter
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
USER lighter
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

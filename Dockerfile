FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S lighter && adduser -S lighter -G lighter
WORKDIR /app
COPY build/libs/*-all.jar app.jar
USER lighter
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xmx256m", "app.jar"]

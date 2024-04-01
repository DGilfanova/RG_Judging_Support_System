FROM amazoncorretto:21-alpine-jdk as build
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar
CMD ["java", "-jar", "app.jar"]
EXPOSE 8080

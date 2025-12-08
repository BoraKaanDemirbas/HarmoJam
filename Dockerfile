FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar demo.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","demo.jar"]

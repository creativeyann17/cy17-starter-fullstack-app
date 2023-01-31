# build
FROM node:16.19.0-alpine AS build-ui
WORKDIR /tmp/ui
COPY ui/package.json ui/package-lock.json ./
RUN npm install
COPY ui/ . 
ARG UI_ENV_FILE
COPY ui/$UI_ENV_FILE .env
RUN npm run build

FROM maven:3.8.1-openjdk-17-slim as build-api
WORKDIR /tmp/api
COPY api/ .
COPY --from=build-ui /tmp/ui/build src/main/resources/static
RUN mvn clean install -DskipTests

FROM openjdk:17-alpine
COPY --from=build-api /tmp/api/target/api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
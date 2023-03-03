# build
FROM node:16.19.0-alpine AS build-ui
WORKDIR /tmp/ui
COPY ui/package.json ui/package-lock.json ./
RUN npm install
COPY ui/ . 
ARG UI_ENV_FILE=.env.dev
COPY ui/$UI_ENV_FILE .env
RUN npm run build

FROM maven:3.8.1-openjdk-17-slim as build-api
WORKDIR /tmp/api
COPY api/ .
COPY --from=build-ui /tmp/ui/build src/main/resources/static
RUN mvn clean install -DskipTests

FROM openjdk:17-alpine as build-jre
WORKDIR /tmp/jre
COPY --from=build-api /tmp/api/target/api-0.0.1-SNAPSHOT.jar app.jar
RUN unzip app.jar -d unzip

RUN $JAVA_HOME/bin/jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    -q \
    --recursive \
    --multi-release 17 \
    --class-path="./unzip/BOOT-INF/lib/*" \
    --module-path="./unzip/BOOT-INF/lib/*" \
    ./app.jar > deps.info

RUN apk add --no-cache binutils

RUN $JAVA_HOME/bin/jlink \
    --verbose \
    --add-modules $(cat deps.info) \
    --add-modules jdk.crypto.ec \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output minimal

FROM alpine:latest

WORKDIR /app

ENV JAVA_HOME=/jre
ENV JAVA_OPTS="-XX:+UseZGC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XshowSettings:vm -XX:+PrintCommandLineFlags"
ENV PATH="$PATH:$JAVA_HOME/bin"

COPY --from=build-jre /tmp/jre/minimal $JAVA_HOME
COPY --from=build-api /tmp/api/target/api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT java $JAVA_OPTS -jar app.jar
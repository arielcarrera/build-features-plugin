FROM amazoncorretto:17-alpine

ARG NEXUS_URL
ARG NEXUS_USER
ARG NEXUS_PASS

WORKDIR /build-features-plugin

ADD . .

RUN sh ./gradlew clean build publish
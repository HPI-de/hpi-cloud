FROM gradle:5.5-jdk8 as builder
COPY --chown=gradle:gradle . /home/src
RUN ls
WORKDIR /home/src/service-club
RUN gradle shadowJar


FROM openjdk:8-jre-slim

WORKDIR /
COPY --from=builder /home/src/service-club/build/libs/service-club.jar ./
CMD java -jar ./service-club.jar

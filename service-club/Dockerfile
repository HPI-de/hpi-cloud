FROM gradle:5.5-jdk12 as builder
COPY --chown=gradle:gradle . /home/src
WORKDIR /home/src/service-club
RUN gradle shadowJar


FROM openjdk:12

WORKDIR /
COPY --from=builder /home/src/service-club/build/libs/service-club.jar ./
CMD java -jar ./service-club.jar

FROM gradle:5.5-jdk12 as builder
COPY --chown=gradle:gradle . /home/src
WORKDIR /home/src/service-crashreporting
RUN gradle shadowJar


FROM openjdk:12

WORKDIR /
COPY --from=builder /home/src/service-crashreporting/build/libs/service-crashreporting-0.0.1-all.jar ./service-crashreporting.jar
CMD java -jar ./service-crashreporting.jar

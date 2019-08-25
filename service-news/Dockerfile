FROM gradle:5.4.1-jdk8 as builder
COPY --chown=gradle:gradle . /home/src
RUN ls
WORKDIR /home/src/service-news
RUN gradle shadowJar


FROM openjdk:8-jre-slim

WORKDIR /
COPY --from=builder /home/src/service-news/build/libs/service-news.jar ./
CMD java -jar ./service-news.jar

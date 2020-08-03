FROM alpine/git as clone
WORKDIR /home/clientService
RUN git clone https://github.com/vladsmirn289/ClientServiceRest.git

FROM maven:3.5-jdk-8-alpine as build
WORKDIR /home/clientService
COPY --from=clone /home/clientService/ClientServiceRest .
RUN mvn -DskipTests=true package

FROM openjdk:8-jre-alpine
WORKDIR /home/clientService
COPY --from=build /home/clientService/target/*.jar .
ENV db_host db
CMD java -jar *.jar --db_url=jdbc:postgresql://${db_host}:5432/shop_db
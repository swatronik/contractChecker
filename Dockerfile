FROM maven:3.6.0-jdk-8-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:8-jre-slim
COPY --from=build /home/app/target/*.jar /usr/local/lib/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]

#docker build  -t cchecker:v1 .
#docker run -d -p 8081:8081 -t cchecker:v1
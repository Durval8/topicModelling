FROM openjdk:22-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
EXPOSE 27017
ENTRYPOINT ["java","-jar","/app.jar"]
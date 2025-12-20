FROM maven
EXPOSE 8080
ENV APIKEY=a9c759e8-c2c7-45dc-98e3-8d77178e36f8
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
FROM maven
EXPOSE 8080
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
#COPY data/ /app/data/
ENTRYPOINT ["java","-jar","app.jar"]
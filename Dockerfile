FROM eclipse-temurin:21-jre
COPY target/prueba-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]

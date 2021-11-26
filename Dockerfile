FROM openjdk:8-jre
ADD target/format-0.0.1-SNAPSHOT.jar /usr/jar/format.jar
CMD ["java", "-jar","/usr/jar/format.jar"]

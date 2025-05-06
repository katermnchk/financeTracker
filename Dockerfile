FROM openjdk:17-jdk-slim-buster
WORKDIR /app
COPY target/financeTracker-0.0.1-SNAPSHOT.jar /app/financetracker.jar
ENTRYPOINT ["java", "-jar", "financetracker.jar"]

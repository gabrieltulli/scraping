FROM openjdk:8

WORKDIR /app

COPY target/*.jar /app/scraping.jar

ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n

ENTRYPOINT ["java", "-jar","scraping.jar"]

EXPOSE 8080
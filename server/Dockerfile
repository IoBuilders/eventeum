FROM openjdk:21-jdk-slim

ARG JAVA_PORT
ENV JAVA_PORT=${JAVA_PORT:-8086}

COPY target/eventeum-server.jar /app/eventeum-server.jar

CMD ["bash", "-c", "java -Dserver.port=${JAVA_PORT} -jar /app/eventeum-server.jar"]

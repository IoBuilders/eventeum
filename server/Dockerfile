FROM openjdk:21-jdk-slim
ADD target/eventeum-server.jar eventeum-server.jar
ADD docker-scripts/start-eventeum.sh start-eventeum.sh
ENV CONF ""
EXPOSE 8060
CMD chmod +x start-eventeum.sh && ./start-eventeum.sh
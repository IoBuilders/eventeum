FROM openjdk:8-jdk-alpine

ARG APP_VERSION
ENV APP_VERSION=${APP_VERSION}
ARG SET_JAVA_OPTS
ENV JAVA_OPTS="${SET_JAVA_OPTS}"

ADD target/eventeum-server.jar app.jar
RUN apk update && apk add bash
VOLUME /tmp
EXPOSE 8060
# CMD ["java", "$JAVA_OPTS", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
CMD java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar

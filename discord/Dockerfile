FROM openjdk:21-jdk
LABEL authors="Kaleidox"
WORKDIR /app

# copy shadow jar
COPY build/libs/chatmod-discord.jar app.jar

# create config dir volume
RUN mkdir -p /srv/chatmod
COPY dev-config.json5 /srv/chatmod/config.json5
RUN sed -i "s|localhost:5672|rabbitmq:5672|g" /srv/chatmod/*.json5
VOLUME /srv/chatmod

ENTRYPOINT ["java", "-jar", "app.jar"]

FROM openjdk:21-jdk

LABEL authors="Kaleidox"
WORKDIR /app
COPY build/libs/chatmod-discord.jar app.jar

VOLUME "/srv/chatmod"

ENTRYPOINT ["java", "-Xmx1G", "-jar", "app.jar"]

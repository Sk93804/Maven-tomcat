FROM tomcat:latest
WORKDIR /usr/local/tomcat/webapps/
COPY ./src/target/*.war .
RUN addgroup -S sudheesh && adduser -S sudheesh -s /bin/sh -G sudheesh --no-create-home
EXPOSE 8080
USER sudheesh
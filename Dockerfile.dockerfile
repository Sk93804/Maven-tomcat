FROM tomcat:latest
WORKDIR /usr/local/tomcat/webapps/
COPY ./target/helloworld.war .
# RUN addgroup -S sudheesh && adduser -S sudheesh -s /bin/sh -G sudheesh --no-create-home
ARG PKG 
RUN $PKG update 
EXPOSE 8080
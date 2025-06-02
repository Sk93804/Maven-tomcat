FROM Maven:latest AS build  
COPY ./pom.xml ./pom.xml
COPY ./src ./src 
RUN mvn validate && \
    mvn test && \
    mvn integration-test && \
    mvn clean package  && \
    mvn deploy 

FROM eclipse-temurin:17-jdk-alpine AS runtime
WORKDIR /app
RUN adduser -S sudheesh && addgroup -S sudheesh -S /bin/bash  -G sudheesh --no-create-home
COPY --from=build ./src/helloworld.jar /app/
CMD ['java', '-jar', 'helloworld.jar']  
USER sudheesh   
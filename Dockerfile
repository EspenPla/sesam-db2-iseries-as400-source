FROM openjdk:13 as build
COPY . /service/
WORKDIR service
RUN ./mvnw clean package

FROM openjdk:13-slim
COPY --from=build /service/target/sesam-db2-source-1.0-SNAPSHOT.jar /opt/sesam-db2-source-1.0-SNAPSHOT.jar
# RUN echo 'hosts: files mdns4_minimal [NOTFOUND=return] dns mdns4' >> /etc/nsswitch.conf
ENTRYPOINT ["java"]
CMD ["-XX:MinRAMPercentage=60", "-XX:MaxRAMPercentage=80", "-XshowSettings:vm", "-XX:+UseContainerSupport", "-jar", "/opt/sesam-db2-source-1.0-SNAPSHOT.jar"]
EXPOSE 8080:8080
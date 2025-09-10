#FROM eclipse-temurin:21-jdk-alpine
#VOLUME /tmp
#COPY *.jar MsSolicitudes.jar
#ENV JAVA_OPTS=" -Xshareclasses:name=cacheapp,cacheDir=/cache,nonfatal -XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
## Replace with a non-root user to avoid running the container with excessive privileges
#USER appuser
#ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar MsSolicitudes.jar" ]

# Imagen base con JDK 21
FROM eclipse-temurin:21-jdk-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el JAR generado
COPY applications/app-service/build/libs/MsSolicitudes.jar app.jar

# Exponer el puerto
EXPOSE 8081

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM lwieske/java-8
# set Time zone
ENV TZ=Asia/Shanghai

RUN set -eux; \
	ln -snf /usr/share/zoneinfo/$TZ /etc/localtime; \
	echo $TZ > /etc/timezone


ADD target/*.jar app.jar

ENTRYPOINT  java  ${JAVA_OPTS}   -jar   /app.jar

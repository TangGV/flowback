FROM lwieske/java-8
# set Time zone
ENV TZ=Asia/Shanghai

RUN set -eux; \
	ln -snf /usr/share/zoneinfo/$TZ /etc/localtime; \
	echo $TZ > /etc/timezone

ENV  JAVA_OPTS -Dfile.encoding=utf-8

ADD target/*.jar app.jar

ENTRYPOINT  java  ${JAVA_OPTS}   -jar   /app.jar

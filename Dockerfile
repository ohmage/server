FROM tomcat:7
MAINTAINER Steve Nolen <technolengy@gmail.com>
# Report issues here: https://github.com/ohmage/server

RUN set -x \
    && export DEBIAN_FRONTEND=noninteractive \
    && apt-get update \
    && apt-get install --no-install-recommends -y openjdk-7-jdk ant ant-optional netcat git\
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/local/tomcat/webapps/ROOT \
    && rm -rf /usr/local/tomcat/webapps/docs \
    && rm -rf /usr/local/tomcat/webapps/examples \
    && rm -rf /usr/local/tomcat/webapps/manager \
    && rm -rf /usr/local/tomcat/webapps/host-manager


 
#### download flyway (ohmage doesn't do migrations) ####
WORKDIR /flyway
ENV FLYWAY_TGZ_URL http://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/3.2.1/flyway-commandline-3.2.1.tar.gz
RUN set -x \
    && curl -fSL "$FLYWAY_TGZ_URL" -o flyway.tar.gz \
    && tar -xvf flyway.tar.gz --strip-components=1 \
    && rm flyway.tar.gz

WORKDIR /app
ADD . /app
RUN cp db/migration/* /flyway/sql/ \
  && ant clean dist \
  && cp dist/webapp-ohmage* "$CATALINA_HOME"/webapps/app.war \
  && cp docker_entrypoint.sh /run.sh \
  && chmod +x /run.sh \
  && rm -rf /app

RUN mkdir -p /var/lib/ohmage
RUN useradd -ms /bin/bash ohmage && \
    chown -R ohmage.ohmage "$CATALINA_HOME" && \
    chown -R ohmage.ohmage /var/lib/ohmage

EXPOSE 8080

VOLUME /var/lib/ohmage

CMD ["/run.sh"]

FROM jboss/wildfly
MAINTAINER Steve Nolen <technolengy@gmail.com>
# Report issues here: https://github.com/ohmage/server

USER root

RUN yum install -y ant ant-junit curl nc git mysql
 
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
  && cp dist/webapp-ohmage* /opt/jboss/wildfly/standalone/deployments/app.war \
  && cp docker_entrypoint.sh /run.sh \
  # modify run.sh to run wildfly instead of tomcat!
  && sed -i 's|^exec.*$|exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0|g' /run.sh \
  && chmod +x /run.sh \
  && rm -rf /app

RUN mkdir -p /var/lib/ohmage && ln -s /var/lib/ohmage /ohmage
RUN useradd -ms /bin/bash ohmage && \
    chown -R ohmage.ohmage /opt/jboss/wildfly/ && \
    chown -R ohmage.ohmage /var/lib/ohmage

EXPOSE 8080

VOLUME /ohmage

CMD ["/run.sh"]

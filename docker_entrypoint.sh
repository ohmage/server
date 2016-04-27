#!/usr/bin/env bash

set -e

# set these to support both --link (deprecated) and newer network container communication.
DB_HOST=mysql
DB_PORT=3306

# use passed env variable or defaults
MYSQL_DATABASE=${MYSQL_DATABASE:-ohmage}
MYSQL_USER=${MYSQL_USER:-ohmage}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-ohmage}
FQDN=${FQDN:-$HOSTNAME}
LOG_LEVEL=${LOG_LEVEL:-WARN}

# cat out ohmage.conf
echo "#
# DATABASE
#
db.driver=com.mysql.jdbc.Driver
db.jdbcurl=jdbc:mysql://$DB_HOST:$DB_PORT/$MYSQL_DATABASE?characterEncoding=utf8
db.username=$MYSQL_USER
db.password=$MYSQL_PASSWORD
#
# LOGGING
#
# The root logger 
log4j.rootLogger=$LOG_LEVEL, stdout

log4j.appender.stdout = org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Threshold = $LOG_LEVEL
log4j.appender.stdout.Target   = System.out
log4j.appender.stdout.layout = org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern = %-5p %d [%t][%F:%L] : %m%n
 
log4j.logger.org.ohmage=$LOG_LEVEL
log4j.logger.org.springframework=$LOG_LEVEL
log4j.logger.org.ohmage.util.JsonUtils=$LOG_LEVEL
log4j.logger.org.ohmage.cache.UserBin=$LOG_LEVEL" > /etc/ohmage.conf

if [ -n "$KEYCLOAK_JSON" ]; then
  echo "keycloak.config=$KEYCLOAK_JSON" >> /etc/ohmage.conf
fi
 
# cat out flyway.conf
# note that the placeholders wont be updated at each boot.
echo "flyway.url=jdbc:mysql://$DB_HOST:$DB_PORT/$MYSQL_DATABASE
flyway.user=$MYSQL_USER
flyway.password=$MYSQL_PASSWORD
flyway.placeholders.fqdn=$FQDN
flyway.placeholders.base_dir=/var/lib/ohmage" > /flyway/conf/flyway.conf

# create database stuffz. different depending on linked mysql container.
echo -n "waiting for mysql to start..."
while ! nc -w 1 $DB_HOST $DB_PORT &> /dev/null
do
  echo -n .
  sleep 1
done
echo 'mysql available.'

# execute migrations
/flyway/flyway migrate

# ensure ohmage directory structure exists
mkdir -p /var/lib/ohmage/audio
mkdir -p /var/lib/ohmage/audits
mkdir -p /var/lib/ohmage/documents
mkdir -p /var/lib/ohmage/images
mkdir -p /var/lib/ohmage/videos
chown -R ohmage.ohmage /var/lib/ohmage
 
# start tomcat in foreground
exec /usr/local/tomcat/bin/catalina.sh run

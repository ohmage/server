#!/usr/bin/env bash

MONGO_DB_LOC=vagrant-support/ohmage-test-db
DB_NAME=ohmage
HOME_DIR="~"

if [[ ! -d "/vagrant/$MONGO_DB_LOC/$DB_NAME" ]] ; then
    echo "WARNING: Mongo dump of \"$DB_NAME\" database not found in $MONGO_DB_LOC. Vagrant cannot populate its internal mongo instance."
fi
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
nginx=stable
apt-get install -y python-software-properties
add-apt-repository -y ppa:nginx/$nginx
apt-get update
apt-get install -y openjdk-7-jdk
mkdir -p /usr/share/tomcat7/bin
cp /vagrant/dev-setup/tomcat/setenv.sh /usr/share/tomcat7/bin
mkdir -p /etc/nginx/sites-enabled
cp /vagrant/dev-setup/nginx/ohmage /etc/nginx/sites-enabled
apt-get install -y tomcat7
apt-get install -y mongodb-org=2.6.3 mongodb-org-server=2.6.3 mongodb-org-shell=2.6.3 mongodb-org-mongos=2.6.3 mongodb-org-tools=2.6.3
debconf-set-selections <<< "postfix postfix/mailname string mytestserver"
debconf-set-selections <<< "postfix postfix/main_mailer_type string 'Internet Site'"
apt-get install -y postfix
apt-get install -y nginx
apt-get install -y ant
apt-get install -y git
apt-get install -y curl
if [[ -d "/vagrant/$MONGO_DB_LOC/$DB_NAME" ]] ; then
    echo "Restoring mongo database from /vagrant/$MONGO_DB_LOC/$DB_NAME -- this may take a while ..."
    mongorestore "/vagrant/$MONGO_DB_LOC"
fi
cd /vagrant
ant dist-without-tests
sudo -utomcat7 cp dist/ohmage.war /var/lib/tomcat7/webapps
sudo rm /etc/nginx/sites-enabled/default
sudo service nginx start
echo export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64 >> ~vagrant/.bashrc
echo export CATALINA_HOME=/var/lib/tomcat7 >> ~vagrant/.bashrc
echo export PATH=$PATH:/opt/ant/bin >> ~vagrant/.bashrc
echo export ANT_HOME=/opt/ant >> ~vagrant/.bashrc

#!/bin/bash

# The one parameter should be the .tar.gz that this build will be built from.
if [ $# != 2 ]; then
    echo "Parameters: <aw2.0 tarball with full path> <buildDatabase.sql with full path>"
    exit
else
    if [ -d /opt/aw ]; then
	echo "The person who runs this script must be the owner of /opt/aw."
    else
	echo "/opt/aw must exist even if it is empty."
	exit
    fi
fi

# Save the current directory for restoration later
ORIGINAL_DIR=`pwd`

echo Attempting to stop any existsing Tomcat instances.
/opt/aw/as/bin/asd.sh stop

echo Attempting to stop any existing MySQL instances.
/opt/aw/dbs/bin/dbsd.sh stop

echo Creating a backup.

# Backup the contents of /opt/aw
BACKUP="/tmp/aw-backup-"`eval date +%Y%m%d%H%M%S`".tgz"
tar czPf $BACKUP /opt/aw

echo Emptying /opt/aw and putting backup into /opt/aw.

# Delete the contents of /opt/aw
rm -rf /opt/aw/*
mv $BACKUP /opt/aw/

echo Unzipping the AndWellness tarball.

# Unzip and untar the folder into /opt/aw
cp $1 /opt/aw/AndWellness.tar.gz
cd /opt/aw
tar xf AndWellness.tar.gz
rm AndWellness.tar.gz

echo Unzipping the directory structure.

# Unzip and untar the local file structure.
tar -xf aw*.tar.gz

echo Starting the database.

# Start the database
/opt/aw/dbs/bin/dbsd.sh start

echo Sleeping to give the database time to startup.

sleep 5

# Check to make sure the database is running
APACHE_SUCCESS="ready for connections"
case `tail /opt/aw/dbs/logs/error.log` in
    *"$APACHE_SUCCESS"*) echo Database is running;;

    *) echo Failed to start the database ; tail /opt/aw/dbs/logs/error.log; exit;;
esac

echo Initializing the database.

# Initialize the database
/opt/aw/thirdparty/mysql-5.1.41-linux-i686-icc-glibc23/bin/mysql -u andwellness -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock -p <$2

echo Installing the configurations.

# Install the configurations
/opt/aw/thirdparty/jre1.6.0_17/bin/java -jar /opt/aw/conf/andwellness-config-loader-1.0.jar /opt/aw/nih-all.xml /opt/aw/conf/loader.properties /opt/aw/conf/configuration-1.0.xsd
/opt/aw/thirdparty/jre1.6.0_17/bin/java -jar /opt/aw/conf/andwellness-config-loader-1.0.jar /opt/aw/chipts-all-surveys.xml /opt/aw/conf/loader.properties /opt/aw/conf/configuration-1.0.xsd

echo Installing the web apps.

# Install the webapps
cp /opt/aw/*war /opt/aw/as/webapps

echo Starting Tomcat.

# Start Tomcat
/opt/aw/as/bin/asd.sh start

echo Waiting to give Tomcat time to startup

sleep 5

# Check to make sure Tomcat is running
TOMCAT_SUCCESS="initialization completed in"
case `tail /opt/aw/as/logs/aw.log` in
    *"$TOMCAT_SUCCESS"*) echo Tomcat successfully started;;

    *) echo Failed to start Tomcat;;
esac

# Go back to the original folder we were working in
cd $ORIGINAL_DIR

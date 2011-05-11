#!/bin/bash

# The one parameter should be the .tar.gz that this build will be built from.
if [ $# != 2 ]; then
    echo "Usage: $0 <aw2.3 tarball with full path> <mobilize | andwellness | chipts>";
    exit;
elif [ ! -d /opt/aw ]; then
    echo "/opt/aw/ must exist and be a directory even if it is empty.";
    exit;
elif [ ! -w /opt/aw ]; then
    echo "You must be able to write to /opt/aw/ .";
    exit;
elif [ ! -r $1 ]; then
    echo "Cannot read $1.";
    exit;
elif [ $2 != mobilize -a $2 != andwellness -a $2 != chipts ]; then
    echo "Unknown mode $2.";
    exit;
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
MYSQL_SUCCESS="ready for connections"
case `tail /opt/aw/dbs/logs/error.log` in
    *"$MYSQL_SUCCESS"*) 
        echo Database is running;
        ;;

    *) echo 
        Failed to start the database; 
        tail /opt/aw/dbs/logs/error.log; 
        exit;
        ;;
esac

echo Initializing the database.

# Initialize the database
/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock </opt/aw/dbs/conf/andwellness-ddl.sql
/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/campaign_privacy_states.sql
/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/campaign_running_states.sql
/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/campaign_roles.sql
/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/class_roles.sql
/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/mobility_privacy_states.sql

if [$2 == chipts]; then
	/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/survey_response_privacy_states-chipts.sql
else
	/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/survey_response_privacy_states.sql
fi

/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/preferences.sql
if [$2 == mobility]; then
	/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/mobility_preferences.sql
elif [$2 == chipts]; then
        /opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/chipts_preferences.sql
else
	/opt/aw/thirdparty/mysql-5.1.40-osx10.5-x86_64/bin/mysql --user=andwellness --password=\&\!sickly -h localhost -P 3306 -S /opt/aw/dbs/logs/dbsd.sock andwellness </opt/aw/dbs/conf/andwellness_preferences.sql
fi

echo Installing the web app.

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
    *"$TOMCAT_SUCCESS"*) 
        echo Tomcat successfully started;
        ;;

    *) 
        echo Failed to start Tomcat;
        tail /opt/aw/as/logs/aw.log;
        exit;
        ;;
esac

# Go back to the original folder we were working in
cd $ORIGINAL_DIR

# Warn about changing the password on the database.
echo It is highly recommended that you change the default MySQL password as it is located in every distribution of this package.
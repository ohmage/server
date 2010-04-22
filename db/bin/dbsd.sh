#!/bin/bash

EXE=`basename $0`

if [ $# != 1 -o "$1" == "--help" ]; then
    cat <<EOF >&2

Controls a DBS.

Usage: 
  $EXE [--help] <start>|<stop>

EOF
    exit 1
fi

case $1 in
    start)
        cd /opt/aw/thirdparty/mysql-5.1.41-linux-i686-icc-glibc23
	./bin/mysqld_safe --defaults-file=/opt/aw/dbs/conf/my.cnf &> /dev/null &
        ;;
    stop)
	[ -r "/opt/aw/dbs/logs/dbsd.pid" ] && PID=`cat /opt/aw/dbs/logs/dbsd.pid`
	if [ -n $PID ] && ps -p $PID &> /dev/null; then 
	    kill $PID
	fi
	;;
    *)
        $0 --help
        ;;
esac

#!/bin/bash

EXE=`basename $0`

if [ $# != 1 -o "$1" == "--help" ]; then
    cat <<EOF >&2

Controls an AS.

Usage: 
  $EXE [--help] <start>|<stop>

EOF
    exit 1
fi

export JRE_HOME="/opt/aw/thirdparty/jre1.6.0_17"
export JAVA_HOME="$JRE_HOME"
#export JAVA_OPTS="-server -Xms1024m -Xmx1536m -XX:+AggressiveHeap -Dsun.net.inetaddr.ttl=300"
export JAVA_OPTS="-server -XX:+AggressiveHeap -Dsun.net.inetaddr.ttl=300"
export CATALINA_HOME="/opt/aw/thirdparty/apache-tomcat-6.0.20"
export CATALINA_SH="$CATALINA_HOME/bin/catalina.sh"
export CATALINA_BASE="/opt/aw/as"
export CATALINA_PID="$CATALINA_BASE/logs/asd.pid"

echo $CATALINA_HOME

case $1 in
    start)
	"$CATALINA_SH" "$1"
	;;
    stop)
	export JAVA_OPTS=""
	"$CATALINA_SH" "$1" -force
	;;
    *)
        "$0" --help
        ;;
esac

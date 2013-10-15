#/bin/sh -e


start() {
	[ ! -d work ] && mkdir -p work	
	nohup java -jar -DSTOP.PORT=8079 -DSTOP.KEY=metacatalogue-solr -Dsolr.solr.home=solr start.jar 2>&1 >> ${LOG} &
}


stop() {
        java -jar -DSTOP.PORT=8079 -DSTOP.KEY=metacatalogue-solr start.jar --stop
}

usage() {
    echo "USAGE"
    echo "  ${prog} <start|stop|restart>"
    echo " "
}

LOG_DIR="./logs"
[ ! -d ${LOG_DIR} ] && mkdir -p ${LOG_DIR}

LOG="${LOG_DIR}/solr.log"

case ${1} in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    *)
        echo "option '${1}' unknown"
        usage
        exit 1
        ;;
esac


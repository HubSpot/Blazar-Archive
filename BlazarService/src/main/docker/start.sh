#!/bin/bash

if [ ${DOCKER_HOST} ]; then
	HOST_AND_PORT=`echo $DOCKER_HOST | awk -F/ '{print $3}'`
	HOST_IP="${HOST_AND_PORT%:*}"
fi

DEFAULT_URI_BASE="http://${HOST_IP:=localhost}:${BLAZAR_PORT:=8099}${BLAZAR_UI_BASE:=/blazar}"


[[ ! ${BLAZAR_PORT:-} ]] || args+=( -Ddw.server.connector.port="$BLAZAR_PORT" )

args+=( -Xmx${BLAZAR_MAX_HEAP:-512m} )
args+=( -Djava.net.preferIPv4Stack=true )
args+=( -Ddw.zookeeper.quorum="${BLAZAR_ZK:=localhost:2181}" )
args+=( -Ddw.zookeeper.namespace="${BLAZAR_ZK_NAMESPACE:=blazar}" )
args+=( -Ddw.ui.baseUrl="${BLAZAR_URI_BASE:=$DEFAULT_URI_BASE}" )

[[ ! ${BLAZAR_DB_USER:-} ]] || args+=( -Ddw.database.user="${BLAZAR_DB_USER}" )
[[ ! ${BLAZAR_DB_PASSWORD:-} ]] || args+=( -Ddw.database.password="${BLAZAR_DB_PASSWORD}" )
[[ ! ${BLAZAR_DB_URL:-} ]] || args+=( -Ddw.database.url="${BLAZAR_DB_URL}" -Ddw.database.driverClass="${BLAZAR_DB_DRIVER_CLASS:-com.mysql.jdbc.Driver}" )

if [[ "${BLAZAR_DB_MIGRATE:-}" != "" ]]; then
	echo "Running: java ${args[@]} -jar /BlazarService.jar db migrate /etc/blazar/blazar.yaml --migrations /etc/blazar/schema.sql"
	java "${args[@]}" -jar /BlazarService.jar db migrate /etc/blazar/blazar.yaml --migrations /etc/blazar/schema.sql
fi

echo "Running: java ${args[@]} -jar /BlazarService.jar $*"
exec java "${args[@]}" -jar /BlazarService.jar $*

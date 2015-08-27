#!/bin/bash
set -e

PATH=/usr/local/sbin:/usr/sbin:/sbin:/usr/lib64/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin

[[ ! ${BLAZAR_APP_ROOT:-} ]] || args+=( -Ddw.server.applicationContextPath="$BLAZAR_APP_ROOT" )

[[ ! ${BLAZAR_PORT:-} ]] || args+=( -Ddw.server.connector.port="$BLAZAR_PORT" )
[[ ! ${BLAZAR_MYSQL_USER:-} ]] || args+=( -Ddw.database.user="$BLAZAR_MYSQL_USER" )
[[ ! ${BLAZAR_MYSQL_PASSWORD:-} ]] || args+=( -Ddw.database.password="$BLAZAR_MYSQL_PASSWORD" )

args+=( -Xmx${BLAZAR_MAX_HEAP:-512m} )
args+=( -Djava.net.preferIPv4Stack=true )
args+=( -Ddw.zookeeper.quorum="${BLAZAR_ZK:=localhost:2181}" )
args+=( -Ddw.zookeeper.namespace="${BLAZAR_ZK_NAMESPACE:=blazar}" )
args+=( -Ddw.database.url="jdbc:mysql://${BLAZAR_MYSQL_HOST:-localhost}:${BLAZAR_MYSQL_PORT:-3306}/${BLAZAR_MYSQL_DATABASE:-Blazar}" )

if [[ -f "${BLAZAR_DB_MIGRATIONS:-}" ]]; then
    echo "Executing db migrations from ${BLAZAR_DB_MIGRATIONS}"
    echo "Running: java ${args[@]} -jar /BlazarService.jar db migrate ${BLAZAR_CONF_FILE:-/etc/blazar/blazar.yaml} --migrations ${BLAZAR_DB_MIGRATIONS}"
    java "${args[@]}" -jar /BlazarService.jar db migrate ${BLAZAR_CONF_FILE:-/etc/blazar/blazar.yaml} --migrations ${BLAZAR_DB_MIGRATIONS}
fi

echo "Running: java ${args[@]} -jar /BlazarService.jar $*"
exec java "${args[@]}" -jar /BlazarService.jar $*

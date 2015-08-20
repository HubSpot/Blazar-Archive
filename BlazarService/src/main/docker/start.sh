#!/bin/bash

PATH=/usr/local/sbin:/usr/sbin:/sbin:/usr/lib64/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin

[[ ! ${BLAZAR_APP_ROOT:-} ]] || args+=( -Ddw.server.applicationContextPath="$BLAZAR_APP_ROOT" )

[[ ! ${BLAZAR_PORT:-} ]] || args+=( -Ddw.server.connector.port="$BLAZAR_PORT" )
[[ ! ${BLAZAR_MYSQL_USER:-} ]] || args+=( -Ddw.database.user="$BLAZAR_MYSQL_USER" )
[[ ! ${BLAZAR_MYSQL_PASSWORD:-} ]] || args+=( -Ddw.database.password="$BLAZAR_MYSQL_PASSWORD" )

args+=( -Xmx${BLAZAR_MAX_HEAP:-512m} )
args+=( -Djava.net.preferIPv4Stack=true )
args+=( -Ddw.database.url="jdbc:mysql://${BLAZAR_MYSQL_HOST:-localhost}:${BLAZAR_MYSQL_PORT:-3306}/${BLAZAR_MYSQL_DATABASE:-Blazar}" )

echo "Running: java ${args[@]} -jar /BlazarService.jar $*"
exec java "${args[@]}" -jar /BlazarService.jar $*

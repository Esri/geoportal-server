#!/bin/sh

# See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# Esri Inc. licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# ------------------------------------------
# Basic Explanation on what this does ...
# ------------------------------------------
function Usage ()
{
        echo
	echo Usage : grants_linuxpg.sh [host] [port] [database] [geoportal10 schema] [postgresUser] [geoportal10User]

	echo Where [host] is the host name
	echo       [port] is the port number of postgreSQL
	echo       [database] is the database name
	echo       [geoportal10 schema] is the geoportal10 schema name
	echo       [geoportal10User] is the geoportal10 user name
	echo       [postgresUser] is the user name to connect as, not the user name to create
	echo eg. grants_linuxpg.sh localhost 5432 postgres geoportal10 postgres geoportal10pwd
	exit 1
}

function Run ()
{
        echo "$(date)" >> grants_pg.txt
	echo  Running grants_pg.sql ...   >> grants_pg.txt
	./createuser  -P -R -S -D -h $HOST_NAME -p $PORT_NUM -U $_USERTOCONNECT $_GEOPORTALUSER >> grants_pg.txt
	./psql  -e -h $HOST_NAME -p $PORT_NUM -d $DB_NAME -U $_USERTOCONNECT -v geoportalschema=$_GEOPORTALSCHEMA -v geoportaluser=$_GEOPORTALUSER -v geoportaluserpwd=$_GEOPORTALUSERPWD -f grants_pg.sql >> grants_pg.txt
	echo  ... All done.   >> grants_pg.txt
	sudo gedit grants_pg.txt
}


HOST_NAME=$1
PORT_NUM=$2
DB_NAME=$3
_GEOPORTALSCHEMA=$4
_USERTOCONNECT=$5
_GEOPORTALUSER=$6


if [ "$1" = "" ]; then 
	Usage 
fi

if [ "$2" = "" ]; then 
	Usage 
fi

if [ "$3" = "" ]; then 
	Usage 
fi

if [ "$4" = "" ]; then 
	Usage 
fi

if [ "$5" = "" ]; then 
	Usage 
fi

if [ "$6" = "" ]; then 
	Usage 
fi

Run

exit 0


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
	echo Usage : create_schema_pg.cmd [host] [port] [geoportal database] [geoportal user] 
	echo Where [host] is the host name
	echo       [port] is the port number of postgreSQL
	echo       [geoportal database] is the geoportal database name
	echo       [geoportal user] is the geoportal user.
	echo e.g. create_schema_pg localhost 5432 geoportal10 geoportal10pwd
	exit 1
}

function Run ()
	{
	echo "$(date)" >> GPT_schema.txt
	echo  Running schema_pg.sql ...   >> GPT_Schema.txt
	./psql  -e -h $HOST_NAME -p $PORT_NUM -d $DB_NAME -U $_GEOPORTALUSER -f schema_pg.sql >> GPT_Schema.txt
	echo  ... All done.   >> GPT_Schema.txt
	sudo gedit GPT_Schema.txt
}

HOST_NAME=$1
PORT_NUM=$2
DB_NAME=$3
_GEOPORTALUSER=$4

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

Run

exit 0

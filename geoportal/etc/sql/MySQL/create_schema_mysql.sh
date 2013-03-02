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
Usage()
{
	echo
	echo Usage : ./grants_mysql.sh [host] [port] [geoportal database] [geoportal username] [geoportal password] 
        echo
	echo "Where [host] is the host name"
	echo "      [port] is the port number of mysql" 
	echo "      [geoportal database] is the geoportal database name"
        echo "      [geoportal username] is the geoportal user"
        echo "      [geoportal password] is the geoportal user password"
        echo
        echo "      e.g. ./create_schema_mysql.sh localhost 3306 geoportal geoportal geoportalpwd"
        exit 1
}

Run()
{
	date >> geoportal_schema.txt
        echo  Running schema_mysql.sql ...   >> geoportal_schema.txt
        mysql --verbose --host=$HOST_NAME --port=$PORT_NUM --user=$GEOPORTALUSER --password=$PASSWORD --execute="DROP DATABASE IF EXISTS $DB_NAME" >> geoportal_schema.txt
        mysql --verbose --host=$HOST_NAME --port=$PORT_NUM --user=$GEOPORTALUSER --password=$PASSWORD --execute="CREATE DATABASE $DB_NAME" >> geoportal_schema.txt
        mysql --verbose --host=$HOST_NAME --port=$PORT_NUM --user=$GEOPORTALUSER --password=$PASSWORD $DB_NAME < schema_mysql.sql >> geoportal_schema.txt
        echo ... All done. >> geoportal_schema.txt
}

HOST_NAME=$1
PORT_NUM=$2
DB_NAME=$3
GEOPORTALUSER=$4
PASSWORD=$5

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

Run

exit 0

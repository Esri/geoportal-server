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
	echo "Usage : ./grants_mysql.sh [dbserver] [port] [geoportal database] [sys username] [sys password] [geoportal username] [geoportal server] [geoportal password]"
        echo
	echo "Where [dbserver] is the name of the database server"
	echo "      [port] is the port number of MySQL"
	echo "      [geoportal database] is the geoportal database name"
        echo "      [sys username] is the username of the sys user in MySQL"
        echo "      [sys password] is the password of the sys user in MySQL"
        echo "      [geoportal username] is the geoportal user"
		echo "      [geoportal server] is the geoportal application server name"
        echo "      [geoportal password] is the geoportal user password"
        echo
        echo "      e.g. ./grants_mysql.sh localhost 3306 geoportal sys sys geoportal localhost geoportalpwd"
        exit 1
}

Run() 
{
        date >> grants.txt
	echo Running grants_mysql.sh ... >> grants.txt
	    mysql --verbose --host=$DBSERVER --port=$PORT_NUM --user=$SYSUSER --password=$SYSPWD --execute="CREATE USER '$GEOPORTALUSER'@'localhost' IDENTIFIED BY '$PASSWORD'" >> grants.txt
        mysql --verbose --host=$DBSERVER --port=$PORT_NUM --user=$SYSUSER --password=$SYSPWD --execute="GRANT ALL ON $DB_NAME.* TO '$GEOPORTALUSER'@'localhost'" >> grants.txt
        mysql --verbose --host=$DBSERVER --port=$PORT_NUM --user=$SYSUSER --password=$SYSPWD --execute="CREATE USER '$GEOPORTALUSER'@'$GEOPORTALSERVER' IDENTIFIED BY '$PASSWORD'" >> grants.txt
        mysql --verbose --host=$DBSERVER --port=$PORT_NUM --user=$SYSUSER --password=$SYSPWD --execute="GRANT ALL ON $DB_NAME.* TO '$GEOPORTALUSER'@'$GEOPORTALSERVER'" >> grants.txt
        echo  ... All done.   >> grants.txt
}

DBSERVER=$1
PORT_NUM=$2
DB_NAME=$3
SYSUSER=$4
SYSPWD=$5
GEOPORTALUSER=$6
GEOPORTALSERVER=$7
PASSWORD=$8

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

if [ "$7" = "" ]; then 
	Usage 
fi

if [ "$8" = "" ]; then 
	Usage 
fi

Run

exit 0

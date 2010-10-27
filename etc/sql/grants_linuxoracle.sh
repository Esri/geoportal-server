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
	echo Usage : grants_linuxoracle [sys username] [sys password] [geoportal username]
	echo Where [sys username] is the username of the sys user in Oracle.
	echo       [sys password] is the password of the sys user in Oracle.
	echo       [geoportal username] is the geoportal user
	echo e.g. grants_linuxoracle  sys sys geoportal10
	exit 1
}

function Run ()
	{
	echo "$(date)" >> grants.txt
	echo  Running grants_oracle.sql ...   >> grants.txt
	SQLPLUS /nolog @grants_oracle.sql $SYSUSER $SYSPWD $GEOPORTALUSER >>  grants.txt
	echo  ... All done.   >> grants.txt
	sudo gedit grants.txt
}


SYSUSER=$1
SYSPWD=$2
GEOPORTALUSER=$3

if [ "$1" = "" ]; then 
	Usage 
fi

if [ "$2" = "" ]; then 
	Usage 
fi

if [ "$3" = "" ]; then 
	Usage 
fi


Run

exit 0

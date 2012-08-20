rem See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem Esri Inc. licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

@ECHO OFF
IF "%1"=="" Goto Usage
IF "%2"=="" Goto Usage
IF "%3"=="" Goto Usage
IF "%4"=="" Goto Usage
IF "%5"=="" Goto Usage



SET HOST_NAME=%1
SET PORT_NUM=%2
SET DB_NAME=%3
SET _GEOPORTALSCHEMA=%4
SET _USERTOCONNECT=%5
SET _GEOPORTALUSER=%6


:Run

date /t > grants_pg.txt
echo  Running grants_pg.sql ...   >> grants_pg.txt
createuser  -P -R -S -D -h %HOST_NAME% -p %PORT_NUM% -U %_USERTOCONNECT% %_GEOPORTALUSER%   >> grants_pg.txt
psql  -e -h %HOST_NAME% -p %PORT_NUM% -d %DB_NAME% -U %_USERTOCONNECT% -v geoportalschema=%_GEOPORTALSCHEMA% -v geoportaluser=%_GEOPORTALUSER% -v geoportaluserpwd=%_GEOP0RTALUSERPWD% -f grants_pg.sql >> grants_pg.txt
echo  ... All done.   >> grants_pg.txt
start notepad grants_pg.txt

if errorlevel 1 goto Failed


goto END



:Failed
ECHO "Setting grants failed"  
ECHO.

:Usage
rem ------------------------------------------
rem Basic Explanation on what this does ...
rem ------------------------------------------
ECHO.
ECHO Usage : grants_pg.cmd [host] [port] [database] [geoportal schema] [Databaseuser] [geoportalUser]
ECHO Where [host] is the machine name hosting PostgreSQL
ECHO       [port] is the port number of postgreSQL
ECHO       [database] is the database name for the geoportal tables
ECHO       [geoportal schema] is the geoportal schema name
ECHO       [Databaseuser] User to connect to the database as
ECHO       [geoportalUser] is the geoportal user name
ECHO e.g. grants_pg localhost 5432 postgres geoportal10 postgres geoportal10
goto END


:END


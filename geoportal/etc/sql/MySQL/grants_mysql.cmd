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
IF "%6"=="" Goto Usage
IF "%7"=="" Goto Usage
IF "%8"=="" Goto Usage

SET DBSERVER=%1
SET PORT_NUM=%2
SET DB_NAME=%3
SET SYSUSER=%4
SET SYSPWD=%5
SET GEOPORTALUSER=%6
SET GEOPORTALSERVER=%7
SET PASSWORD=%8


:Run

date /t > Grants.txt
echo  Running grants_mysql.sql ...   >> grants.txt
mysql --verbose --host=%DBSERVER% --port=%PORT_NUM% --user=%SYSUSER% --password=%SYSPWD% --execute="CREATE USER '%GEOPORTALUSER%'@'localhost' IDENTIFIED BY '%PASSWORD%'" >> grants.txt
mysql --verbose --host=%DBSERVER% --port=%PORT_NUM% --user=%SYSUSER% --password=%SYSPWD% --execute="GRANT ALL ON %DB_NAME%.* TO '%GEOPORTALUSER%'@'localhost'" >> grants.txt
mysql --verbose --host=%DBSERVER% --port=%PORT_NUM% --user=%SYSUSER% --password=%SYSPWD% --execute="CREATE USER '%GEOPORTALUSER%'@'%GEOPORTALSERVER%' IDENTIFIED BY '%PASSWORD%'" >> grants.txt
mysql --verbose --host=%DBSERVER% --port=%PORT_NUM% --user=%SYSUSER% --password=%SYSPWD% --execute="GRANT ALL ON %DB_NAME%.* TO '%GEOPORTALUSER%'@'%GEOPORTALSERVER%'" >> grants.txt
echo  ... All done.   >> grants.txt
start notepad grants.txt


if errorlevel 1 goto Failed


goto END




:Failed
ECHO "Executing MySQL Grants failed"  
ECHO.

:Usage
rem ------------------------------------------
rem Basic Explanation on what this does ...
rem ------------------------------------------
ECHO.
ECHO Usage : grants_mysql [dbserver] [port] [Geoportal database] [sys username] [sys password] [geoportal username] [geoportal server] [geoportal password]
ECHO Where [dbserver] is the machine name hosting MySQL
ECHO       [port] is the port number of MySQL
ECHO       [Geoportal database] is the database that contains the Geoportal Schema
ECHO       [sys username] is the username of the sys user in MySQL
ECHO       [sys password] is the password of the sys user in MySQL
ECHO       [geoportal username] is the geoportal user
ECHO       [geoportal server] is the name of the geoportal web server
ECHO       [geoportal password] is the geoportal user password
ECHO e.g. grants_mysql localhost 3306 geoportal sys sys geoportal localhost geoportalpwd
goto END


:END
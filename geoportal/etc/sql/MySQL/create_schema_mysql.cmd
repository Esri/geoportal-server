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
SET GEOPORTALUSER=%4
set PASSWORD=%5


:Run

date /t > Geoportal_schema.txt
echo  Running schema_mysql.sql ...   >> Geoportal_schema.txt
mysql --verbose --host=%HOST_NAME% --port=%PORT_NUM% --user=%GEOPORTALUSER% --password=%PASSWORD% --execute="DROP DATABASE IF EXISTS %DB_NAME%" >> Geoportal_schema.txt
mysql --verbose --host=%HOST_NAME% --port=%PORT_NUM% --user=%GEOPORTALUSER% --password=%PASSWORD% --execute="CREATE DATABASE %DB_NAME%" >> Geoportal_schema.txt
mysql --verbose --host=%HOST_NAME% --port=%PORT_NUM% --user=%GEOPORTALUSER% --password=%PASSWORD% %DB_NAME% < schema_mysql.sql >> Geoportal_schema.txt
echo  ... All done.   >> Geoportal_schema.txt
start notepad Geoportal_schema.txt

if errorlevel 1 goto Failed


goto END



:Failed
ECHO "Geoportal database building has failed"  
ECHO.

:Usage
rem ------------------------------------------
rem Basic Explanation on what this does ...
rem ------------------------------------------
ECHO.
ECHO Usage : create_schema_mysql.cmd [host] [port] [Geoportal database] [geoportal user] [geoportal password]
ECHO Where [host] is the machine name hosting MySQL
ECHO       [port] is the port number of MySQL
ECHO       [Geoportal database] is the database that contains the Geoportal Schema
ECHO       [geoportal user] is the geoportal user.
ECHO       [geoportal password] is the geoportal password.
ECHO e.g. create_schema_mysql localhost 3306 geoportal geoportal geoportalpwd
goto END


:END

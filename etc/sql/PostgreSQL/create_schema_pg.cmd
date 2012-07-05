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


SET HOST_NAME=%1
SET PORT_NUM=%2
SET DB_NAME=%3
SET GEOPORTALUSER=%4


:Run

date /t > Geoportal_schema.txt
echo  Running schema_pg.sql ...   >> Geoportal_schema.txt
psql  -e -h %HOST_NAME% -p %PORT_NUM% -d %DB_NAME% -U %GEOPORTALUSER% -f schema_pg.sql >> Geoportal_schema.txt
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
ECHO Usage : create_schema_pg.cmd [host] [port] [Geoportal database] [geoportal10 user] 
ECHO Where [host] is the machine name hosting PostgreSQL
ECHO       [port] is the port number of postgreSQL
ECHO       [Geoportal database] is the database that contains the Geoportal Schema
ECHO       [geoportal10 user] is the geoportal10 user.
ECHO e.g. create_schema_pg localhost 5432 postgres geoportal10
goto END


:END

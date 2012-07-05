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



SET DBSERVER=%1
SET DATABASE=%2
SET GEOPORTALUSERLOGIN=%3
SET GEOPORTALUSERPWD=%4


:Run

echo Building Database %DATABASE% on %DBSERVER%...
osql -E -S %DBSERVER% -d MASTER -w 200 -n -b -Q "if exists (Select * from master.dbo.sysdatabases where name = '%DATABASE%') drop database %DATABASE%"
if errorlevel 1 (
  ECHO.
  ECHO Unable to drop database
  osql -E -S %DBSERVER% -d MASTER -w 200 -n -b -Q "select spid, left(p.hostname, 20) as hostname, left(p.program_name, 30) as program_name, left(p.loginame, 20) as loginname from master.dbo.sysprocesses p join master.dbo.sysdatabases d on d.dbid = p.dbid where d.name = '%DATABASE%'"
  ECHO.
  goto Failed
  )

if errorlevel 1 goto Failed

osql -E -S %DBSERVER% -d MASTER -w 200 -n -b -Q "Create Database %DATABASE%" 
if errorlevel 1 goto failed

osql -E -S %DBSERVER% -d MASTER -w 200 -n -b -Q  "if not exists (select * from dbo.syslogins where name = N'%GEOPORTALUSERLOGIN%') EXEC sp_addlogin N'%GEOPORTALUSERLOGIN%', N'%GEOPORTALUSERPWD%',N'%DATABASE%'"
if errorlevel 1 goto failed


osql -E -S %DBSERVER% -d %DATABASE% -w 200 -n -b -Q  "if not exists (select * from dbo.sysusers where name = N'%GEOPORTALUSERLOGIN%' and uid < 16382)  EXEC sp_grantdbaccess N'%GEOPORTALUSERLOGIN%'"
if errorlevel 1 goto failed


osql -E -S %DBSERVER% -d %DATABASE% -w 200 -n -b -Q  "if exists (select * from dbo.sysusers where name = N'%GEOPORTALUSERLOGIN%' and uid < 16382)  EXEC sp_addrolemember  N'db_owner', N'%GEOPORTALUSERLOGIN%'"
if errorlevel 1 goto failed


osql -S %DBSERVER% -d %DATABASE%   -U %GEOPORTALUSERLOGIN%  -P  %GEOPORTALUSERPWD%  -n -b -i schema_mssql.sql -o build_schema.log
if errorlevel 1 goto failed


goto END




:Failed
ECHO "Geoportal Database building has failed"  
ECHO.

:Usage
rem ------------------------------------------
rem Basic Explanation on what this does ...
rem ------------------------------------------
ECHO.
ECHO Usage : Create_schema_mssql [database server machine] [Geoportal database name] [Geoportal database user] [Geoportal database user password]
ECHO Where [database server machine] is the machine name on which SQL Server is hosted.
ECHO       [Geoportal database name] is the name of the Geoportal database
ECHO       [Geoportal database user] is the name of the login and user that will have access to the Geoportal database,
ECHO       [Geoportal database user password] is the password for the login and user of the Geoportal database
ECHO e.g. Create_schema_mssql.cmd  localhost geoportal10 geoportal10 geoportal10pwd
goto END


:END
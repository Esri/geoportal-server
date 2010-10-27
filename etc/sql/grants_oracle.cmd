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


SET SYSUSER=%1
SET SYSPWD=%2
SET GEOPORTALUSER=%3


:Run

date /t > Grants.txt
echo  Running grants_oracle.sql ...   >> grants.txt
SQLPLUS /nolog @grants_oracle.sql %SYSUSER% %SYSPWD% %GEOPORTALUSER% >>  grants.txt
echo  ... All done.   >> grants.txt
start notepad grants.txt


if errorlevel 1 goto Failed


goto END




:Failed
ECHO "Executing Oracle Grants failed"  
ECHO.

:Usage
rem ------------------------------------------
rem Basic Explanation on what this does ...
rem ------------------------------------------
ECHO.
ECHO Usage : grants_oracle [sys username] [sys password] [geoportal username]
ECHO Where [sys username] is the username of the sys user in Oracle.
ECHO       [sys password] is the password of the sys user in Oracle.
ECHO       [geoportal username] is the geoportal user
ECHO e.g. grants_oracle  sys sys geoportal10
goto END


:END
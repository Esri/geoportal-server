@ECHO OFF
IF "%1"=="" Goto Usage
IF "%2"=="" Goto Usage


SET GEOPORTALUSER=%1
SET GEOPORTALPWD=%2


:Run

date /t > Geoportal_schema.txt
echo  Running schema_oracle.sql ...   >> Geoportal_Schema.txt
SQLPLUS /nolog @schema_oracle.sql %GEOPORTALUSER% %GEOPORTALPWD% >> Geoportal_Schema.txt
echo  ... All done.   >> Geoportal_Schema.txt
start notepad Geoportal_Schema.txt

if errorlevel 1 goto Failed


goto END



:Failed
ECHO "Geoportal Database building has failed"  
ECHO.

:Usage
rem ------------------------------------------
rem Basic Explanation on what this does ...
rem ------------------------------------------
ECHO.
ECHO Usage : create_schema_oracle [geoportal10 username] [geoportal10 password]
ECHO Where [geoportal10 username] is the geoportal10 user.
ECHO       [geoportal10 password] is the password of the geoportal10 user
ECHO e.g. create_schema_oracle geoportal10 geoportal10pwd
goto END


:END
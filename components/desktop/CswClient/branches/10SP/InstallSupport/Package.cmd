echo Packaging Install
REM attrib -R C:\Projects\GeoportalServer\components\desktop\*.* /D /S
REM "C:\Projects\GeoportalServer\components\desktop\CswClient\trunk\Install\RemoveDirectories.exe" REM "C:\Projects\GeoportalServer\Geoportal\trunk\src\gpt\search\profiles" .svn /r
REM "C:\Projects\GeoportalServer\components\desktop\CswClient\trunk\Install\RemoveDirectories.exe" REM "C:\Projects\GeoportalServer\components\desktop\CswClient\addins\explorer\trunk\src\Images" .svn /r
REM DEL /F /Q "\\esri.com\PSdata\CM_APPSRV\Projects\Geoportal\Latest\CSWClients\*.*"
"C:\Program Files\Installshield\2011 SAB\System\IsCmdBld.exe" -p "C:\Builds\GeoportalServer\CSWClients\Sources\CswClient\InstallSupport\CSWClients.ism" -y 0.0.0
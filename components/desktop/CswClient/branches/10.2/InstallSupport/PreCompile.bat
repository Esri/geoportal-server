RD /S /Q C:\Builds\GeoportalServer\10.2_CSWClients\Sources\AppLogger\branches\10.2
RD /S /Q C:\Builds\GeoportalServer\10.2_CSWClients\Sources\Geoportal\trunk\src\gpt\search\profiles


XCOPY C:\Repositories\geoportal-server\components\desktop\AppLogger\branches\10.2\*.* C:\Builds\GeoportalServer\10.2_CSWClients\Sources\AppLogger\branches\10.2 /E /I /Y
XCOPY C:\Repositories\geoportal-server\geoportal\src\gpt\search\profiles\*.* C:\Builds\GeoportalServer\10.2_CSWClients\Sources\Geoportal\trunk\src\gpt\search\profiles /E /I /Y

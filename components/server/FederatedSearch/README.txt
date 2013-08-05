=====================================================
ESRI Geoportal Server
Copyright �2010 Esri. 

=====================================================

This module supports integrating search results from Geoportal Server into the search results inside the
Portal for ArcGIS 10.2.


DEPLOYMENT:

1. Copy provided 'custom' folder into the 'home' web application of your Portal for ArcGIS, e.g., \\C:\Program Files\ArcGIS\Portal\webapps\arcgis#home\custom
   
2. Edit the \\custom\federated-searches.js file's 'config' section and add as many as needed REST endpoints of instances of
   Geoportal Server, for example:
   
   {rest: "http://myserver/geoportal/rest/find/document", caption: "My Server"}
   
3. Locate the 'search.html' file in the 'home' application of your Portal for ArcGIS, and add the following snippet
   of code just before the section <style> starts:
   
   <script type="text/javascript" src="./custom/federated-searches.js">
   </script>
   <link rel="stylesheet" type="text/css" href="./custom/federated-searches.css">
   

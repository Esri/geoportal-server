=====================================================
ESRI Geoportal Server
Copyright ©2010 Esri. 

=====================================================

This module allows to integrate search results from 
Geoportal Server into the serach results inside the
Portal for ArcGIS 10.2

DEPLOYMENT:

1. Copy provided 'custom' folder into the 'home' 
   application of your Portal for ArcGIS,
   
2. Edit custom/federated-searches.js file, section 'config'
   and add as many as needed REST end points of instances of
   Geoportal Server, for example:
   
   {rest: "http://myserver/geoportal/rest/find/document", caption: "My Server"}
   
3. Locate 'search.html' file in the 'home' application 
   of your Portal for ArcGIS, and add the following snippet
   of code just before the section <style> starts:
   
   </script>
   <script type="text/javascript" src="./custom/federated-searches.js">
   </script>
   <link rel="stylesheet" type="text/css" href="./custom/federated-searches.css">
   
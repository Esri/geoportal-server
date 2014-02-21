=====================================================
ESRI Geoportal Server
Copyright 2010 Esri. 

=====================================================

This module supports integrating search results from Geoportal Server into the search results inside the
Portal for ArcGIS 10.2.


DEPLOYMENT:

1. Copy provided 'custom' folder into the 'home' web application of your Portal for ArcGIS, e.g., \\C:\Program Files\ArcGIS\Portal\webapps\home\custom
   
2. Edit the \\custom\federated-searches-json.js file's 'config' section and add as many as needed REST endpoints of instances of
   Geoportal Server, for example:
   
   {rest: "http://myserver/geoportal/rest/find/document", caption: "My Server"}
   
3. Locate the 'search.html' file in the 'home' application of your Portal for ArcGIS, and add the following snippet
   of code just before the section <style> starts:
   
   <script type="text/javascript" src="./custom/federated-searches-json.js">
   </script>
   <link rel="stylesheet" type="text/css" href="./custom/federated-searches-json.css">
   
4. Save the two files you adjusted.

5. To test, launch the Portal web application and do a search for content. When results are returned, you should see your server listed on the left, under the "Search additional catalogues" heading. Click the link to your server, and search results from your endpoint should appear in the interface.
   

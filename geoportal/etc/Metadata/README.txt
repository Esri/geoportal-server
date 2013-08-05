===============================================================================

Esri Metadata Editor for ArcGIS Portal
Copyright 2013 Esri. 

===============================================================================
   
Deployment instructions:

1. Deploy provided 'metadata.war' web application file on the 
   Oracle (TM) GlassFish application server running Esri ArcGIS Portal, e.g. \\ArcGIS\Portal\framework\runtime\tomcat\webapps.
   
   Use application name: 'metadata'
       application type: 'Web Application'
	   
   Leave all other settings default.
   
2. Locate 'item.html' file from 'home' application. Typical location is:

   /portal/portal/webapps/home/item.html (Linux)
   
   or
   
   C:\portal\portal\webapps\home\item.html (Windows)
   
3. Look for the line including script 'arcgisonline.js'; right after that line 
   paste the following snippet:
   
    <!-- Customization for extended metadata (view/edit) -->
    <script type="text/javascript">
        gptConfig = {
            "GptCore.serverContextPath": "/metadata",
            "ItemMetadata.enabled": true,
            "ItemMetadata.allowSaveDraft": true,
            "ItemMetadata.gxeProxyUrl": "",
            "ItemMetadata.useGxeProxyUrl": false
        }
    </script>
    <script type="text/javascript" src="/metadata/gpt/agp/AgpAddins.js">
    </script>
    <!-- ............................................... -->
   
4. If you decide to change application name in step 1, correct the following:

   "GptCore.serverContextPath"
   URL to 'AgpAddins.js'

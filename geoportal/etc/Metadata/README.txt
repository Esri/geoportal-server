===============================================================================

Esri Metadata Editor for ArcGIS Portal
Copyright ©2013 Esri. 

===============================================================================
   
Deployment instructions:

1. Verify that a web application server other than the one that comes with Portal for ArcGIS is available in the same domain as Portal for ArcGIS. If one is not available, then install web application server software, e.g., Tomcat, GlassFish, or WebLogic. In these instructions, you will be deploying the metadata.war file in this container. IMPORTANT: Do NOT deploy into the web application server running Portal for ArcGIS, i.e., \\C:\Program Files\ArcGIS\Portal\framework\runtime\tomcat\webapps

One option is to install Tomcat and configure isapi redirect per http://ashrafhossain.wordpress.com/2010/09/20/how-to-configure-iis-7-and-tomcat-redirection-on-windows-server-2008-64-bit/ 

2. Deploy the 'metadata.war' web application file into the web app container discussed in Step 1. 
   
3. Locate 'item.html' file from 'home' application. Typical location is:

    /portal/portal/webapps/home/item.html (Linux)

    or

    C:\portal\portal\webapps\home\item.html (Windows)

4.  Look for the line including script 'arcgisonline.js'; right after that line paste the following snippet:

<!-- Customization for extended metadata (view/edit) -->
<script type="text/javascript">
gptConfig = {
"GptCore.serverContextPath": "http://your_web_app_server_URL/metadata",
"ItemMetadata.enabled": true,
"ItemMetadata.allowSaveDraft": true,
"ItemMetadata.gxeProxyUrl": "",
"ItemMetadata.useGxeProxyUrl": false,
"ItemMetadata.gemetThemesProxy": null
}
</script>
<script type="text/javascript" src=" http://your_web_app_server_URL/metadata/gpt/agp/AgpAddins.js">
</script>
<!-- ............................................... -->

5. Update the snippet at the ‘GptCore.serverContextPath’ and second ‘script’ src to reference the URL to your web application server.

6. If the geoportal metadata instance and Portal for ArcGIS instance are served on different hostname/port, need to configure proxy using the following steps:
	a. Copy gpt/gxe/gxe-proxy.jsp to arcgis#sharing/gxe-proxy.jsp
	b. Set "ItemMetadata.useGxeProxyUrl" to true
	c. Set "ItemMetadata.gxeProxyUrl" to http://portalforarcgisservername/arcgis/sharing/gxe-proxy.jsp

7. Save the item.html file


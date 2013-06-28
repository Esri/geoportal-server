<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
        <title>ArcGIS Viewer for Flex</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta name="description" content="Esri ArcGIS viewer for Flex"/>
        <meta name="keywords" content="Esri, ArcGIS, Flex Viewer"/>
        <meta name="author" content="Esri"/>

        <!-- Include CSS to eliminate any default margins/padding and set the height of the html element and
             the body element to 100%, because Firefox, or any Gecko based browser, interprets percentage as
             the percentage of the height of its parent container, which has to be set explicitly.  Fix for
             Firefox 3.6 focus border issues.  Initially, don't display flashContent div so it won't show
             if JavaScript disabled.
        -->
        <style type="text/css" media="screen">
            html, body  { height:100%; }
            body { margin:0; padding:0; overflow:auto; text-align:center;
                   background-color: #ffffff; }
            object:focus { outline:none; }
            #flashContent { display:none; }
        </style>

        <!-- Enable Browser History by replacing useBrowserHistory tokens with two hyphens -->
        <!-- BEGIN Browser History required section >
        <link rel="stylesheet" type="text/css" href="history/history.css" />
        <script type="text/javascript" src="history/history.js"></script>
        <! END Browser History required section -->

        <script type="text/javascript">
            var isIElt9 = false;
        </script>
        <!--[if lt IE 9]>
            <script type="text/javascript">
                isIElt9 = true;
            </script>
        <![endif]-->
        <script type="text/javascript" src="swfobject.js"></script>
        <script type="text/javascript">
        
        <!-- Geoportal Function -->

        function getFlexApp(appName) {
          if (navigator.appName.indexOf ("Microsoft") !=-1)
          {
            return window[appName];
          }
          else
          {
            return document[appName];
          }
        }

        function addResource(title, resource) {
          getFlexApp("index").addResource(title, resource); 
        }
        <%
          java.util.Locale locale = request.getLocale();
          
		  String sConfigFile = "config.xml";
		  String sLocaleConfig = "config_" + locale.toString() + ".xml";
		  String sCurrentDirectory = getServletContext().getRealPath("/") + "viewer";
		  String sConfigFilePath = sCurrentDirectory + java.io.File.separator + sLocaleConfig;
		  java.io.File file = new java.io.File(sConfigFilePath);
          boolean exists = file.exists();
		  if(exists){
		    sConfigFile = sLocaleConfig;
		  }
		  
          String sLocales = locale.toString();
          int i = 0;
          java.util.Enumeration e = request.getLocales();
          while (e.hasMoreElements()) {
            sLocales += "," + ((java.util.Locale)e.nextElement()).toString();

          }
          sLocales += ",en_US";
          sLocales = sLocales.replaceAll(",,", ",");
           
        %>
            <!-- For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection. -->
            var swfVersionStr = "10.2.0";
            <!-- To use express install, set to playerProductInstall.swf, otherwise the empty string. -->
            var xiSwfUrlStr = "playerProductInstall.swf";
            var flashvars = {};
            flashvars.localeChain = "<%=sLocales%>";
			flashvars.config = "<%=sConfigFile%>";
            var params = {};
            params.quality = "high";
            params.bgcolor = "#ffffff";
            params.allowscriptaccess = "sameDomain";
            params.allowfullscreen = "true";
            var isAIR = navigator.userAgent.indexOf("AdobeAIR") != -1;
            if (isAIR || isIElt9)
            {
                // workaround for overlaying tool tips and other content when loaded into an AIR app
                // workaround for cursor issue - https://bugs.adobe.com/jira/browse/SDK-26818
                params.wmode = "opaque";
            }
            var attributes = {};
            attributes.id = "index";
            attributes.name = "index";
            attributes.align = "middle";
            swfobject.embedSWF(
                "index.swf", "flashContent",
                "100%", "100%",
                swfVersionStr, xiSwfUrlStr,
                flashvars, params, attributes);
            <!-- JavaScript enabled so display the flashContent div in case it is not replaced with a swf object. -->
            swfobject.createCSS("#flashContent", "display:block;text-align:left;");
        </script>
    </head>
    <body>
        <!-- SWFObject's dynamic embed method replaces this alternative HTML content with Flash content when enough
             JavaScript and Flash plug-in support is available. The div is initially hidden so that it doesn't show
             when JavaScript is disabled.
        -->
        <div id="flashContent">
            <p>
                To view this page ensure that Adobe Flash Player version
                10.2.0 or greater is installed.
            </p>
            <script type="text/javascript">
                var pageHost = ((document.location.protocol == "https:") ? "https://" : "http://");
                document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='"
                                + pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" );
            </script>
        </div>

        <noscript>
            <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="100%" id="index">
                <param name="movie" value="index.swf" />
                <param name="quality" value="high" />
                <param name="bgcolor" value="#ffffff" />
                <param name="allowScriptAccess" value="sameDomain" />
                <param name="allowFullScreen" value="true" />
                <!--[if !IE]>-->
                <object type="application/x-shockwave-flash" data="index.swf" width="100%" height="100%">
                    <param name="quality" value="high" />
                    <param name="bgcolor" value="#ffffff" />
                    <param name="allowScriptAccess" value="sameDomain" />
                    <param name="allowFullScreen" value="true" />
                <!--<![endif]-->
                <!--[if gte IE 6]>-->
                    <p>
                        Either scripts and active content are not permitted to run or Adobe Flash Player version
                        10.2.0 or greater is not installed.
                    </p>
                <!--<![endif]-->
                    <a href="http://www.adobe.com/go/getflashplayer">
                        <img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash Player" />
                    </a>
                <!--[if !IE]>-->
                </object>
                <!--<![endif]-->
            </object>
        </noscript>
   </body>
</html>

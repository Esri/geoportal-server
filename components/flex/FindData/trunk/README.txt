=====================================================
ESRI Geoportal Server
Copyright ©2010 Esri. 

=====================================================

This is a Flex based widget for searching Geoportals.

Required tools for project compilation and testing:

1. Flex 4.6 SDK

2) ArcGIS Flex API 3.3 or 3.5

3) ArcGIS Viewer for Flex 3.3 or 3.5 

4) Flash Builder 4.6 IDE


To compile/run this widget you need to:


1) Download ArcGIS Flex API 3.5 library from the esri resource center

2) Download ArcGIS Viewer For Flex source code from the esri resource center/github and unzip

3) Copy the src folder in this directory into the root directory of the above 
unzipped resource

4) Follow the ArcGIS Viewer for Flex instructions to import the source code to the Flex
Builder

5) Under project properties -> Flex compiler, put in

-source-path  locale/{locale} geoportal-git/geoportal/locale/{locale} -locale en_US

6) Compile the com.esri.gpt.* files into a geoportal.swc which you then copy into the lib directory

7) Add the mxml widgets/GeoportalSearch/AGSGptRSSWidget.mxml into your modules and 
into the config.xml
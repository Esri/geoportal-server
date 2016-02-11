This folder contains build scripts to create Geoportal Server JAR package 
and Geoportal Server WAR web application.

BUILD INSTRUCTIONS

1. Obtain a copy of Apache Maven from http://maven.apache.org
2. Invoke the following command from geoportal/maven directory:

	mvn clean install
	
USING NETBEANS

1. Select "File -> Open Project" option from main menu and navigate to the geoportal/maven directory
2. Select "Run -> Clean and build" option from main menu

USING ECLIPSE

1. Select "File -> Import" option from main menu
2. Select "Maven -> Existing Maven Projects" from import dialog box and click "Next" button
3. Click "Browse" button beside "Root Directory" and navigate to the geoportal/maven folder
4. Once list of projects appear on the "Projects" list, make sure at least parent project is selected, 
   then click "Finish" button
5. Select "Project -> Build All" from main menu

CUSTOM BUILDS

You can customize your build by providing your own version for gpt.xml, gpt.properties or any other 
configuration file. To do that, use <USER_HOME_DIRECTORY>/geoportal/endorsed folder to store your
custom files. This folder retains an original structure of Geoportal Server web application, so
to override gpt.xml, the exact location of the custom file will be:

<USER_HOME_DIRECTORY>/geoportal/endorsed/WEB-INF/classes/gpt/config/gpt.xml

Any other examples of files you may wont to replace, are:

<USER_HOME_DIRECTORY>/geoportal/endorsed/WEB-INF/classes/logging.properties
<USER_HOME_DIRECTORY>/geoportal/endorsed/META_INF/context.xml

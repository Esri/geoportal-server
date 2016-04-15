This folder contains build scripts to create Geoportal Server JAR package 
and Geoportal Server WAR web application.

BUILD PROFILES

This projects allows to use two profiles:

1. release-profile (this is a default profile)
2. development-profile 

The release-profile will produce artifacts ready for distribution and deployment, however a 'war' application will need to be configured after deployment.

The development-profile will produce pre-configured 'war' file. This profile is usefull when working on product development with predefined configuration.
Any such configuration must be stored inside <USER_HOME_DIRECTORY>/geoportal/endorsed folder. The content of this folder retains structure of the web
application, with two most distinctive folders: WEB-INF and META-INF.

Most typically, developer would need custom logging.properties, gpt.xml and context.xml. They should be placed as follows:

<USER_HOME_DIRECTORY>/geoportal/endorsed/WEB-INF/classes/gpt/config/gpt.xml
<USER_HOME_DIRECTORY>/geoportal/endorsed/WEB-INF/classes/logging.properties
<USER_HOME_DIRECTORY>/geoportal/endorsed/META_INF/context.xml

Note! When development-profile is selected, build will fail if <USER_HOME_DIRECTORY>/geoportal/endorsed folder is absent.


BUILD INSTRUCTIONS

1. Obtain a copy of Apache Maven from http://maven.apache.org
2. Invoke the following command from geoportal/maven directory:

	mvn clean 
	mvn install 
	
	or 
	
	mvn install -P development-profile
	
	
USING NETBEANS

1. Select "File -> Open Project" option from main menu and navigate to the geoportal/maven directory
2. Select desired profile "Run -> Set Project Configuration -> devlopment-profile" or leave as default profile
3. Select "Run -> Clean and build" option from main menu


USING ECLIPSE

1. Select "File -> Import" option from main menu
2. Select "Maven -> Existing Maven Projects" from import dialog box and click "Next" button
3. Click "Browse" button beside "Root Directory" and navigate to the geoportal/maven folder
4. Once list of projects appear on the "Projects" list, make sure at least parent project is selected, 
   then click "Finish" button
5. Expand geoportal project node in "Project Explorer"
6. Select "Project -> Properties"
7. Select "Maven"
8. Type "devlopment-profile" in the input box or leave input box empty for default profile
9. Right click on the top level pom.xml and select "Run As -> Maven Clean" then "Run As -> Maven Install"

<<<<<<< HEAD
<<<<<<< HEAD

ESRI Geoportal Server
=====================================================
Copyright �2010 Esri. 

This is the primary Java Web Application for the ESRI Geoportal Server.

Required tools for project compilation and testing:

1. Java SDK 6 (tested against):
   - version 1.6.22 or later
   
2. Java Server Faces 2.0 (tested against):
   - Project Mojarra (https://javaserverfaces.dev.java.net/)
   
3. Servlet Container 2.5 (tested against):
   - Apache Tomcat 6.0.26
   - Glassfish 3.0.1
   - Oracle WebLogic 9i 

4. LDAP server (tested against):
   - Apache Directory Server 1.5.7   
   
5. Database and JDBC drivers for Java 6 (tested against):
   - PostgreSQL 8.4.5
   - Microsoft SQL Server 2008
   - Oracle Database Server 10g, 11g
   
6. IDE (developed on):
   - Eclipse IDE
   - NetBeans IDE   

=====================================================
   
Quick start:

1. Create Geoportal database schema on the installed 
   database server(scripts available in etc/sql folder)
   
2. Create groups and users within installed LDAP server
   - create groups: gptRegisteredUser, gptPublisher, 
     gptAdministrator
   - create at least one user belonging to each of 
     these groups
   
3. Create "jdbc/gpt" resource and connection pool on 
   the web server.
   - refer to the user manual for Glassfish 3.0.1 or
     Oracle WebLogic 9i
   - for Apache Tomcat 6.0.26 you may create resource 
     within the deployment context as shown in 
	   etc/tomcat/geoportal.xml
   
4. Create project within your favorite IDE

5. Compile, deploy, enjoy.
=======
# Geoportal

## Welcome 

Geoportal Server is a standards-based, open source product that enables discovery and use of geospatial resources including data and services.

Geoportal Server allows you to catalog the locations and descriptions of your organization's geospatial resources in a central repository called a geoportal, which you can publish to the Internet or your intranet. Visitors to the geoportal can search and access these resources to use with their projects. If you grant them permission, visitors can also register geospatial resources with the geoportal. Geoportals give you an enterprise-level view of your geospatial resources regardless of their type or location. Resources are registered with a geoportal using metadata, which describes the location, age, quality, and other characteristics of the resources. With access to this information about resources, an organization can make decisions based on the best resources available.
Geoportal Server has been hosted on SourceForge since 2010. We are in the process of migrating to GitHub as part of Esri's larger effort to support developers of the Esri platform with open source apps, samples, and SDK. Stay tuned for updates of this migration process.

With the Geoportal Server you can:
* Improve the efficiency and effectiveness of geospatial activities within your enterprise and across organizations.
* Support collaboration and cooperation among departments and organizations by facilitating the sharing of geospatial resources regardless of the GIS platform.
* Gain an enterprise-level awareness of disparate geospatial data, Web services, and activities.
* Leverage existing geospatial resources so your organization doesn't duplicate those resources or the effort to create them.
* Ensure the use of approved, high-quality datasets.
* Reduce the time users spend trying to find relevant, usable geospatial resources.

Want to learn more? Try our [sandbox site](http://gptogc.esri.com)!


## Documentation

Geoportal Server documentation is broken down into three buckets: a wiki, Javadoc for server components, and JavaScript documentation for the Geoportal XML Editor.

* The Geoportal Server media wiki hosts topics for how to install, configure, customize, and use the Geoportal Server and its related components.
* The Geoportal Server Javadoc describes the Java packages used in the Geoportal Server web application.
* The Geoportal XML Editor JavaScript documentation explains its JavaScript classes and XML elements and configuration files.

## Support or Contact

Contact us at [portal@esri.com](mailto:portal@esri.com) for questions and we’ll help you sort it out.


## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Anyone and everyone is welcome to contribute.

## Licensing

Copyright 2010-2012 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

>>>>>>> origin/gh-pages
=======
# Geoportal

## Welcome 

Geoportal Server is a standards-based, open source product that enables discovery and use of geospatial resources including data and services.

Geoportal Server allows you to catalog the locations and descriptions of your organization's geospatial resources in a central repository called a geoportal, which you can publish to the Internet or your intranet. Visitors to the geoportal can search and access these resources to use with their projects. If you grant them permission, visitors can also register geospatial resources with the geoportal. Geoportals give you an enterprise-level view of your geospatial resources regardless of their type or location. Resources are registered with a geoportal using metadata, which describes the location, age, quality, and other characteristics of the resources. With access to this information about resources, an organization can make decisions based on the best resources available.
Geoportal Server has been hosted on SourceForge since 2010. We are in the process of migrating to GitHub as part of Esri's larger effort to support developers of the Esri platform with open source apps, samples, and SDK. Stay tuned for updates of this migration process.

With the Geoportal Server you can:
* Improve the efficiency and effectiveness of geospatial activities within your enterprise and across organizations.
* Support collaboration and cooperation among departments and organizations by facilitating the sharing of geospatial resources regardless of the GIS platform.
* Gain an enterprise-level awareness of disparate geospatial data, Web services, and activities.
* Leverage existing geospatial resources so your organization doesn't duplicate those resources or the effort to create them.
* Ensure the use of approved, high-quality datasets.
* Reduce the time users spend trying to find relevant, usable geospatial resources.

Want to learn more? Try our [sandbox site](http://gptogc.esri.com)!


## Documentation

Geoportal Server documentation is broken down into three buckets: a wiki, Javadoc for server components, and JavaScript documentation for the Geoportal XML Editor.

* The Geoportal Server media wiki hosts topics for how to install, configure, customize, and use the Geoportal Server and its related components.
* The Geoportal Server Javadoc describes the Java packages used in the Geoportal Server web application.
* The Geoportal XML Editor JavaScript documentation explains its JavaScript classes and XML elements and configuration files.

## Support or Contact

Contact us at [portal@esri.com](mailto:portal@esri.com) for questions and we’ll help you sort it out.


## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Anyone and everyone is welcome to contribute.

## Licensing

Copyright 2010-2012 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
>>>>>>> e85b518985c2fd6c17ca8f762ff79fe6803735d6

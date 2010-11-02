=====================================================
ESRI Geoportal Server
Copyright ©2010 Esri. 

=====================================================

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
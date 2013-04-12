04/12/2013 - Sample for implementing checkboxes instead of Text Fields for custom search page
======
This customization provides for checkboxes instead of search fields for a custom search criteria field. See the included screenshot 'custom_checkbox.png' as an example. 

*IMPORTANT*
For this customization you will need to understand the steps discussed at the Add Custom Search Criteria topic (http://github.com/Esri/geoportal-server/wiki/Add-Custom-Search-Criteria). This example follows the same steps as described in that document, except includes changes to support checkboxes instead of free text search. 

*Steps*
In this example, we add a few values from the MD_ScopeCode codelist from ISO 19115:2003 (http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml) as checkboxes on the main search page. You can adjust these steps as needed for your own search criteria.
Also useful may be setting up the geoportal code in Eclipse. You can use any other java IDE of course, but for Eclipse instructions see http://github.com/Esri/geoportal-server/wiki/Eclipse-Project-from-Source-Code.

1) Follow steps as described in http://github.com/Esri/geoportal-server/wiki/Add-Custom-Search-Criteria), except you will use the files included in this customization for reference instead of the java class, criteria.jsp, gpt-config.xml and gpt.properties provided in the wiki topic. Note, configuration for the web.xml is the same as in the wiki topic, but the sample is included here as well.
 
2) Once the classes are compiled, you can put them in the \\geoportal\www\WEB-INF\classes location:
\\ geoportal\www\WEB-INF\classes\CustomRestQueryServlet.class
\\ geoportal\www\WEB-INF\classes\SearchFilterHierarchy.class

3) Other files go here:
\\geoportal\www\WEB-INF\web.xml
\\geoportal\www\WEB-INF\gpt-faces-config.xml
\\geoportal\www\catalog\search\criteria.jsp
\\geoportal\www\WEB-INF\classes\gpt\resources\gpt.properties

4) To test, you will need sample metadata that has values in the /gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode element. Included are two records with which you can test; one has a Hierarchy level of dataset, the other of model. Publish these sample records to your geoportal and approve them. Now go to the search page. If you check the ‘dataset’ checkbox and click search, your dataset record should be returned. If you check the ‘model’ checkbox and click search, the  model record should return, etc.

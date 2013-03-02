/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
Package contains classes pertaining to performing a search on the
selected search interface.

<h2>Adding a Custom Search Filter to the Search Criteria</h2>

An example of how to do this can be found in the GPT help documentation called
"Add Custom Search Criteria".

The following describes how to add a new Search Filter.  It's not the only way.
The following describes how to achieve this with or without the source code.
<p>
Create a new search filter object that either inherits from ISearchFilter,
or from one of the SearchFilter* classes.
If JSF is being used, the custom filter class can be made into a managed bean so that
its properties can be set on the Search JSP page.
<p>
The new custom filter should then be added to the searchCriteria.
<p>
<code>
Search Criteria searchCriteria = new SearchCriteria();
CustomFilter filter = new CustomFilter();
searchCriteria.getMisselleniousFilters().add(filter);
</code>
<p>
If you are using JSF and you can do the above by adding the custom filter
managed bean into the Search Criteria's misselleniousFilters list in
the WEB-INF/gpt-faces-config.xml.
<p>
During Search, your filter will be translated into an xml representation 
togather with all the other filters.  The xsd for this representation
can be fould at WEB-INF/class/gpt/search/gptNativeSearch.xsd.  
This xml is then transformed into a CSW request using the gpt2csw_OGCCore.xslt.
Your last step will therefore be to customize the xslt so that your custom 
search filter parameters can affect the query.  
<p>
<strong>WARNING:</strong>  Its important to have all your custom filter classes in the 
miscellaneous filter list 
have unique classnames (Object.class.getCanonicalName).  If a search criteria is 
saved, the uniqueness in the names of the classes in the miscellaneous filter 
list will be important in recreating the new saved search criteria.

<strong>NOTE:</strong> In order for your search filter to work, you may have to

 <p>
 <UL>
   <P>
   <LI> Deprecated - gpt2csw_OGCCore.xslt no longer used. 
      <strike> Edit the gpt2csw_OGCCore.xslt 
     (your XSLT pointed to in gpt.xml/gptConfig/search/@gpt2cswXslt </strike>
   <P>
   <LI> New at version 10.  Override the method 
     {@link com.esri.gpt.control.georss.RestQueryServlet#parseRequest}.  
     Relates the rest query to the CSW query.
   
   <P>
   <LI> New at version 10.  Override the method 
     {@link com.esri.gpt.control.georss.RestQueryServlet#toSearchCriteria}
     The method relates the custom search filter object to the rest parameters,
     and adds the custom search filter to a search criteria object. 
   
    <P>
     <LI> New at version 10. Replace the query servlet in web.xml with
      your custom queryServlet.
     <pre>
       &lt;servlet&gt;
        &lt;servlet-name&gt;RestQueryServlet&lt;/servlet-name&gt;
        &lt;servlet-class&gt;com.esri.gpt.control.georss.CustomQueryServlet&lt;/servlet-class&gt;
        &lt;init-param&gt;
          &lt;param-name&gt;bundleBaseName&lt;/param-name&gt;
          &lt;param-value&gt;gpt.resources.gpt&lt;/param-value&gt;
        &lt;/init-param&gt;
        &lt;load-on-startup&gt;6&lt;/load-on-startup&gt;
       &lt;/servlet&gt;
     </pre>
   
   <P>
   <LI> New at version 10.  Edit criteria.jsp specifically javascript function 
     "scReadRestUrlParams" to include your HTML component value into the rest
     
   <P>
   <LI> If your query is to be passed through to other distributed search types
    you may have to look into the xslts in WEB-INF/class/gpt/profiles to identify which
   xslts need to be modified e.g. if your query is to be passed to another
   GPT then .*OGCCORE_ESR_GPT.* xslts will have to be edited.
   
   <P>
   <LI> And/or Add a custom search engine if you want external searches
   to consult with your custom filter (See topic below on how to do this)
 
 </UL>


<h2>Adding a Custom Search Engine</h2>

Search Engine objects are objects that take in a search criteria object and
return an object with search results.  Currently there are various CSW
search engines implemented.  
Newly introduced in version 10 is the SearchEngineRest which does not use a
CSW kind of workflow.

If you would like to implement your custom
search engine, you have to :-

<P>

<UL> 
<LI>Inherit from the abstract class com.esri.gpt.catalog.search.ASearchEngine 
or it's existing children.
<br/>
<b>Note:</b>Version 10: has some new methods.  Your class will need to be 
enhanced to take into account the new methods.

<P>
<LI> Add an entry into gpt.xml that specifies when your search engine should
be invoked so that the SearchEngineFactory automatically picks it up.
The entries fall under the xpath /gptConfig/search/repositories.  You will need
to add a ./repository element with an attribute of \@key and with an \@class 
attribute.  The \@key attribute is a regular expression, and it is a string 
which the searchFactory matches to an ID  so that the searchFactory can pick out 
the correct Search Engine Object. The \@abstractResourceKey is the abstract
shown in the Search In window

If you input an \repository\@labelResourcekey attribute, then in the Search External Sites
list gui, your search engine will be added as an entry.  An example of this is
our ArcGIS.com search


  &lt;repository key="ArcGIS.COM"  
          class="com.esri.gpt.catalog.search.SearchEngineRest"
          labelResourceKey="catalog.search.searchSite.agsonline"
          abstractResourceKey="catalog.search.searchSite.agsonline.abstract"&gt;
          &lt;pa&lt;ameter key="endPointSearchUrl" 
              value="http://www.arcgis.com/sharing/search?q={searchTerms}&amp;start={startIndex}&amp;num={count}&amp;focus={gpt:contenttype?}&amp;f=json"/&gt;  
          &lt;parameter key="defaultParamValues"
              value="q&#x2714;access:shared || access:public || access:private "/&gt;  
          &lt;parameter key="profileId" 
              value="urn:esri:gpt:HTTP:JSON:ESRI:AGSONLINE" /&gt;
          &lt;parameter key="gpt:contentType"
              value="liveMap&#x2714;maps&#x2715;application&#x2714;applications"/&gt;     
  &lt;/repository&gt;  


<P>

The parameters you put in at \repository\parameters[@key, @value] will be passed
on to the search engine of your choice as attributes.  These values will be 
initialized in the search engine as shown in the below sequence diagram.

<img src="doc-files/SequenceSearchFactoryInit.jpg"></img>
<P>

Currently the id of the harvest repository or the key given in the repository item 
is input into the Search Factory via the search interfaces (HTML, REST ...).  
The searchFactory will get the host URL from the harvest repository if one exists .  
This is the URL associated with the id.  The Search Factory
will then sequentially go down the Search Engine list in the XML file.  It
will attempt a match the URL with each \@key.
first match will be the search engine used for the search.  

<P>

If the URL does not yield any Search Engine Object, then the Search Engine 
Factory will take the input
repository id to match against the \\@key as 
it did for the id's URL, i.e. The searchFactory will sequentially go down
the repository list in the xml file.  The first match will be the match
that will be used for the search. 

</UL>
<br/>
<h4>Search Engine Initialization for Search</h4>

<br/>
<strong>NOTE:</strong> The Search Factory is fail safe, if a search engine is
not found, then the default search engine will be used for a search.  If
this is the case, the behaviour is logged on the server
but the user does not see any error.

<h2>Adding a Custom Map Viewer</h2>

Inherit from IMapViewer (Read IMapViewer javadoc for more info).  Add the class
and configurations to gpt.xml mapViewer/instance.  There is an example in
gpt.xml   
*/
package com.esri.gpt.catalog.search;

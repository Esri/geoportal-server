<%--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="org.w3c.dom.Element"%>
<%
/**
 * index.jsp
 * GIS Portal Toolkit webhelp system dispatch page.
 */
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Map,java.util.TreeMap" %>
<%@page import="com.esri.gpt.framework.util.Val" %>
<%@page import="java.io.InputStream" %>
<%@page import="java.net.URL" %>
<%@page import="java.net.HttpURLConnection" %>
<%@page import="java.io.IOException"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="com.esri.gpt.framework.xml.DomUtil"%>
<%@page import="javax.xml.xpath.XPathFactory"%>
<%@page import="javax.xml.xpath.XPath"%>
<%@page import="org.xml.sax.SAXException"%>
<%@page import="javax.xml.parsers.ParserConfigurationException"%>
<%@page import="com.esri.gpt.framework.xml.NodeListAdapter"%>
<%@page import="org.w3c.dom.Node"%>
<%@page import="javax.xml.xpath.XPathExpressionException"%>
<%@page import="javax.xml.xpath.XPathConstants"%>
<%@page import="org.w3c.dom.NodeList"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   
<%!
// DEFAULT_PAGE default page; used when accessing requested page fails
private static final String DEFAULT_HELPID= "geoportal-welcome";
// system defined.
private static final String LOCAL_HELP_SYSTEM = "/webhelp";
// CONTENT_DIR is a folder where help content is available
private static final String CONTENT_DIR = "/geoportal";
// requested language fails
private static final String DEFAULT_LANG= "en";
// CX help file
private static final String CX_HELP = "cxhelp.xml";
// REMOTE_HELP_SYSTEM is an absolute URL to the help system on the net.
private static final String REMOTE_HELP_SYSTEM = "https://github.com/Esri/geoportal-server/wiki";
%>

<%!
// translation map: CMD => help ID
private static TreeMap<String,String> translations = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
private static TreeMap<String,String> pages = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
private static Map<String,String> mapping = null;

// initialization table for translation mappings: CMD => help ID
private static String INIT[][] = {
  // home page
  {"catalog.main.home",                            "geoportal-welcome",            "/#welcome-to-the-esri-geoportal-server"},
  // identity/login
  {"catalog.identity.changePassword",              "login-manage-password",        "/How-to-Login-and-Manage-my-Password"},
  {"catalog.identity.encryptPassword",             "security",                     "/Security-Concepts"},
  {"catalog.identity.feedback",                    "provide-feedback",             "/Feedback"},
  {"catalog.identity.forgotPassword",              "login-manage-password",        "/How-to-Login-and-Manage-my-Password"},
  {"catalog.identity.login",                       "login-manage-password",        "/How-to-Login-and-Manage-my-Password"},
  {"catalog.identity.myProfile",                   "create-manage-profile",        "/How-to-Login-and-Manage-my-Password"},
  {"catalog.identity.userRegistration",            "create-account",               "/Create-a-user-account"},
  // search
  {"catalog.search.directedSearch",                "search-geoportal",             "/How-to-Search-for-Resources"},
  {"catalog.search.searchResult.viewFullMetadata", "",                             "/#welcome-to-the-esri-geoportal-server"},
  {"catalog.search.directedSearch.extSite",        "",                             "/#welcome-to-the-esri-geoportal-server"},
  {"catalog.search.home",                          "search-geoportal",             "/How-to-Search-for-Resources"},
  {"catalog.search.viewMetadataDetails",           "use-search-page-results",      "/How-to-Search-for-Resources"},
  // publication
  {"catalog.publication.addMetadata",              "publish-resources",            "/How-to-Publish-Resources"},
  {"catalog.publication.createMetadata",           "publish-resources",            "/How-to-Publish-Resources"},
  {"catalog.publication.editMetadata",             "manage-resources",             "/How-to-Manage-and-Edit-Resources"},
  {"catalog.publication.manageMetadata",           "manage-resources",             "/How-to-Manage-and-Edit-Resources"},
  {"catalog.publication.uploadMetadata",           "publish-resources",            "/How-to-Publish-Resources"},
  {"catalog.publication.validateMetadata",         "publish-resources",            "/How-to-Publish-Resources"},
  {"catalog.publication.manage.user.role",         "geoportal-welcome",            "/User-Management-Interface"},
  // harvest
  {"catalog.harvest.manage.create",                "publish-resources",            "/How-to-Publish-Resources"},
  {"catalog.harvest.manage.edit",                  "manage-resources",             "/How-to-Manage-and-Edit-Resources"},
  {"catalog.harvest.manage.history",               "manage-resources",             "/How-to-Manage-and-Edit-Resources"},
  {"catalog.harvest.manage.report",                "manage-resources",             "/How-to-Manage-and-Edit-Resources"},
  // pop-ups
  {"catalog.harvest.iso.caption",                  "",                             "/#welcome-to-the-esri-geoportal-server"},
  {"catalog.harvest.lookup.caption",               "",                             "/#welcome-to-the-esri-geoportal-server"},
  // download
  {"catalog.download.download",                    "use-data-download",            "/How-to-Use-the-Data-Download-Feature"},
  // live data preview
  {"catalog.livedata.preview",                     "geoportal-preview",            "/Preview-Function"},
  // browse
  {"catalog.browse",                               "geoportal-browse-resources",   "/How-to-Browse-for-Resources"},
  // resource
  {"catalog.search.resource.details",              "use-search-page-results",      "/How-to-Use-Search-Page-Results"},
  {"catalog.search.resource.review",               "geoportal-resource-review",    "/How-to-Leave-a-Resource-Review"},
  {"catalog.search.resource.relationships",        "geoportal-view-relationships", "/How-to-View-Resource-Relationships"},
  {"catalog.search.resource.preview",              "geoportal-preview",            "/Preview-Function"},
  // migration
  {"catalog.migration.migrateMetadata",            "",                             "/#welcome-to-the-esri-geoportal-server"},
  // misc
  {"catalog.content.about",                        "geoportal-welcome",            "/#welcome-to-the-esri-geoportal-server"},
  {"catalog.content.disclaimer",                   "geoportal-welcome",            "/#welcome-to-the-esri-geoportal-server"},
  {"catalog.content.privacy",                      "geoportal-welcome",            "/#welcome-to-the-esri-geoportal-server"},
 };

// initializes of translation mappings: CMD => help page
static {
  for (int i=0; i<INIT.length; i++) {
    translations.put(INIT[i][0], INIT[i][1]);
    pages.put(INIT[i][0], INIT[i][2]);
  }
}
%>

<%!

/**
 * Concatenates several strings using slash (/).
 * @param elements elements to concatenate
 * @return concatenated string
 */
private String concat(String ... elements) {
  String output = elements.length>0? Val.chkStr(elements[0]): "";
  for (int i=1; i<elements.length; i++) {
    String element = Val.chkStr(elements[i]).replaceAll("^/+", "");
    if (element.length()>0) {
      output = Val.chkStr(output).replaceAll("/+$", "") + "/" + element;
    }
  }
  return output;
}

/**
 * Gets help ID based upon command.
 * @param cmd command received within request
 * @return help ID
 */
private String getHelpID(String cmd) {
  return Val.chkStr(translations.get(Val.chkStr(cmd)), DEFAULT_HELPID);
}

/**
 * Gets root context of the current application. Root context is a <server>/<deployment context>.
 * @param request HTTP request
 * @return root context
 */
private String getRootContextPath(HttpServletRequest request) {
  return com.esri.gpt.framework.context.RequestContext.resolveBaseContextPath(request);
}

/**
 * Checks connection to the given URL.
 * @param URL URL to check
 * @return <code>true</code> if connection can be established
 */
private boolean checkUrl(String url) {
  url = Val.chkStr(url);
  if (url.length()==0) return false;
  InputStream input = null;
  try {
    URL queryUrl = new URL(url);
    HttpURLConnection httpCon = (HttpURLConnection) queryUrl.openConnection();
    httpCon.setDoInput(true);
    httpCon.setRequestMethod("GET");
    input = httpCon.getInputStream();
    return httpCon.getResponseCode() == HttpURLConnection.HTTP_OK;
  } catch (Exception ex) {
    return false;
  } finally {
    if (input!=null) {
      try {
        input.close();
      } catch (Exception ex) {}
    }
  }
}

/**
 * Loads CX mapping.
 * @param cxHelpMappingFilePath cx mapping file path
 * @return mapping
 */
private Map<String,String> loadCXMapping(String cxHelpMappingFilePath) throws IOException, ServletException  {
  InputStream inputStream = null;
  try {
    URL helpFileUrl = new URL(cxHelpMappingFilePath);
    inputStream = helpFileUrl.openStream();
    TreeMap<String,String> mapping = new TreeMap<String,String>();
    Document doc = DomUtil.makeDomFromSource(new InputSource(inputStream), false);
    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList ndHelpTopics = (NodeList)xPath.evaluate("HelpTopics/HelpTopic", doc, XPathConstants.NODESET);
    for (Node ndHelpTopic : new NodeListAdapter(ndHelpTopics)) {
      String id = ((Element)ndHelpTopic).getAttribute("id");
      String url = ((Element)ndHelpTopic).getAttribute("url");
      mapping.put(id, url);
    }
    return mapping;
  } catch (IOException ex) {
    throw ex;
  } catch (Exception ex) {
    throw new ServletException("Error parsing CX help file.", ex);
  } finally {
    if (inputStream!=null) {
      try {
        inputStream.close();
      } catch (Exception e) {};
    }
  }
}
%>

<%
// read parameters
String cmd  = Val.chkStr((String)request.getParameter("cmd"));
String lang = Val.chkStr((String)request.getParameter("lang"),DEFAULT_LANG);

// get help page from the table
String helpID = getHelpID(cmd);

// get root context path
String rootContextPath = getRootContextPath(request);

if (mapping==null) {
  // get CX help mapping file path
  String cxHelpMappingFilePath = concat(rootContextPath, LOCAL_HELP_SYSTEM, DEFAULT_LANG, CONTENT_DIR, CX_HELP);

  // load mapping
  mapping = loadCXMapping(cxHelpMappingFilePath);
}

// convert helpID into url
String helpPartialUrl = mapping.get(helpID);

// convert command into page name
String helpPage = pages.get(cmd);

// help full url
String helpFullUrl = "";


// Only web help is available
helpFullUrl = REMOTE_HELP_SYSTEM + helpPage;

// redirect to the page
response.sendRedirect(helpFullUrl);
%>

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
<% // downloadMetadata.jsp - Download metadata page (JSP) %>
<%@page import="com.esri.gpt.catalog.schema.MetadataDocument"%>
<%@page import="com.esri.gpt.framework.context.RequestContext"%>
<%@page import="com.esri.gpt.framework.security.principal.Publisher"%>
<%@page import="com.esri.gpt.framework.util.LogUtil"%>
<%@page import="com.esri.gpt.framework.util.Val"%>
<%@page import="java.util.logging.Level"%>
<%
  String sUuid = Val.chkStr(request.getParameter("uuid"));
  boolean bAsAttachment = !Val.chkStr(request.getParameter("option")).equals("view");;
  String sXml = "";
  String sErr = "";
  
  // read the XML string associated with the UUID
  if (sUuid.length() > 0) {
    try {
      sXml = readXml(request,sUuid,bAsAttachment);
      if (sXml.length() == 0) {
        //sErr = "Download Failed.";
      }
    } catch (Exception e) {
      sErr = "Download Failed.";
      LogUtil.getLogger().log(Level.SEVERE,"Metadata download failed",e);
    }
  }
  
  // return the XML
  if ((sUuid.length() > 0) && (sXml.length() > 0) && (sErr.length() == 0)) {
    
    // return the XML string as an attachment
    if (bAsAttachment) {
      response.reset();
      response.setContentType("APPLICATION/OCTET-STREAM; charset=UTF-8");
      response.setHeader("Content-Disposition","attachment; filename=\""+sUuid+".xml\"");
      out.clear();
      out.print(sXml);
      out.flush();
      //out.close();
 
    // return the XML as a string
    } else {
      response.reset();
      response.setContentType("text/xml; charset=UTF-8");
      out.clear();
      out.print(sXml);
      out.flush();
      //out.close();          
    }
  
  // use javascript to alert the user if an error has occurred
  } else if (sErr.length() > 0) {
    out.println("<script type=\"text/javascript\" language=\"Javascript\">");
    out.println("alert(\""+sErr+"\");");
    out.println("</script>");
  }
%>

<%!

// read the XML string associated with the UUID
private String readXml(HttpServletRequest request, String uuid, boolean bAsAttachment) throws Exception {
  String sXml = "";
  RequestContext context = null;
  try {
    context = RequestContext.extract(request);
    Publisher publisher = new Publisher(context);
    MetadataDocument mdDoc = new MetadataDocument();
    sXml = mdDoc.prepareForDownload(context,publisher,uuid);
    
    com.esri.gpt.framework.collection.StringAttributeMap params = context.getCatalogConfiguration().getParameters();
    String s = Val.chkStr(params.getValue("Administration.viewMetadata.stripStyleSheets"));
    boolean bStripStyleSheets = s.equalsIgnoreCase("true");
    if (!bAsAttachment && bStripStyleSheets) {
      //sXml = sXml.replaceAll("<\\?xml\\-stylesheet.*\\?>|<\\!DOCTYPE.*>","");
      sXml = sXml.replaceAll("<\\?xml\\-stylesheet.+?>|<\\!DOCTYPE.+?>","");
    }
  } finally {
    if (context != null) {
      context.onExecutionPhaseCompleted();
    }
  }
  return sXml;
}

%>
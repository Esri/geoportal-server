/*
 * Copyright 2012 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.server.erosfeed;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.discovery.rest.RestQueryParser;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.catalog.search.*;
import com.esri.gpt.control.georss.AtomFeedWriter;
import com.esri.gpt.control.georss.FeedWriter;
import com.esri.gpt.control.georss.RecordSnippetWriter;
import com.esri.gpt.control.georss.RestQueryServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.text.ParseException;
import java.util.Date;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Eros query servlet.
 */
public class ErosQueryServlet extends RestQueryServlet {
  private String defaultQuery = "";
  private Map<String,String[]> defaultParameters = new HashMap<String, String[]>();
  private IsoDateFormat dateFormat = new IsoDateFormat(IsoDateFormat.Format.basic);

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    defaultQuery = Val.chkStr(config.getInitParameter("defaultQuery"),"");
    defaultParameters = parseParameters(defaultQuery);
  }

  @Override
  protected void execute(HttpServletRequest request, HttpServletResponse response, RequestContext context) throws Exception {
    RoleSet roleSet = new RoleSet();
    roleSet.add("gptAdministrator");
    context.getUser().getAuthenticationStatus().authorizeAction(roleSet);
    super.execute(request, response, context);
  }
  
  @Override
  protected RestQuery parseRequest(HttpServletRequest request, RequestContext context) {
    RestQuery query = new RestQuery();
    RestQueryParser parser = new ErosQueryParser(request,context,query,defaultParameters);
   
    parser.parseResponseFormat("f");
    String requestURI = Val.chkStr(request.getRequestURI());
    if (requestURI.toLowerCase().endsWith("/sitemap")) {
      String tmp = Val.chkStr(query.getResponseFormat());
      if (tmp.length() == 0) {
        query.setResponseFormat("sitemap");
      }
    }
    
    parser.parseRepositoryId("rid");
    parser.parseResponseFormat("f");
    parser.parseResponseGeometry("geometryType");
    parser.parseResponseStyle("style");
    parser.parseResponseTarget("target");
    parser.parseStartRecord("start",1);
    parser.parseMaxRecords("max",10);
    parser.parsePropertyIsEqualTo("uuid","uuid");
    parser.parsePropertyIsLike("searchText","anytext");
    parser.parsePropertyList("contentType","dc:type",",",true);
    parser.parsePropertyList("dataCategory","dc:subject",",",true);
    parser.parsePropertyRange("after","before","dct:modified");
    parser.parseSpatialClause("bbox","spatialRel","geometry");
    parser.parseSortables("orderBy");
    
    //parser.parsePropertyRange("validAfter","validBefore","dct:valid"); // date valid
    parser.parsePropertyIsEqualTo("publisher","dc:publisher"); // publisher
    parser.parsePropertyIsEqualTo("source","dc:source"); // harvesting id (uuid)
    parser.parsePropertyIsEqualTo("isPartOf","dct:isPartOf"); // collection subset
    //parser.parsePropertyList("hasFormat","dct:hasFormat",",",true);
    
    return query;
  }

  private Date extractDate(HttpServletRequest request, String paramName) {
    Date date = null;
    
    String paramValue = Val.chkStr(request.getParameter(paramName));
    if (!paramValue.isEmpty()) {
      try {
        date = dateFormat.parseObject(paramValue);
      } catch (ParseException ex) {}
    }
    
    return date;
  }
  
  @Override
  protected SearchResult executeQuery1(HttpServletRequest request, RequestContext context, MessageBroker messageBroker, RestQuery query) throws SearchException {
    Date after  = extractDate(request, "after");
    Date before = extractDate(request, "before");
    
    SearchResult result = executeRepoQuery(request, context, messageBroker, query, after, before);
    int maxRecords = query.getFilter().getMaxRecords();
    
    if (result.getRecordSize()>0) {
      query.getFilter().setStartRecord(1);
    } else {
      query.getFilter().setStartRecord(Math.max(1,query.getFilter().getStartRecord()-result.getRecords().getOpenSearchProperties().getNumberOfHits()));
    }
    query.getFilter().setMaxRecords(query.getFilter().getMaxRecords()-result.getRecordSize());
    SearchResult recResult = super.executeQuery1(request, context, messageBroker, query);
    
    if (result.getRecordSize()<maxRecords) {
      result.getRecords().addAll(recResult.getRecords());
    }
    
    result.setSearchTimeInSeconds(result.getSearchTimeInSeconds()+recResult.getSearchTimeInSeconds());
    result.getRecords().getOpenSearchProperties().setNumberOfHits(result.getRecords().getOpenSearchProperties().getNumberOfHits()+recResult.getRecords().getOpenSearchProperties().getNumberOfHits());
    //result.getRecords().getOpenSearchProperties().setRecordsPerPage(result.getRecords().size());
    
    return result;
  }

  protected SearchResult executeRepoQuery(HttpServletRequest request, RequestContext context, MessageBroker messageBroker, RestQuery query, Date after, Date before) throws SearchException {
    ResourceIdentifier resourceIdentifier = ResourceIdentifier.newIdentifier(context);
    SearchResult result = new SearchResult();
    Connection conn = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT DOCUUID, TITLE, HOST_URL, PROTOCOL_TYPE, PROTOCOL FROM ").append(context.getCatalogConfiguration().getResourceTableName());
    sb.append(" WHERE PROTOCOL_TYPE IS NOT NULL");
    if (BaseDao.getIsDbCaseSensitive(context)) {
      sb.append(" AND (UPPER(APPROVALSTATUS)='").append(ApprovalStatus.approved.name().toUpperCase()).append("' OR UPPER(APPROVALSTATUS)='").append(ApprovalStatus.reviewed.name().toUpperCase()).append("')");
    } else {
      sb.append(" AND (APPROVALSTATUS='").append(ApprovalStatus.approved.name()).append("' OR APPROVALSTATUS='").append(ApprovalStatus.reviewed.name()).append("')");
    }
    
    if (after!=null) {
      sb.append(" AND UPDATEDATE>=?");
    }
    if (before!=null) {
      sb.append(" AND UPDATEDATE<=?");
    }
    
    try {
      conn = context.getConnectionBroker().returnConnection("").getJdbcConnection();
      st = conn.prepareStatement(sb.toString());
      int n = 0;
      if (after!=null) {
        st.setTimestamp(++n, new Timestamp(after.getTime()));
      }
      if (before!=null) {
        st.setTimestamp(++n, new Timestamp(before.getTime()));
      }
      
      rs = st.executeQuery();
      
      ArrayList<SearchResultRecord> hrAcceptedRecords = new ArrayList<SearchResultRecord>();
      while (rs.next()) {
        String uuid = Val.chkStr(rs.getString("DOCUUID"));
        String name = Val.chkStr(rs.getString("TITLE"));
        String url  = Val.chkStr(rs.getString("HOST_URL"));
        String kind = Val.chkStr(rs.getString("PROTOCOL_TYPE")).toLowerCase();
        
        kind = decodeKind(resourceIdentifier, kind, url);
        if (Val.chkStr(kind).isEmpty()) {
          continue;
        }
        
        SearchResultRecord record = new SearchResultRecord();
        record.setUuid(uuid);
        record.setTitle(name);
        record.setServiceType(kind);
        record.setResourceUrl(url);
        
        hrAcceptedRecords.add(record);
      }
      
      for (int i=query.getFilter().getStartRecord()-1; i<hrAcceptedRecords.size() && result.getRecordSize()<query.getFilter().getMaxRecords(); i++) {
        SearchResultRecord record = hrAcceptedRecords.get(i);
        result.getRecords().add(record);
      }

      String basePath = RequestContext.resolveBaseContextPath(request);
      String osURL = basePath+"/openSearchDescription";
      OpenSearchProperties osProps = new OpenSearchProperties();
      osProps.setShortName(messageBroker.retrieveMessage("catalog.openSearch.shortName"));
      osProps.setDescriptionURL(osURL);
      osProps.setNumberOfHits(hrAcceptedRecords.size());
      osProps.setStartRecord(query.getFilter().getStartRecord());
      osProps.setRecordsPerPage(query.getFilter().getMaxRecords());
      result.getRecords().setOpenSearchProperties(osProps);  
      
      return result;
    } catch (SQLException ex) {
      throw new SearchException("Error searching records", ex);
    } finally {
      if (rs!=null) {
        try {
          rs.close();
        } catch (SQLException ex) {
        }
      }
      if (st!=null) {
        try {
          st.close();
        } catch (SQLException ex) {
        }
      }
      context.getConnectionBroker().closeAll();
    }
  }
  
  protected String decodeKind(ResourceIdentifier resourceIdentifier, String kind, String url) {
    if ("arcgis".equals(kind)) {
      kind = "ags";
    } else if ("res".equals(kind)) {
      kind = resourceIdentifier.guessServiceTypeFromUrl(url);
      if (kind.isEmpty()) {
        return null;
      }
    } else if ("waf".equals(kind)) {

    } else if ("csw".equals(kind)) {

    } else {
      return null;
    }
    return kind;
  }
  
  @Override
  protected FeedWriter makeFeedWriter(HttpServletRequest request, RequestContext context, PrintWriter printWriter, MessageBroker messageBroker, RestQuery query) {
    ResponseFormat format = getResponseFormat(request,query);
    if (format.equals(ResponseFormat.atom)){
      ResourceIdentifier resourceIdentifier = ResourceIdentifier.newIdentifier(context);
      IdentityAdapter idAdapter =  context.newIdentityAdapter();
      ErosEmailFinder emailFinder = new ErosEmailFinder(context, idAdapter);
      
      String sTarget = query.getResponseTarget();
      RecordSnippetWriter.Target target = RecordSnippetWriter.Target.checkValueOf(sTarget);
      AtomFeedWriter atomWriter = new ErosAtomFeedWriter(emailFinder, resourceIdentifier, printWriter);
      atomWriter.setEntryBaseUrl(query.getRssProviderUrl());
      atomWriter.set_messageBroker(messageBroker);
      atomWriter.setTarget(target);
      return atomWriter;
    }
    return super.makeFeedWriter(request, context, printWriter, messageBroker, query);
  }
  
  
  private Map<String,String[]> parseParameters(String parameters) {
    HashMap<String,ArrayList<String>> parametersMap = new HashMap<String, ArrayList<String>>();
    
    String [] elements = parameters.split("&");
    for (String element: elements) {
      String [] kvp = element.split("=");
      if (kvp!=null && kvp.length>=1) {
        ArrayList<String> values = null;
        if (parametersMap.containsKey(kvp[0])) {
          values = parametersMap.get(kvp[0]);
        } else {
          values = new ArrayList<String>();
          parametersMap.put(kvp[0], values);
        }
        values.add(kvp.length>=2? decode(kvp[1]): "");
      }
    }
    
    return castParametersMap(parametersMap);
  }
  
  private Map<String,String[]> castParametersMap(Map<String,ArrayList<String>> map) {
    TreeMap<String,String[]> parametersMap = new TreeMap<String, String[]>();
    for (Map.Entry<String,ArrayList<String>> e: map.entrySet()) {
      parametersMap.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
    }
    return parametersMap;
  }
  
  private String decode(String s) {
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return s;
    }
  }
}

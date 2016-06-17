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
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.discovery.DiscoveryClause;
import com.esri.gpt.catalog.discovery.SpatialClause;
import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.discovery.rest.RestQueryParser;
import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.CswResourceLinkBuilder;
import com.esri.gpt.catalog.search.GetRecordsGenerator;
import com.esri.gpt.catalog.search.ISearchFilterSpatialObj;
import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.catalog.search.ResourceLinkBuilder;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchEngineCSW;
import com.esri.gpt.catalog.search.SearchEngineFactory;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchFilterContentTypes;
import com.esri.gpt.catalog.search.SearchFilterHarvestSites;
import com.esri.gpt.catalog.search.SearchFilterKeyword;
import com.esri.gpt.catalog.search.SearchFilterPagination;
import com.esri.gpt.catalog.search.SearchFilterSort;
import com.esri.gpt.catalog.search.SearchFilterSpatial;
import com.esri.gpt.catalog.search.SearchFilterTemporal;
import com.esri.gpt.catalog.search.SearchFilterThemeTypes;
import com.esri.gpt.catalog.search.SearchFiltersList;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.catalog.search.SearchResultRecords;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.components.CoreQueryables;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet end-point for rest based catalog query requests.
 */
public class RestQueryServlet extends BaseServlet {

  public final static String EXTRA_REST_ARGS_MAP = "EXTRA_REST_ARGS_MAP";
  public final static String PARAM_KEY_SHOW_THUMBNAIL = "showThumbnail";
  public final static String PARAM_KEY_SHOW_RELATIVE_URLS = "showRelativeUrl";
  public final static String PARAM_KEY_IS_JSFREQUEST = "isJsfRequest";

  /**
   * constructors ============================================================
   */
  /**
   * Default constructor.
   */
  public RestQueryServlet() {
  }

  /**
   * methods =================================================================
   */
  /**
   * Processes the HTTP request.
   *
   * @param request the HTTP request.
   * @param response HTTP response.
   * @param context request context
   * @throws Exception if an exception occurs
   */
  @Override
  protected void execute(HttpServletRequest request,
          HttpServletResponse response,
          RequestContext context)
          throws Exception {
    getLogger().finer("Handling rest query string=" + request.getQueryString());
    MessageBroker msgBroker = new FacesContextBroker(request, response).extractMessageBroker();

    // extra params
    Map<String, String> extraMap = new HashMap<String, String>();
    extraMap.put(PARAM_KEY_SHOW_THUMBNAIL,
            request.getParameter(PARAM_KEY_SHOW_THUMBNAIL));
    extraMap.put(PARAM_KEY_SHOW_RELATIVE_URLS,
            request.getParameter(PARAM_KEY_SHOW_RELATIVE_URLS));
    extraMap.put(PARAM_KEY_IS_JSFREQUEST,
            request.getParameter(PARAM_KEY_IS_JSFREQUEST));
    context.getObjectMap().put(EXTRA_REST_ARGS_MAP, extraMap);
    if (request.getScheme().toLowerCase().equals("https")
            && extraMap.get(PARAM_KEY_SHOW_THUMBNAIL) == null) {
      String agent = request.getHeader("user-agent");
      if (agent != null && agent.toLowerCase().indexOf("msie") > -1) {
        extraMap.put(PARAM_KEY_SHOW_THUMBNAIL, "false");
      }
    }

    // parse the query
    RestQuery query = null;
    try {
      query = parseRequest(request, context);
    } catch (Throwable t) {
      getLogger().log(Level.SEVERE, "Error parsing request.", t);
    }
    if (query == null) {
      query = new RestQuery();
    }
    
    // validate spatial clause
    for (DiscoveryClause clause: query.getFilter().getRootClause().getClauses()) {
      if (clause instanceof SpatialClause) {
        Envelope env = ((SpatialClause)clause).getBoundingEnvelope();
        if (!env.isEmpty() && !env.isValidWGS84()) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
        }
      }
    }


    // establish the response content type, print writer and feed writer
    RestQueryServlet.ResponseFormat format = getResponseFormat(request, query);
    String sFormat = getRequestParameter(request, "f");
    FeedWriter2 feedWriter2 = WriterFactory.createWriter(
            sFormat, msgBroker, query, request, response, context);
    FeedWriter feedWriter = null;
    PrintWriter printWriter = null;
    if (feedWriter2 != null) {
      feedWriter = (FeedWriter) feedWriter2;
    } else {
      this.setResponseContentType(request, response, query);
      printWriter = response.getWriter();
      feedWriter = makeFeedWriter(request, context, printWriter, msgBroker, query);
    }
    // execute the query, write the response
    try {
      if (format == RestQueryServlet.ResponseFormat.xjson) {

        // init query
        query.setReturnables(new CoreQueryables(context).getFull());
        toSearchCriteria(request, context, query);
        
        IFeedRecords result = JsonSearchEngine.createInstance().search(request, response, context, query);
        if (result.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        String callback = request.getParameter("callback");
        if (callback != null) {
          printWriter.print(callback + "(");
        }
        
        feedWriter.write(result);

        if (callback != null) {
          printWriter.print(")");
        }

      }else if (format == RestQueryServlet.ResponseFormat.dcat) {
        
        // The following part of the code has been disabled since DCAT content
        // is being cached.
        query.setReturnables(new CoreQueryables(context).getFull());
        toSearchCriteria(request, context, query);
        
        IFeedRecords result = DcatJsonSearchEngine.createInstance().search(request, response, context, query);
        if (result.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        
        String callback = request.getParameter("callback");
        if (callback != null) {
          printWriter.print(callback + "(");
        }
        
        feedWriter.write(result);

        if (callback != null) {
          printWriter.print(")");
        }

      } else {
        SearchResult result = executeQuery1(request, context, msgBroker, query);
        if (!result.getHasRecords() && !Val.chkStr(request.getParameter("uuid")).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        if (feedWriter instanceof FeedWriter2) {
          ((FeedWriter2) feedWriter).write(result);
        } else if (feedWriter instanceof HtmlAdvancedWriter) {
          ((HtmlAdvancedWriter) feedWriter).write(result);
        } else {
          feedWriter.write(new SearchResultRecordsAdapter(result.getRecords()));
        }
      }
    } catch (Exception e) {
      getLogger().log(Level.SEVERE, "Error executing query.", e);
      if (feedWriter instanceof FeedWriter2) {
        ((FeedWriter2) feedWriter).writeError(e);
      } else {

        // feedWriter.write(new SearchResultRecords());
        String msg = Val.chkStr(e.getMessage());
        if (msg.length() == 0) {
          msg = e.toString();
        }
        printWriter = null;
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
      }
    } finally {
      try {
        if (printWriter != null && (feedWriter instanceof FeedWriter2) == false) {
          printWriter.flush();
        }
      } catch (Exception ef) {
        getLogger().log(Level.INFO, "Error while flushing printwriter", ef);
      }
    }
  }

  /**
   * Execute Query that returns a SearchResult object (SearchResult as opposed
   * to the SearchResultRecord which executeQuery does).
   *
   * @param request the request
   * @param context the context
   * @param messageBroker the message broker
   * @param query the query
   * @return the search result
   * @throws SearchException the search exception
   */
  protected SearchResult executeQuery1(HttpServletRequest request,
          RequestContext context,
          MessageBroker messageBroker,
          RestQuery query) throws SearchException {

    // make the search engine
    ASearchEngine engine = null;
    SearchCriteria criteria = this.toSearchCriteria(request, context, query);
    SearchResult result = new SearchResult();
    String rid = Val.chkStr(query.getRepositoryId());
    CswContext cswContext = CswContext.create(query.getCswUrl(), query.getCswProfile());
    RestQueryServlet.ResponseFormat format = getResponseFormat(request, query);

    boolean isJavascriptEnabled =
            Val.chkBool(request.getParameter("isJavascriptEnabled"), false);
    if (format.toString().toLowerCase().startsWith("searchpage")
            || isJavascriptEnabled == true) {

      if (format.toString().toLowerCase().startsWith("searchpage")) {
        @SuppressWarnings("unchecked")
        Map<String, String> extraArgs = (Map<String, String>) context
                .getObjectMap().get(EXTRA_REST_ARGS_MAP);
        if (extraArgs != null) {
          if (extraArgs.get(PARAM_KEY_SHOW_RELATIVE_URLS) == null) {
            extraArgs.put(PARAM_KEY_SHOW_RELATIVE_URLS, "true");
          }
          extraArgs.put(PARAM_KEY_IS_JSFREQUEST, "true");
        }
      }
      context.setViewerExecutesJavascript(true);
    } else {
      context.setViewerExecutesJavascript(false);
    }
    
    ResourceLinkBuilder rBuild = cswContext==null?
      ResourceLinkBuilder.newBuilder(context,request, messageBroker):
      CswResourceLinkBuilder.newBuilder(context,cswContext,request, messageBroker);



    // handle a request against the local repository
    if ((rid.length() == 0 || rid.equalsIgnoreCase("local")) && cswContext==null ) {

      // generate the CSW request string
      String cswRequest = "";
      try {
        GetRecordsGenerator grg = new GetRecordsGenerator();
        cswRequest = grg.generateCswRequest(query);
      } catch (Exception e) {
        throw new SearchException(e);
      }

      // execute the query
      engine = SearchEngineFactory.createSearchEngine(criteria, result, context, messageBroker);
      SearchEngineCSW csw = (SearchEngineCSW) engine;
      csw.setResourceLinkBuilder(rBuild);
      csw.doSearch(cswRequest);

      // handle a request against a remote repository
    } else {

      // create the criteria, execute the query
      int iSearchTime = Val.chkInt(request.getParameter("maxSearchTimeMilliSec"), -1);
      if (cswContext!=null) {
        engine = SearchEngineFactory.createSearchEngine(criteria, result, context, cswContext, messageBroker);
      } else {
        engine = SearchEngineFactory.createSearchEngine(criteria, result, context, rid, messageBroker);
      }
      engine.setResourceLinkBuilder(rBuild);
      if (iSearchTime > 0) {
        engine.setConnectionTimeoutMs(iSearchTime);
        engine.setResponseTimeout(iSearchTime);
      }
      engine.doSearch();

    }

    // set the OpenSearch properties
    String basePath = RequestContext.resolveBaseContextPath(request);
    String osURL = basePath + "/openSearchDescription";
    //String osURL = request.getRequestURL().toString();
    //osURL = osURL.replaceAll("/rest/find/document","/openSearchDescription");
    OpenSearchProperties osProps = new OpenSearchProperties();
    osProps.setShortName(messageBroker.retrieveMessage("catalog.openSearch.shortName"));
    osProps.setDescriptionURL(osURL);
    osProps.setRestURL(basePath+"/rest/find/document");
    osProps.setSearchText(request.getParameter("searchText"));
    osProps.setBbox(request.getParameter("bbox"));
    osProps.setClientId(request.getParameter("clientId"));
    osProps.setNumberOfHits(result.getMaxQueryHits());
    osProps.setStartRecord(query.getFilter().getStartRecord());
    osProps.setRecordsPerPage(query.getFilter().getMaxRecords());
    result.getRecords().setOpenSearchProperties(osProps);
    return result;

  }

  /**
   * Executes the query.
   *
   * @param request the HTTP request
   * @param context the request context
   * @param messageBroker the resource message broker
   * @param query the query to execute
   * @return the resultant records
   * @throws SearchException if an exception occurs
   */
  protected SearchResultRecords executeQuery(HttpServletRequest request,
          RequestContext context,
          MessageBroker messageBroker,
          RestQuery query)
          throws SearchException {

    return this.executeQuery1(request, context, messageBroker, query)
            .getRecords();

  }

  /**
   * Gets the HTTP request parameter value associated with a key.
   *
   * @param request the HTTP request
   * @param parameterKey the parameter key
   * @return parameter value (empty string if not found, trimmed never null)
   */
  protected String getRequestParameter(HttpServletRequest request, String parameterKey) {
    Map<String, String[]> parMap = request.getParameterMap();
    for (Map.Entry<String, String[]> e : parMap.entrySet()) {
      if (e.getKey().equalsIgnoreCase(parameterKey)) {
        if (e.getValue().length > 0) {
          return Val.chkStr(e.getValue()[0]);
        } else {
          return "";
        }
      }
    }
    return "";
  }

  /**
   * Sets the HTTP content type for the response.
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param query the rest query
   */
  protected void setResponseContentType(HttpServletRequest request, HttpServletResponse response, RestQuery query) {
    String fmt = Val.chkStr(query.getResponseFormat());
    if (fmt.equalsIgnoreCase("sitemap") || fmt.toLowerCase().startsWith("sitemap.")) {
      response.setContentType("text/xml;charset=UTF-8");
      return;
    }

    switch (getResponseFormat(request, query)) {
      case georss:
        response.setContentType("application/rss+xml;charset=UTF-8");
        break;
      case html:
      case searchpageresults:
      case searchpage:
      case htmlfragment:
        response.setContentType("text/html;charset=UTF-8");
        break;
      case atom:
        response.setContentType("application/atom+xml;charset=UTF-8");
        break;
      case dcat:
        response.setContentType("application/json;charset=UTF-8");
        //response.setHeader("Content-disposition", "attachment; filename=\"dcat.json\"");
        break;
      case json:
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-disposition", "attachment; filename=\"document.json\"");
        break;
      case pjson:
      case xjson:
        response.setContentType("text/plain;charset=UTF-8");
        break;
      default:
      case kml:
        response.setContentType("application/vnd.google-earth.kml+xml;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"document.kml\"");
        break;
    }
  }

  /**
   * Determines the response format.
   *
   * @param request the HTTP request
   * @param query the rest query
   * @return the response format
   */
  protected RestQueryServlet.ResponseFormat getResponseFormat(HttpServletRequest request, RestQuery query) {
    String sFormat = "";
    if (query != null) {
      sFormat = Val.chkStr(query.getResponseFormat());
    }
    if (sFormat.length() == 0) {
      sFormat = getRequestParameter(request, "f");
    }
    return RestQueryServlet.ResponseFormat.checkValueOf(sFormat);
  }

  /**
   * Initializes the servlet. <br/>Init parameter "bundleBaseName" is read for
   * message configuration.
   *
   * @param config the servlet configuration
   * @throws ServletException if an exception occurs
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  /**
   * Makes a writer capable of generating an appropriate response based upon the
   * requested response format.
   *
   * @param request the HTTP request
   * @param context the request context
   * @param printWriter the underlying print writer
   * @param messageBroker the message broker
   * @param query the query
   * @return the appropriate writer
   */
  protected FeedWriter makeFeedWriter(HttpServletRequest request,
          RequestContext context,
          PrintWriter printWriter,
          MessageBroker messageBroker,
          RestQuery query) {

    String fmt = Val.chkStr(query.getResponseFormat());
    if (fmt.equalsIgnoreCase("sitemap")
            || fmt.toLowerCase().startsWith("sitemap.")) {
      context.getObjectMap().put(
              "com.esri.gpt.catalog.search.isSitemapRequest", "true");
      return new SitemapWriter(request, context, printWriter, messageBroker, query);
    }

    RestQueryServlet.ResponseFormat format = getResponseFormat(request, query);
    String sTarget = query.getResponseTarget();
    RecordSnippetWriter.Target target = RecordSnippetWriter.Target.checkValueOf(sTarget);

    // HTML writer
    if (format.equals(RestQueryServlet.ResponseFormat.html)) {
      HtmlFeedWriter htmlFeedWriter = new HtmlFeedWriter(
              messageBroker, printWriter);
      htmlFeedWriter.setTarget(target);
      String[] responseStyle = query.getResponseStyle().split(",");
      htmlFeedWriter.setStyleUrl(responseStyle);
      return htmlFeedWriter;

      // HTML fragment writer
    } else if (format.equals(RestQueryServlet.ResponseFormat.htmlfragment)) {
      HtmlFragmentFeedWriter htmlFeedWriter = new HtmlFragmentFeedWriter(
              messageBroker, printWriter);
      htmlFeedWriter.setTarget(target);
      return htmlFeedWriter;

      // KML writer
    } else if (format.equals(RestQueryServlet.ResponseFormat.kml)) {
      KmlFeedWriter kmlFeedWriter = new KmlFeedWriter(
              messageBroker, printWriter);
      kmlFeedWriter.setTarget(target);
      String responseGeometry = query.getResponseGeometry();
      kmlFeedWriter.setGeometry(KmlFeedWriter.Geometry.checkValueOf(responseGeometry));
      return kmlFeedWriter;

      // ATOM writer  
    } else if (format.equals(RestQueryServlet.ResponseFormat.atom)) {
      AtomFeedWriter atomWriter = new AtomFeedWriter(request, printWriter);
      atomWriter.setEntryBaseUrl(query.getRssProviderUrl());
      atomWriter.set_messageBroker(messageBroker);
      atomWriter.setTarget(target);
      return atomWriter;

      // JSON and PJSON writer
    } else if (format.equals(RestQueryServlet.ResponseFormat.json) || format.equals(RestQueryServlet.ResponseFormat.pjson)) {
      JsonFeedWriter jsonWriter = new JsonFeedWriter(printWriter, query, format == RestQueryServlet.ResponseFormat.pjson);
      jsonWriter.setCallback(request.getParameter("callback"));
      jsonWriter.setMessageBroker(messageBroker);
      return jsonWriter;
      
      // Normalized DCAT JSON writer
    } else if (format.equals(RestQueryServlet.ResponseFormat.dcat)) {
      DcatJsonFeedWriterFactory factory = DcatJsonFeedWriterFactory.getInstance();
      DcatJsonFeedWriter jsonWriter = factory.create(request, context, printWriter, query);
      jsonWriter.setMessageBroker(messageBroker);
      return jsonWriter;
      
      // Normalized JSON writer
    } else if (format.equals(RestQueryServlet.ResponseFormat.xjson)) {
      ExtJsonFeedWriter jsonWriter = ExtJsonFeedWriter.createInstance(request, context, printWriter, query, true);
      jsonWriter.setMessageBroker(messageBroker);
      return jsonWriter;
      // Advanced html writer
    } else if (format.equals(RestQueryServlet.ResponseFormat.searchpageresults)
            || format.equals(RestQueryServlet.ResponseFormat.searchpage)) {
      HtmlAdvancedWriter htmlAdvWriter = new HtmlAdvancedWriter();
      htmlAdvWriter.setRequestContext(context);
      htmlAdvWriter.setCriteria(this.toSearchCriteria(request, context, query));
      htmlAdvWriter.setResultsOnly(
              format.equals(RestQueryServlet.ResponseFormat.searchpageresults));
      return htmlAdvWriter;
      // default: GEORSS writer
    } else {
      GeorssFeedWriter rssWriter = new GeorssFeedWriter(
              messageBroker, printWriter, query.getRssProviderUrl(), query.getRssSourceUrl());
      rssWriter.setTarget(target);
      String responseGeometry = query.getResponseGeometry();
      rssWriter.setGeometry(GeorssFeedWriter.Geometry.checkValueOf(responseGeometry,GeorssFeedWriter.Geometry.esriGeometryPolygon));
      return rssWriter;
    }

  }

  /**
   * Parses the request and generates a populated query suitable for execution.
   * <p/>
   * This method essentially uses URL key-value pairs to generate filter and
   * response components for a rest based query.
   * <p/>
   * This method is the primary extensibility point for the rest API. Code
   * example for this method given below for reference during extension.
   * <code>
   * <pre>
   *RestQuery query = new RestQuery();
   * RestQueryParser parser = new RestQueryParser(request,context,query);
   *
   * parser.parseRepositoryId("rid");
   * parser.parseResponseFormat("f");
   * parser.parseResponseGeometry("geometryType");
   * parser.parseResponseStyle("style");
   * parser.parseResponseTarget("target");
   * parser.parseStartRecord("start",1);
   * parser.parseMaxRecords("max",10);
   * parser.parsePropertyIsEqualTo("uuid","uuid");
   * parser.parsePropertyIsLike("searchText","anytext");
   * parser.parsePropertyList("contentType","dc:type",",",true);
   * parser.parsePropertyList("dataCategory","dc:subject",",",true);
   * parser.parsePropertyRange("after","before","dct:modified");
   * parser.parseSpatialClause("bbox","spatialRel","geometry");
   * parser.parseSortables("orderBy");
   * return query;
   * </pre>
   * </code>
   *
   * @param request the HTTP request
   * @param context the request context
   * @return the populated rest query
   */
  protected RestQuery parseRequest(HttpServletRequest request, RequestContext context) {
    RestQuery query = new RestQuery();
    RestQueryParser parser = new RestQueryParser(request, context, query);

    parser.parseResponseFormat("f");
    String requestURI = Val.chkStr(request.getRequestURI());
    if (requestURI.toLowerCase().endsWith("/sitemap")) {
      String tmp = Val.chkStr(query.getResponseFormat());
      if (tmp.length() == 0) {
        query.setResponseFormat("sitemap");
      }
    }

    parser.parseRepositoryId("rid");
    parser.parseCswUrl("cswUrl");
    parser.parseCswProfile("cswProfile");
    parser.parseResponseFormat("f");
    parser.parseResponseGeometry("geometryType");
    parser.parseResponseStyle("style");
    parser.parseResponseTarget("target");
    parser.parseStartRecord("start", 1);
    parser.parseMaxRecords("max", 10);
    parser.parsePropertyIsEqualTo("uuid", "uuid");
    parser.parsePropertyIsLike("searchText", "anytext");
    parser.parsePropertyList("contentType", "dc:type", ",", true);
    parser.parsePropertyList("dataCategory", "dc:subject", ",", true);
    parser.parsePropertyRange("after", "before", "dct:modified");
    parser.parseSpatialClause("bbox", "spatialRel", "geometry");
    parser.parseSortables("orderBy");

    //parser.parsePropertyRange("validAfter","validBefore","dct:valid"); // date valid
    parser.parsePropertyIsEqualTo("publisher", "dc:publisher"); // publisher
    parser.parsePropertyIsEqualTo("source", "dc:source"); // harvesting id (uuid)
    parser.parsePropertyIsEqualTo("isPartOf", "dct:isPartOf"); // collection subset
    //parser.parsePropertyList("hasFormat","dct:hasFormat",",",true);
	
    //parameter &filter
	parser.parsePropertyIsLike("filter","anytext");

    return query;
  }

  /**
   * Generates a search critera object from the request.
   *
   * @param request the HTTP request
   * @param context the request context
   * @param query the pre-populated rest query
   * @return the search criteria object
   */
  protected SearchCriteria toSearchCriteria(HttpServletRequest request,
          RequestContext context, RestQuery query) {
    SearchCriteria criteria = new SearchCriteria();
    RestQueryParser parser = new RestQueryParser(request, context, new RestQuery());

    // keyword filter
    String sKeyword = Val.chkStr(parser.getRequestParameter("searchText"));
    if (sKeyword.length() > 0) {
      SearchFilterKeyword fKeyword = new SearchFilterKeyword();
      fKeyword.setSearchText(sKeyword);
      criteria.setSearchFilterKeyword(fKeyword);
    }

    // spatial filter
    SpatialClause bbox = parser.extractSpatialClause("bbox", "spatialRel", "geometry");
    if (bbox != null) {
      SearchFilterSpatial fSpatial = new SearchFilterSpatial();
      fSpatial.setSelectedEnvelope(bbox.getBoundingEnvelope());
      if (bbox instanceof SpatialClause.GeometryIsWithin) {
        fSpatial.setSelectedBounds(ISearchFilterSpatialObj.OptionsBounds.dataWithinExtent.name());
      } else {
        fSpatial.setSelectedBounds(ISearchFilterSpatialObj.OptionsBounds.useGeogExtent.name());
      }
      criteria.setSearchFilterSpatial(fSpatial);
    }

    // content type filter
    String sContentType = Val.chkStr(parser.getRequestParameter("contentType"));
    try {
      if (sContentType.length() > 0) {
        SearchFilterContentTypes fContentTypes = new SearchFilterContentTypes();
        fContentTypes.setSelectedContentType(
                SearchEngineCSW.AimsContentTypes.valueOf(sContentType).name());
        criteria.setSearchFilterContentTypes(fContentTypes);
      }
    } catch (IllegalArgumentException ex) {
      // if invalid content type simply do not create filter
    }

    // data category filter
    String delimitedThemes = Val.chkStr(parser.getRequestParameter("dataCategory"));
    String[] themes = Val.tokenize(delimitedThemes, ",");
    if (themes != null && themes.length > 0) {
      ArrayList<String> alThemes = new ArrayList<String>();
      for (String theme : themes) {
        alThemes.add(theme);
      }
      SearchFilterThemeTypes fThemes = new SearchFilterThemeTypes();
      fThemes.setSelectedThemes(alThemes);
      criteria.setSearchFilterThemes(fThemes);
    }

    // temporal filter
    String sAfter = Val.chkStr(parser.getRequestParameter("after"));
    String sBefore = Val.chkStr(parser.getRequestParameter("before"));
    if ((sAfter.length() > 0) || (sBefore.length() > 0)) {
      SearchFilterTemporal fTemporal = new SearchFilterTemporal();
      fTemporal.setDateModifiedFrom(sAfter);
      fTemporal.setDateModifiedTo(sBefore);
      fTemporal.setSelectedModifiedDateOption(
              SearchFilterTemporal.SelectedTimePeriod.beforeAndOrAfterPeriod.name());
      criteria.setSearchFilterTemporal(fTemporal);
    }

    // pagination filter
    SearchFilterPagination fPagination = new SearchFilterPagination();
    fPagination.setStartPostion(query.getFilter().getStartRecord());
    fPagination.setRecordsPerPage(query.getFilter().getMaxRecords());
    int startRecord = query.getFilter().getStartRecord();
    int maxRecords = query.getFilter().getMaxRecords();
    double page = (((double) startRecord) / ((double) maxRecords));
    fPagination.setCurrentPage((new Double(Math.ceil(page))).intValue());
    criteria.setSearchFilterPageCursor(fPagination);

    // sort filter
    String sOrderBy = Val.chkStr(parser.getRequestParameter("orderBy"));
    try {
      if (sOrderBy.length() > 0) {
        SearchFilterSort fSort = new SearchFilterSort();
        fSort.setSelectedSort(SearchFilterSort.OptionsSort.valueOf(sOrderBy).name());
        criteria.setSearchFilterSort(fSort);
      }
    } catch (IllegalArgumentException ex) {
      // if invalid content type simply do not create filter
    }

    // Distributed search
    String rid = parser.getRequestParameter("rid");
    SearchFilterHarvestSites harvestSites = new SearchFilterHarvestSites();
    harvestSites.setSelectedHarvestSiteId(rid);
    SearchFiltersList filterList = new SearchFiltersList();
    filterList.add(harvestSites);
    criteria.setMiscelleniousFilters(filterList);
    return criteria;
  }

  /**
   * enumerations ============================================================
   */
  /**
   * Enumeration of response formats.
   */
  protected enum ResponseFormat {
  	dcat,
    georss,
    kml,
    html,
    htmlfragment,
    atom,
    json,
    pjson,
    xjson,
    searchpage {
      @Override
      public boolean isApi() {
        return false;
      }
    },
    searchpageresults {
      @Override
      public boolean isApi() {
        return false;
      }
    };

    /**
     * Checks then returns the format associated with a value. <br/>If the value
     * is invalid, the default is returned.
     *
     * @param value the value to check.
     * @return the format Default: {@link ResponseFormat#georss}
     */
    protected static RestQueryServlet.ResponseFormat checkValueOf(String value) {
      value = Val.chkStr(value);
      for (RestQueryServlet.ResponseFormat f : values()) {
        if (f.name().equalsIgnoreCase(value)) {
          return f;
        }
      }
      return georss;
    }
    
    public boolean isApi() {
      return true;
    }
  }
}

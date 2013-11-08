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
package com.esri.gpt.control.rest;

import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchEngineFactory;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.catalog.search.SearchResultRecord;
import com.esri.gpt.catalog.search.SearchResultRecords;
import com.esri.gpt.control.georss.JsonFeedWriter;
import com.esri.gpt.control.georss.KmlFeedWriter;
import com.esri.gpt.control.georss.KmlFeedWriter.KmlSignatureProvider;
import com.esri.gpt.control.georss.RecordSnippetWriter;
import com.esri.gpt.control.georss.RestQueryServlet;
import com.esri.gpt.control.georss.SearchResultRecordAdapter;
import com.esri.gpt.control.georss.SearchResultRecordsAdapter;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * REST servlet.
 * Provides REST functionality. 
 */
public class RestServlet extends BaseServlet {

// class variables =============================================================

/** format parameter key ("f")*/
private static final String FORMAT_KEY = "f";

/** style parameter key ("style") */
private static final String STYLE_KEY = "style";


// constructors ================================================================

/**
 * Creates instance of the servlet.
 */
public RestServlet() {}

// properties ==================================================================

// methods =====================================================================
/**
 * Initializes servlet.
 * @param config servlet configuration
 * @throws ServletException if error initializing servlet
 */
@Override
public void init(ServletConfig config) throws ServletException {
  super.init(config);
}

/**
 * Process the HTTP request request.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws ServletException if error invoking command.
 * @throws IOException if error writing to the buffer.
 */
@Override
protected void execute(HttpServletRequest request,
                     HttpServletResponse response,
                     RequestContext context)
  throws ServletException, IOException {
  MessageBroker msgBroker = 
    new FacesContextBroker(request,response).extractMessageBroker();
  Format format = extractFormat(request);
  setContentType(response,format);
  PrintWriter writer = response.getWriter();
  
  // extra params
  Map<String,String> extraMap = new HashMap<String,String>();
  extraMap.put(RestQueryServlet.PARAM_KEY_SHOW_THUMBNAIL, 
      request.getParameter(RestQueryServlet.PARAM_KEY_SHOW_THUMBNAIL));
  extraMap.put(RestQueryServlet.PARAM_KEY_SHOW_RELATIVE_URLS, 
      request.getParameter(RestQueryServlet.PARAM_KEY_SHOW_RELATIVE_URLS));
  extraMap.put(RestQueryServlet.PARAM_KEY_IS_JSFREQUEST, 
      request.getParameter(RestQueryServlet.PARAM_KEY_IS_JSFREQUEST));
  context.getObjectMap().put(RestQueryServlet.EXTRA_REST_ARGS_MAP, extraMap);
  if(request.getScheme().toLowerCase().equals("https")
      && extraMap.get(RestQueryServlet.PARAM_KEY_SHOW_THUMBNAIL) == null) {
    String agent = request.getHeader("user-agent");
    if (agent != null && agent.toLowerCase().indexOf("msie") > -1) {
      extraMap.put(RestQueryServlet.PARAM_KEY_SHOW_THUMBNAIL, "false");
    }
  }

  try {

    // extract the id parameters  
    String id = Val.chkStr(request.getParameter("id"));
    String rid = Val.chkStr(request.getParameter("rid"));
    if (id.length() == 0) {
      String tmp = request.getPathInfo().replaceAll("^/", "");
      id = URLDecoder.decode(tmp,"UTF-8");
    }
    
    // create search dao
    SearchCriteria criteria = new SearchCriteria();
    SearchResult result = new SearchResult();
    ASearchEngine dao =
      rid.length()==0?
      SearchEngineFactory.createSearchEngine(criteria, result, context, 
          msgBroker):
      SearchEngineFactory.createSearchEngine(criteria, result, context, rid,
          msgBroker);

    switch (format) {
      case html: {
          String [] styleUrl = extractStyle(request);
          SearchResultRecord record = dao.getMetadataAsSearchResultRecord(id);
          printHtml(msgBroker, writer, record, styleUrl);
        }
        break;
      case htmlfragment: {
          SearchResultRecord record = dao.getMetadataAsSearchResultRecord(id);
          printHtmlFragment(msgBroker, writer, record);
        }
        break;
      case kml: {
          SearchResultRecord record = dao.getMetadataAsSearchResultRecord(id);
          printKml(msgBroker, writer, record);
        }
        break;
      case json:
      case pjson: {    	
          SearchResultRecord record = dao.getMetadataAsSearchResultRecord(id);
          printPjson(msgBroker, writer, record,format);
        }
        break;     
      default:
      case xml: {
          ASearchEngine.ARecord aRecord = dao.getARecord(id);
          String sMetadata = aRecord.getMetadataAsText();
          Date lastModified = aRecord.getModifiedDate();
          if (lastModified!=null) {
            response.addHeader("Last-Modified", new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", request.getLocale()).format(lastModified));
          }
          printXml(context, writer, sMetadata);
        }
        break;
    }

  } catch (Exception ex) {

    response.setContentType("text/plain;charset=UTF-8");
    String s = "Unable to return the document associated with the supplied identifier.";
    writer.println(s);
    LogUtil.getLogger().log(Level.SEVERE, "Error getting metadata", ex);

  } finally {
    writer.flush();
  }
}

/**
 * Sets content type according to the format.
 * @param response HTTP response
 * @param format format
 */
private void setContentType(HttpServletResponse response, Format format) {
  switch (format) {
    case html:
    case htmlfragment:
      response.setContentType("text/html;charset=UTF-8");
      break;
    case kml:
      response.setContentType("application/vnd.google-earth.kml+xml;charset=UTF-8");
      response.setHeader("Content-Disposition","attachment; filename=\"document.kml\"");
      break;
    case json:
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-disposition", "attachment; filename=\"document.json\"");
        break;
    case pjson:
	   response.setContentType("text/plain;charset=UTF-8");
	   break;  
    default:
    case xml:
      response.setContentType("text/xml;charset=UTF-8");
      break;
  }
}

/**
 * Extracts response format.
 * @param request HTTP request
 * @return response format
 */
private Format extractFormat(HttpServletRequest request) {
  
        return Format.checkValueOf(getParameterByKey(request,FORMAT_KEY));
}

/**
 * Extracts style url.
 * @param request HTTP request
 * @return array of styles URL's
 */
private String[] extractStyle(HttpServletRequest request) {
  return getParameterByKey(request,STYLE_KEY).split(",");
}

/**
 * Prints document as HTML
 * @param msgBroker the message broker
 * @param writer writer
 * @param record record
 * @param styleUrl array of styles URL's
 * @throws SearchException if extracting document failed
 */
private void printHtml(MessageBroker msgBroker, 
                       PrintWriter writer,
                       SearchResultRecord record,
                       String [] styleUrl) throws SearchException {
  String sLang = msgBroker.getLocale().getLanguage();
  
  writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
  writer.println("<html lang=\"" +sLang+ "\">");
  
  writer.println("<head>");
  writer.println("<title>" +record.getTitle()+ "</title>");
  for (String style : styleUrl) {
    style = Val.chkStr(style);
    if (style.length()>0) {
      writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" +style+ "\"/>");
    }
  }
  writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
  writer.println("</head>");
  writer.println("<body>");
  printHtmlFragment(msgBroker, writer, record);
  writer.println("</body>");
  writer.println("</html>");
}

/**
 * Prints document as HTML fragment
 * @param msgBroker the message broker
 * @param writer writer
 * @param record record
 * @throws SearchException if extracting document failed
 */
private void printHtmlFragment(MessageBroker msgBroker,
                               PrintWriter writer,
                               SearchResultRecord record) throws SearchException {
  RecordSnippetWriter snippetWriter = new RecordSnippetWriter(msgBroker, writer);
  snippetWriter.setShowTitle(true);
  snippetWriter.setShowIcon(true);
  snippetWriter.setClipText(true);
  snippetWriter.write(new SearchResultRecordAdapter(record));
}

/**
 * Prints document as XML
 * @param writer writer
 * @param sMetadata metadata as string
 * @throws SearchException if extracting document failed
 * @throws SchemaException if parsing document failed
 */
private void printXml(RequestContext context, PrintWriter writer, String sMetadata)
  throws SearchException, SchemaException {
  
  StringAttributeMap params = context.getCatalogConfiguration().getParameters();
  String s = Val.chkStr(params.getValue("RestServlet.printXml.stripStyleSheets"));
  boolean bStripStyleSheets = s.equalsIgnoreCase("true");
  if (bStripStyleSheets) {
    //sMetadata = sMetadata.replaceAll("<\\?xml\\-stylesheet.*\\?>|<\\!DOCTYPE.*>","");
    sMetadata = sMetadata.replaceAll("<\\?xml\\-stylesheet.+?>|<\\!DOCTYPE.+?>","");
  }
  
  MetadataDocument document = new MetadataDocument();
  String sXml = document.prepareForFullViewing(sMetadata);
  writer.write(sXml);
}

/**
 * Gets parameter value.
 * @param request HTTP request
 * @param parameterKey parameter key
 * @return parameter name
 */
private String getParameterByKey(HttpServletRequest request,
                                 String parameterKey) {
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
 * Prints records as KML.
 * @param msgBroker message broker
 * @param writer underlying writer
 * @param record record to write
 */
private void printKml(MessageBroker msgBroker, PrintWriter writer, final SearchResultRecord record) {
  SearchResultRecords records = new SearchResultRecords();
  records.add(record);
  KmlFeedWriter kmlWriter = new KmlFeedWriter(msgBroker, writer);
  kmlWriter.setKmlSignatureProvider(new KmlSignatureProvider() {
    public String getTitle() {
      return record.getTitle();
    }
    public String getDescription() {
      return record.getAbstract();
    }
  });
  kmlWriter.write(new SearchResultRecordsAdapter(records));
}


/**
 * Prints records as KML.
 * @param msgBroker message broker
 * @param writer underlying writer
 * @param record record to write
 */
private void printPjson(MessageBroker msgBroker, PrintWriter writer, final SearchResultRecord record, Format format) {
  SearchResultRecords records = new SearchResultRecords();
  records.add(record);
  JsonFeedWriter jsonWriter = new JsonFeedWriter(writer, null, format == Format.pjson );
  jsonWriter.setMessageBroker(msgBroker);  
  jsonWriter.write(new SearchResultRecordsAdapter(records));
}
// enums =======================================================================
/**
 * Response format.
 */
private enum Format {

/** XML (full metadata) */
xml,
/** HTML */
html,
/** HTML FRAGMENT (snippet) */
htmlfragment,
/** JSON */
json,
/** HTML */
pjson,
/** KML */
kml;

/**
 * Checks value.
 * @param value textual value
 * @return value. Default: {@link Format#xml}
 */
public static Format checkValueOf(String value) {
  value = Val.chkStr(value);
  for (Format f: values()) {
    if (f.name().equalsIgnoreCase(value)) {
      return f;
    }
  }
  return xml;
}
}
}

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

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.*;
import com.esri.gpt.control.georss.IFeedRecords.FieldMeta;
import com.esri.gpt.control.georss.RestQueryServlet.ResponseFormat;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.context.UrnMap;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;

/**
 * JSON feed writer. 
 * Writes response in JSON (or pretty JSON) format.
 * <p/>
 * Uses <i>json.writer.className</i> parameter from <b>gpt.xml</b> to create an instance.
 * Creates default instance if parameter is empty.
 * <p/>
 * Default implementation uses <i>json.predicate.banned</i> parameter from <b>gpt.xml</b>, 
 * which is a regular expression used to stop printing certain fields. Note
 * that fields beginning with "index.sys.xml" are not printed by default; the
 * only way to have it printed is to specify them explicitely in <i>outFields</i>
 * request parameter.
 * @see JsonSearchEngine
 */
public class ExtJsonFeedWriter implements FeedWriter {

  /**
   * Logger.
   */
  protected static final Logger LOG = Logger.getLogger(ExtJsonFeedWriter.class.getCanonicalName());
  /**
   * tab size
   */
  private final static int TAB_SIZE = 2;
  /**
   * ISO date format
   */
  protected final static IsoDateFormat DF = new IsoDateFormat();
  /**
   * parameter map
   */
  protected Map<String,String[]> parameterMap;
  /**
   * print writer
   */
  private PrintWriter writer;
  /**
   * original query
   */
  protected RestQuery query;
  /**
   * flag indicating if is this a pretty formated JSON
   */
  private boolean pretty;
  /**
   * predicates
   */
  protected Pattern predicateBanned;
  /**
   * indentation level
   */
  private int level = 0;
  /**
   * message broker
   */
  protected MessageBroker messageBroker;

  /**
   * Creates instance of the feed writer. 
   *
   * @param parameterMap parameter map
   * @param context request context
   * @param writer writer
   * @param query query
   * @param pretty <code>true</code> for 'pretty' JSON output
   * @return instance of {@link ExtJsonFeedWriter}
   */
  public static ExtJsonFeedWriter createInstance(Map<String,String[]> parameterMap, RequestContext context, PrintWriter writer, RestQuery query, Boolean pretty) {
    String className = getConfigParam("json.writer.className");
    if (className.isEmpty()) {
      return new ExtJsonFeedWriter(parameterMap, context, writer, query, pretty);
    } else {
      try {
      	//TODO this code throws exception
        Class writerClass = Class.forName(className);
        Constructor constructor = writerClass.getConstructor(new Class[]{Map.class, RequestContext.class, PrintWriter.class, RestQuery.class, Boolean.class});
        return (ExtJsonFeedWriter) constructor.newInstance(parameterMap,context, writer, query, pretty);
      	//return new DcatJsonFeedWriter(request, context, writer, query, pretty);
      } catch (Exception ex) {
        LOG.log(Level.INFO, "Error creating JSON feed writer class: " + className + ". Using default writer instead.", ex);
        return new ExtJsonFeedWriter(parameterMap, context, writer, query, pretty);
      }
    }
  }

  /**
   * Creates instance of the feed writer. 
   *
   * @param request HTTP servlet request
   * @param context request context
   * @param writer writer
   * @param query query
   * @param pretty <code>true</code> for 'pretty' JSON output
   * @return instance of {@link ExtJsonFeedWriter}
   */
  public static ExtJsonFeedWriter createInstance(HttpServletRequest request, RequestContext context, PrintWriter writer, RestQuery query, Boolean pretty) {
    return createInstance(request.getParameterMap(), context, writer, query, pretty);
  }

  /**
   * Sets message broker
   *
   * @param messageBroker the messageBroker to set
   */
  public void setMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
  }

  /**
   * Gets message broker
   *
   * @return the messageBroker
   */
  public MessageBroker getMessageBroker() {
    return messageBroker;
  }
  
  @Override
  public void write(IFeedRecords records) {
    if (Val.chkBool(getRequestParam("returnCountOnly"),false)) {
      writeCountOnly(records.getOpenSearchProperties().getNumberOfHits());
      return;
    }
    
    if (Val.chkBool(getRequestParam("returnIdsOnly"),false)) {
      writeIdsOnly(records);
      return;
    }
            
    String sTitle = messageBroker.retrieveMessage("catalog.rest.title");
    String sDescription = messageBroker.retrieveMessage("catalog.rest.description");
    String sCopyright = messageBroker.retrieveMessage("catalog.rest.copyright");
    String sGenerator = messageBroker.retrieveMessage("catalog.rest.generator");

    if (sTitle.startsWith("???")) {
      sTitle = "";
    }
    if (sDescription.startsWith("???")) {
      sDescription = "";
    }
    if (sCopyright.startsWith("???")) {
      sCopyright = "";
    }
    if (sGenerator.startsWith("???")) {
      sGenerator = "";
    }

    println("{");
    levelUp();

    printArg("title", sTitle, true);
    printArg("description", sDescription, true);
    printArg("copyright", sCopyright, true);
    printArg("updated", DF.format(new Date()), true);
    printArg("type", "FeatureCollection", true);
    if (query != null) {
      printArg("provider", query.getRssProviderUrl(), true);
      printArg("source", query.getRssSourceUrl(), true);
      if (!query.getRepositoryId().isEmpty()) {
        printArg("repositoryId", query.getRepositoryId(), true);
      }
    }
    OpenSearchProperties osProps = records.getOpenSearchProperties();
    if (osProps != null) {
      Long totalResults = new Long(osProps.getNumberOfHits());
      Long startIndex = new Long(osProps.getStartRecord());
      Long itemsPerPage = new Long(osProps.getRecordsPerPage());
      if (query != null) {
        if (startIndex + itemsPerPage - 1 < totalResults) {
          printArg("more", query.getMoreUrl(), true);
        }
      }
      printArg("totalResults", totalResults, true);
      printArg("startIndex", startIndex, true);
      printArg("itemsPerPage", itemsPerPage, true);
    }

    printArg("displayFieldName", sTitle, true);
    println("\"fieldAliases\": {");
    levelUp();
    List<FieldMeta> fml = new ArrayList();
    for (FieldMeta fm: records.getMetaData()) {
      if (checkAttr(fm.getName())) {
        fml.add(fm);
      }
    }
    for (int i = 0; i < fml.size(); i++) {
      FieldMeta f = fml.get(i);
      printArg(f.getName(), f.getAlias(), i < fml.size() - 1);
    }
    levelDown();
    println("},");
    printArg("geometryType", "esriGeometryPolygon", true);
    println("\"spatialReference\": { \"wkid\": " +getOutputSpatialReference()+ " },");
    println("\"fields\": [");
    levelUp();
    for (int i = 0; i < fml.size(); i++) {
      FieldMeta f = fml.get(i);
      printField(f, i < fml.size() - 1);
    }
    levelDown();
    println("],");

    printRecords(records, true);
    printAPI(false);
    levelDown();
    println("}");
  }

  /**
   * Creates instance of the feed.
   *
   * @param request HTTP request
   * @param context request context
   * @param writer writer to write feed
   * @param query query
   * @param pretty <code>true</code> to print pretty response
   */
  public ExtJsonFeedWriter(HttpServletRequest request, RequestContext context, PrintWriter writer, RestQuery query, Boolean pretty) {
    this(request.getParameterMap(), context, writer, query, pretty);
  }
  
  /**
   * Creates instance of the feed.
   *
   * @param parameterMap parameter map
   * @param context request context
   * @param writer writer to write feed
   * @param query query
   * @param pretty <code>true</code> to print pretty response
   */
  public ExtJsonFeedWriter(Map<String,String[]> parameterMap, RequestContext context, PrintWriter writer, RestQuery query, Boolean pretty) {
    this.parameterMap = parameterMap;
    this.writer = writer;
    this.query = query;
    this.pretty = pretty;

    String sPredicateBanned = getConfigParam("json.predicate.banned");

    if (!sPredicateBanned.isEmpty()) {
      try {
        this.predicateBanned = Pattern.compile(sPredicateBanned);
      } catch (PatternSyntaxException ex) {
        Logger.getLogger(ExtJsonFeedWriter.class.getCanonicalName()).log(Level.INFO, "Error compiling predicate: " + sPredicateBanned, ex);
      }
    }
  }

  /**
   * Writes number of matching records only.
   * @param numberOfHits
   */
  protected void writeCountOnly(int numberOfHits) {
    println("{");
    levelUp();
    printArg("count", numberOfHits, false);
    levelDown();
    println("}");
  }
  
  /**
   * Writes records ids only.
   * @param records list of records
   */
  protected void writeIdsOnly(List<IFeedRecord> records) {
    println("{");
    levelUp();
    printArg("objectIdFieldName", "UUID", true);
    println("\"objectIds\": [");
    levelUp();
    
    for (int i=0; i<records.size(); i++) {
      IFeedRecord r = records.get(i);
      String uuid = r.getUuid();
      println("\""+uuid+"\""+(i<records.size()-1? ",": ""));
    }
    
    levelDown();
    println("]");
    levelDown();
    println("}");
  }

  /**
   * Prints a field.
   *
   * @param f field
   * @param more more fields
   */
  protected void printField(FieldMeta f, boolean more) {
    println("{");
    levelUp();
    printArg("name", f.getName(), true);
    printArg("type", f.getType(), true);
    printArg("alias", f.getAlias(), f.getLength() != null);
    if (f.getLength() != null) {
      printArg("length", f.getLength(), false);
    }
    levelDown();
    println("}" + (more ? "," : ""));
  }

  /**
   * Prints all records.
   *
   * @param records records to print
   * @param more <code>true</code> if more info will be printed after that
   * section
   */
  protected void printRecords(IFeedRecords records, boolean more) {
    try {
      List<Envelope> envelopes = new ArrayList<Envelope>();
      for (IFeedRecord r : records) {
        envelopes.add(r.getEnvelope());
      }
      String outSR = getRequestParam("outSR");
      if (!outSR.isEmpty() && !"4326".equals(outSR)) {
        GeometryService gs = GeometryService.createDefaultInstance();
        envelopes = gs.project(envelopes, outSR);
      }

      println("\"features\"" + sp() + ":" + sp() + "[");
      levelUp();

      for (int i = 0; i < records.size(); i++) {
        printRecord(records.get(i), envelopes.get(i), i < records.size() - 1);
      }

      levelDown();
      println("]" + (more ? "," : ""));
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "Error writing records", ex);
    } catch (JSONException ex) {
      LOG.log(Level.SEVERE, "Error writing records", ex);
    }
  }

  /**
   * Prints record.
   *
   * @param r record to print
   * @param env envelope
   * @param more <code>true</code> if more info will be printed after that
   * section
   */
  protected void printRecord(IFeedRecord r, Envelope env, boolean more) {
    println("{");
    levelUp();

    printArg("type", "Feature", true);
    printAttributes("properties", r, true);
    printAttributes("attributes", r, true);
    
    if (!hasRequestParam("returnGeometry") || Val.chkBool(getRequestParam("returnGeometry"), true)) {
      printGeometry(env, true);
    }


    ResourceLinks links = r.getResourceLinks();
    printLinks(links, true);

    printResources(links, false);

    levelDown();
    println("}" + (more ? "," : ""));
  }

  /**
   * Prints attributes.
   *
   * @param name name
   * @param r record
   * @param more <code>true</code> if more info will be printed after that
   * section
   */
  protected void printAttributes(String name, IFeedRecord r, boolean more) {
    println("\"" +name+ "\"" + sp() + ":" + sp() + "{");
    levelUp();

    Map<String, IFeedAttribute> index = r.getData(IFeedRecord.STD_COLLECTION_INDEX);
    List<String> indexKeys = new ArrayList<String>(index.keySet());

    ArrayList<Map.Entry<String, IFeedAttribute>> entries = new ArrayList<Map.Entry<String, IFeedAttribute>>(r.getData(IFeedRecord.STD_COLLECTION_CATALOG).entrySet());
    Collections.sort(entries, new Comparator<Map.Entry<String, IFeedAttribute>>() {
      @Override
      public int compare(Entry<String, IFeedAttribute> o1, Entry<String, IFeedAttribute> o2) {
        return o1.getKey().compareToIgnoreCase(o2.getKey());
      }
    });

    boolean before = false;

    if (checkAttr("objectid")) {
      before = printAttr(before, "objectid", r.getObjectId());
    }
    if (checkAttr("title")) {
      before = printAttr(before, "title", r.getTitle());
    }
    if (checkAttr("id")) {
      before = printAttr(before, "id", r.getUuid());
    }
    if (checkAttr("fileIdentifier") && !r.getFileIdentifier().isEmpty()) {
      before = printAttr(before, "fileIdentifier", r.getFileIdentifier());
    }
    if (checkAttr("updated") && r.getModfiedDate() instanceof Date) {
      before = printAttr(before, "updated", DF.format(r.getModfiedDate()));
    }
    if (checkAttr("contentType") && !r.getContentType().isEmpty()) {
      before = printAttr(before, "contentType", UrnMap.URN_ESRI_GPT + ":contentType:" + r.getContentType());
    }
    if (checkAttr("summary")) {
      before = printAttr(before, "summary", r.getAbstract());
    }

    for (int i = 0; i < indexKeys.size(); i++) {
      String indexKey = indexKeys.get(i);
      IFeedAttribute indexValue = index.get(indexKey);

      String attrName = IFeedRecord.STD_COLLECTION_INDEX+"." + indexKey;
      if (checkAttr(attrName)) {
        if (before) {
          print(false, ",");
          print(false, "\r\n");
        }
        print(before, "\"" + attrName + "\"" + sp() + ":" + sp() + indexValue);
        before = true;
      }
    }

    for (int i = 0; i < entries.size(); i++) {
      Entry<String, IFeedAttribute> e = entries.get(i);

      String attrName = IFeedRecord.STD_COLLECTION_CATALOG+"." + e.getKey();
      if (checkAttr(attrName)) {
        if (before) {
          print(false, ",");
          print(false, "\r\n");
        }
        print(before, "\"" + attrName + "\"" + sp() + ":" + sp() + e.getValue());
        before = true;
      }
    }

    print(false, "\r\n");

    levelDown();
    println("}" + (more ? "," : ""));
  }

  /**
   * Prints geometry.
   *
   * @param env geometry
   * @param more flag to indicate if there will be more arguments
   */
  protected void printGeometry(Envelope env, boolean more) {
    println("\"geometry\"" + sp() + ":" + sp() + "{");
    levelUp();
    printArg("type", "Polygon", true);
    printPolygonShape("coordinates",env, true);
    printPolygonShape("rings",env, false);
    levelDown();
    println("}" + (more ? "," : ""));
  }

  /**
   * Prints a polygon.
   *
   * @param env
   */
  protected void printPolygonShape(String name, Envelope env, boolean more) {
  //printArg("type", "Polygon", true);
    println("\"" +name+ "\"" + sp() + ":" + sp() + "[");
    printPolygon(env);
    println("]" + (more ? "," : ""));
  //  println("],");
  //  println("\"spatialReference\":{\"wkid\":" + Val.chkStr(env.getWkid(), getOutputSpatialReference()) + "  }");
  }

  /**
   * Prints polygon.
   *
   * @param env polygon to print
   */
  protected void printPolygon(Envelope env) {
    levelUp();
    println("[");
    levelUp();
    println(
            coord(env.getMinX(), env.getMinY()) + "," + sp()
            + coord(env.getMinX(), env.getMaxY()) + "," + sp()
            + coord(env.getMaxX(), env.getMaxY()) + "," + sp()
            + coord(env.getMaxX(), env.getMinY()) + "," + sp()
            + coord(env.getMinX(), env.getMinY()));
    levelDown();
    println("]");
    levelDown();
  }

  /**
   * Prints all links.
   *
   * @param links collection of resource links
   * @param more flag to indicate if there will be more arguments
   */
  protected void printLinks(ResourceLinks links, boolean more) {
    RequestContext rc = RequestContext.extract(null);
    ResourceIdentifier ri = ResourceIdentifier.newIdentifier(rc);

    println("\"links\"" + sp() + ":" + sp() + "[");
    levelUp();

    ArrayList<ResourceLink> allLinks = new ArrayList<ResourceLink>();
    for (int j = 0; j < links.size(); j++) {
      ResourceLink rl = links.get(j);
      if (!rl.getTag().equals(ResourceLink.TAG_CONTENTTYPE) && !rl.getTag().equals(ResourceLink.TAG_THUMBNAIL)) {
        allLinks.add(links.get(j));
      }
    }

    for (int i = 0; i < allLinks.size(); i++) {
      printLink(ri, allLinks.get(i), i < allLinks.size() - 1);
    }

    levelDown();
    println("]" + (more ? "," : ""));
  }

  /**
   * Prints all resources.
   *
   * @param links collection of resource links
   * @param more flag to indicate if there will be more arguments
   */
  protected void printResources(ResourceLinks links, boolean more) {
    RequestContext rc = RequestContext.extract(null);
    ResourceIdentifier ri = ResourceIdentifier.newIdentifier(rc);

    println("\"resources\"" + sp() + ":" + sp() + "[");
    levelUp();

    ResourceLink thumbnail = links.getThumbnail();
    ResourceLink icon = links.getIcon();

    if (thumbnail != null) {
      printLink(ri, thumbnail, icon != null);
    }
    if (icon != null) {
      printLink(ri, icon, false);
    }

    levelDown();
    println("]" + (more ? "," : ""));
  }

  /**
   * Prints a link.
   *
   * @param ri resource identifier
   * @param link resource link
   * @param more flag to indicate if there will be more arguments
   */
  protected final void printLink(ResourceIdentifier ri, ResourceLink link, boolean more) {
    if (!link.getTag().isEmpty() && !link.getUrl().isEmpty()) {
      String tag = link.getTag().equals(ResourceLink.TAG_CONTENTTYPE) ? "icon" : link.getTag();
      printLink(link.getUrl(), tag, link.getLabel(), guessServiceUrn(ri, link), more);
    }
  }

  /**
   * Prints link.
   *
   * @param url url
   * @param tag tag
   * @param label label
   * @param urn classified as urn
   * @param more flag to indicate if there will be more arguments
   */
  protected void printLink(String url, String tag, String label, String urn, boolean more) {
    println("{");
    levelUp();
    printArg("href", url, true);
    printArg("hrefType", UrnMap.URN_ESRI_GPT + ":hrefType:" + tag, true);
    printArg("label", label, true);
    printArg("type", tag, !urn.isEmpty());
    if (!urn.isEmpty()) {
      printArg("classifiedAs", urn, false);
    }
    levelDown();
    println("}" + (more ? "," : ""));
  }

  /**
   * Prints bottom links.
   *
   * @param more more links
   */
  protected void printAPI(boolean more) {
    //String [] alts  = new String[]{"georss","atom","html","htmlfragment","json","xjson","kml","csv"};
    ArrayList<ResponseFormat> alts = new ArrayList<ResponseFormat>();
    for (ResponseFormat f : ResponseFormat.values()) {
      if (f.isApi()) {
        alts.add(f);
      }
    }
    println("\"api\": [");
    if (query != null) {
      levelUp();
      for (int i = 0; i < alts.size(); i++) {
        ResponseFormat alt = alts.get(i);
        String label = alt.name().toUpperCase();
        if (alt == ResponseFormat.htmlfragment) {
          label = "FRAGMENT";
        }
        if (alt == ResponseFormat.pjson) {
          label = "JSON";
        }
        if (alt == ResponseFormat.xjson) {
          label = "JSON (Extended)";
        }
        String url = query.getRssSourceUrl().replaceAll(
                "f=" + ResponseFormat.pjson + "|f=" + ResponseFormat.json + "|f=" + ResponseFormat.xjson,
                "f=" + (alt == ResponseFormat.json ? ResponseFormat.pjson : alt));
        printLink(url, alt.name(), label, "", i < alts.size() - 1);
      }
      levelDown();
    }
    println("]" + (more ? "," : ""));
  }

  /**
   * Guesses URN of resource link type.
   *
   * @param ri instance of resource identifier
   * @param link resource link
   * @return URN of resource link type
   */
  protected String guessServiceUrn(ResourceIdentifier ri, ResourceLink link) {
    if (link.getTag().equals(ResourceLink.TAG_OPEN)) {
      return ri.guessServiceUrnFromUrl(link.getUrl());
    }
    return "";
  }

  /**
   * Prints argument.
   *
   * @param argName argument name
   * @param argVal argument value
   * @param more flag to indicate if there will be more arguments
   */
  protected final void printArg(String argName, String argVal, boolean more) {
    argName = Val.chkStr(argName);
    argVal = Val.chkStr(argVal);
    if (argName.length() > 0) {
      println("\"" + Val.escapeStrForJson(argName) + "\"" + sp() + ":" + sp() + "\"" + Val.escapeStrForJson(argVal) + "\"" + (more ? "," : ""));
    }
  }

  /**
   * Prints argument.
   *
   * @param argName argument name
   * @param argVal argument value
   * @param more flag to indicate if there will be more arguments
   */
  protected final void printArg(String argName, Number argVal, boolean more) {
    argName = Val.chkStr(argName);
    if (argName.length() > 0) {
      println("\"" + Val.escapeStrForJson(argName) + "\"" + sp() + ":" + sp() + argVal + (more ? "," : ""));
    }
  }

  /**
   * Prints attribute.
   *
   * @param before <ocde>true</code> if anything has been printed before
   * @param argName argument name
   * @param argVal argument value
   * @return <code>true</code> if anything has been printed
   */
  protected final boolean printAttr(boolean before, String argName, String argVal) {
    argName = Val.chkStr(argName);
    argVal = Val.chkStr(argVal);
    if (checkAttr(argName) && argName.length() > 0) {
      if (before) {
        print(false, ",");
        print(false, "\r\n");
      }
      print(true, "\"" + Val.escapeStrForJson(argName) + "\"" + sp() + ":" + sp() + "\"" + Val.escapeStrForJson(argVal) + "\"");
      before = true;
    }
    return before;
  }

  /**
   * Prints attribute.
   *
   * @param before <ocde>true</code> if anything has been printed before
   * @param argName argument name
   * @param argVal argument value
   * @return <code>true</code> if anything has been printed
   */
  protected final boolean printAttr(boolean before, String argName, long argVal) {
    argName = Val.chkStr(argName);
    if (checkAttr(argName) && argName.length() > 0) {
      if (before) {
        print(false, ",");
        print(false, "\r\n");
      }
      print(true, "\"" + Val.escapeStrForJson(argName) + "\"" + sp() + ":" + sp() + argVal);
      before = true;
    }
    return before;
  }

  /**
   * Makes coordinates.
   *
   * @param x x coordinate
   * @param y y coordinate
   * @return coordinate in JSON format
   */
  protected String coord(double x, double y) {
    return "[" + String.format("%f",x) + "," + String.format("%f",y) + "]";
  }

  /**
   * Checks if attribute of a given name can be printed.
   *
   * @param attrName attribute name
   * @return <code>true</code> if attribute can be printed
   */
  protected boolean checkAttr(String attrName) {
    Set<String> outFieldsSet = buildOutFieldsSet();
    
    // check if any xml is allowed; by default no xml is allowed, only if specified 
    // in outFields
    String xmlField = IFeedRecord.STD_COLLECTION_INDEX+"."+"sys.xml";
    if (attrName.startsWith(xmlField) && (outFieldsSet==null || !outFieldsSet.contains(attrName))) {
      return false;
    }
    
    // must pass banned predicate; 'banned' is a regular expression which competly
    // hides a field from reading - even specifying it in outFields will not let it pass
    if (predicateBanned != null && predicateBanned.matcher(attrName).matches()) {
      return false;
    }
    
    // check if user requested specified fields through oufFields; if specified
    // and the current attribute is not on that list, don't print it
    if (outFieldsSet!=null && !outFieldsSet.contains(attrName)) {
      return false;
    }
    
    
    return true;
  }
  
  /**
   * Builds collection of names of fields for output.
   * @return set of fields names.
   */
  protected Set<String> buildOutFieldsSet() {
    Set<String> outFieldsSet = null;
    String outFields = getRequestParam("outFields");
    if (!outFields.isEmpty() && !outFields.equals("*")) {
      outFieldsSet = new TreeSet<String>(Arrays.asList(outFields.split(",")));
    }
    return outFieldsSet;
  }

  /**
   * Gets configuration parameter.
   *
   * @param paramName parameter name
   * @return parameter value
   */
  protected static String getConfigParam(String paramName) {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    return Val.chkStr(appCfg.getCatalogConfiguration().getParameters().getValue(paramName));
  }

  /**
   * Gets request parameter.
   *
   * @param paramName parameter name
   * @return request parameter
   */
  protected String getRequestParam(String paramName) {
    String[] paramValues = parameterMap.get(paramName);
    if (paramValues!=null) {
      for (String param: parameterMap.get(paramName)) {
        param = Val.chkStr(param);
        if (!param.isEmpty()) {
          return param;
        }
      }
    }
    return "";
  }
  
  /**
   * Checks if parameter has even been supplied.
   * @param paramName parameter name
   * @return <code>true</code> if parameter is present (even an empty string)
   */
  protected boolean hasRequestParam(String paramName) {
    String[] paramValues = parameterMap.get(paramName);
    return paramValues!=null && paramValues.length>0;
  }

  /**
   * Gets output spatial reference.
   * @return output spatial reference. Default: <b>4326</b>.
   */
  protected String getOutputSpatialReference() {
      String outSR = getRequestParam("outSR");
      if (!outSR.isEmpty()) {
        return outSR;
      }
      return "4326";
  }
  
  /**
   * Increases level of indentation.
   */
  protected final void levelUp() {
    level++;
  }

  /**
   * Decreases level of indentation.
   */
  protected final void levelDown() {
    level--;
  }

  /**
   * Prints a single line. Depending on the 'pretty' flag, line is indented or
   * not.
   *
   * @param text text to print
   */
  protected final void println(String text) {
    if (pretty) {
      printTab();
      writer.println(text);
    } else {
      writer.print(text);
    }
  }

  /**
   * Prints a single line without ending it with a new line.
   *
   * @param indent <code>true</code> to indent the line
   * @param text text to print
   */
  protected final void print(boolean indent, String text) {
    if (pretty) {
      if (indent) {
        printTab();
      }
      writer.print(text);
    } else {
      writer.print(text);
    }
  }

  /**
   * Prints tabulator. Tabulator width depends on the indentation level.
   */
  protected final void printTab() {
    for (int i = 0; i < level * TAB_SIZE; i++) {
      writer.print(" ");
    }
  }

  /**
   * Creates a single space. Depending on the 'pretty' flag, it's either a space
   * or no space at all.
   *
   * @return single space
   */
  protected final String sp() {
    return pretty ? " " : "";
  }
}

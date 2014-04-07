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

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.schema.indexable.tp.TpUtil;
import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.util.Val;

import java.util.logging.Logger;

/**
 * Writes Dcat json response of search results using dcat mappings.
 */
public class DcatJsonFeedWriter extends ExtJsonFeedWriter {
  private static final Logger LOGGER = Logger.getLogger(DcatJsonFeedWriter.class.getCanonicalName());

  private HashMap<String, String> defaultValues = new HashMap<String, String>();
  private DcatSchemas dcatSchemas; 

  protected DcatJsonFeedWriter(HttpServletRequest request,
          RequestContext context, PrintWriter writer, RestQuery query,
          Boolean pretty) {
    super(request, context, writer, query, pretty);
  }

  protected DcatJsonFeedWriter(Map<String, String[]> parameterMap,
          RequestContext context, PrintWriter writer, RestQuery query,
          Boolean pretty) {
    super(parameterMap, context, writer, query, pretty);
  }

  public DcatJsonFeedWriter(RequestContext context, PrintWriter writer, RestQuery query) {
    super(new HashMap<String, String[]>(), context, writer, query, true);
    this.parameterMap.put("p", new String[]{"dcat"});
  }

  @Override
  public void write(IFeedRecords records) {

    String sTitle = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.title"));
    String sDescription = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.description"));
    String sKeyword = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.keyword"));
    String sModified = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.modified"));
    String sPublisher = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.publisher"));
    String sPerson = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.contactPoint"));
    String sMbox = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.mbox"));
    String sIdentifier = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.identifier"));
    String sAccessLevel = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.accessLevel"));
    String sAccessLevelComment = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.accessLevelComment"));
    String sBureauCode = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.bureauCode"));
    String sProgramCode = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.programCode"));
    String sDataDictionary = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.dataDictionary"));
    String sAccessUrl = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.accessURL"));
    String sWebService = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.webService"));
    String sFormat = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.format"));
    String sLicense = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.license"));
    String sSpatial = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.spatial"));
    String sTemporal = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.temporal"));
    
    defaultValues.put("title", sTitle);
    defaultValues.put("description", sDescription);
    
    defaultValues.put("keyword", sKeyword);
    defaultValues.put("modified", sModified);
    
    defaultValues.put("publisher", sPublisher);
    defaultValues.put("contactPoint", sPerson);
    defaultValues.put("mbox", sMbox);
    defaultValues.put("identifier", "");
    defaultValues.put("accessLevel", sAccessLevel);
    defaultValues.put("accessLevelComment", sAccessLevelComment);

    defaultValues.put("bureauCode", sBureauCode);
    defaultValues.put("programCode", sProgramCode);
    
    defaultValues.put("dataDictionary", sDataDictionary);
    defaultValues.put("accessURL", sAccessUrl);
    defaultValues.put("webService", sWebService);
    defaultValues.put("format", sFormat);
    defaultValues.put("spatial", sSpatial);
    defaultValues.put("temporal", "");

    println("[");
    levelUp();

    println("{");
    levelUp();

    printArg("title", sTitle, true);
    printArg("description", sDescription, true);
    printArg2("keyword", sKeyword, true);
    if(sModified.length() > 0 ){
    	printArg("modified", sModified, true);
    }else {
    	printArg("modified", DF.format(new Date()), true);
    }
    printArg("publisher", sPublisher, true);
    printArg("contactPoint", sPerson, true);
    printArg("mbox", sMbox, true);
    printArg("identifier", sIdentifier, true);
    printArg("accessLevel", sAccessLevel, true);
    printArg("accessLevelComment", sAccessLevelComment, true);
    printArg2("bureauCode", sBureauCode, true);
    printArg2("programCode", sProgramCode, true);
    if(sDataDictionary.length() > 0){
    	printArg("dataDictionary", sDataDictionary, true);
    }

    if (sAccessUrl.length() > 0) {
      printLinkArg("accessURL", sAccessUrl, true);
    } else if (query != null) {
      printLinkArg("accessURL", query.getRssProviderUrl(), true);
    } else {
      printLinkArg("accessURL", "", true);
    }

    if (sWebService.length() > 0) {
      printLinkArg("webService", sWebService, true);
    } else if (query != null) {
      printLinkArg("webService", query.getRssSourceUrl(), true);
    } else {
      printArg("webService", "", true);
    }

    printArg("format", "application/json", true);
    printArg("license", sLicense, true);
    printArg("spatial", sSpatial, true);
    printArg("temporal", sTemporal, false);

    levelDown();
    println(records!=null && records.size() > 0 ? "}," : "}");
    if (records!=null) {
      printRecords(records, true);
    }

    levelDown();
    println("]");
  }

  private String normalizeResource(String s) {
    if (s.startsWith("???")) {
      s = "";
    }
    return s;
  }

  /**
   * Prints all records.
   *
   * @param records records to print
   * @param more <code>true</code> if more info will be printed after that
   * section
   */
  @Override
  protected void printRecords(IFeedRecords records, boolean more) {
    int numberOfHits = records.getOpenSearchProperties().getNumberOfHits();
    int counter = 0;
    
    this.dcatSchemas = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getDcatSchemas();
    LOGGER.info("Beginning processing " +numberOfHits+ " DCAT records...");
    for (int i = 0; i < records.size(); i++) {
      IFeedRecord record = records.get(i);
      Envelope envelope = record.getEnvelope();
      printRecord(record, envelope, i < records.size() - 1);
      
      if ((++counter)%1000==0) {
        LOGGER.info("Processed "+counter+"/" +numberOfHits+ " DCAT records ("+(100*counter)/numberOfHits+"%)");
      }
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
  @Override
  protected void printRecord(IFeedRecord r, Envelope env, boolean more) {
    println("{");
    levelUp();

    printAttributesUserDcatMappings(r, false);

    levelDown();
    println("}" + (more ? "," : ""));
  }

  /**
   * Looks up dcat mapping field for a give a record and writes response.
   *
   * @param r the record
   * @param dcatField the dcat field
   * @param before the indentation boolean cursor
   * @return the indentation boolean cursor
   */
  private boolean lookUpFieldFromDcat(IFeedRecord r, DcatField dcatField, boolean before) {
    String delimiter = ",";
    String dl = dcatField.getDelimiter();
	 if(dl.length() > 0){
		 delimiter = dl;
	 }
    String indexKey = dcatField.getIndex();
    Map<String, IFeedAttribute> index = r.getData(IFeedRecord.STD_COLLECTION_INDEX);
    if (indexKey.length() > 0) {
      before = writeField(r,index, dcatField, delimiter, before);
    } else if (dcatField.getName().equalsIgnoreCase("identifier") && indexKey.length() == 0) {
      ResourceLinks links = r.getResourceLinks();
      before = writeFieldValue("\"" + getIdentifierUrl(links) + "\"", "identifier", delimiter, before,dcatField);
    }
    return before;
  }

  /**
   * Prints attributes using dcat mappings
   *
   * @param r record
   * @param before <code>true</code> if more info will be printed after that
   * section
   */
  protected void printAttributesUserDcatMappings(IFeedRecord r, boolean before) {

    String schemaKey = "";
    Map<String, IFeedAttribute> index = r.getData(IFeedRecord.STD_COLLECTION_INDEX);
    IFeedAttribute schemaKeyAttr = index.get("sys.schema.key");
    if (schemaKeyAttr != null) {
      schemaKey = cleanValue(schemaKeyAttr + "","","",null);
    }
    DcatFields dcatFields = null;
    Set<String> keys = this.dcatSchemas.keySet();
    schemaKey = schemaKey.replaceAll("\"", "");
    for (String key : keys) {
	    String[] parts = key.split(",");
	    for(String part : parts){
	      if (part.equalsIgnoreCase(schemaKey)) {
	        dcatFields = this.dcatSchemas.get(schemaKey);
	        break;
	      }
	    }
    }
    if (dcatFields == null) {
      dcatFields = this.dcatSchemas.get("others");
    }
    if (dcatFields == null) {
      return;
    }

    printTab();
    for (DcatField dcatField : dcatFields) {
      before = lookUpFieldFromDcat(r, dcatField, before);
    }
    ResourceLinks links = r.getResourceLinks();
    printLinks(links, false, before);
  }
  
  /**
   * Prints argument.
   *
   * @param argName argument name
   * @param argVal argument value
   * @param more flag to indicate if there will be more arguments
   */
  
  protected  void printArg2(String argName, String argVal, boolean more) {
    argName = Val.chkStr(argName);
    argVal = Val.chkStr(argVal);
    if (argName.length() > 0) {
      println("\"" + Val.escapeStrForJson(argName) + "\"" + sp() + ":" + sp() +   (argVal) +  (more ? "," : ""));
    }
  }

  /**
   * Finds metadata url from resource links
   *
   * @param links the resource links
   * @return the metadata url
   */
  private String getIdentifierUrl(ResourceLinks links) {
    String identifierUrl = "";
    for (int j = 0; j < links.size(); j++) {
      ResourceLink link = links.get(j);
      if (link.getTag().equals(ResourceLink.TAG_METADATA)) {
        identifierUrl = link.getUrl();
        return identifierUrl;
      }
    }
    return identifierUrl;
  }

  /**
   * Prints all links.
   *
   * @param links collection of resource links
   * @param before flag to indicate if there will be more arguments
   */
  protected void printLinks(ResourceLinks links, boolean more, boolean before) {
    if (before) {
      print(false, ",");
      print(false, "\r\n");
    }
    println("\"distribution\"" + sp() + ":" + sp() + "[");
    levelUp();
    boolean moreLinks = false;
    for (int j = 0; j < links.size(); j++) {
      boolean bPrintLink = false;
      String format = "";
      ResourceLink link = links.get(j);
      /*if (link.getTag().equals(ResourceLink.TAG_OPEN)) {
        defaultValues.put("webService", link.getUrl());
      }*/
      if (link.getTag().equals(ResourceLink.TAG_METADATA)) {
        bPrintLink = true;
        if (!moreLinks) {
          moreLinks = true;
        } else {
          moreLinks = false;
        }
        format = "text/xml";
      }
      if (link.getTag().equals(ResourceLink.TAG_DETAILS)) {
        bPrintLink = true;
        if (!moreLinks) {
          moreLinks = true;
        } else {
          moreLinks = false;
        }
        format = "text/html";
      }
      if (bPrintLink) {
        printLink(link, moreLinks, format);
      }
    }
    levelDown();
    println("]" + (more ? "," : ""));
  }

  /**
   * Prints a link.
   *
   * @param link resource link
   * @param more flag to indicate if there will be more arguments
   */
  protected void printLink(ResourceLink link, boolean more, String format) {
    if (!link.getTag().isEmpty() && !link.getUrl().isEmpty()) {
      println("{");
      levelUp();
      printLinkArg("accessURL", link.getUrl(), true);
      printLinkArg("format", format, false);
      levelDown();
      println("}" + (more ? "," : ""));
    }
  }

  /**
   * Prints argument.
   *
   * @param argName argument name
   * @param argVal argument value
   * @param more flag to indicate if there will be more arguments
   */
  protected void printLinkArg(String argName, String argVal, boolean more) {
    argName = Val.chkStr(argName);
    argVal = Val.chkStr(argVal);
    if (argName.length() > 0) {
      println("\"" + argName + "\"" + sp() + ":" + sp() + "\"" + Val.escapeStrForJson(argVal) + "\"" + (more ? "," : ""));
    }
  }

  /**
   * Cleans value from lucene index
   *
   * @param value value to clean
   * @return cleaned value
   */
  private String cleanValue(String value, String type, String dcatFieldName,DcatField dcatField) {
	 String delimiter = ", ";
	 String dateFormat = "";
	 int maxChars = -1;
	 if(dcatField != null){		 
		 String df = dcatField.getDateFormat();
		 if(df.length() > 0){
			 dateFormat = df;
		 }
		 int mc = dcatField.getMaxChars();
		 if(mc > -1){
			 maxChars = mc;
		 }
	 }
    if (value == null) {
      return "";
    }
    if ("null".equals(value)) {
      return "";
    }
    if(type.equalsIgnoreCase("date") || dcatFieldName.equalsIgnoreCase("spatial")) { 
      value = value.replaceAll("\"", "");
    }
    if (value.startsWith("[")) {
      value = value.replace("[", "");
      if(type.equalsIgnoreCase("string")){
        value = value.replaceAll("\",\"", ",");       
      }
    }
    if (value.endsWith("]")) {
      value = value.replace("]", "");
    }
        
    // only one webService url
    if(dcatFieldName.equalsIgnoreCase("webService") && value != null && value.length() > 0){
	    String[] parts = value.split(",http");
	    if(parts != null && parts.length > 0){
	    	value = parts[0];
	    	if(!value.startsWith("\"")){
	    		value = "\"" + Val.escapeStrForJson(value);
	    	}
	    	if(!value.endsWith("\"")){
	    		value += "\""; 
	    	}
	    }
    }
        
    if (value != null && value.length() > 0){
	    if(type.equalsIgnoreCase("date")) {
	    	int firstIdx = value.indexOf(",");
	    	if(firstIdx > -1){
	    		value = value.substring(0,firstIdx);
	    	}
	    	value = parseDateTime(value);
	    	if(dateFormat.length() > 0){
	    		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    		Date dt = null;
				try {
                    dt = new IsoDateFormat().parseObject(value);
                    if (dt!=null) {
                      value = sdf.format(dt);
                    } else {
                      value = "";
                    }
                    
				} catch (ParseException e) {
                    value = "";
                }
	    	}
	    }else if(type.equalsIgnoreCase("array")) {
	    	value = value.replace("\"", "").replace("\"", "");
	    	String[] parts = value.split(",");
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("[");
	    	boolean hasValue = false;
            HashMap<String, String> repKeyword = new HashMap<String, String>();
	    	for (String part : parts){
                part = part.trim();
                if (part.isEmpty()) continue;
                String partTrimUpper = part.toUpperCase();
                if (!part.startsWith("\"") && !part.endsWith("\"")) {
                    if ((!dcatFieldName.equalsIgnoreCase("keyword")) || (!repKeyword.containsKey(partTrimUpper))) {
                        repKeyword.put(partTrimUpper, partTrimUpper);
                        if (hasValue) {
                          sb.append(delimiter);
                        }

                        sb.append("\"").append(Val.escapeStrForJson(part)).append("\"");
                        hasValue = true;
                    }
                }
	    	}	    		    
	    	sb.append("]");
	    	value = sb.toString();
	    }
	    
	    if(type.equalsIgnoreCase("string")){
		    if(maxChars > -1 && value.length() > maxChars){		    	
              value = Val.escapeStrForJson(value.substring(0,maxChars));
		    }
	    }
    }    
    return value;
  }

  /**
   * Writes fields value in response.
   *
   * @param fieldValue the lucene field value
   * @param jsonKey the dcat field
   * @param delimiter the delimiter in lucene field
   * @param before the indentation flag
   * @return always <code>true</code>
   */
  private boolean writeFieldValue(String fieldValue, String jsonKey, String delimiter, boolean before,DcatField dcatField) {
    String cleanedVal = cleanValue(fieldValue,"",jsonKey,dcatField);
    if (before) {
      print(false, ",");
      print(false, "\r\n");
    }
    print(before, "\"" + jsonKey + "\"" + sp() + ":" + sp() +  cleanedVal);
    before = true;
    return before;
  }

  /**
   * Writes fields value to response using field mappings.
   *
   * @param index the lucene index records
   * @param fieldName the lucene field
   * @param jsonKey the dcat field
   * @param delimiter the delimiter in lucene field
   * @param before the indentation flag
   * @param isDate true if date field
   * @return always <code>true</code>
   */
  private boolean writeField(IFeedRecord r,Map<String, IFeedAttribute> index,DcatField dcatField, String delimiter, boolean before) {
	String indexFieldName = dcatField.getIndex();
	String dcatFieldName = dcatField.getName();
	String fieldType = dcatField.getType();
    String fldValues = "";
    String[] flds = indexFieldName.split(",");
    for (String fld : flds) {
      IFeedAttribute indexValue = index.get(fld);
      String val = "";
      if (indexValue == null) {
    	  if(dcatField.isRequired()){
    		  if(dcatFieldName.equalsIgnoreCase("accessURL")){
    			  ResourceLinks links = r.getResourceLinks();
    			  for (int j = 0; j < links.size(); j++) {
    			      ResourceLink link = links.get(j);
    			      if (link.getTag().equals(ResourceLink.TAG_METADATA)) { 			       
    			    	  val = link.getUrl();
    			    	  break;
    			      }
    			  }
    		  }else{
    			  val =  defaultValues.get(dcatFieldName);
    		  }
    	  }else{
    		  continue;
    	  }
      }else{
    	  val = "" + indexValue.simplify();
    	  if(dcatFieldName.equalsIgnoreCase("format") && val.equalsIgnoreCase("[\"unknown\"]")){
    		  val =  defaultValues.get(dcatFieldName);
    	  }
      }
      String cleanedVal = cleanValue(val,fieldType,dcatFieldName,dcatField);
      if(dcatFieldName.equalsIgnoreCase("dataDictionary") && !(cleanedVal.startsWith("http://") || cleanedVal.startsWith("https://"))){
    	  continue;
      }
     
      if(!fldValues.contains(cleanedVal)){     
    	  if (fldValues.length() > 0) {
    		  StringBuilder sb = new StringBuilder();
    		  if(fieldType.equalsIgnoreCase("array")){
    			  if(!cleanedVal.equalsIgnoreCase(defaultValues.get(dcatFieldName))){
	    			  if(fldValues.startsWith("[") && fldValues.endsWith("]")){
	    				  fldValues = fldValues.replace("[", "").replace("]", "");
	    			  }
	    			  if(cleanedVal.startsWith("[") && cleanedVal.endsWith("]")){
	    				  cleanedVal = cleanedVal.replace("[", "").replace("]", "");
	    			  }    			  
	    			  sb.append("[").append(fldValues).append(delimiter).append(cleanedVal).append("]");
	    			  fldValues = sb.toString();
    			  }
    		  }else{
    			  if(fldValues.startsWith("\"") && fldValues.endsWith("\"")){
    				  fldValues = fldValues.replace("\"", "").replace("\"", "");
    			  }
    			  if(cleanedVal.startsWith("\"") && cleanedVal.endsWith("\"")){
    				  cleanedVal = cleanedVal.replace("\"", "").replace("\"", "");
    			  }
    			  sb.append("\"").append(fldValues).append(delimiter).append(cleanedVal).append("\"");
    			  fldValues = sb.toString();
    		  }
	      }else{
	    	  fldValues += cleanedVal;
	      }
      }
    }
    if (fldValues.length() == 0) {
      fldValues = defaultValues.get(dcatFieldName);
      if (fldValues == null) {
        fldValues = "";
      }
    }
    if (fldValues.length() > 0) {
      if (fieldType.equalsIgnoreCase("array")) {
    	fldValues = fldValues.replaceAll(",", ", ");
      }
      if (before) {
        print(false, ",");
        print(false, "\r\n");
      }
      if(!fldValues.startsWith("\"") && !fldValues.startsWith("[") && !fldValues.endsWith("\"") && !fldValues.endsWith("]")){
    	  fldValues = "\"" + fldValues + "\"";
      }
      print(before, "\"" + dcatFieldName + "\"" + sp() + ":" + sp()  + fldValues);
      before = true;
    }
    return before;
  }

  /**
   * Parses a date/time string.
   *
   * @param dateTime the date/time
   * @return the corresponding time
   * @throws IllegalArgumentException if the input does not conform
   */
  private String parseDateTime(String dateTime) {
    dateTime = Val.chkStr(dateTime);
    String lc = dateTime.toLowerCase();
    if(lc.contains(",")) return dateTime;
    if (lc.equals("*")) {
      return "";
    } else if (lc.equals("now") || lc.equals("present")) {
      return "";
    } else if (lc.equals("unknown")) {
      return "";
    } else {

      Calendar calendar = null;
      String s = dateTime;
      if (s.startsWith("-")) {
        s = s.substring(1);
      }
      if (s.length() >= "1000000000".length()) {
        boolean bChkMillis = true;
        char[] ca = s.toCharArray();
        for (char c : ca) {
          if (!Character.isDigit(c)) {
            bChkMillis = false;
            break;
          }
        }
        if (bChkMillis) {
          try {
            long l = Long.valueOf(dateTime);
            calendar = new GregorianCalendar();
            calendar.setTimeInMillis(l);
          } catch (NumberFormatException nfe) {
            calendar = null;
          }
        }
      }
      if (calendar == null) {
        calendar = TpUtil.parseIsoDateTime(dateTime);
      }
      return TpUtil.printIsoDateTime(calendar);
    }
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
    String xmlField = "sys.xml";
    if (attrName.startsWith(xmlField) && (outFieldsSet == null || !outFieldsSet.contains(attrName))) {
      return false;
    }

    // must pass banned predicate; 'banned' is a regular expression which competly
    // hides a field from reading - even specifying it in outFields will not let it pass
    if (predicateBanned != null && predicateBanned.matcher(attrName).matches()) {
      return false;
    }

    // check if user requested specified fields through oufFields; if specified
    // and the current attribute is not on that list, don't print it
    if (outFieldsSet != null && !outFieldsSet.contains(attrName)) {
      return false;
    }


    return true;
  }

  /**
   * Builds collection of names of fields for output.
   *
   * @return set of fields names.
   */
  @Override
  protected Set<String> buildOutFieldsSet() {
    String outFields = "title,summary,id,updated,envelope.full,keywords.ref,publisher,updatedate,owner,docuuid,acl,dataTheme";
    Set<String> outFieldsSet = new TreeSet<String>(Arrays.asList(outFields.split(",")));
    return outFieldsSet;
  }
}

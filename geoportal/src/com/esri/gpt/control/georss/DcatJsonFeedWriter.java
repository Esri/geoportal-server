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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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
import com.esri.gpt.framework.util.Val;

/**
 * Writes Dcat json response of search results using dcat mappings.
 */
public class DcatJsonFeedWriter extends ExtJsonFeedWriter {

	private HashMap<String,String> defaultValues =  new HashMap<String,String>();
	private DcatFields dcatfields;
	
	protected DcatJsonFeedWriter(HttpServletRequest request,
			RequestContext context, PrintWriter writer, RestQuery query,
			Boolean pretty) {
		super(request, context, writer, query, pretty);
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
            
    String sTitle = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.title"));
    String sDescription = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.description"));
    String sKeyword = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.keyword"));
    String sPublisher = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.publisher"));    
    String sPerson = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.person"));
    String sMbox = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.mbox"));
    String sIdentifier = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.identifier"));
    String sAccessLevel = normalizeResource(messageBroker.retrieveMessage("catalog.json.dcat.accessLevel"));
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
    defaultValues.put("publisher", sPublisher);
    defaultValues.put("person", sPerson);
    defaultValues.put("mbox", sMbox);
    defaultValues.put("identifier", "");
    defaultValues.put("accessLevel", sAccessLevel);
    defaultValues.put("dataDictionary", sDataDictionary);
    defaultValues.put("acessURL", sAccessUrl);
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
    printArg("keyword", sKeyword, true);
    printArg("modified", DF.format(new Date()), true);   
    printArg("publisher", sPublisher, true);
    printArg("person", sPerson, true);
    printArg("mbox", sMbox, true);
    printArg("identifier", sIdentifier, true);
    printArg("accessLevel", sAccessLevel, true);
    printArg("dataDictionary", sDataDictionary, true);
    
    if(sAccessUrl.length() > 0){    	
    	printLinkArg("accessURL", sAccessUrl, true);   
    }
     else if (query != null) {     
    	 printLinkArg("accessURL", query.getRssProviderUrl(), true);  
     }else{    	
    	 printLinkArg("accessURL", "", true);
     }
    
    if(sWebService.length() > 0){    	
    	printLinkArg("webService", sWebService, true);
    }
     else if (query != null) {     
    	 printLinkArg("webService", query.getRssSourceUrl(), true);     
     }else{    	
      printArg("webService", "", true);
     }
     
    printArg("format", sFormat, true);
    printArg("license", sLicense, true);    
    printArg("spatial", sSpatial, true);
    printArg("temporal", sTemporal, false);
     
    levelDown();
    println(records.size() > 0 ? "}," : "}");
       
    printRecords(records, true);

    levelDown();
    println("]");
  }
	
	private String normalizeResource(String s){
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
      List<Envelope> envelopes = new ArrayList<Envelope>();
      for (IFeedRecord r : records) {
        envelopes.add(r.getEnvelope());
      }
      
      this.dcatfields = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getDcatFields();
      for (int i = 0; i < records.size(); i++) {
        printRecord(records.get(i), envelopes.get(i), i < records.size() - 1);
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

    printAttributes(r, true);

    levelDown();
    println("}" + (more ? "," : ""));
  }
	
	/**
	 * Looks up dcat mapping field for a give a record
	 * and writes response.
	 * @param r the record
	 * @param name the name of dcat field
	 * @param before the indentation boolean cursor
	 * @return the indentation boolean cursor
	 */
	private boolean lookUpFieldFromDcat(IFeedRecord r,String name,boolean before){
		String COMMA = ",";
		String schemaKey = "";
    Map<String, IFeedAttribute> index = r.getData(IFeedRecord.STD_COLLECTION_INDEX);
    IFeedAttribute schemaKeyAttr = index.get("sys.schema.key");
    if(schemaKeyAttr != null){
    	schemaKey = cleanValue(schemaKeyAttr + "");    	
    }
		StringBuilder nameSchemaKey = new StringBuilder();
		nameSchemaKey.append(name).append("-").append(schemaKey);
		String key = "";
		DcatField dcatField = null;
		Set<String> keyset = this.dcatfields.keySet();
		for(String k : keyset){
			if(k.contains(nameSchemaKey.toString())){
				key = nameSchemaKey.toString();
				dcatField = this.dcatfields.get(k);
				break;
			}
		}
		if(key.length() == 0 && dcatField == null && this.dcatfields.containsKey(name)){
			key = name;
			dcatField = this.dcatfields.get(key);
		}		
		if(key.length() > 0){			
    	if(dcatField.isVisible()){
    		before = writeField(index,dcatField.getIndex(),dcatField.getName(),COMMA,before,dcatField.isDate());
    	}
    }else if(name.equalsIgnoreCase("identifier") && key.length() == 0){
    	ResourceLinks links = r.getResourceLinks();
    	before = writeFieldValue(getIdentifierUrl(links),"identifier",COMMA,before); 
    }
		return before;
	}
	
	/**
   * Prints attributes using dcat mappings
   * @param r record
   * @param more <code>true</code> if more info will be printed after that
   * section
   */
  protected void printAttributesUserDcatMappings(IFeedRecord r, boolean more) {

    boolean before = false;
    before = lookUpFieldFromDcat(r,"title",before);
    before = lookUpFieldFromDcat(r,"abstract",before);
    before = lookUpFieldFromDcat(r,"keyword",before);
    before = lookUpFieldFromDcat(r,"modified",before);
    before = lookUpFieldFromDcat(r,"publisher",before);
    before = lookUpFieldFromDcat(r,"person",before);
    before = lookUpFieldFromDcat(r,"mbox",before);
    before = lookUpFieldFromDcat(r,"identifier",before);                  
    before = lookUpFieldFromDcat(r,"accessLevel",before);   
    before = lookUpFieldFromDcat(r,"dataDictionary",before);
    
    ResourceLinks links = r.getResourceLinks();
    printLinks(links, true, before);    
    before = false;
    
    before = lookUpFieldFromDcat(r,"webService",before);
    before = lookUpFieldFromDcat(r,"format",before);
    before = lookUpFieldFromDcat(r,"license",before);
    before = lookUpFieldFromDcat(r,"spatial",before);
    before = lookUpFieldFromDcat(r,"temporal",before);    
    print(false, "\r\n");
  }
  
  /**
   * Prints attributes.
   * @param r record
   * @param more <code>true</code> if more info will be printed after that
   * section
   */
  protected void printAttributes(IFeedRecord r, boolean more) {
  	
  	if(this.dcatfields.size() > 0){
  		printAttributesUserDcatMappings(r, more);
  		return;
  	}
  	
    Map<String, IFeedAttribute> index = r.getData(IFeedRecord.STD_COLLECTION_INDEX);

    boolean before = false;
    String COMMA = ",";
    String SPACE = " ";

    if (checkAttr("title")) {
      before = printAttr(before, "title", cleanValue(r.getTitle()));
    }
    if (checkAttr("summary")) {
      before = printAttr(before, "abstract", cleanValue(r.getAbstract()));
    }
    
    before = writeField(index,"keywords,dataTheme,apisoTopicCategory","keyword",COMMA,before,false);
    
    if (checkAttr("dateModified") && r.getModfiedDate() instanceof Date) {
      before = printAttr(before, "modified", DF.format(r.getModfiedDate()));
    }
    
    before = writeField(index,"apiso.OrganizationName,publisher","publisher",COMMA, before,false);
    
    before = writeField(index,"dcat.person","person",COMMA,before,false);
    before = writeField(index,"dcat.mbox","mbox",COMMA,before,false);
    ResourceLinks links = r.getResourceLinks();
    boolean  isIso = false;
    IFeedAttribute schemaKey = index.get("sys.schema.key");
    if(schemaKey != null){
    	String sk = cleanValue(schemaKey + "");
    	if(sk.equalsIgnoreCase("iso-19115")||sk.equalsIgnoreCase("iso-19119")){
    		before = writeField(index,"fileIdentifier","identifier",COMMA,before,false);
    		isIso = true;
    	}else{    
      	before = writeFieldValue(getIdentifierUrl(links),"identifier",COMMA,before);      	
      }
    }else{    
    	before = writeFieldValue(getIdentifierUrl(links),"identifier",COMMA,before);
    }
 
    before = writeField(index,"dcat.accessLevel","accessLevel",COMMA,before,false);
    before = writeField(index,"dcat.dataDictionary","dataDictionary",COMMA,before,false);
    printLinks(links, true, before);
    before = false;
    before = writeField(index,"resource.url","webService",COMMA,before,false);
    
    before = writeField(index,"contentType", "format",COMMA,before,false);
    before = writeField(index,"dcat.license","license",COMMA,before,false);
    before = writeField(index,"envelope.minx,envelope.miny,envelope.maxx,envelope.maxy","spatial",SPACE,before,false);
    if(isIso){    	
    	before = writeField(index,"apiso.TempExtent_begin,apiso.TempExtent_end","temporal",COMMA,before,true);
    } else {
    	before = writeField(index,"timeperiod.l.0,timeperiod.u.0","temporal",COMMA,before,true);
    }
  
    print(false, "\r\n");

  }
	
  /**
   * Finds metadata url from resource links
   * @param links the resource links
   * @return the metadata url
   */
	private String getIdentifierUrl(ResourceLinks links){
		 String identifierUrl = "";
		 for (int j = 0; j<links.size(); j++) {
			 ResourceLink link = links.get(j);
			 if(link.getTag().equals(ResourceLink.TAG_METADATA)){
				 identifierUrl = link.getUrl();
				 return identifierUrl;
			 }
		 }
		 return identifierUrl;
	}
	
	/**
	 * Prints all links.
	 * @param links collection of resource links
	 * @param before flag to indicate if there will be more arguments
	 */
	protected void printLinks(ResourceLinks links, boolean more, boolean before) {
		if (before) {
			print(false, ",");
			print(false, "\r\n");
		}
	  println("\"distribution\"" +sp()+ ":" +sp()+ "[");
	  levelUp();
	  for (int j = 0; j<links.size(); j++) {
	  	boolean bPrintLink = false;
	  	String format = "";
	    ResourceLink link = links.get(j);
	    if(link.getTag().equals(ResourceLink.TAG_OPEN)){
	    	 defaultValues.put("webService", link.getUrl());
	    }
	    if(link.getTag().equals(ResourceLink.TAG_METADATA)){
	    	 bPrintLink = true;
		     format = "xml";
	    }
	    if(link.getTag().equals(ResourceLink.TAG_DETAILS)){
	    	bPrintLink = true;
	    	format = "html";
	    }
	    if(bPrintLink){
	    	printLink(link, j<links.size()-1,format);
	    }
	  }
	  levelDown();
	  println("]" + (more ? "," : ""));
	}
	
	/**
	 * Prints a link.
	 * @param link resource link
	 * @param more flag to indicate if there will be more arguments
	 */
	protected void printLink(ResourceLink link, boolean more,String format) {
	  if (!link.getTag().isEmpty() && !link.getUrl().isEmpty()) {
	    println("{");
	    levelUp();
	    printLinkArg("accessURL", link.getUrl(), true);
	    printLinkArg("format", format, false);
	    levelDown();
	    println("}" + (more? ",": ""));
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
      println("\"" + argName + "\"" + sp() + ":" + sp() + "\"" + argVal + "\"" + (more ? "," : ""));
    }
  }
	
	private String cleanValue(String value){
		if(value == null) return "";
		if(value == "null") return "";
		value = value.replaceAll("\"", "");
		if(value.startsWith("[")){
			value = value.replace("[", "");
		}
		if(value.endsWith("]")){
			value = value.replace("]", "");
		}
		return value;
	}
	
	private boolean writeFieldValue(String fieldValue,String jsonKey, String delimiter, boolean before){
		String cleanedVal = cleanValue(fieldValue);
    if (before) {
      print(false, ",");
      print(false, "\r\n");
    }        
    print(before, "\"" + jsonKey + "\"" + sp() + ":" + sp() + "\"" + cleanedVal + "\"");
    before = true;
    return before;
	}

	private boolean writeField(Map<String, IFeedAttribute> index, String fieldName,String jsonKey, String delimiter, boolean before,
			boolean isDate){
		String fldValues = "";
		String[] flds = fieldName.split(",");
		for(String fld: flds){
			IFeedAttribute indexValue =  index.get(fld);
			if(indexValue == null) continue;
			if(fldValues.length() > 0){
				fldValues += delimiter;
			}
			String cleanedVal = cleanValue("" + indexValue);
			if(cleanedVal != null && cleanedVal.length() >0 && isDate){
				cleanedVal = parseDateTime(cleanedVal);
			}
			fldValues += cleanedVal; 
		}
		if(fldValues.length() == 0){
			fldValues = defaultValues.get(jsonKey);
			if(fldValues == null) fldValues = "";
		}
		if(fldValues.length() > 0){
	    if (before) {
	      print(false, ",");
	      print(false, "\r\n");
	    }        
	    print(before, "\"" + jsonKey + "\"" + sp() + ":" + sp() + "\"" + fldValues + "\"");
	    before = true;
		}
    return before;
	}
	
	/**
   * Parses a date/time string.
   * @param dateTime the date/time
   * @return the corresponding time
   * @throws IllegalArgumentException if the input does not conform
   */
  private String parseDateTime(String dateTime) {
    dateTime = Val.chkStr(dateTime);
    String lc = dateTime.toLowerCase();
    if (lc.equals("*")) {
      return "";
    } else if (lc.equals("now") || lc.equals("present")) {
      return "";
    } else if (lc.equals("unknown")) { 
      return "";
    } else {
      
      Calendar calendar = null;
      String s = dateTime;
      if (s.startsWith("-")) s = s.substring(1);
      if (s.length() >= "1000000000".length()) {
        boolean bChkMillis = true;
        char[] ca = s.toCharArray();
        for (char c: ca) {
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
  @Override
  protected Set<String> buildOutFieldsSet() {
  	String outFields = "title,summary,id,updated,envelope.full,keywords.ref,publisher,updatedate,owner,docuuid,acl,dataTheme";
    Set<String> outFieldsSet =  new TreeSet<String>(Arrays.asList(outFields.split(",")));
    return outFieldsSet;
  }
	

}

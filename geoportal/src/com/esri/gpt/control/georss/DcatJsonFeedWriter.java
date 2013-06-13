package com.esri.gpt.control.georss;

import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;

public class DcatJsonFeedWriter extends ExtJsonFeedWriter {

	private HashMap<String,String> defaultValues =  new HashMap<String,String>();
	
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
   * Prints attributes.
   *
   * @param r record
   * @param more <code>true</code> if more info will be printed after that
   * section
   */
  protected void printAttributes(IFeedRecord r, boolean more) {

    Map<String, IFeedAttribute> index = r.getData(IFeedRecord.STD_COLLECTION_INDEX);
    //Map<String, IFeedAttribute> dbInfo = r.getData(IFeedRecord.STD_COLLECTION_CATALOG);
  
    boolean before = false;
    String COMMA = ",";
    String SPACE = " ";
    String DASH = "-";

    if (checkAttr("title")) {
      before = printAttr(before, "title", cleanValue(r.getTitle()));
    }
    if (checkAttr("summary")) {
      before = printAttr(before, "abstract", cleanValue(r.getAbstract()));
    }
    
    before = writeField(index,"keywords,dataTheme,apisoTopicCategory","keyword",COMMA,before);
    
    if (checkAttr("dateModified") && r.getModfiedDate() instanceof Date) {
      before = printAttr(before, "modified", DF.format(r.getModfiedDate()));
    }
    
    before = writeField(index,"apiso.OrganizationName,publisher","publisher",COMMA, before);
    
    before = writeField(index,"dcat.person","person",COMMA,before);
    before = writeField(index,"dcat.mbox","mbox",COMMA,before);
    ResourceLinks links = r.getResourceLinks();
    before = writeFieldValue(getIdentifierUrl(links),"identifier",COMMA,before);
 
    before = writeField(index,"dcat.accessLevel","accessLevel",COMMA,before);
    before = writeField(index,"dcat.dataDictionary","dataDictionary",COMMA,before);
    printLinks(links, true);

    before = writeField(index,"resource.url","webService",COMMA,before);
    
    before = writeField(index,"contentType", "format",COMMA,before);
    before = writeField(index,"dcat.license","license",COMMA,before);
    before = writeField(index,"envelope.minx,envelope.miny,envelope.maxx,envelope.maxy","spatial",SPACE,before);
    before = writeField(index,"timeperiod.l.0,timeperiod.u.0","temporal",DASH,before);
  
    print(false, "\r\n");

  }
	
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
	 * @param more flag to indicate if there will be more arguments
	 */
	protected void printLinks(ResourceLinks links, boolean more) {
		print(false, ",");
		print(false, "\r\n");
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
	  println("]");
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

	private boolean writeField(Map<String, IFeedAttribute> index, String fieldName,String jsonKey, String delimiter, boolean before){
		String fldValues = "";
		String[] flds = fieldName.split(",");
		for(String fld: flds){
			IFeedAttribute indexValue =  index.get(fld);
			if(indexValue == null) continue;
			if(fldValues.length() > 0){
				fldValues += delimiter;
			}
			String cleanedVal = cleanValue("" + indexValue);
			/*if(cleanedVal != null && cleanedVal.length() >0 && delimiter == "-"){
					cleanedVal =  DF.format(new Date(cleanedVal));
			}*/
			fldValues += cleanedVal; 
		}
		if(fldValues.length() == 0){
			fldValues = defaultValues.get(jsonKey);
			if(fldValues == null) fldValues = "";
		}
		if(fldValues.length() > 0){
			// fldValues = ensureValidSize(fldValues);
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

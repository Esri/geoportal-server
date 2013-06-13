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
package com.esri.gpt.catalog.schema;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.gpt.catalog.arcims.GetDocumentRequest;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.publication.PublicationRecord;
import com.esri.gpt.catalog.publication.PublicationRequest;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.codec.Base64;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;

/**
 * Provides functionality to connect a Schema with a metadata document.
 */
public class MetadataDocument {

  // class variables =============================================================
  private static XsltTemplates XSLTTEMPLATES = new XsltTemplates();
  
  // instance variables ==========================================================
  private String _enclosedXml = "";
  private String _transformedToKnownXml = "";
  private String _wrappingEsriDocId = "";
  private String _sourceUri = "";
  
  // constructors ================================================================
    
  /** Default constructor. */
  public MetadataDocument() {}
  
  // properties ==================================================================
  
  /**
   * Gets the configured schemas.
   * @param requestContext the request context
   * @return the configured schemas
   */
  private Schemas getConfiguredSchemas(RequestContext requestContext) {
    return requestContext.getCatalogConfiguration().getConfiguredSchemas();
  }
  
  // methods =====================================================================
  
  /**
   * Loads, interrogates and evaluates the schema associated with an
   * XML string.
   * @param context the active request context
   * @param xml the document XML string
   * @param checkForEnclosure if true, check the XML for an enclosure
   * @return the evaluated and validated schema
   * @throws SchemaException if a schema related exception occurs
   */
  private Schema evaluateSchema(RequestContext context, 
                                String xml,
                                boolean checkForEnclosure) 
    throws SchemaException {
    Schema schema = null;
    _transformedToKnownXml = "";
    try {
      
      // load and interrogate the document
      Document dom = loadDom(xml,checkForEnclosure);
      Schemas schemas = getConfiguredSchemas(context);
      schema = schemas.interrogate(dom);
      if ((_enclosedXml != null) && (_enclosedXml.length() > 0)) {
        schema.setActiveDocumentXml(_enclosedXml);
      } else {
        schema.setActiveDocumentXml(xml);
      }

      // transform to a known schema if required
      String toKnownSchemaXslt = schema.getInterrogation().getToKnownSchemaXslt();
      if ((toKnownSchemaXslt != null) && (toKnownSchemaXslt.length() > 0)) {
        try {
          XsltTemplate template = getCompiledTemplate(toKnownSchemaXslt);
          DOMSource source = new DOMSource(dom);
          
          StringWriter writer = new StringWriter();
          StreamResult result = new StreamResult(writer);             
          HashMap<String,String> params = new HashMap<String,String>();   
          params.put("currentDate", getDateTime());
          params.put("sourceUrlUuid", 
              "{" + 
              UUID.nameUUIDFromBytes(
                  this._sourceUri.toLowerCase().toString().getBytes())
                    .toString().toUpperCase() + "}");
          params.put("sourceUrl", this._sourceUri.toLowerCase().contains("?f=json")?this._sourceUri.substring(0,this._sourceUri.indexOf("?")):this._sourceUri);
          if(this._sourceUri != null){
            String[] s = this._sourceUri.split("&");
            for(int i=0;i < s.length; i++){
              int serviceIndexStart = s[i].toUpperCase().indexOf("SERVICE=");
              if( serviceIndexStart > -1){
                params.put("serviceType", s[i].substring(serviceIndexStart+8,s[i].length()));             
              }
            }                  
          }
          template.transform(source,result,params);        
          _transformedToKnownXml = Val.chkStr(writer.toString());
          dom = loadDom(_transformedToKnownXml);
          schema = schemas.interrogate(dom);
          schema.setActiveDocumentXml(_transformedToKnownXml);
        } catch (TransformerException e) {
          throw new SchemaException("Unable to transform document with: "+toKnownSchemaXslt,e);
        }
      }
        
      // evaluate
      schema.evaluate(dom);
      
    } catch (XPathExpressionException e) {
      throw new SchemaException("Invalid schema XPath expression.",e);
    }
    return schema;
  }
  
  private String getDateTime() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    Date date = new Date();
    return dateFormat.format(date);
  }
  
  
  /**
   * Gets a compiled XSLT template.
   * @param xsltPath the path to an XSLT
   * @return the compiled template
   * @throws IOException if an IO exception occurs
   * @throws TransformerException if a transformation exception occurs
   * @throws SAXException if a SAX parsing exception occurs
   */
  private synchronized XsltTemplate getCompiledTemplate(String xsltPath)
    throws TransformerException {
    String sKey = xsltPath;
    XsltTemplate template = XSLTTEMPLATES.get(sKey);
    if (template == null) {
      template = XsltTemplate.makeTemplate(xsltPath);
      XSLTTEMPLATES.put(sKey,template);
    }
    return template;
  }
  
  /**
   * Loads an XML string into an XML Document.
   * @param xml the document XML string
   * @return the document
   * @throws SchemaException if the document fails to load
   */
  private Document loadDom(String xml) throws SchemaException {
    try {
      Document dom = DomUtil.makeDomFromString(xml,true);
      return dom;
    } catch (ParserConfigurationException e) {
      throw new SchemaException("Unable to parse document.",e);
    } catch (SAXException e) {
      throw new SchemaException("Unable to parse document.",e);
    } catch (IOException e) {
      throw new SchemaException("Unable to parse document.",e);
    }
  }
  
  /**
   * Loads an XML string into an XML Document.
   * @param xml the document XML string
   * @param checkForEnclosure if true, check the XML for an enclosure
   * @return the document
   * @throws SchemaException if the document fails to load
   */
  private Document loadDom(String xml, boolean checkForEnclosure) 
    throws SchemaException {
    Document dom = loadDom(xml);
    if (checkForEnclosure) {
      String sEnclosedXml = lookForEnclosure(dom);
      if (sEnclosedXml.length() > 0) {
        dom = loadDom(sEnclosedXml);
      }
    }
    return dom;
  }
  
  /**
   * Looks for an enclosed XML document.
   * @param dom the document to check
   * @return the enclosed document's XML string (empty if no enclosure exists)
   */
  private String lookForEnclosure(Document dom) {
    _enclosedXml = "";
    _wrappingEsriDocId = "";
    String sEnclosedXml = "";
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      String sPath = "/metadata/Binary/Enclosure/Data[@SourceMetadata='yes']";
      String sAgsMmdPath = "/metadata/Esri/ArcGISFormat";
      String sAgsMmd = xpath.evaluate(sAgsMmdPath,dom);
      String sEncoded = xpath.evaluate(sPath,dom);
      if ((sEncoded != null) && (sEncoded.length() > 0) && (sAgsMmd == null)) {
        String sDecoded = Val.chkStr(Base64.decode(sEncoded,null));
        sEnclosedXml = sDecoded;
        _wrappingEsriDocId = Val.chkStr(xpath.evaluate("/metadata/Esri/PublishedDocID",dom));
      }
    } catch (XPathExpressionException e) {
      // should never be thrown
    } catch (IOException e) {
      // should never be thrown
    }
    _enclosedXml = sEnclosedXml;
    return sEnclosedXml;
  }
  
  /**
   * Prepares a schema for creation by the metadata editor.
   * @param context the active request context
   * @param schemaKey the key associated with the schema to create
   * @return the new schema
   * @throws SchemaException if a schema related exception occurs
   */
  public Schema prepareForCreate(RequestContext context, 
                                 String schemaKey)
    throws SchemaException {
    Schemas schemas = getConfiguredSchemas(context);
    return schemas.locate(schemaKey);
  }
  
  /**
   * Prepares a document for download by a publisher.
   * @param context the active request context
   * @param publisher the active publisher
   * @param uuid the document UUID
   * @return the document XML
   * @throws ImsServiceException if an ArcIMS communication exception occurs
   * @throws SchemaException if a schema related exception occurs
   */
  public String prepareForDownload(RequestContext context, 
                                   Publisher publisher,
                                   String uuid) 
    throws ImsServiceException, SchemaException {
    
    try {
      GetDocumentRequest imsRequest = new GetDocumentRequest(context,publisher);
      imsRequest.executeGet(uuid);
      String sXml = imsRequest.getXml();
      Document dom = loadDom(sXml); 
      String sEnclosedXml = lookForEnclosure(dom);
      if (sEnclosedXml.length() > 0) {
        return sEnclosedXml;
      } else {
        return sXml;
      }
    } catch (TransformerException e) {
      throw new SchemaException("Unable to transform document.",e);
    }
  }
  
  /**
   * Prepares a document for editing.
   * @param context the active request context
   * @param publisher the active publisher
   * @param uuid the document UUID
   * @return the evaluated schema
   * @throws ImsServiceException if an ArcIMS communication exception occurs
   * @throws SchemaException if a schema related exception occurs
   */
  public Schema prepareForEdit(RequestContext context, 
                               Publisher publisher,
                               String uuid) 
    throws ImsServiceException, SchemaException {
    try {
      GetDocumentRequest imsRequest = new GetDocumentRequest(context,publisher);
      imsRequest.executeGet(uuid);
      String sXml = imsRequest.getXml();
      return evaluateSchema(context,sXml,true);    
    } catch (TransformerException e) {
      throw new SchemaException("Unable to transform document.",e);
    }
  }

  /**
   * Prepares a document for publication.
   * <br/>The schema is loaded, interrogated, evaluated and validated.
   * @param context request context
   * @param record publication record
   * @return the evaluated and validated schema
   * @throws SchemaException if a schema related exception occurs
   */
  public Schema prepareForPublication(RequestContext context, PublicationRecord record)
    throws SchemaException {
    String sXml = record.getSourceXml();
    String status = record.getApprovalStatus();
    boolean isDraft = status.equalsIgnoreCase(MmdEnums.ApprovalStatus.draft.toString());
    this._sourceUri = record.getSourceFileName();

    // evaluate
    Schema schema = evaluateSchema(context,sXml,true);
    if ((_enclosedXml != null) && (_enclosedXml.length() > 0)) {
      sXml = _enclosedXml;
      record.setSourceXml(sXml);
    }
    if ((_transformedToKnownXml != null) && (_transformedToKnownXml.length() > 0)) {
      sXml = _transformedToKnownXml;
      record.setSourceXml(sXml);
    }

    // validate (don't fully validate a draft)
    if (!isDraft) {
      schema.validate();
      if (schema.getXsdLocation().length() > 0) {
        XsdValidator xsdv = new XsdValidator();
        xsdv.validate(schema,sXml);
      } if (schema.getSchematronXslt().length() > 0) {
        SchematronValidator sv = new SchematronValidator();
        sv.validate(schema,sXml);
      }
    } else {
      schema.ensureMinimals();
    }
    return schema;
  }
  
  /**
   * Prepares a document for publication.
   * <br/>The schema is loaded, interrogated, evaluated and validated.
   * @param request publication request
   * @return the evaluated and validated schema
   * @throws SchemaException if a schema related exception occurs
   */
  public Schema prepareForPublication(PublicationRequest request) 
    throws SchemaException {
    return this.prepareForPublication(request.getRequestContext(), request.getPublicationRecord());
  }
  
  /**
   * Prepares a document for viewing.
   * @param context the active request context
   * @param xml the document xml
   * @return the evaluated schema
   * @throws SchemaException if a schema related exception occurs
   */
  public Schema prepareForView(RequestContext context, String xml) 
    throws SchemaException {
    Schema schema = evaluateSchema(context,xml,true);
    schema.ensureMinimals();
    return schema;
  }
  
  /**
   * Prepare xml for full viewing.
   * @param xml the xml to be prepared
   * @return the xml string
   * @throws SchemaException the schema exception
   */
  public String prepareForFullViewing(String xml) throws SchemaException {
  	try{
  		Document dom = loadDom(xml); 
  		String sEnclosedXml = lookForEnclosure(dom);
  		if (sEnclosedXml.length() > 0) {
  			return sEnclosedXml;
  		} else {
  			return xml;
  		}
  	} catch (SchemaException e) {
  		throw new SchemaException("Unable to transform document.",e);
  	}
  }
  
  /**
   * Executes a transformation.
   * @param xml the document XML
   * @param xsltPath the path to the XSLT
   * @return the transformed document
   * @throws TransformerException if an exception occurs
   */
  public String transform(String xml, String xsltPath) throws TransformerException {
    XsltTemplate template = this.getCompiledTemplate(xsltPath);
    String result = template.transform(xml);
    return result;
  }
  
  /**
   * Transforms an metadata document XML to an HTML fragment suitable for display within the
   * Metadata Details page. 
   * @param xml the document xml to transform
   * @param xsltPath the path to the details XSLT for the schema
   * @return the HTML fragment
   * @throws TransformerException
   * @deprecated Instead use transformDetails(String xml, String xsltPath,Locale locale)
   */
  public String transformDetails(String xml, String xsltPath) throws TransformerException {
	XsltTemplate template = this.getCompiledTemplate(xsltPath);	
	return template.transform(xml);
  }
 
  /**
   * Transforms an metadata document XML to an HTML fragment suitable for display within the
   * Metadata Details page. 
   * @param xml the document xml to transform
   * @param detailsXslPath the path to the details XSLT for the schema
   * @param mb the message broker
   * @return the HTML fragment
   * @throws TransformerException
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   * @throws XPathExpressionException 
   */
  public String transformDetails(String xml, String detailsXslPath,MessageBroker mb) throws TransformerException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {
	Document detailsXml = DomUtil.makeDomFromResourcePath(detailsXslPath,true);
	Namespaces namespaces = new Namespaces();
    namespaces.add("xsl","http://www.w3.org/1999/XSL/Transform");	    
	NamespaceContextImpl ns = new NamespaceContextImpl(namespaces);
	XPath xpath = XPathFactory.newInstance().newXPath();
	xpath.setNamespaceContext(ns);
	Node root = (Node) xpath.evaluate("/xsl:stylesheet", detailsXml, XPathConstants.NODE);		
	if(root != null){	
	  NodeList nlTemplates = (NodeList) xpath.evaluate("//xsl:template", root, XPathConstants.NODESET);
	  if(nlTemplates != null){	
		for (int j = 0; j < nlTemplates.getLength(); j++) {
		  Node ndTemplate = nlTemplates.item(j); 
		  if (ndTemplate != null) {
		    NodeList nlWhen = (NodeList) xpath.evaluate("//xsl:when", ndTemplate, XPathConstants.NODESET);
		    if (nlWhen != null) {
			  for (int i = 0; i < nlWhen.getLength(); i++) {
			    Node ndWhen = nlWhen.item(i);        
			    String key = Val.chkStr(ndWhen.getTextContent());
			    if(key.startsWith("i18n.catalog.")){            	            	 
			      String value = mb.retrieveMessage(key.replace("i18n.", ""));
			      ndWhen.setTextContent(value);
			    }
			  }
		    }
		  }
		 }
	   }
	}
           
	XsltTemplate xsl = new XsltTemplate();
	String result =  xsl.transform(XmlIoUtil.domToString(detailsXml), xml, null);
    return result;
  }

}
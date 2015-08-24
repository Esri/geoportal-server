/*
 * Copyright 2014 Esri, Inc..
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
package com.esri.gpt.control.webharvest.client.dcat;

import com.esri.gpt.framework.dcat.dcat.DcatContactPoint;
import com.esri.gpt.framework.dcat.dcat.DcatDistribution;
import com.esri.gpt.framework.dcat.dcat.DcatPublisher;
import com.esri.gpt.framework.dcat.dcat.DcatRecord;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.StringUri;
import com.esri.gpt.framework.util.Val;

import static com.esri.gpt.framework.util.Val.escapeXml;

import com.esri.gpt.server.csw.client.NullReferenceException;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 *
 */
public class DCATRecord extends CommonPublishable {

  private final DcatRecord baseRecord;

  public DCATRecord(DcatRecord baseRecord) {
    this.baseRecord = baseRecord;
  }

  @Override
  public SourceUri getSourceUri() {
    return new StringUri(baseRecord.getIdentifier());
  }

  @Override
  public String getContent() throws IOException, TransformerException, SAXException, NullReferenceException {
	  String theXML = "";
	  
	  theXML = "<?xml version='1.0' encoding='UTF-8'?>" +
				"<rdf:RDF " +
				"	xmlns:pod='http://project-open-data.cio.gov/v1.1/schema' " +
				"	xmlns:dcat='http://www.w3.org/ns/dcat#' " +
				"	xmlns:dct='http://purl.org/dc/terms/' " +
				"	xmlns:dctype='http://purl.org/dc/dcmitype/' " +
				"	xmlns:foaf='http://xmlns.com/foaf/0.1/' " +
				"	xmlns:org='http://www.w3.org/ns/org#' " +
				"	xmlns:ows='http://www.opengis.net/ows' " +
				"	xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' " +
				"	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' " +
				"	xmlns:skos='http://www.w3.org/2004/02/skos/core#' " +
				"	xmlns:vcard='http://www.w3.org/2006/vcard/ns#' " +
				"	xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
				"  <dcat:dataset>" +
				"    <dct:identifier>" + escapeXml(baseRecord.getIdentifier()) + "</dct:identifier>" +
				"	 <dct:title>" + escapeXml(baseRecord.getTitle()) + "</dct:title>" +
				"	 <dct:description>" + escapeXml(baseRecord.getDescription())+ "</dct:description>" +
				getKeywords() +
			    "	 <dct:modified>" + escapeXml(baseRecord.getModified()) + "</dct:modified>" +
				"	 <dct:issued>" + escapeXml(baseRecord.getIssued()) + "</dct:issued>" +
				getPublisher() +
				getContactPoint() +
				"	 <pod:accessLevel>" + escapeXml(baseRecord.getAccessLevel()) + "</pod:accessLevel>" +
                getBureauCodes() +
                getProgramCodes() +
				"	 <pod:dataQuality>" + escapeXml(baseRecord.getDataQuality()) + "</pod:dataQuality>" +
				"	 <pod:primaryITInvestmentUII>" + escapeXml(baseRecord.getPrimaryITInvestmentUII()) + "</pod:primaryITInvestmentUII>" +
				"	 <pod:isPartOf>" + escapeXml(baseRecord.getIsPartOf()) + "</pod:isPartOf>" +
				getDistributions() +
				"	 <dct:license>" + escapeXml(baseRecord.getLicense()) + "</dct:license>" +
				"	 <dct:rights>" + escapeXml(baseRecord.getRights()) + "</dct:rights>" +
				getWGS84BoundingBox() +
                getTemporal() +
				"	 <dct:accrualPeriodicity>" + escapeXml(baseRecord.getAccrualPeriodicity()) + "</dct:accrualPeriodicity>" +
				getLanguages() +
				getThemes() +
			    getReferences() +
			    "    <dcat:landingPage>" + escapeXml(baseRecord.getLandingPage()) + "</dcat:landingPage>" +
			    "    <dcat:describedBy>" + escapeXml(baseRecord.getDescribedBy()) + "</dcat:describedBy>" +
			    "    <dcat:describedByType>" + escapeXml(baseRecord.getDescribedByType()) + "</dcat:describedByType>" +
			    "  </dcat:dataset>" +
				"</rdf:RDF>";
	  
	  
	  
	  return theXML;
  }
  
  private String getLanguages() {
    StringBuilder sb = new StringBuilder();
    for (String language: baseRecord.getLanguages()) {
      sb.append("<dct:language>").append(language).append("</dct:language>");
    }
    return sb.toString();
  }
  
  private String getBureauCodes() {
    StringBuilder sb = new StringBuilder();
    for (String code: baseRecord.getBureauCodes()) {
      sb.append("<pod:bureauCode>").append(code).append("</pod:bureauCode>");
    }
    return sb.toString();
  }
  
  private String getProgramCodes() {
    StringBuilder sb = new StringBuilder();
    for (String code: baseRecord.getProgramCodes()) {
      sb.append("<pod:programCode>").append(code).append("</pod:programCode>");
    }
    return sb.toString();
  }
  
  private String getWGS84BoundingBox() {
	  
	    StringBuilder sb = new StringBuilder();
	    String spatial = escapeXml(baseRecord.getSpatial());
	    String sides[] = spatial.split(",");
	    
	    sb.append("<ows:WGS84BoundingBox>");
	    if (sides.length == 4) {
			sb.append("    <ows:LowerCorner>" + sides[0] + "," + sides[1] + "</ows:LowerCorner>");
			sb.append("    <ows:UpperCorner>" + sides[2] + "," + sides[3]  + "</ows:UpperCorner>");
	    } else {
			sb.append("    <ows:LowerCorner>-180,-90</ows:LowerCorner>");
			sb.append("    <ows:UpperCorner>180,90</ows:UpperCorner>");	    	
	    }
	   	sb.append("</ows:WGS84BoundingBox>");	    	
	    
	    return sb.toString();
  }
  
  /* TODO: need to get publisher as object */
  private String getPublisher() {
	    StringBuilder sb = new StringBuilder();
	    DcatPublisher publisher = baseRecord.getPublisher();
	    
	    if (publisher != null) {
		    sb.append("<dct:publisher>");
	    	sb.append("    <foaf:name>" + publisher.getName() + "</foaf:name>");
	    	sb.append("    <org:subOrganizationOf>" + publisher.getSubOrganizationOf() + "</org:subOrganizationOf>");
		   	sb.append("</dct:publisher>");
	    }
	    
	    return sb.toString();
  }
  
  private String getContactPoint() {
	    StringBuilder sb = new StringBuilder();
	    DcatContactPoint contactPoint = baseRecord.getContactPoint();
	    
	    sb.append("<dcat:contactPoint>");
    	sb.append("    <vcard:fn>" + contactPoint.getName() + "</vcard:fn>");
    	sb.append("    <vcard:hasEmail>" + contactPoint.getMBox() + "</vcard:hasEmail>");
	   	sb.append("</dcat:contactPoint>");
	    
	    return sb.toString();
  }

  private String getDistributions() {
	    StringBuilder sb = new StringBuilder();
	    for (DcatDistribution distribution: baseRecord.getDistribution()) {
	    	sb.append("<dcat:distribution>");
	    	sb.append("    <dcat:downloadURL>" + escapeXml(distribution.getDownloadURL()) + "</dcat:downloadURL>");
	    	sb.append("    <dcat:accessURL>" + escapeXml(distribution.getAccessURL()) + "</dcat:accessURL>");
	    	sb.append("    <dcat:mediaType>" + escapeXml(distribution.getMediaType()) + "</dcat:mediaType>");
	    	sb.append("    <dct:format>" + escapeXml(distribution.getFormat()) + "</dct:format>");
	    	sb.append("    <dct:title>" + escapeXml(distribution.getTitle()) + "</dct:title>");
	    	sb.append("    <dct:description>" + escapeXml(distribution.getDescription()) + "</dct:description>");
	    	sb.append("</dcat:distribution>");
	    }
	    return sb.toString();
  }
  
  private String getThemes() {
	    StringBuilder sb = new StringBuilder();
	    for (String theme: baseRecord.getThemes()) {
	    	if (theme.length()>0) {
	    		sb.append("<dcat:theme>").append(escapeXml(theme)).append("</dcat:theme>");
	        }
	    }
	    return sb.toString();
  }
  
  private String getKeywords() {
    StringBuilder sb = new StringBuilder();
    for (String keyword: baseRecord.getKeywords()) {
    	if (keyword.length()>0) {
    		sb.append("<dcat:keyword>").append(escapeXml(keyword)).append("</dcat:keyword>");
        }
    }
    return sb.toString();
  }
  
  private String getReferences() {
    StringBuilder sb = new StringBuilder();
    for (String reference: baseRecord.getReferences()) {
      if (reference.length()>0) {
    	  sb.append("<dct:references>").append(escapeXml(reference)).append("</dct:references>");
      }
    }
    return sb.toString();
  }
  
  private String getTemporal() {
    StringBuilder sb = new StringBuilder();
    String temporalDef = Val.chkStr(baseRecord.getTemporal());
    String [] temporalRange = temporalDef.split("/");
    for (String temporal: temporalRange) {
      sb.append("<dct:temporal>").append(escapeXml(temporal)).append("</dct:temporal>");
    }
    return sb.toString();
  }

}

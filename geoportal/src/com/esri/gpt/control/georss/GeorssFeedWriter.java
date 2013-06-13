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

import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinkBuilder;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * GEORSS feed writer.
 */
/* package */ class GeorssFeedWriter implements FeedWriter {

// class variables =============================================================
/** update date format */  
private static final DateFormat DF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZ");

// instance variables ==========================================================

/** Message broker */
private MessageBroker _messageBroker;

/** print writer */
private PrintWriter _writer;

/** geometry */
private Geometry _geometry = Geometry.esriGeometryPolygon;

/** links target */
private RecordSnippetWriter.Target _target = RecordSnippetWriter.Target.blank;

/** provider URL */
private String _providerUrl = "";

/** source URL */
private String _sourceUrl = "";

// constructors ================================================================
/**
 * Creates instance of the writer.
 * @param messageBroker message broker
 * @param writer underlying print writer
 * @param sourceUrl source URL
 * @param providerUrl provider URL
 */
public GeorssFeedWriter(MessageBroker messageBroker, PrintWriter writer, 
  String providerUrl, String sourceUrl) {
  _messageBroker = messageBroker;
  if (_messageBroker == null) {
    throw new IllegalArgumentException("A MessageBroker is required.");
  }
  _writer = writer;
  _providerUrl = Val.chkStr(providerUrl);
  _sourceUrl = Val.chkStr(sourceUrl);
}

// properties ==================================================================

/**
 * Gets links target.
 * @return links targets
 */
public RecordSnippetWriter.Target getTarget() {
  return _target;
}

/**
 * Sets links target.
 * @param target links target
 */
public void setTarget(RecordSnippetWriter.Target target) {
  _target = target;
}

/**
 * Gets geometry type.
 * @return geometry type
 */
public Geometry getGeometry() {
  return _geometry;
}

/**
 * Sets geometry type.
 * @param geometry geometry type
 */
public void setGeometry(Geometry geometry) {
  _geometry = geometry;
}


// methods =====================================================================
/**
 * Writers records.
 * @param records records to write
 */
public void write(IFeedRecords records) {
  
  String sTitle = _messageBroker.retrieveMessage("catalog.rest.title");
  String sDescription = _messageBroker.retrieveMessage("catalog.rest.description");
  String sCopyright = _messageBroker.retrieveMessage("catalog.rest.copyright");
  String sGenerator = _messageBroker.retrieveMessage("catalog.rest.generator");
  if (sTitle.startsWith("???")) sTitle = "";
  if (sDescription.startsWith("???")) sDescription = "";
  if (sCopyright.startsWith("???")) sCopyright = "";
  if (sGenerator.startsWith("???")) sGenerator = "";

  // writeTag signature
  _writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  _writer.println("<rss version=\"2.0\""+
                  " xmlns:georss=\"http://www.georss.org/georss\""+
                  " xmlns:media=\"http://search.yahoo.com/mrss/\""+
                  " xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\""+
                  " xmlns:atom=\"http://www.w3.org/2005/Atom\""+
                  " xmlns:geoportal=\"http://www.esri.com/geoportal\"" +
                  ">");

  // writeTag channel
  _writer.println("<channel>");

  // writeTag header
  if (sTitle.length() > 0) {
    writeTag("title", sTitle);
  }
  if (sDescription.length() > 0) {
    writeTag("description", sDescription);
  }
  
  if (_providerUrl.length()>0) {
    this.writeTag("link", _providerUrl);
  }
  
  // docs, category, copyright, generator
  writeTag("docs", "http://www.rssboard.org/rss-specification");
  writeTag("category", "GeoRss" );
  if (sCopyright.length()>0) {
    writeTag("copyright", sCopyright);
  }
  if (sGenerator.length()>0) {
    writeTag("generator", sGenerator);
  }
  
  // emails
  ApplicationContext appCtx = ApplicationContext.getInstance();
  ApplicationConfiguration appCfg = appCtx.getConfiguration();
  String sEmail = appCfg.getMailConfiguration().getIncomingToAddress();
  if (sEmail.length()>0) {
    writeTag("managingEditor", sEmail);
    writeTag("webMaster", sEmail);
  }
  
  // add OpenSearch response elements
  OpenSearchProperties osProps = records.getOpenSearchProperties();
  if (osProps != null) {
    String osTitle = Val.escapeXml(osProps.getShortName());
    StringBuffer osQuery = new StringBuffer();
    osQuery.append(" role=\"request\"");
    osQuery.append(" start=\"1\"");
    osQuery.append(" searchTerms=\"data\"");
    
    _writer.println("<atom:link rel=\"search\" type=\"application/opensearchdescription+xml\""+
        " href=\""+osProps.getDescriptionURL()+"\" title=\""+osTitle+"\"/>");
    _writer.println("<opensearch:totalResults>"+osProps.getNumberOfHits()+"</opensearch:totalResults>");
    _writer.println("<opensearch:startIndex>"+osProps.getStartRecord()+"</opensearch:startIndex>");
    _writer.println("<opensearch:itemsPerPage>"+osProps.getRecordsPerPage()+"</opensearch:itemsPerPage>");
    //_writer.println("<opensearch:Query "+osQuery.toString()+"/>");
    //<opensearch:Query role="request" searchTerms="New York History" startPage="1" />
  }

  // write Atom 'self' link
  if (_sourceUrl.length()>0) {
    _writer.println("<atom:link href=\"" +Val.escapeXml(_sourceUrl)+ "\" rel=\"self\" type=\"application/rss+xml\" />");
  }
  
  // writeTag items
  RecordSnippetWriter snippetWriter = new RecordSnippetWriter(_messageBroker, _writer);
  snippetWriter.setTarget(_target);
  for (IFeedRecord record : records) {
    writeRecord(snippetWriter, record, sTitle);
  }

  // complete data
  _writer.println("</channel>");
  _writer.println("</rss>");
}

/**
 * Writes a single record.
 * @param snippetWriter description HTML snippet writer
 * @param record records to writeTag
 * @param sSourceTitle source title
 */
private void writeRecord(
  RecordSnippetWriter snippetWriter, 
  IFeedRecord record,
  String sSourceTitle) {
  
  _writer.println("<item>");

  // link
  String sMetadataLink = record.getResourceLinks().findUrlByTag(ResourceLink.TAG_METADATA);
  String sEncodedLink = Val.escapeXml(sMetadataLink);

  // guid
  if (sEncodedLink.length() > 0) {
    writeTag("link", sMetadataLink);
    _writer.print("<guid isPermaLink=\"true\">");
    _writer.print(sEncodedLink);
    _writer.println("</guid>");
  }

  // title
  String sTitle = record.getTitle();
  if (sTitle.length() == 0) {
    sTitle = record.getUuid();
  }
  writeTag("title", sTitle);
  
  // pub date
  if (record.getModfiedDate()!=null) {
    writeTag("pubDate", DF.format(record.getModfiedDate()));
  }
  
  // source
  if (_sourceUrl.length()>0) {
    _writer.print("<source url=\"");
    _writer.print(Val.escapeXml(_sourceUrl));
    _writer.print("\">");
    _writer.print(Val.escapeXml(sSourceTitle));
    _writer.println("</source>");
  }

  // description
  _writer.println("<description><![CDATA[");
  snippetWriter.write(record);
  _writer.println("]]></description>");


  // content type (category)
  if (record.getContentType().length() > 0) {
    writeTag("category", record.getContentType());
  }

  // thumbnail (media)
  String url = Val.chkUrl(record.getResourceLinks().getThumbnail().getUrl());
  if (url.length() > 0) {
    _writer.print("<media:content url=\"");
    _writer.print(Val.escapeXml(url));
    _writer.println("\"/>");
    writeTag("media:text", sTitle);
  }

  // record type thumbnail
  ResourceLink contentTypeLink = record.getResourceLinks().findByTag(ResourceLink.TAG_CONTENTTYPE);
  String recordTypeThumbnail = Val.chkUrl(contentTypeLink!=null? contentTypeLink.getUrl(): "");
  if (url.length()>0 || recordTypeThumbnail.length() > 0) {
    _writer.print("<media:thumbnail url=\"");
    _writer.print(Val.escapeXml(url.length()>0? url: recordTypeThumbnail));
    _writer.println("\"/>");
  }
  
  // write geometry
  writeGeometry(record);

  // resource URL
  ResourceLink openRL = record.getResourceLinks().findByTag(ResourceLink.TAG_OPEN);
  if (openRL!=null) {
    String serviceType = Val.chkStr(openRL.getParameters().getValue(ResourceLinkBuilder.RESOURCE_TYPE));
      _writer.print("<geoportal:resourceUrl");
      if (serviceType.length()>0) {
        _writer.print(" " +ResourceLinkBuilder.RESOURCE_TYPE+ "=\"");
        _writer.print(Val.escapeXml(serviceType));
        _writer.print("\"");
      }
      _writer.print(">");
      _writer.print(Val.escapeXml(openRL.getUrl()));
      _writer.println("</geoportal:resourceUrl>");
  }

  _writer.println("</item>");
  _writer.flush();
}

/**
 * Writes geometry.
 * @param envelope envelope
 */
private void writeGeometry(IFeedRecord record) {
  if (record.getEnvelope()!=null && record.getEnvelope().isValid()) {
    switch (getGeometry()) {
      case esriGeometryPoint:
        writePoint(record.getEnvelope());
        break;
      default:
      case esriGeometryPolygon:
        writePolygon(record.getEnvelope());
        break;
      case esriGeometryEnvelope:
        writeBox(record.getEnvelope());
        break;
    }
  }
}

/**
 * Writes simple attribute.
 * @param name attribute name
 * @param value attribute value
 */
private void writeTag(String name, String value) {
  _writer.print("<" + name + ">");
  _writer.print(Val.escapeXml(value));
  _writer.println("</" + name + ">");
}

/**
 * Writes geometry as esriGeometryPoint.
 * @param envelope envelope
 */
private void writePoint(Envelope envelope) {
  _writer.print("<georss:point>");
  _writer.print(getValidCenterY(envelope) + " " + getValidCenterX(envelope));
  _writer.println("</georss:point>");
}

/**
 * Writes geometry as esriGeometryPolygon.
 * @param envelope envelope
 */
private void writePolygon(Envelope envelope) {
  _writer.print("<georss:polygon>");
  _writer.print(getValidMaxY(envelope) + " " + envelope.getMinX());
  _writer.print(" ");
  _writer.print(getValidMaxY(envelope) + " " + envelope.getMaxX());
  _writer.print(" ");
  _writer.print(getValidMinY(envelope) + " " + envelope.getMaxX());
  _writer.print(" ");
  _writer.print(getValidMinY(envelope) + " " + envelope.getMinX());
  _writer.print(" ");
  _writer.print(getValidMaxY(envelope) + " " + envelope.getMinX());
  _writer.println("</georss:polygon>");
}

/**
 * Writes geometry as esriGeometryPolygon.
 * @param envelope envelope
 */
private void writeBox(Envelope envelope) {
  _writer.print("<georss:box>");
  _writer.print(getValidMinY(envelope) + " " + envelope.getMinX());
  _writer.print(" ");
  _writer.print(getValidMaxY(envelope) + " " + envelope.getMaxX());
  _writer.println("</georss:box>");
}

/**
 * Gets valid center X.
 * @return valid center X
 */
private double getValidCenterX(Envelope envelope) {
  return envelope.getCenterX();
}

/**
 * Gets valid center Y
 * @return valid center Y
 */
private double getValidCenterY(Envelope envelope) {
  return Math.min(+90.0, Math.max(-90.0, envelope.getCenterY()));
}

/**
 * Gets valid min Y.
 * @return valid min Y
 */
private double getValidMinY(Envelope envelope) {
  return Math.min(+90.0, Math.max(-90.0, envelope.getMinY()));
}

/**
 * Gets valid max Y
 * @return valid max Y
 */
private double getValidMaxY(Envelope envelope) {
  return Math.min(+90.0, Math.max(-90.0, envelope.getMaxY()));
}

// enums =======================================================================
/**
 * Geometry
 */
public enum Geometry {

/** esriGeometryPoint */
esriGeometryPoint,
/** esriGeometryPolygon */
esriGeometryPolygon,
/** esriGeometryEnvelope */
esriGeometryEnvelope,
/** lattitude, longitude */
spatial;

/**
 * Checks value given as string.
 * @param value value given as string
 * @return geometry reprsented by the string or {@link Geometry#esriGeometryPolygon} if
 * geometry not recognized
 */
public static Geometry checkValueOf(String value) {
  value = Val.chkStr(value);
  for (Geometry g : values()) {
    if (g.name().equalsIgnoreCase(value)) {
      return g;
    }
  }
  return esriGeometryPolygon;
}
  }
}

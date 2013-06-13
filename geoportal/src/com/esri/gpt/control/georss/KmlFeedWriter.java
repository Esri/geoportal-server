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

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;

/**
 * KML feed writer.
 */
public class KmlFeedWriter implements FeedWriter {

// class variables =============================================================
// instance variables ==========================================================
/** Message broker. */
protected MessageBroker _messageBroker;
/** print writer */
private PrintWriter _writer;
/** links target */
private RecordSnippetWriter.Target _target = RecordSnippetWriter.Target.blank;
/** geometry */
protected Geometry _geometry = Geometry.esriGeometryPolygon;
/** signature provider */
protected KmlSignatureProvider kmlSignatureProvider = new DefaultKmlSignatureProvider();

// constructors ================================================================
/**
 * Creates instance of the writer.
 * @param messageBroker message broker
 * @param writer underlying print writer
 */
public KmlFeedWriter(MessageBroker messageBroker, PrintWriter writer) {
  _messageBroker = messageBroker;
  if (_messageBroker == null) {
    throw new IllegalArgumentException("A MessageBroker is required.");
  }
  _writer = writer;
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

/**
 * Gets KML signature provider.
 * @return KML signature provider
 */
public KmlSignatureProvider getKmlSignatureProvider() {
  return kmlSignatureProvider;
}

/**
 * Sets KML signature provider.
 * @param kmlSignatureProvider KML signature provider
 */
public void setKmlSignatureProvider(KmlSignatureProvider kmlSignatureProvider) {
  this.kmlSignatureProvider = kmlSignatureProvider!=null? kmlSignatureProvider: new DefaultKmlSignatureProvider();
}
// methods =====================================================================
/**
 * Writers records.
 * @param records records to write
 */
public void write(IFeedRecords records) {
  String sTitle = getKmlSignatureProvider().getTitle();
  String sDescription = getKmlSignatureProvider().getDescription();

  // writeTag signature
  _writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  _writer.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");

  // writeTag channel
  _writer.println("<Document>");

  // writeTag header
  if (sTitle.length() > 0) {
    writeTag("name", sTitle);
  }
  writeTag("open", "1");
  if (sDescription.length() > 0) {
    writeTag("description", sDescription);
  }
  writeStyle("main", "7d0000ff");

  // writeTag items
  RecordSnippetWriter snippetWriter =
    new RecordSnippetWriter(_messageBroker, _writer);
  snippetWriter.setTarget(_target);
  for (IFeedRecord record : records) {
    writeRecord(snippetWriter, record);
  }

  // complete data
  _writer.println("</Document>");
  _writer.println("</kml>");
}

/**
 * Writes a single record.
 * @param snippetWriter description HTML snippet writer
 * @param record records to writeTag
 */
protected void writeRecord(
  RecordSnippetWriter snippetWriter, IFeedRecord record) {

  _writer.println("<Placemark>");

  // name
  String sTitle = record.getTitle();
  if (sTitle.length() == 0) {
    sTitle = record.getUuid();
  }
  writeTag("name", sTitle);

  // description
  _writer.println("<description><![CDATA[");
  snippetWriter.write(record);
  _writer.println("]]></description>");

  writeTag("styleUrl", "#main");

  writeGeometry(record);


  _writer.println("</Placemark>");
}

/**
 * Writes geometry.
 * @param record record
 */
protected void writeGeometry(IFeedRecord record) {
  if (record.getEnvelope() != null && record.getEnvelope().isValid()) {
    switch (getGeometry()) {
      case esriGeometryPoint:
        writePoint(record.getEnvelope());
        break;
      default:
      case esriGeometryPolygon:
        writePolygon(record.getEnvelope());
        break;
    }
  }
}

/**
 * Writes simple attribute.
 * @param name attribute name
 * @param value attribute value
 */
protected void writeTag(String name, String value) {
  _writer.print("<" + name + ">");
  _writer.print(Val.escapeXmlForBrowser(value));
  _writer.println("</" + name + ">");
}

/**
 * Writes simple attribute.
 * @param name attribute name
 * @param value attribute value
 */
protected void writeDirectTag(String name, String value) {
  _writer.print("<" + name + ">");
  _writer.print(value);
  _writer.println("</" + name + ">");
}

/**
 * Writes geometry as esriGeometryPoint.
 * @param envelope envelope
 */
private void writePoint(Envelope envelope) {
  _writer.print("<Point><coordinates>");
  if(envelope.getMinX() == envelope.getMaxX() && 
		  envelope.getMinY() == envelope.getMaxY()) {
	_writer.print(envelope.getMinX() + "," + envelope.getMinY() + ",0");  
  } else {
    _writer.print(getValidCenterX(envelope) + "," + getValidCenterY(envelope));
  }
  _writer.println("</coordinates></Point>");
}

/**
 * Writes geometry as esriGeometryPolygon.
 * @param envelope envelope
 */
private void writePolygon(Envelope envelope) {
  if(envelope.getMinX() == envelope.getMaxX() && 
		  envelope.getMinY() == envelope.getMaxY()) {
	writePoint(envelope);
	return;
  }
  _writer.println("<Polygon>");
  writeTag("extrude", "0");
  writeTag("altitudeMode", "clampToGround");
  _writer.println("<outerBoundaryIs>");
  _writer.println("<LinearRing>");
  _writer.println("<coordinates>");
  _writer.println(envelope.getMinX() + "," + getValidMaxY(envelope) + ",0");
  _writer.println(envelope.getMaxX() + "," + getValidMaxY(envelope) + ",0");
  _writer.println(envelope.getMaxX() + "," + getValidMinY(envelope) + ",0");
  _writer.println(envelope.getMinX() + "," + getValidMinY(envelope) + ",0");
  _writer.println(envelope.getMinX() + "," + getValidMaxY(envelope) + ",0");
  _writer.println("</coordinates>");
  _writer.println("</LinearRing>");
  _writer.println("</outerBoundaryIs>");
  _writer.println("</Polygon>");
}

/**
 * Writes style.
 * @param styleName style name
 * @param styleColor style color
 */
private void writeStyle(String styleName, String styleColor) {
  _writer.println("<Style id=\"" + styleName + "\">");
  _writer.println("<LineStyle>");
  _writer.println("<width>1.5</width>");
  _writer.println("</LineStyle>");
  _writer.println("<PolyStyle>");
  _writer.println("<color>" + styleColor + "</color>");
  _writer.println("</PolyStyle>");
  _writer.println("</Style>");
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
esriGeometryPolygon;

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

/**
 * Provides KML signature (header).
 */
public static interface KmlSignatureProvider {
  String getTitle();
  String getDescription();
}

private class DefaultKmlSignatureProvider implements KmlSignatureProvider {
  public String getTitle() {
    return _messageBroker.retrieveMessage("catalog.rest.title");
  }

  public String getDescription() {
    return _messageBroker.retrieveMessage("catalog.rest.description");
}
}

}

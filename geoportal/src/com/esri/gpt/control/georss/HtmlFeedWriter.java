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
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;

/**
 * HtmlFeedWriter
 */
/* package */
class HtmlFeedWriter extends HtmlFragmentFeedWriter {

// class variables =============================================================

// instance variables ==========================================================
/** array of styles URL's */
private String [] _styleUrl = new String[] {};  

// attributes ==================================================================
/**
 * Gets style URL.
 * @return array of styles URL's
 */
public String[] getStyleUrl() {
  return _styleUrl;
}

/**
 * Sets style URL.
 * @param styleUrl array of styles URL's
 */
public void setStyleUrl(String [] styleUrl) {
  _styleUrl = styleUrl!=null? styleUrl: new String[]{};
}
// constructors ================================================================
/**
 * Creates instance of the writer.
 * @param messageBroker message broker
 * @param writer underlying print writer
 */
public HtmlFeedWriter(MessageBroker messageBroker, PrintWriter writer) {
  super(messageBroker, writer);
}

/**
 * Writers records.
 * @param records records to write
 */
@Override
public void write(IFeedRecords records) {
  String sLang = _messageBroker.getLocale().getLanguage();
  String sTitle = _messageBroker.retrieveMessage("catalog.rest.title");
  
  _writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
  _writer.println("<html lang=\"" +sLang+ "\">");
    _writer.println("<head>");
  _writer.println("<title>" +Val.escapeXml(sTitle)+ "</title>");
  for (String style : getStyleUrl()) {
    style = Val.chkStr(style);
    if (style.length()>0) {
      _writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" +Val.escapeXmlForBrowser(style)+ "\"/>");
    }
  }
  _writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
  
  // add OpenSearch response elements
  OpenSearchProperties osProps = records.getOpenSearchProperties();
  if (osProps != null) {
    String osTitle = Val.escapeXml(osProps.getShortName());
    _writer.println("<link rel=\"search\" type=\"application/opensearchdescription+xml\""+
        " href=\""+osProps.getDescriptionURL()+"\" title=\""+osTitle+"\"/>");
    _writer.println("<meta name=\"startIndex\" content=\""+osProps.getStartRecord()+"\"/>");
    _writer.println("<meta name=\"itemsPerPage\" content=\""+osProps.getRecordsPerPage()+"\"/>");
    _writer.println("<meta name=\"totalResults\" content=\""+osProps.getNumberOfHits()+"\"/>");
  }
  
  _writer.println("</head>");
  _writer.println("<body>");
  super.write(records);
  _writer.println("</body>");
  _writer.println("</html>");
}
}

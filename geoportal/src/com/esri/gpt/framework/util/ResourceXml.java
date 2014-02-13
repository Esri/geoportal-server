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
package com.esri.gpt.framework.util;

import java.io.IOException;

import com.esri.gpt.framework.http.HttpClientRequest;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resource XML helper.
 */
public class ResourceXml {

/**
 * Returns resource xml from the response.
 * @param resourceUrl
 * @return resource xml
 * @throws IOException
 */
public String makeResourceXmlFromResponse(String resourceUrl) throws IOException {
  HttpClientRequest client = new HttpClientRequest();
  client.setUrl(resourceUrl);
  String response = Val.chkStr(client.readResponseAsCharacters());
  if (client.getResponseInfo().getResponseCode()!=HttpURLConnection.HTTP_OK) {
    throw new IOException("Invalid response received: "+client.getResponseInfo().getResponseMessage());
  }
  return response;
}

/**
 * makes resource from CSW 'get record by id' response.
 * @param cswResponse CSW response
 * @param about about identifier
 * @return resource string
 */
public String makeResourceFromCswResponse(String cswResponse, String about)  {
  Pattern cswRecordStart = Pattern.compile("<csw:Record>");
  Pattern cswRecordEnd = Pattern.compile("</csw:Record>");
  
  Matcher cswRecordStartMatcher = cswRecordStart.matcher(cswResponse);
  Matcher cswRecordEndMatcher = cswRecordEnd.matcher(cswResponse);
  
  if (cswRecordStartMatcher.find() && cswRecordEndMatcher.find()) {
    String dcResponse = cswResponse.substring(cswRecordStartMatcher.end(), cswRecordEndMatcher.start());
    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dct=\"http://purl.org/dc/terms/\">");
    xml.append("<rdf:Description ");
    if (about.length()>0) {
      xml.append("rdf:about=\"").append(Val.escapeXml(about)).append("\"");
    }
    xml.append(">");
    
    xml.append(dcResponse);
      
    xml.append("</rdf:Description>");
    xml.append("</rdf:RDF>");
    
    return xml.toString();
  }
  
  return cswResponse;
}

/**
 * Creates simple resource XML metadata.
 * @param title title
 * @param resourceUrl resource URL
 * @return simple resource XML metadata
 * @throws IllegalArgumentException if no title provided
 */
public String makeSimpleResourceXml(String title, String resourceUrl) throws IllegalArgumentException {
  title = Val.chkStr(title);
  if (title.length()==0) {
    throw new IllegalArgumentException("No title provided.");
  }
  resourceUrl = Val.chkStr(resourceUrl);
  StringBuffer xml = new StringBuffer();
  xml.append("<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dct=\"http://purl.org/dc/terms/\">");
  xml.append("<rdf:Description ");
  if (resourceUrl.length()>0) {
    xml.append("rdf:about=\"").append(Val.escapeXml(resourceUrl)).append("\"");
  }
  xml.append(">");
  if (resourceUrl.length()>0) {
    xml.append("<dc:identifier>");
    xml.append(Val.escapeXml(resourceUrl));
    xml.append("</dc:identifier>");
  }
  xml.append("<dc:title>");
  xml.append(Val.escapeXml(title));
  xml.append("</dc:title>");
  if (resourceUrl.length()>0) {
    xml.append("<dct:references>");
    xml.append(Val.escapeXml(resourceUrl));
    xml.append("</dct:references>");
  }
  xml.append("</rdf:Description>");
  xml.append("</rdf:RDF>");
  return xml.toString();
}
}

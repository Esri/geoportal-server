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
package com.esri.gpt.framework.xml;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans comments, new-lines and namespaces from an XML string.
 */
public class XmlCleaner {

// class variables =============================================================

// instance variables ==========================================================

/** Default constructor. */
public XmlCleaner() {}

// properties ==================================================================
  
// methods =====================================================================

/**
 * Cleans an XML string.
 * <br/>Comments, new-lines and namespaces are removed.
 * @param xml the string to modify
 * @return the modified string
 */
public String clean(String xml) {
  xml = Val.chkStr(xml);

  // remove new lines
  xml = xml.replaceAll("\r\n","");
  xml = xml.replaceAll("\r","");
  xml = xml.replaceAll("\n","");

  // remove comments
  xml = xml.replaceAll("<!--.*?-->","");

  // remove namespaces
  xml = removeNameSpaces(xml);

  return xml;
}

/**
 * Polishes CDATA sections within an xml string.
 * @param xml the string to modify
 * @return the modified string
 */
public String polishCDataSections(String xml) {
  Pattern pattern;
  Matcher matcher;
  String sMatch = "";
  xml = Val.chkStr(xml);
  xml = xml.replaceAll("\r\n","");
  pattern = Pattern.compile("&lt;!\\[CDATA\\[.*?]]&gt;");
  matcher = pattern.matcher(xml);
  while (matcher.find()) {
    sMatch = matcher.group();
    sMatch = sMatch.replaceFirst("&lt;!\\[CDATA\\[","<![CDATA[");
    sMatch = sMatch.replaceFirst("]]&gt;","]]>");
  }
  if (sMatch.length() > 0) {
    xml = xml.replaceFirst("&lt;!\\[CDATA\\[.*?]]&gt;",sMatch);
  }
  return xml;
}


/**
* Removes namespaces from an xml string.
* @return xml the xml string to modify
*/
private String removeNameSpaces(String xml) {

  // collect name spaces
  Pattern pattern = Pattern.compile("xmlns:\\w.*?=");
  Matcher matcher = pattern.matcher(xml);
  ArrayList<String> nsList = new ArrayList<String>();
  while (matcher.find()) {
    String sMatch = matcher.group();
    sMatch = sMatch.substring(6,sMatch.length()-1);
    if (sMatch.length() > 0) {
      boolean bNotFound = true;
      for (int i=0;i<nsList.size()&& bNotFound;i++) {
        String ns = (String)nsList.get(i);
        if (ns.equals(sMatch)) {
          bNotFound = false;
        }
      }
      if (bNotFound) {
        nsList.add(sMatch);
      }
    }
  }

  // remove default namespaces and doctypes
  xml = xml.replaceAll("xmlns=\".*?\"", "");
  xml = xml.replaceAll("xmlns:.*?=\".*?\"", "");
  xml = xml.replaceAll("xsi:.*?=\".*?\"", "");
  xml = xml.replaceAll("<!DOCTYPE.*?]>", "");
  xml = xml.replaceAll("<!DOCTYPE.*?>", "");

  // remove namespaces have been collected before
  for (int i=0;i< nsList.size();i++) {
    String ns = (String) nsList.get(i) + ":";
    xml = xml.replaceAll(ns,"");
  }

  return xml;
}

}

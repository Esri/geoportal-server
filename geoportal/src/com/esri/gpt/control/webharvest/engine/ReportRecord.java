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
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Report record.
 */
class ReportRecord {

/** source URI */
private String sourceUri = "";
/** validated */
private boolean validated;
/** published */
private boolean published;
/** collection of errors */
private Collection<String> errors = new ArrayList<String>();

/**
 * Creates instance of the record.
 */
public ReportRecord() {
}

/**
 * Creates instance of the record.
 * @param sourceUri source URI
 * @param validated validated flag
 * @param published published flag
 * @param errors errors
 */
public ReportRecord(String sourceUri, boolean validated, boolean published, Collection<String> errors) {
  setSourceUri(sourceUri);
  setValidated(validated);
  setPublished(published);
  setErrors(errors);
}

/**
 * Gets source URI.
 * @return source URI
 */
public String getSourceUri() {
  return sourceUri;
}

/**
 * Sets source URI.
 * @param sourceUri source URI
 */
public void setSourceUri(String sourceUri) {
  this.sourceUri = Val.chkStr(sourceUri);
}

/**
 * Gets validated flag.
 * @return validated flag
 */
public boolean getValidated() {
  return validated;
}

/**
 * Sets validated flag.
 * @param validated validated flag
 */
public void setValidated(boolean validated) {
  this.validated = validated;
}

/**
 * Gets published flag.
 * @return published flag
 */
public boolean getPublished() {
  return published;
}

/**
 * Sets published flag.
 * @param published published flag
 */
public void setPublished(boolean published) {
  this.published = published;
}

/**
 * Gets collection of errors.
 * @return collection of errors
 */
public Collection<String> getErrors() {
  return errors;
}

/**
 * Sets collection of errors.
 * @param errors collection of errors
 */
public void setErrors(Collection<String> errors) {
  this.errors = errors != null ? errors : new ArrayList<String>();
}

/**
 * Provides XML snippet of the record.
 * @return XML snippet of the record
 */
public String toXmlSnippet() {
  StringBuilder sb = new StringBuilder();
  sb.append("<record>\n");
  sb.append("<sourceUri>" +Val.escapeXml(sourceUri)+ "</sourceUri>\n");
  if (!validated) {
    sb.append("<validate>\n");
    sb.append("<status>failed</status>\n");
    for (String error : errors) {
      sb.append("<error>" +Val.escapeXml(error)+ "</error>\n");
    }
    sb.append("</validate>\n");
  } else {
    sb.append("<validate><status>ok</status></validate>\n");
    if (!published) {
      sb.append("<publish>\n");
      sb.append("<status>failed</status>\n");
      for (String error : errors) {
        sb.append("<error>" +Val.escapeXml(error)+ "</error>\n");
      }
      sb.append("</publish>\n");
    } else {
      sb.append("<publish><status>ok</status></publish>\n");
    }
  }
  sb.append("</record>\n");
  return sb.toString();
}
}

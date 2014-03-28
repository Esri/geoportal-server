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

import com.esri.gpt.framework.dcat.dcat.DcatDistribution;
import com.esri.gpt.framework.dcat.dcat.DcatRecord;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.StringUri;
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
    return "<?xml version='1.0' encoding='UTF-8'?>"
      + "<rdf:RDF xmlns:dct='http://purl.org/dc/terms/' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:fo='http://www.w3.org/1999/XSL/Format' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:exslt='http://exslt.org/common' xmlns:rim='urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0' xmlns:dcmiBox='http://dublincore.org/documents/2000/07/11/dcmi-box/' xmlns:ows='http://www.opengis.net/ows'>"
      + "<rdf:Description rdf:about='rdf_about'>"
      + "<dc:title>" + baseRecord.getTitle() + "</dc:title>"
      + "<dc:description>" + baseRecord.getDescription()+ "</dc:description>"
      + "<dct:abstract>" + baseRecord.getAbstract() + "</dct:abstract>"
      + "<dc:format>" + baseRecord.getFormat() + "</dc:format>"
      + "<dct:publisher>" + baseRecord.getPublisher() + "</dct:publisher>"
      + "<dc:identifier>" + baseRecord.getIdentifier() + "</dc:identifier>"
      + getSubjects() +
      "<dct:modified>" + baseRecord.getModified() + "</dct:modified>"
      + getReferences() +
      "<ows:WGS84BoundingBox>" + baseRecord.getSpatial() + "</ows:WGS84BoundingBox>"
      + "</rdf:Description>"
      + "</rdf:RDF>";

  }
  
  private String getSubjects() {
    StringBuilder sb = new StringBuilder();
    for (String keyword: baseRecord.getKeywords()) {
      sb.append("<dc:subject>").append(keyword).append("</dc:subject>");
    }
    return sb.toString();
  }
  
  private String getReferences() {
    StringBuilder sb = new StringBuilder();
    for (DcatDistribution distribution: baseRecord.getDistribution()) {
      String accessURL = distribution.getAccessURL();
      sb.append("<dct:references>").append(accessURL).append("</dct:references>");
    }
    return sb.toString();
  }

}

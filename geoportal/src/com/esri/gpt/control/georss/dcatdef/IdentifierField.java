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
package com.esri.gpt.control.georss.dcatdef;

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Esri, Inc.
 */
public class IdentifierField extends BaseDcatField {

  public IdentifierField(String fldName) {
    super(fldName);
  }

  public IdentifierField(String fldName, long flags) {
    super(fldName, flags);
  }
  
  protected String readValue(IFeedAttribute attr) {
    return attr.simplify().getValue().toString();
  }
  
  protected String readLink(ResourceLinks links) {
    for (ResourceLink l: links) {
      if (l.getTag().equals(ResourceLink.TAG_METADATA)) {
        return l.getUrl();
      }
    }
    return "";
  }
  
  protected String getDefaultValue(Properties properties) {
    return "";
  }

  @Override
  public void print(DcatPrinter printer, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    IFeedAttribute attr = getFeedAttribute(dcatSchemas, r);
    
    String value;
    if (attr==null) {
      if ((flags & OBLIGATORY)!=0) {
        value = getDefaultValue(properties);
      } else {
        return;
      }
    } else {
      value = Val.chkStr(readLink(r.getResourceLinks()), readValue(attr));
    }
    
    printer.printAttribute(getOutFieldName(),value);
  }
  
}

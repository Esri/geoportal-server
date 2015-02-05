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

import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import static com.esri.gpt.framework.util.Val.chkStr;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Identifier field.
 */
public class IdentifierField extends BaseDcatField {

  /**
   * Creates instance of the class.
   * @param fldName field name
   */
  public IdentifierField(String fldName) {
    super(fldName);
  }

  /**
   * Creates instance of the class.
   * @param fldName field name
   * @param flags flags
   */
  public IdentifierField(String fldName, long flags) {
    super(fldName, flags);
  }
  
  /**
   * Reads value as string.
   * @param attr attribute
   * @return value
   */
  protected String readValue(IFeedAttribute attr) {
    return attr.simplify().getValue().toString();
  }

  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    IFeedAttribute attr = getFeedAttribute(dcatSchemas, r);
    
    String value;
    if (attr==null) {
      if ((flags.provide(r, attr, properties) & OBLIGATORY)!=0) {
        value = chkStr(r.getFileIdentifier(), r.getUuid());
      } else {
        return;
      }
    } else {
      value = chkStr(readValue(attr),chkStr(r.getFileIdentifier(), r.getUuid()));
    }
    
    jsonWriter.name(getOutFieldName()).value(value);
  }
  
}

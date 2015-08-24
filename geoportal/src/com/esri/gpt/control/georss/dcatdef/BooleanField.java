/*
 * Copyright 2015 Esri, Inc.
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

import com.esri.gpt.control.georss.DcatField;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Boolean field.
 */
public class BooleanField extends BaseDcatField {

  public BooleanField(String fldName) {
    super(fldName);
  }

  public BooleanField(String fldName, long flags) {
    super(fldName, flags);
  }

  public BooleanField(String fldName, FlagsProvider flags) {
    super(fldName, flags);
  }

  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    Boolean value = eval(properties, dcatSchemas, r);
    if (value!=null) {
      jsonWriter.name(getOutFieldName()).value(value);
    }
  }
  
  /**
   * Reads value.
   * @param dcatSchemas DCAT schemas
   * @param dcatField DCAT field
   * @param r feed records
   * @param attr attribute attribute
   * @return value
   */
  protected String readValue(DcatSchemas dcatSchemas, DcatField dcatField, IFeedRecord r, IFeedAttribute attr) {
    return dcatField.translate(attr.simplify().getValue().toString());
  }
  
  /**
   * Validates value.
   * @param value value to validate
   * @return <code>true</code> if value is valid
   */
  protected boolean validateValue(String value) {
    value = Val.chkStr(value).toLowerCase();
    String [] validValues = {"true", "false"};
    for (String validValue: validValues) {
      if (validValue.equals(value)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Evaluate.
   * @param properties properties
   * @param dcatSchemas schemas
   * @param r record
   * @return value
   */
  public Boolean eval(Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) {
    Map<String, IFeedAttribute> index = getIndex(r);
    if (index == null) {
      return null;
    }
    DcatField field = getAttributeField(dcatSchemas, index, r, fldName);
    if (field == null) {
      return null;
    }
    IFeedAttribute attr = getFeedAttribute(index, field);
    
    String sValue = Val.chkStr(attr!=null? readValue(dcatSchemas, field, r, attr): "");
    if (sValue.isEmpty() || !validateValue(sValue)) {
      return null;
    }
    
    try {
      return Boolean.parseBoolean(sValue);
    } catch (Exception ex) {
      return null;
    }
  }
  
}

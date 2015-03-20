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

import com.esri.gpt.control.georss.DcatField;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Array Field.
 */
public class ArrayField extends BaseDcatField {

  /**
   * Creates instance of the class.
   * @param fldName field name
   */
  public ArrayField(String fldName) {
    super(fldName);
  }

  /**
   * Creates instance of the class.
   * @param fldName field name
   * @param flags flags
   */
  public ArrayField(String fldName, long flags) {
    super(fldName, flags);
  }

  protected ArrayList<String> readValue(DcatSchemas dcatSchemas, DcatField dcatField, IFeedRecord r, IFeedAttribute attr) {
    ArrayList<String> value = new ArrayList<String>();
    if (attr.getValue() instanceof List) {
      try {
        for (IFeedAttribute o : (List<IFeedAttribute>) attr.getValue()) {
          value.add(dcatField.translate(o.simplify().getValue().toString()));
        }
      } catch (ClassCastException ex) {
      }
    } else {
      value.add(dcatField.translate(attr.simplify().getValue().toString()));
    }
    return value;
  }

  protected List<String> getDefaultValue(Properties properties) {
    return new ArrayList<String>();
  }
  
  /**
   * Validates value.
   * @param value value to validate
   * @return <code>true</code> if value is valid
   */
  protected String validateValue(String value) {
    return Val.chkStr(value);
  }

  /**
   * Evaluate array string
   * @param properties properties
   * @param dcatSchemas schemas
   * @param r record
   * @return 
   */
  public List<String> eval(Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) {
    Map<String, IFeedAttribute> index = getIndex(r);
    if (index == null) {
      return null;
    }
    DcatField field = getAttributeField(dcatSchemas, index, r, fldName);
    if (field == null) {
      return null;
    }
    IFeedAttribute attr = getFeedAttribute(index, field);

    ArrayList<String> value = new ArrayList<String>();
    if (attr!=null) {
      for (String val: readValue(dcatSchemas, field, r, attr)) {
        val = validateValue(val);
        if (!val.isEmpty()){
          value.add(val);
        }
      }
    }
    
    if (value.isEmpty()) {
      if ((flags.provide(r, attr, properties) & OBLIGATORY) != 0) {
        value.addAll(getDefaultValue(properties));
      } else {
        return null;
      }
    }
    
    return value;
  }
  
  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    List<String> value = eval(properties, dcatSchemas, r);
    if (value!=null) {
      jsonWriter.name(getOutFieldName()).beginArray();
      for (String val: value) {
        jsonWriter.value(val);
      }
      jsonWriter.endArray();
    }
  }
}

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
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dcat field.
 */
public class DateField extends BaseDcatField {

  private static final Logger LOGGER = Logger.getLogger(DateField.class.getCanonicalName());
  private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

  /**
   * Creates instance of the class.
   *
   * @param fldName field name
   */
  public DateField(String fldName) {
    super(fldName);
  }

  /**
   * Creates instance of the class.
   *
   * @param fldName field name
   * @param flags flags
   */
  public DateField(String fldName, long flags) {
    super(fldName, flags);
  }

  protected Date getDefaultValue(Properties properties) {
    return Calendar.getInstance().getTime();
  }
  
  /**
   * Reads date as date.
   *
   * @param attr attribute
   * @return date
   */
  protected Date readValueAsDate(IFeedAttribute attr) {
    try {
      String value = readValueAsString(attr);
      return new Date(Long.parseLong(value));
    } catch (NumberFormatException ex) {
      return null;
    }
  }
  
  /**
   * Reads date as string.
   *
   * @param attr attribute
   * @return date
   */
  protected String readValueAsString(IFeedAttribute attr) {
    return attr.simplify().getValue().toString();
  }

  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    Map<String, IFeedAttribute> index = getIndex(r);
    if (index == null) {
      return;
    }
    DcatField field = getAttributeField(dcatSchemas, index, r, fldName);
    if (field == null) {
      return;
    }
    IFeedAttribute attr = getFeedAttribute(index, field);
    
    String dateFormat = Val.chkStr(field.getDateFormat(),DEFAULT_DATE_FORMAT);
    SimpleDateFormat DF = new SimpleDateFormat(dateFormat);

    String value;
    try {
      Date defaultDate = getDefaultValue(properties);
      defaultDate = defaultDate!=null? defaultDate: Calendar.getInstance().getTime();
      
      if (attr == null) {
        if ((flags.provide(r, attr, properties) & OBLIGATORY) != 0) {
          value = DF.format(defaultDate);
        } else {
          return;
        }
      } else {
        if ("date".equals(field.getType().toLowerCase())) {
          Date date = readValueAsDate(attr);
          value = DF.format(date!=null? date: defaultDate);
        } else if ("string".equals(field.getType().toLowerCase())) {
          value = readValueAsString(attr);
        } else {
          value = "";
        }
      }
    } catch (IllegalArgumentException ex) {
      LOGGER.log(Level.FINE, "Invalid date format", ex);
      return;
    }

    jsonWriter.name(getOutFieldName()).value(value);
  }

}

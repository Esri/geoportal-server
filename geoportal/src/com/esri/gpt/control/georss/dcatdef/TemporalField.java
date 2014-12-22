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
import com.esri.gpt.control.georss.DcatFields;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedAttribute.FeedList;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Temporal field.
 */
public class TemporalField extends BaseDcatField {
  protected static final IsoDateFormat ISODF = new IsoDateFormat();

  /**
   * Creates instance of the class.
   * @param fldName field name
   */
  public TemporalField(String fldName) {
    super(fldName);
  }

  /**
   * Creates instance of the class.
   * @param fldName field name
   * @param flags flags
   */
  public TemporalField(String fldName, long flags) {
    super(fldName, flags);
  }

  @Override
  protected IFeedAttribute getFeedAttribute(Map<String, IFeedAttribute> index, DcatField field) {
    String[] indexes = field.getIndex().split(",");
    
    ArrayList<IFeedAttribute> list = new ArrayList<IFeedAttribute>();
    for (String idx: indexes) {
      IFeedAttribute attr = index.get(idx);
      if (attr!=null) {
        list.add(attr);
      }
    }
    
    return list.size()==2? new FeedList(list): null;
  }
  
  /**
   * Reads value.
   * @param attr attribute
   * @param field field
   * @return value
   */
  protected List<Date> readValue(IFeedAttribute attr,DcatField field) {
    ArrayList<Date> dates = new ArrayList<Date>();
    List valueList = (List)attr.getValue();
    
    try {
      for (Object v: valueList) {
        String value = ((IFeedAttribute)v).simplify().getValue().toString();
        Date date = new Date(Long.parseLong(value));
        dates.add(date);
      }
    } catch (NumberFormatException ex) {
      
    }
    return dates.size()==2? dates: null;
  }
  
  /**
   * Reads default value.
   * @param properties properties
   * @return default value
   */
  protected List<Date> getDefaultValue(Properties properties) {
    ArrayList<Date> dates = new ArrayList<Date>();
    Date date = Calendar.getInstance().getTime();
    dates.add(date);
    dates.add(date);
    return dates;
  }

  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    Map<String, IFeedAttribute> index = getIndex(r);
    if (index == null) {
      return;
    }
    String schemaKey = getSchemaKey(index);
    if (schemaKey == null) {
      return;
    }
    DcatFields dcatFields = getDcatFields(dcatSchemas, schemaKey);
    if (dcatFields == null) {
      return;
    }
    DcatField field = getDcatField(dcatFields);
    if (field == null) {
      return;
    }
    IFeedAttribute attr = getFeedAttribute(index, field);
    
    List<Date> value;
    if (attr==null) {
      if ((flags & OBLIGATORY)!=0) {
        value = getDefaultValue(properties);
      } else {
        return;
      }
    } else {
      value = readValue(attr,field);
    }
    
    if (value==null) return;
    ArrayList<String>  strDates = new ArrayList<String>();
    for (Date date: value) {
      strDates.add(ISODF.format(date));
    }
    
    String temporal = Val.join(strDates.toArray(new String[strDates.size()]), field.getDelimiter());
    
    jsonWriter.name(getOutFieldName()).value(temporal);
  }
  
}

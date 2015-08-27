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
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Temporal field.
 */
public class TemporalField extends BaseDcatField {
  private static final Logger LOGGER = Logger.getLogger(TemporalField.class.getCanonicalName());
  private static final String DATE_FORMAT = "yyyy-MM-dd";

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
    ArrayList<IFeedAttribute> list = new ArrayList<IFeedAttribute>();
    
    if (field.getIndex().isEmpty()) return null;
    
    List<List<String>> lIndexes = field.getIndex();
    
    top:
    for (List<String> lIndex: lIndexes) {
      for (String idxItem: lIndex) {
        String[] indexes = idxItem.split(",");
        for (String idx: indexes) {
          IFeedAttribute attr = index.get(idx);
          if (attr!=null) {
            list.add(attr);
          }
        }
        if (list.size()!=2) {
          list.clear();
        } else {
          break top;
        }
      }
    }

    /*
    for (String idxItem: field.getIndex().get(0)) {
      String[] indexes = idxItem.split(",");
      for (String idx: indexes) {
        IFeedAttribute attr = index.get(idx);
        if (attr!=null) {
          list.add(attr);
        }
      }
      if (list.size()!=2) {
        list.clear();
      }
    }
    */
    
    return list.size()==2? new FeedList(list): null;
  }

  
  /**
   * Reads value.
   * @param attr attribute
   * @param field field
   * @return value
   */
  protected List<String> readValuesAsString(IFeedAttribute attr,DcatField field) {
    List<String> dates = new ArrayList<String>();
    List valueList = (List)attr.getValue();
    
    for (Object v: valueList) {
      String value = ((IFeedAttribute)v).simplify().getValue().toString();
      if (value!=null) {
        dates.add(value);
      }
    }
    
    return dates.size()==2? dates: null;
  }
  
  /**
   * Reads value.
   * @param attr attribute
   * @param field field
   * @return value
   */
  protected List<Date> readValuesAsDate(IFeedAttribute attr,DcatField field) {
    List<String> sDates = readValuesAsString(attr, field);
    List<Date> dates = new ArrayList<Date>();
    
    for (String sDate: sDates) {
      try {
        Date date = new Date(Long.parseLong(sDate));
        dates.add(date);
      } catch (NumberFormatException ex) {
        
      }
    }
    
    return dates.size()==2? dates: null;
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
        long lValue = Long.parseLong(value);
        if (lValue>=0) {
          Date date = new Date(lValue);
          dates.add(date);
        }
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
    
    List<String> sValue = null;
    List<Date> value = null;
    if (attr==null) {
      if ((flags.provide(r, attr, properties) & OBLIGATORY)!=0) {
        value = getDefaultValue(properties);
      } else {
        return;
      }
    } else {
      if ("date".equals(field.getType().toLowerCase())) {
        value = readValuesAsDate(attr,field);
      } else if ("string".equals(field.getType().toLowerCase())) {
        sValue = readValuesAsString(attr, field);
      }
    }
    
    if (value!=null) {
      String dateFormat = Val.chkStr(field.getDateFormat(),DATE_FORMAT);
      SimpleDateFormat DF = new SimpleDateFormat(dateFormat);
      
      sValue = new ArrayList<String>();
      for (Date date: value) {
        try {
          sValue.add(DF.format(date));
        } catch (IllegalArgumentException ex) {
          LOGGER.log(Level.FINE, "Invalid date format", ex);
        }
      }
    }
    
    if (sValue==null || sValue.isEmpty()) return;
    
    String temporal = Val.join(sValue.toArray(new String[sValue.size()]), field.getDelimiter());
    
    jsonWriter.name(getOutFieldName()).value(temporal);
  }
  
}

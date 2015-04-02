/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.control.georss;

import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Feed attribute.
 */
public interface IFeedAttribute {

  /**
   * Gets value.
   * @return value
   */
  Object getValue();
  /**
   * Gets Esri type.
   * @return Esri type
   */
  String getEsriType();
  /**
   * Gets attribute length.
   * @return attribute length
   */
  int getLength();
  /**
   * Simplifies attribute (if array).
   * @return attribute
   */
  IFeedAttribute simplify();
  /**
   * As list.
   * @return list of values
   */
  List<String> asList();
  /**
   * To flat string
   * @param separator separator
   * @return flat string
   */
  String join(String separator);
  /**
   * Checks if attribute is empty.
   * @return <code>true</code> if value is empty
   */
  boolean isEmpty();
  
  /**
   * Feed attribute factory.
   */
  static class Factory {
    /**
     * Creates attribute.
     * @param value value
     * @param length length
     * @return attribute
     */
    static IFeedAttribute create(String value, int length) {
      return new FeedString(value, length);
    }
    /**
     * Creates attribute.
     * @param value value
     * @param length length
     * @return attribute
     */
    static IFeedAttribute create(Number value, int length) {
      return new FeedNumber(value, length);
    }
    /**
     * Creates attribute.
     * @param value value
     * @param length length
     * @return attribute
     */
    static IFeedAttribute create(Date value, int length) {
      return new FeedDate(value);
    }
    /**
     * Creates attribute.
     * @param value value
     * @param length length
     * @return attribute
     */
    static IFeedAttribute create(Object value, int length) {
      if (value instanceof String) {
        return create((String)value, length);
      }
      if (value instanceof Number) {
        return create((Number)value, length);
      }
      if (value instanceof Date) {
        return create((Date)value, length);
      }
      return new FeedNull();
    }
    
    static IFeedAttribute create(List<IFeedAttribute> list) {
      return new FeedList(list);
    }
    
    public static IFeedAttribute createSum(List<IFeedAttribute> list, String joinOperator) {
      return new SumList(list,joinOperator);
    }
  }
  
  /**
   * Null feed.
   */
  static class FeedNull implements IFeedAttribute {
    @Override
    public String toString() {
      return "\"\"";
    }

    @Override
    public String join(String separator) {
      return "";
    }

    @Override
    public Object getValue() {
      return null;
    }

    @Override
    public String getEsriType() {
      return "esriFieldTypeString";
    }

    @Override
    public int getLength() {
      return 0;
    }

    @Override
    public IFeedAttribute simplify() {
      return this;
    }

    @Override
    public List<String> asList() {
      return Arrays.asList(new String[]{});
    }

    @Override
    public boolean isEmpty() {
      return true;
    }
    
    
  }
  
  /**
   * String feed.
   */
  static class FeedString implements IFeedAttribute {
    private final String value;
    private final int length;
    
    public FeedString(String value, int length) {
      this.value = Val.chkStr(value);
      this.length = length;
    }
    
    @Override
    public String toString() {
    	if(value.startsWith("http")){
    		return "\"" + value + "\"";
    	}else{
    		return "\"" + Val.escapeStrForJson(value) + "\"";
    	}
    }

    @Override
    public String join(String separator) {
      return Val.escapeStrForJson(value);
    }

    @Override
    public Object getValue() {
      return value;
    }

    @Override
    public String getEsriType() {
      return "esriFieldTypeString";
    }

    @Override
    public int getLength() {
      return length;
    }

    @Override
    public IFeedAttribute simplify() {
      return this;
    }

    @Override
    public List<String> asList() {
      return Arrays.asList(new String[]{value});
    }

    @Override
    public boolean isEmpty() {
      return value.isEmpty();
    }
    
  }
  
  /**
   * Number feed.
   */
  static class FeedNumber implements IFeedAttribute {
    private final Number number;
    private final int length;
    
    public FeedNumber(Number number, int length) {
      this.number = number;
      this.length = length;
    }
    
    @Override
    public String toString() {
      return number.toString();
    }

    @Override
    public String join(String separator) {
      return number.toString();
    }

    @Override
    public Object getValue() {
      return number;
    }

    @Override
    public String getEsriType() {
      return number instanceof Double || number instanceof Float? "esriFieldTypeDouble": "esriFieldTypeInteger";
    }

    @Override
    public int getLength() {
      return length;
    }

    @Override
    public IFeedAttribute simplify() {
      return this;
    }

    @Override
    public List<String> asList() {
      return Arrays.asList(new String[]{number.toString()});
    }

    @Override
    public boolean isEmpty() {
      return false;
    }
    
  }
  
  /**
   * Date feed.
   */
  static class FeedDate implements IFeedAttribute {
    private final static IsoDateFormat DF = new IsoDateFormat();
    private final Date date;
    
    public FeedDate(Date date) {
      this.date = date;
    }
    
    @Override
    public String toString() {
      return "\"" + DF.format(date) + "\"";
    }

    @Override
    public String join(String separator) {
      return DF.format(date);
    }

    @Override
    public Object getValue() {
      return date;
    }

    @Override
    public String getEsriType() {
      return "esriFieldTypeDate";
    }

    @Override
    public int getLength() {
      return 20;
    }

    @Override
    public IFeedAttribute simplify() {
      return this;
    }

    @Override
    public List<String> asList() {
      return Arrays.asList(new String[]{DF.format(date)});
    }

    @Override
    public boolean isEmpty() {
      return false;
    }
  }
  
  /**
   * List feed.
   */
  static class FeedList implements IFeedAttribute {
    private final List<IFeedAttribute> list;
    
    public FeedList(List<IFeedAttribute> list) {
      this.list = list;
    }
    
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i=0; i<list.size(); i++) {
        if (i>0) {
          sb.append(",");
        }
        sb.append(list.get(i).toString());
      }
      sb.append("]");
      return sb.toString();
    }
 
    @Override
    public String join(String separator) {
      StringBuilder sb = new StringBuilder();
       for (int i=0; i<list.size(); i++) {
        String subValue = list.get(i).join(separator);
        if (!subValue.isEmpty()) {
          if (sb.length()>0) {
            sb.append(separator);
          }
          sb.append(subValue);
        }
      }
      return sb.toString();
    }

    @Override
    public Object getValue() {
      return list;
    }

    @Override
    public String getEsriType() {
      return "";
    }
    
    @Override
    public int getLength() {
      return list.size();
    }

    @Override
    public IFeedAttribute simplify() {
      switch (list.size()) {
        case 0:
          return new FeedNull();
        case 1:
          return list.get(0);
        default:
          return this;
      }
    }

    @Override
    public List<String> asList() {
      ArrayList<String> lst = new ArrayList<String>();
      for (IFeedAttribute attr: list) {
        lst.addAll(attr.asList());
      }
      return lst;
    }

    @Override
    public boolean isEmpty() {
      for (IFeedAttribute attr: list) {
        if (!attr.isEmpty()) return false;
      }
      return true;
    }
    
    
    
 }
  
  
  static class SumList extends FeedList {
    private final String joinOperator;

    public SumList(List<IFeedAttribute> list, String joinOperator) {
      super(list);
      this.joinOperator = joinOperator!=null? joinOperator: " ";
    }
    
    @Override
    public String toString() {
      return join(joinOperator);
    }

    @Override
    public IFeedAttribute simplify() {
      String strValue = join(joinOperator);
      return new FeedString(strValue, strValue.length());
    }
    
  }
  
}

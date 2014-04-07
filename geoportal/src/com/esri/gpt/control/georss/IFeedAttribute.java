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
      if (value instanceof Object) {
        return new FeedObject(value, length);
      }
      return new FeedNull();
    }
    
    static IFeedAttribute create(List<IFeedAttribute> list) {
      return new FeedList(list);
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
  }
  
  /**
   * String feed.
   */
  static class FeedString implements IFeedAttribute {
    private String value;
    private int length;
    
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
  }
  
  /**
   * Number feed.
   */
  static class FeedNumber implements IFeedAttribute {
    private Number number;
    private int length;
    
    public FeedNumber(Number number, int length) {
      this.number = number;
    }
    
    @Override
    public String toString() {
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
  }
  
  /**
   * Date feed.
   */
  static class FeedDate implements IFeedAttribute {
    private final static IsoDateFormat DF = new IsoDateFormat();
    private Date date;
    
    public FeedDate(Date date) {
      this.date = date;
    }
    
    @Override
    public String toString() {
      return "\"" + DF.format(date) + "\"";
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
  }
  
  /**
   * Object feed.
   */
  static class FeedObject implements IFeedAttribute {
    private Object obj;
    private int length;
    
    public FeedObject(Object obj, int length) {
      this.obj = obj;
    }
    
    @Override
    public String toString() {
      return "\"" + Val.escapeStrForJson(obj.toString()) + "\"";
    }

    @Override
    public Object getValue() {
      return obj;
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
  }
  
  /**
   * List feed.
   */
  static class FeedList implements IFeedAttribute {
    private List<IFeedAttribute> list;
    
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
 }
}

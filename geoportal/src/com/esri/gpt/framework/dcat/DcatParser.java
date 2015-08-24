/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.framework.dcat;

import com.esri.gpt.framework.dcat.adaptors.DcatRecordAdaptor;
import com.esri.gpt.framework.dcat.dcat.DcatRecord;
import com.esri.gpt.framework.dcat.dcat.DcatRecordList;
import com.esri.gpt.framework.dcat.json.JsonArray;
import com.esri.gpt.framework.dcat.json.JsonAttribute;
import com.esri.gpt.framework.dcat.json.JsonAttributes;
import com.esri.gpt.framework.dcat.json.JsonRecord;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DCAT parser.
 */
public class DcatParser {

  private static final Logger LOGGER = Logger.getLogger(DcatParser.class.getCanonicalName());

  private static final Map<String,String> recordRenameMap = new HashMap<String, String>();
  static {
    recordRenameMap.put("rights", "accessLevelComment");
  }

  private static final Map<String,String> distributionRenameMap = new HashMap<String, String>();
  static {
    distributionRenameMap.put("format", "mediaType");
  }

  private final JsonReader jsonReader;
  private DcatVersion version = DcatVersion.DV10;

  /**
   * Creates instance of the parser.
   *
   * @param input input stream to parse
   * @throws java.io.IOException if error reading stream
   */
  public DcatParser(InputStream input) throws IOException {
    this.jsonReader = new JsonReader(new InputStreamReader(input, "UTF-8"));
  }

  /**
   * Creates instance of the parser.
   *
   * @param reader data reader
   */
  public DcatParser(Reader reader) {
    this.jsonReader = new JsonReader(reader);
  }

  /**
   * Gets parsed DCAT version.
   * @return version
   */
  public DcatVersion getDcatVersion() {
    return version;
  }
  
  /**
   * Closes parser.
   * @throws java.io.IOException if error closing reader
   */
  public void close() throws IOException {
    jsonReader.close();
  }

  /**
   * Parses data (event driven style).
   *
   * @param listener event listener
   * @throws DcatParseException if parsing fails
   * @throws java.io.IOException if error reading stream
   */
  public void parse(final Listener listener) throws DcatParseException, IOException {
    ListenerInternal localListener = new ListenerInternal() {
      @Override
      public boolean onRecord(DcatRecord record) {
        LOGGER.log(Level.FINEST, record != null ? record.toString() : "<empty record>");
        listener.onRecord(record);
        return true;
      }
    };
    parse(localListener);
  }

  /**
   * Parses data (DOM building style).
   *
   * @param policy limit policy
   * @return list of records.
   * @throws DcatParseException if parsing fails
   * @throws java.io.IOException if error reading stream
   */
  public DcatRecordList parse(final LimitPolicy policy) throws DcatParseException, IOException {
    final DcatRecordListImpl list = new DcatRecordListImpl();
    DcatParser.ListenerInternal listener = new DcatParser.ListenerInternal() {
      private void parse(final ListenerInternal listener) throws DcatParseException, IOException {
          if (!jsonReader.hasNext()) {
            throw new DcatParseException("No more data available.");
          }
          JsonToken token = jsonReader.peek();
          if (token != JsonToken.BEGIN_ARRAY) {
            throw new DcatParseException("No array found.");
          }

          parseRecords(listener);
      }

      @Override
      public boolean onRecord(DcatRecord record) {
        list.add(record);
        if (policy == null || list.size() >= policy.getLimit()) {
          return true;
        } else {
          policy.onLimit(list);
          return false;
        }
      }
    };
    parse(listener);
    return list;
  }

  /**
   * Parses DCAT using internal listener.
   *
   * @param listener internal listener
   * @throws DcatParseException if parsing DCAT fails
   */
  void parse(final ListenerInternal listener) throws DcatParseException, IOException {
      if (!jsonReader.hasNext()) {
        throw new DcatParseException("No more data available.");
      }

      JsonToken token = jsonReader.peek();
      switch (token) {
        case BEGIN_OBJECT:
          version = DcatVersion.DV11;
          jsonReader.beginObject();
          parseContent(listener);
          break;
        case BEGIN_ARRAY:
          jsonReader.beginArray();
          parseRecords(listener);
          break;
        default:
          throw new DcatParseException("Neither array nor object is found.");
      }
  }
  
  /**
   * Parses entire content of DCAT11.
   * 
   * @param listener internal listener
   * @throws DcatParseException if parsing DCAT fails
   * @throws IOException if reading DCAT fails
   */
  void parseContent(ListenerInternal listener) throws DcatParseException, IOException {
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case NAME:
          String attrName = jsonReader.nextName();
          if (!"dataset".equals(attrName)) {
            if (!jsonReader.hasNext()) {
              throw new DcatParseException("No more data available.");
            }
            jsonReader.skipValue();
          } else {
            JsonToken subToken = jsonReader.peek();
            if (subToken!=JsonToken.BEGIN_ARRAY) {
              throw new DcatParseException("Unexpected token in the data: " + subToken);
            }
            jsonReader.beginArray();
            if (!parseRecords(listener)) {
              return;
            }
          }
          break;
        case BEGIN_OBJECT:
          if (!parseRecords(listener) ) {
            return;
          }
          break;
        case END_DOCUMENT:
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }
    
    jsonReader.endArray();
    jsonReader.endObject();
  }
  
  /**
   * Parses DCAT records using internal listener.
   *
   * @param listener internal listener
   * @throws DcatParseException if parsing DCAT fails
   * @throws IOException if reading DCAT fails
   */
  boolean parseRecords(ListenerInternal listener) throws DcatParseException, IOException {

      while (jsonReader.hasNext()) {
        JsonToken token = jsonReader.peek();
        switch (token) {
          case BEGIN_OBJECT:
            jsonReader.beginObject();
            if (!parseRecord(listener)) {
              return false;
            }
            break;
          case END_DOCUMENT:
            return false;
          default:
            throw new DcatParseException("Unexpected token in the data: " + token);
        }
      }

      jsonReader.endArray();
      return true;
  }

  private boolean parseRecord(ListenerInternal listener) throws DcatParseException, IOException {
    JsonRecord record = new JsonRecord();
    Map<String,String> renameMap = version.compareTo(DcatVersion.DV11)>=0?
      Collections.EMPTY_MAP:recordRenameMap;
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case NAME:
          parseAttribute(record,renameMap);
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }

    jsonReader.endObject();
    return listener.onRecord(new DcatRecordAdaptor(record));
  }

  private void parseAttribute(JsonRecord record,Map<String,String> renameMap) throws DcatParseException, IOException {
    String attrName = jsonReader.nextName();
    attrName = renameMap.get(attrName)!=null? renameMap.get(attrName): attrName;
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case STRING:
          record.put(attrName, new JsonAttribute(jsonReader.nextString()));
          return;
        case NUMBER:
          record.put(attrName, new JsonAttribute(jsonReader.nextDouble()));
          return;
        case BOOLEAN:
          record.put(attrName, new JsonAttribute(jsonReader.nextBoolean()));
          return;
        case BEGIN_ARRAY:
          if ("distribution".equals(attrName)) {
            jsonReader.beginArray();
            parseDistributions(record.getDistribution());
            jsonReader.endArray();
          } else if ("keyword".equals(attrName)) {
            jsonReader.beginArray();
            parsePrimitiveArray(record.getKeywords());
            jsonReader.endArray();
          } else if ("bureauCode".equals(attrName)) {
            jsonReader.beginArray();
            parsePrimitiveArray(record.getBureauCodes());
            jsonReader.endArray();
          } else if ("programCode".equals(attrName)) {
            jsonReader.beginArray();
            parsePrimitiveArray(record.getProgramCodes());
            jsonReader.endArray();
          } else if ("language".equals(attrName)) {
            jsonReader.beginArray();
            parsePrimitiveArray(record.getLanguages());
            jsonReader.endArray();
          } else if ("theme".equals(attrName)) {
            jsonReader.beginArray();
            parsePrimitiveArray(record.getThemes());
            jsonReader.endArray();
          } else if ("references".equals(attrName)) {
            jsonReader.beginArray();
            parsePrimitiveArray(record.getReferences());
            jsonReader.endArray();
          } else {
            // skip
            jsonReader.skipValue();
          }
          return;
        case BEGIN_OBJECT:
          if ("publisher".endsWith(attrName)) {
            jsonReader.beginObject();
            parsePublisher(record);
            jsonReader.endObject();
          } else if ("contactPoint".endsWith(attrName)) {
            jsonReader.beginObject();
            parseContactPoint(record);
            jsonReader.endObject();
          } else {
            jsonReader.skipValue();
          }
          return;
        case NULL:
          jsonReader.nextNull();
          record.put(attrName, new JsonAttribute());
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }
  }
  
  private void parsePublisher(JsonRecord record) throws DcatParseException, IOException {
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case NAME:
          String attrName = jsonReader.nextName();
          if ("name".equals(attrName)) {
            storeAttribute("publisher", record);
          } else {
            jsonReader.skipValue();
          }
          break;
        case END_OBJECT:
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }
  }
  
  private void parseContactPoint(JsonRecord record) throws DcatParseException, IOException {
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case NAME:
          String attrName = jsonReader.nextName();
          if ("fn".equals(attrName)) {
            storeAttribute("contactPoint", record);
          } else if ("hasEmail".equals(attrName)) {
            storeAttribute("mbox", record);
          } else {
            jsonReader.skipValue();
          }
          break;
        case END_OBJECT:
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }
  }
  
  private void parsePrimitiveArray(List<JsonAttribute> attributes) throws DcatParseException, IOException {

    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case END_ARRAY:
          jsonReader.endArray();
          break;
        case STRING:
          attributes.add(new JsonAttribute(jsonReader.nextString()));
          break;
        case NUMBER:
          attributes.add(new JsonAttribute(jsonReader.nextDouble()));
          break;
        case BOOLEAN:
          attributes.add(new JsonAttribute(jsonReader.nextBoolean()));
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }
  }

  private void parseDistributions(JsonArray<JsonAttributes> distributions) throws DcatParseException, IOException {

    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case BEGIN_OBJECT:
          jsonReader.beginObject();
          parseDistribution(distributions);
          jsonReader.endObject();
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }

  }
  
  private void parseDistribution(JsonArray<JsonAttributes> distributions) throws DcatParseException, IOException {
    JsonAttributes attributes = new JsonAttributes();
    Map<String,String> renameMap = version.compareTo(DcatVersion.DV11)>=0?
      Collections.EMPTY_MAP:distributionRenameMap;
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case NAME:
          parseAttribute(attributes,renameMap);
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }

    distributions.add(attributes);
  }

  private void parseAttribute(JsonAttributes attributes,Map<String,String> renameMap) throws DcatParseException, IOException {
    String attrName = jsonReader.nextName();
    attrName = renameMap.get(attrName)!=null? renameMap.get(attrName): attrName;
    if (jsonReader.hasNext()) {
      storeAttribute(attrName, attributes);
    } else {
      throw new DcatParseException("Invalid attribute: " + attrName);
    }
  }
  
  private void storeAttribute(String attrName, JsonAttributes attributes) throws IOException, DcatParseException {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case STRING:
          attributes.put(attrName, new JsonAttribute(jsonReader.nextString()));
          return;
        case NUMBER:
          attributes.put(attrName, new JsonAttribute(jsonReader.nextDouble()));
          return;
        case BOOLEAN:
          attributes.put(attrName, new JsonAttribute(jsonReader.nextBoolean()));
          return;
        case NULL:
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
  }
  
  /**
   * Record listener.
   */
  public static interface Listener {

    /**
     * Called upon a single record parsed.
     *
     * @param record record
     */
    void onRecord(DcatRecord record);
  }

  /**
   * Limit policy.
   */
  public static interface LimitPolicy {

    /**
     * Gets size limit.
     *
     * @return size limit
     */
    long getLimit();

    /**
     * Called when size reached limit.
     *
     * @param recordsList current list of records
     */
    void onLimit(DcatRecordList recordsList);
  }

  static interface ListenerInternal {

    boolean onRecord(DcatRecord record);
  }

  private static class DcatRecordListImpl extends ArrayList<DcatRecord> implements DcatRecordList {
  }
}

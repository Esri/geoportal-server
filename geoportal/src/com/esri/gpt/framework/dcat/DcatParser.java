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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DCAT parser.
 */
public class DcatParser {

  private static final Logger LOGGER = Logger.getLogger(DcatParser.class.getCanonicalName());

  private JsonReader jsonReader;

  /**
   * Creates instance of the parser.
   *
   * @param input input stream to parse
   */
  public DcatParser(InputStream input) throws UnsupportedEncodingException {
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
   * Closes parser.
   */
  public void close() throws IOException {
    jsonReader.close();
  }

  /**
   * Parses data (event driven style).
   *
   * @param listener event listener
   * @throws DcatParseException if parsing fails
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
      if (token != JsonToken.BEGIN_ARRAY) {
        throw new DcatParseException("No array found.");
      }
      jsonReader.beginArray();

      parseRecords(listener);
  }

  /**
   * Parses DCAT records using internal listener.
   *
   * @param listener internal listener
   * @throws DcatParseException if parsing DCAT fails
   */
  void parseRecords(ListenerInternal listener) throws DcatParseException, IOException {

      while (jsonReader.hasNext()) {
        JsonToken token = jsonReader.peek();
        switch (token) {
          case BEGIN_OBJECT:
            jsonReader.beginObject();
            if (!parseRecord(listener)) {
              return;
            }
            break;
          default:
            throw new DcatParseException("Unexpected token in the data: " + token);
        }
      }

      jsonReader.endArray();
  }

  private boolean parseRecord(ListenerInternal listener) throws DcatParseException, IOException {
    JsonRecord record = new JsonRecord();
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case NAME:
          parseAttribute(record);
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }

    jsonReader.endObject();
    return listener.onRecord(new DcatRecordAdaptor(record));
  }

  private void parseAttribute(JsonRecord record) throws DcatParseException, IOException {
    String attrName = jsonReader.nextName();
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
            parseKeywords(record.getKeywords());
            jsonReader.endArray();
          } else {
            // skip
            jsonReader.skipValue();
          }
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }
  }
  
  private void parseKeywords(List<JsonAttribute> keywords) throws DcatParseException, IOException {

    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case END_ARRAY:
          jsonReader.endArray();
          break;
        case STRING:
          keywords.add(new JsonAttribute(jsonReader.nextString()));
          break;
        case NUMBER:
          keywords.add(new JsonAttribute(jsonReader.nextDouble()));
          break;
        case BOOLEAN:
          keywords.add(new JsonAttribute(jsonReader.nextBoolean()));
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
    while (jsonReader.hasNext()) {
      JsonToken token = jsonReader.peek();
      switch (token) {
        case NAME:
          parseAttribute(attributes);
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
    }

    distributions.add(attributes);
  }

  private void parseAttribute(JsonAttributes attributes) throws DcatParseException, IOException {
    String attrName = jsonReader.nextName();
    while (jsonReader.hasNext()) {
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
        default:
          throw new DcatParseException("Unexpected token in the data: " + token);
      }
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

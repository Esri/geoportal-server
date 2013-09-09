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
import com.esri.gpt.framework.dcat.raw.RawDcatArray;
import com.esri.gpt.framework.dcat.raw.RawDcatAttribute;
import com.esri.gpt.framework.dcat.raw.RawDcatAttributes;
import com.esri.gpt.framework.dcat.raw.RawDcatRecord;
import javax.json.stream.JsonParser;
import static javax.json.stream.JsonParser.Event.END_ARRAY;
import static javax.json.stream.JsonParser.Event.START_OBJECT;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.spi.JsonProvider;
import static javax.json.stream.JsonParser.Event.END_OBJECT;
import static javax.json.stream.JsonParser.Event.KEY_NAME;

/**
 * DCAT parser.
 */
public class DcatParser {
  private static final Logger LOGGER = Logger.getLogger(DcatParser.class.getCanonicalName());

  private JsonParser jsonParser;

  /**
   * Creates instance of the parser.
   *
   * @param input input stream to parse
   */
  public DcatParser(InputStream input) {
    this.jsonParser = JsonProvider.provider().createParser(input);
  }

  /**
   * Creates instance of the parser.
   *
   * @param reader data reader
   */
  public DcatParser(Reader reader) {
    this.jsonParser = JsonProvider.provider().createParser(reader);
  }

  /**
   * Closes parser.
   */
  public void close() {
    jsonParser.close();
  }

  /**
   * Parses data (event driven style).
   *
   * @param listener event listener
   * @throws DcatParseException if parsing fails
   */
  public void parse(final Listener listener) throws DcatParseException {
    ListenerInternal localListener = new ListenerInternal() {
      @Override
      public boolean onRecord(DcatRecord record) {
        LOGGER.log(Level.FINEST, record!=null? record.toString(): "<empty record>");
        listener.onRecord(record);
        return true;
      }
    };
    parse(localListener);
  }

  /**
   * Parses data (DOM building style).
   * @param policy limit policy
   * @return list of records.
   * @throws DcatParseException if parsing fails
   */
  public DcatRecordList parse(final LimitPolicy policy) throws DcatParseException {
    final DcatRecordListImpl list = new DcatRecordListImpl();
    DcatParser.ListenerInternal listener = new DcatParser.ListenerInternal() {
      private void parse(final ListenerInternal listener) throws DcatParseException {
        if (!jsonParser.hasNext()) {
          throw new DcatParseException("No more data available.");
        }

        JsonParser.Event event = jsonParser.next();
        if (event != JsonParser.Event.START_ARRAY) {
          throw new DcatParseException("No array found.");
        }

        parseRecords(listener);
      }

      @Override
      public boolean onRecord(DcatRecord record) {
        list.add(record);
        if (policy == null || list.size()>=policy.getLimit()) {
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
   * @param listener internal listener
   * @throws DcatParseException if parsing DCAT fails
   */
  void parse(final ListenerInternal listener) throws DcatParseException {
    if (!jsonParser.hasNext()) {
      throw new DcatParseException("No more data available.");
    }

    JsonParser.Event event = jsonParser.next();
    if (event != JsonParser.Event.START_ARRAY) {
      throw new DcatParseException("No array found.");
    }

    parseRecords(listener);
  }

  /**
   * Parses DCAT records using internal listener.
   * @param listener internal listener
   * @throws DcatParseException if parsing DCAT fails
   */
  void parseRecords(ListenerInternal listener) throws DcatParseException {

    while (jsonParser.hasNext()) {
      JsonParser.Event event = jsonParser.next();
      switch (event) {
        case END_ARRAY:
          return;
        case START_OBJECT:
          if (!parseRecord(listener)) {
            return;
          }
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + event);
      }
    }

    throw new DcatParseException("Unexpected end of data.");
  }

  private boolean parseRecord(ListenerInternal listener) throws DcatParseException {
    RawDcatRecord record = new RawDcatRecord();
    while (jsonParser.hasNext()) {
      JsonParser.Event event = jsonParser.next();
      switch (event) {
        case END_OBJECT:
          return listener.onRecord(new DcatRecordAdaptor(record));
        case KEY_NAME:
          parseAttribute(record);
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + event);
      }
    }

    throw new DcatParseException("Unexpected end of data.");
  }

  private void parseAttribute(RawDcatRecord record) throws DcatParseException {
    String attrName = jsonParser.getString();
    while (jsonParser.hasNext()) {
      JsonParser.Event event = jsonParser.next();
      switch (event) {
        case VALUE_STRING:
          record.put(attrName, new RawDcatAttribute(jsonParser.getString()));
          return;
        case VALUE_NUMBER:
          record.put(attrName, new RawDcatAttribute(jsonParser.getBigDecimal().doubleValue()));
          return;
        case VALUE_FALSE:
          record.put(attrName, new RawDcatAttribute(false));
          return;
        case VALUE_TRUE:
          record.put(attrName, new RawDcatAttribute(true));
          return;
        case START_ARRAY:
          parseDistributions(record.getDistribution());
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + event);
      }
    }
  }

  private void parseDistributions(RawDcatArray<RawDcatAttributes> distributions) throws DcatParseException {

    while (jsonParser.hasNext()) {
      JsonParser.Event event = jsonParser.next();
      switch (event) {
        case END_ARRAY:
          return;
        case START_OBJECT:
          parseDistribution(distributions);
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + event);
      }
    }

    throw new DcatParseException("Unexpected end of data.");
  }

  private void parseDistribution(RawDcatArray<RawDcatAttributes> distributions) throws DcatParseException {
    RawDcatAttributes attributes = new RawDcatAttributes();
    while (jsonParser.hasNext()) {
      JsonParser.Event event = jsonParser.next();
      switch (event) {
        case END_OBJECT:
          distributions.add(attributes);
          return;
        case KEY_NAME:
          parseAttribute(attributes);
          break;
        default:
          throw new DcatParseException("Unexpected token in the data: " + event);
      }
    }

    throw new DcatParseException("Unexpected end of data.");
  }

  private void parseAttribute(RawDcatAttributes attributes) throws DcatParseException {
    String attrName = jsonParser.getString();
    while (jsonParser.hasNext()) {
      JsonParser.Event event = jsonParser.next();
      switch (event) {
        case VALUE_STRING:
          attributes.put(attrName, new RawDcatAttribute(jsonParser.getString()));
          return;
        case VALUE_NUMBER:
          BigDecimal bd = jsonParser.getBigDecimal();
          attributes.put(attrName, new RawDcatAttribute(bd.doubleValue()));
          return;
        case VALUE_FALSE:
          attributes.put(attrName, new RawDcatAttribute(false));
          return;
        case VALUE_TRUE:
          attributes.put(attrName, new RawDcatAttribute(true));
          return;
        default:
          throw new DcatParseException("Unexpected token in the data: " + event);
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
     * @return size limit
     */
    long getLimit();

    /**
     * Called when size reached limit.
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

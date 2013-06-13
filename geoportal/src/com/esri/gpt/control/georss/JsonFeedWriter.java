/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.*;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;
import java.util.Date;

/**
 * JSON feed writer.
 * Writes response in JSON (or pretty JSON) format.
 */
public class JsonFeedWriter implements FeedWriter {

/** tab size */
private final static int TAB_SIZE = 2;
/** ISO date format */
private final static IsoDateFormat DF = new IsoDateFormat();

/** print writer */
private PrintWriter writer;
/** original query */
private RestQuery query;
/** flag indicating if is this a pretty formated JSON */
private boolean pretty;

/** message broker */
private MessageBroker messageBroker;

/** callback */
private String callback = "";

/**
 * Creates instance of the feed.
 * @param writer writer to write feed
 * @param query query
 * @param pretty <code>true</code> to print pretty response
 */
public JsonFeedWriter(PrintWriter writer, RestQuery query, boolean pretty) {
  this.writer = writer;
  this.query = query;
  this.pretty = pretty;
}

/** indentation level */
private int level = 0;

@Override
public void write(IFeedRecords records) {
  String sTitle = messageBroker.retrieveMessage("catalog.rest.title");
  String sDescription = messageBroker.retrieveMessage("catalog.rest.description");
  String sCopyright = messageBroker.retrieveMessage("catalog.rest.copyright");
  String sGenerator = messageBroker.retrieveMessage("catalog.rest.generator");
  if (sTitle.startsWith("???")) sTitle = "";
  if (sDescription.startsWith("???")) sDescription = "";
  if (sCopyright.startsWith("???")) sCopyright = "";
  if (sGenerator.startsWith("???")) sGenerator = "";

  if (!getCallback().isEmpty()) {
    println(getCallback()+"({");
  } else {
    println("{");
  }
  
  levelUp();

  printArg("title", sTitle, true);
  printArg("description", sDescription, true);
  printArg("copyright", sCopyright, true);
  if(query != null){
	  printArg("provider", query.getRssProviderUrl(), true);
  }
  printArg("updated", DF.format(new Date()), true);
  if(query != null){
	  printArg("source", query.getRssSourceUrl(), true);
	  if (records.size()>=query.getFilter().getMaxRecords()) {
	    printArg("more", query.getMoreUrl(), true);
	  }
  }
  OpenSearchProperties osProps = records.getOpenSearchProperties();
  if (osProps!=null) {
    printArg("totalResults", new Long(osProps.getNumberOfHits()), true);
    printArg("startIndex", new Long(osProps.getStartRecord()), true);
    printArg("itemsPerPage", new Long(osProps.getRecordsPerPage()), true);
  }
  printRecords(records, false);

  levelDown();
  
  if (!getCallback().isEmpty()) {
    println("})");
  } else {
    println("}");
  }
}

/**
 * Gets callback.
 * @return callback or empty string if no callback
 */
public String getCallback() {
  return callback;
}

/**
 * Sets callback.
 * @param callback callback name
 */
public void setCallback(String callback) {
  this.callback = Val.chkStr(callback);
}

/**
 * Increases level of indentation.
 */
private void levelUp() {
  level++;
}

/**
 * Decreases level of indentation.
 */
private void levelDown() {
  level--;
}

/**
 * Prints all records.
 * @param records records to print
 * @param more flag to indicate if there will be more arguments
 */
private void printRecords(IFeedRecords records, boolean more) {
  println("\"records\"" +sp()+ ":" +sp()+ "[");
  levelUp();

  for (int i=0; i<records.size(); i++) {
    printRecord(records.get(i), i<records.size()-1);
  }

  levelDown();
  println("]"+(more? ",": ""));
}

/**
 * Prints record.
 * @param r record to print
 * @param more flag to indicate if there will be more arguments
 */
private void printRecord(IFeedRecord r, boolean more) {
  println("{");
  levelUp();

  printArg("title", r.getTitle(), true);
  printArg("id", r.getUuid(), true);
  if (r.getModfiedDate() instanceof Date) {
    printArg("updated", DF.format(r.getModfiedDate()), true);
  }
  printArg("summary", r.getAbstract(), true);
  println("\"bbox\""+sp()+":"+sp()+bbox(r.getEnvelope())+",");

  printGeometry(r.getEnvelope(), true);
  printLinks(r.getResourceLinks(), false);

  levelDown();
  println("}"+(more? ",": ""));
}

/**
 * Prints geometry.
 * @param env geometry
 * @param more flag to indicate if there will be more arguments
 */
private void printGeometry(Envelope env, boolean more) {
  println("\"geometry\"" +sp()+ ":" +sp()+ "{");
  levelUp();
  printArg("type", "Polygon", true);
  println("\"coordinates\"" +sp()+ ":" +sp()+ "[");
  printPolygon(env);
  println("]");
  levelDown();
  println("}"+(more? ",": ""));
}

/**
 * Prints polygon.
 * @param env polygon to print
 */
private void printPolygon(Envelope env) {
  levelUp();
  println("[");
  levelUp();
  println(
    coord(env.getMinX(),env.getMinY())+","+sp() +
    coord(env.getMinX(),env.getMaxY())+","+sp() +
    coord(env.getMaxX(),env.getMaxY())+","+sp() +
    coord(env.getMaxX(),env.getMinY())+","+sp() +
    coord(env.getMinX(),env.getMinY())
    );
  levelDown();
  println("]");
  levelDown();
}

/**
 * Prints all links.
 * @param links collection of resource links
 * @param more flag to indicate if there will be more arguments
 */
private void printLinks(ResourceLinks links, boolean more) {
  println("\"links\"" +sp()+ ":" +sp()+ "[");
  levelUp();
  if (links.getThumbnail()!=null) {
    printLink(links.getThumbnail(), links.size()>0);
  }
  for (int j = 0; j<links.size(); j++) {
    ResourceLink link = links.get(j);
    printLink(link, j<links.size()-1);
  }
  levelDown();
  println("]"+(more? ",": ""));
}

/**
 * Prints a link.
 * @param link resource link
 * @param more flag to indicate if there will be more arguments
 */
private void printLink(ResourceLink link, boolean more) {
  if (!link.getTag().isEmpty() && !link.getUrl().isEmpty()) {
    println("{");
    levelUp();
    printArg("href", link.getUrl(), true);
    printArg("type", link.getTag(), false);
    levelDown();
    println("}" + (more? ",": ""));
  }
}

/**
 * Prints argument.
 * @param argName argument name
 * @param argVal argument value
 * @param more flag to indicate if there will be more arguments
 */
private void printArg(String argName, String argVal, boolean more) {
  argName = Val.chkStr(argName);
  argVal = Val.chkStr(argVal);
  if (argName.length()>0) {
    println("\"" +argName+ "\"" + sp() + ":" + sp() + "\"" +Val.escapeStrForJson(argVal)+ "\""+(more? ",": ""));
  }
}

/**
 * Prints argument.
 * @param argName argument name
 * @param argVal argument value
 * @param more flag to indicate if there will be more arguments
 */
private void printArg(String argName, Number argVal, boolean more) {
  argName = Val.chkStr(argName);
  if (argName.length()>0) {
    println("\"" +argName+ "\"" + sp() + ":" + sp() + argVal + (more? ",": ""));
  }
}

/**
 * Prints a single line. Depending on the 'pretty' flag, line is indencated or not.
 * @param text text to print
 */
private void println(String text) {
  if (pretty) {
    printTab();
    writer.println(text);
  } else {
    writer.print(text);
  }
}

/**
 * Prints tabulator. Tabulator width depends on the indentation level.
 */
private void printTab() {
  for (int i = 0; i < level * TAB_SIZE; i++) {
    writer.print(" ");
  }
}

/**
 * Makes bounding box.
 * @param env bounding box
 * @return bounding box in JSON format
 */
private String bbox(Envelope env) {
  return "[" + Double.toString(env.getMinX()) + "," + sp() + Double.toString(env.getMinY())  + "," + sp() + Double.toString(env.getMaxX()) + "," + sp() + Double.toString(env.getMaxY()) + "]";
}

/**
 * Makes coordinates.
 * @param x x coordinate
 * @param y y coordinate
 * @return coordinate in JSON format
 */
private String coord(double x, double y) {
  return "[" + Double.toString(x) + "," +Double.toString(y) + "]";
}

/**
 * Creates a single space. Depending on the 'pretty' flag, it's either a space or
 * no space at all.
 * @return single space
 */
private String sp() {
  return pretty? " ": "";
}

/**
 * Gets message broker
 * @return the messageBroker
 */
public MessageBroker getMessageBroker() {
  return messageBroker;
}

/**
 * Sets message broker
 * @param messageBroker the messageBroker to set
 */
public void setMessageBroker(MessageBroker messageBroker) {
  this.messageBroker = messageBroker;
}
}

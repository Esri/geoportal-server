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

import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.framework.jsf.MessageBroker;
import java.io.PrintWriter;

/**
 * HTML fragment feed writer.
 */
/* package */ class HtmlFragmentFeedWriter implements FeedWriter {

// class variables =============================================================

// instance variables ==========================================================
  
/** Message broker. */
protected MessageBroker _messageBroker;

/** print writer */
protected PrintWriter _writer;

/** links target */
protected RecordSnippetWriter.Target _target = RecordSnippetWriter.Target.blank;

// constructors ================================================================
/**
 * Creates instance of the writer.
 * @param messageBroker message broker
 * @param writer underlying print writer
 */
public HtmlFragmentFeedWriter(MessageBroker messageBroker, PrintWriter writer) {
  _messageBroker = messageBroker;
  if (_messageBroker == null) {
    throw new IllegalArgumentException("A MessageBroker is required.");
  }
  _writer = writer;
}

// properties ==================================================================

/**
 * Gets links target.
 * @return links targets
 */
public RecordSnippetWriter.Target getTarget() {
  return _target;
}

/**
 * Sets links target.
 * @param target links target
 */
public void setTarget(RecordSnippetWriter.Target target) {
  _target = target;
}


// methods =====================================================================
/**
 * Writers records.
 * @param records records to write
 */
public void write(IFeedRecords records) {
	
  // add OpenSearch response elements as hidden elements
  OpenSearchProperties osProps = records.getOpenSearchProperties();
  if (osProps != null) {
    _writer.println("<input type=\"hidden\" id=\"startIndex\" value=\""+osProps.getStartRecord()+"\"/>");
    _writer.println("<input type=\"hidden\" id=\"itemsPerPage\" value=\""+osProps.getRecordsPerPage()+"\"/>");
    _writer.println("<input type=\"hidden\" id=\"totalResults\" value=\""+osProps.getNumberOfHits()+"\"/>");
  }
  
  // writeTag items
  RecordSnippetWriter snippetWriter = 
    new RecordSnippetWriter(_messageBroker, _writer);
  snippetWriter.setShowTitle(true);
  snippetWriter.setShowIcon(true);
  snippetWriter.setClipText(true);
  snippetWriter.setTarget(_target);
  for (IFeedRecord record : records) {
    writeRecord(snippetWriter, record);
  }
}

/**
 * Writes a single record.
 * @param snippetWriter description HTML snippet writer
 * @param record records to writeTag
 */
protected void writeRecord(
  RecordSnippetWriter snippetWriter, IFeedRecord record) {
  snippetWriter.write(record);
  _writer.flush();
}

}

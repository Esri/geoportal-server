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
package com.esri.gpt.control.webharvest.engine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Record writer.
 */
class RecordWriter extends Writer {

/** logger */
private static final Logger LOGGER = Logger.getLogger(RecordWriter.class.getCanonicalName());

/** writer */
private Writer writer;

/**
 * Creates instance of the writer.
 * @param output underlying output stream to write into
 */
public RecordWriter(OutputStream output) {
  try {
    this.writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
  } catch (UnsupportedEncodingException ex) {
    LOGGER.log(Level.SEVERE, "Error creating record writer", ex);
  }
}

/**
 * Writes single record.
 * @param record record
 * @throws IOException if writing record fails
 */
public void write(ReportRecord record) throws IOException {
  // write basic record information (obligatory)
  writer.write(record.getSourceUri() + ReportBuilder.UNIT_SEPARATOR + record.getValidated() + ReportBuilder.UNIT_SEPARATOR + record.getPublished());
  // write error messages (optional)
  for (String error : record.getErrors()) {
    writer.write("\n");
    writer.write(error);
  }
  // write ASCII record separator
  writer.write(ReportBuilder.RECORD_SEPARATOR+"\n");
}

@Override
public void write(char[] cbuf, int off, int len) throws IOException {
  writer.write(cbuf, off, len);
}

@Override
public void flush() throws IOException {
  writer.flush();
}

@Override
public void close() throws IOException {
  writer.close();
}
}

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

import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Record reader.
 */
class RecordReader extends Reader {

/** logger */
private static final Logger LOGGER = Logger.getLogger(RecordReader.class.getCanonicalName());

/** buffered reader */
private BufferedReader reader;

/**
 * Creates instance of the reader.
 * @param input input stream
 */
public RecordReader(InputStream input) {
  try {
    this.reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
  } catch (UnsupportedEncodingException ex) {
    LOGGER.log(Level.SEVERE, "Error creating record reader", ex);
  }
}

/**
 * Reads single record.
 * @return record or <code>null</code> if no more records
 * @throws IOException if reading record fails
 */
public ReportRecord readRecord() throws IOException {
  // read line, it supposed to be an obligatory part of record
  String line = reader.readLine();
  // no mo records if no line
  if (line==null) return null;
  // check if more records expected
  boolean more = true;
  if (line.endsWith(ReportBuilder.RECORD_SEPARATOR)) {
    more = false;
    line = line.substring(0, line.length()-1);
  }
  // extract basic information
  String [] params = extractBasicInformation(line);

  // create record instance
  ReportRecord record = new ReportRecord();
  record.setSourceUri(params[0]);
  record.setValidated(Val.chkBool(params[1], false));
  record.setPublished(Val.chkBool(params[2], false));

  // read and update errors
  ArrayList<String> errors = new ArrayList<String>();
  while (more) {
    line = reader.readLine();
    if (line==null) break;
    if (line.endsWith(ReportBuilder.RECORD_SEPARATOR)) {
      more = false;
      line = line.substring(0, line.length()-1);
    }
    errors.add(line);
  }

  if (errors.size()>0) {
    record.setErrors(errors);
  }

  return record;
}

@Override
public int read(char[] cbuf, int off, int len) throws IOException {
  return reader.read(cbuf, off, len);
}

@Override
public void close() throws IOException {
  reader.close();
}


/**
 * Extracts basic record information.
 * @param line line of the record entry
 * @return record information (sourceUri,validated,published)
 * @throws IOException if extracting information fails
 */
private String [] extractBasicInformation(String line) throws IOException {
  String [] params = line.split(ReportBuilder.UNIT_SEPARATOR);
  if (params.length!=3) throw new IOException("Invalid record entry.");
  return params;
}

}

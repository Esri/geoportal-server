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

import com.esri.gpt.framework.util.TimePeriod;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Report builder.
 */
class ReportBuilder implements Statistics {
private static final String DETAILS_OPENING_TAG = "<publishDetails>";
private static final String DETAILS_CLOSING_TAG = "</publishDetails>";
//private int DETAILS_OPENING_TAG_LENGTH;
//private int DETAILS_CLOSING_TAG_LENGTH;
//
//{
//  try {
//    DETAILS_OPENING_TAG_LENGTH = DETAILS_OPENING_TAG.getBytes("UTF-8").length;
//    DETAILS_CLOSING_TAG_LENGTH = DETAILS_CLOSING_TAG.getBytes("UTF-8").length;
//  } catch (UnsupportedEncodingException ex) {
//  }
//}

/** record separator */
public static final String RECORD_SEPARATOR = "\u001E";
/** unit separator */
public static final String UNIT_SEPARATOR   = "\u001F";

/** temporary storage file prefix */
private static final String PREFIX = "rep";
/** temporary storage file suffix */
private static final String SUFFIX = ".txt";
/** default sub-folder for the intermediate report files */
private static final String SUBFOLDER = "/geoportal/reports";
/** maximum documents to harvest */
private Integer maxDocToHarvest;
/** maximum documents to report */
private Long maxRepRecords;
/** maximum errors to report */
private Long maxRepErrors;
/** number of harvested records */
private volatile long harvested;
/** number of validated records */
private volatile long validated;
/** number of added records */
private volatile long added;
/** number of updated records */
private volatile long updated;
/** number of deleted records */
private volatile long deleted;
/** last excption */
private Exception exception;
/** directory */
private File directory;
/** storage file */
private File storage;
/** writer */
private RecordWriter writer;
/** start time */
private volatile Date startTime;
/** end time */
private volatile Date endTime;
  
/**
 * Creates instance of the builder.
 * @param directory temporary file directory
 * @param maxDocToHarvest number of maximum documents to harvest or <ocde>null</code> if no limit
 * @param maxRepRecords maximum number of records to report
 * @param maxRepErrors maximum number of errors to report
 * @throws IOException if creating report failed
 */
public ReportBuilder(File directory, Integer maxDocToHarvest, Long maxRepRecords, Long maxRepErrors) throws IOException {
  this.directory = directory;
  this.maxDocToHarvest = maxDocToHarvest;
  this.directory.mkdirs();
  this.storage = File.createTempFile(PREFIX, SUFFIX, directory);
  this.writer = new RecordWriter(new FileOutputStream(storage));
  this.maxRepRecords = maxRepRecords;
  this.maxRepErrors = maxRepErrors;
  
  this.storage.deleteOnExit();
}

/**
 * Creates instance of the builder.
 * @param directory temporary file directory
 * @param maxDocToHarvest number of maximum documents to harvest or <ocde>null</code> if no limit
 * @param maxRepRecords maximum number of records to report
 * @param maxRepErrors maximum number of errors to report
 * @throws IOException if creating report failed
 */
public ReportBuilder(String directory, Integer maxDocToHarvest, Long maxRepRecords, Long maxRepErrors) throws IOException {
  this(new File(directory), maxDocToHarvest, maxRepRecords, maxRepErrors);
}

/**
 * Creates instance of the builder.
 * Directory of the intermediate report files will be stored in the in the
 * <i>"/reports"</i> sub-folder of the system temporary files folder
 * <i>System.getProperty("java.io.tmpdir")</i>.
 * @param maxDocToHarvest number of maximum documents to harvest or <ocde>null</code> if no limit
 * @param maxRepRecords maximum number of records to report
 * @param maxRepErrors maximum number of errors to report
 * @throws IOException if creating report failed
 */
public ReportBuilder(Integer maxDocToHarvest, Long maxRepRecords, Long maxRepErrors) throws IOException {
  this(System.getProperty("java.io.tmpdir")+SUBFOLDER, maxDocToHarvest, maxRepRecords, maxRepErrors);
}

  @Override
public synchronized Date getStartTime() {
  return startTime;
}

/**
 * Sets start time.
 * @param startTime start time
 */
public synchronized void setStartTime(Date startTime) {
  this.startTime = startTime;
}

  @Override
public synchronized Date getEndTime() {
  return endTime;
}

/**
 * Sets end time.
 * @param endTime end time
 */
public synchronized void setEndTime(Date endTime) {
  this.endTime = endTime;
}

  @Override
public synchronized long getDuration() {
  if (getStartTime()==null) return 0;
  return (getEndTime()!=null? getEndTime(): new Date()).getTime() - getStartTime().getTime();
}

  @Override
public synchronized double getPerformance() {

  double harvestedCount = getHarvestedCount();
  double durationInMinutes = (double)getDuration()/60000.0;

  BigDecimal performance = new BigDecimal(durationInMinutes>0? harvestedCount/durationInMinutes: 0);

  return performance.setScale(1,RoundingMode.HALF_UP).doubleValue();
}

/**
 * Creates regular entry.
 * @param sourceUri source URI of the record
 * @param newEntry <code>true</code> if record has been added, <code>false</code> if modified
 * @throws IOException if creating entry fails
 */
public void createEntry(String sourceUri, boolean newEntry) throws IOException {
  this.createEntry(sourceUri, true, true, newEntry, null);
}

/**
 * Creates entry of the invalid record.
 * @param sourceUri source URI of the record
 * @param errors collection of errors
 * @throws IOException if creating entry fails
 */
public void createInvalidEntry(String sourceUri, Collection<String> errors) throws IOException {
  this.createEntry(sourceUri, false, false, false, errors);
}

/**
 * Creates entry of the unpublished record.
 * @param sourceUri source URI of the record
 * @param errors collection of errors
 * @throws IOException if creating entry fails
 */
public void createUnpublishedEntry(String sourceUri, Collection<String> errors) throws IOException {
  this.createEntry(sourceUri, true, false, false, errors);
}

  @Override
public synchronized long getHarvestedCount() {
  return this.harvested;
}

  @Override
public synchronized long getValidatedCount() {
  return this.validated;
}

  @Override
public synchronized long getAddedCount() {
  return this.added;
}

@Override
public synchronized long getModifiedCount() {
  return this.updated;
}

@Override
public synchronized long getDeletedCount() {
  return deleted;
}  

public synchronized void setDeletedCount(long deletedCount) {
  this.deleted = deletedCount;
}

public synchronized Exception getException() {
  return exception;
}

public synchronized void setException(Exception exception) {
  this.exception = exception;
}

@Override
public long getPublishedCount() {
  return getAddedCount() + getModifiedCount();
}

private long getReportRecordsLength(int maxBytes) throws IOException {
  InputStream repo = null;
  try {
    long length = 0;

    FileInputStream input = new FileInputStream(storage);
    RecordReader reader = new RecordReader(input);
    repo = new ReportRecordsInputStream(reader, maxBytes);

    for (int ch = repo.read(); ch>-1; ch = repo.read()) {
      length++;
    }

    return length;
  } finally {
    if (repo!=null) {
      try {
        repo.close();
      } catch (IOException ex) {}
    }
  }
}
/**
 * Creates report stream. Adding new entries will cause exception.
 * @return input stream of the report
 * @throws IOException if creating report fails
 */
public ReportStream createReportStream() throws IOException {
  close();

  FileInputStream input = new FileInputStream(storage);
  RecordReader reader = new RecordReader(input);

  String header = "<?xml version=\"1.0\"?><metadata>";
  String statictsics = getReportStats();
  String footer = "</metadata>";

  byte [] headerB = header.getBytes("UTF-8");
  byte [] statictsicsB = statictsics.getBytes("UTF-8");
  byte [] footerB = footer.getBytes("UTF-8");
  byte [] detailsOpeningB = DETAILS_OPENING_TAG.getBytes("UTF-8");
  byte [] detailsClosingB = DETAILS_CLOSING_TAG.getBytes("UTF-8");

  final int maxBytes = Integer.MAX_VALUE - (headerB.length + statictsicsB.length + footerB.length + detailsOpeningB.length + detailsClosingB.length);
  final long length = headerB.length + statictsicsB.length + getReportRecordsLength(maxBytes) + footerB.length + detailsOpeningB.length + detailsClosingB.length;

  final InputStream[] streams = new InputStream[]{
    new ByteArrayInputStream(headerB),
    new ByteArrayInputStream(statictsicsB),
    new ByteArrayInputStream(detailsOpeningB),
    new ReportRecordsInputStream(reader, maxBytes),
    new ByteArrayInputStream(detailsClosingB),
    new ByteArrayInputStream(footerB)
  };

  final InputStream inputStream = new InputStream() {
    private int streamIndex = -1;
    private InputStream currentStream = null;

    @Override
    public int read() throws IOException {
      InputStream input = getCurrentStream();
      if (input==null) return -1;
      int ch = input.read();
      if (ch>=0) return ch;
      currentStream = null;
      return read();
    }

    @Override
    public void close() throws IOException {
      if (streams!=null) {
        for (InputStream is: streams) {
          if (is!=null) {
            try {
              is.close();
            } catch (IOException ex) {}
          }
        }
      }
    }

    private InputStream getCurrentStream() {
      if (currentStream==null) {
        streamIndex++;
        if (streamIndex<streams.length) {
          currentStream = streams[streamIndex];
        }
      }
      return currentStream;
    }
  };

  ReportStream reportStream = new ReportStream() {
      @Override
      public InputStream getStream() {
        return inputStream;
      }

      @Override
      public long getLength() {
        return length;
      }
  };

  return reportStream;
}

/**
 * Deletes temporary file as no needed anymore.
 */
public void cleanup() {
  close();
  if (storage!=null) {
    storage.delete();
    storage = null;
  }
}

/**
 * Creates entry.
 * @param sourceUri source URI of the record
 * @param validated <code>true</code> if error has been validated
 * @param published <code>true</code> if error has been published
 * @param newEntry <code>true</code> if record has been added, <code>false</code> if modified
 * @param errors collection of errors
 * @throws IOException if creating entry fails
 */
private void createEntry(String sourceUri, boolean validated, boolean published, boolean newEntry, Collection<String> errors) throws IOException {
  this.harvested++;
  if (validated) {
    this.validated++;
    if (published) {
      if (newEntry) {
        this.added++;
      } else {
        this.updated++;
      }
    }
  }
  if (writer!=null) {
    boolean underMaxRepRecords = maxRepRecords==null || harvested<=maxRepRecords;
    boolean underMaxRepErrors  = maxRepErrors==null || (harvested - (added + updated))<=maxRepErrors;

    if (underMaxRepRecords || (underMaxRepErrors && errors!=null && errors.size()>0)) {
      if (!underMaxRepErrors) {
        errors = new ArrayList<String>();
      }
      writer.write(new ReportRecord(sourceUri, validated, published, errors));
    }
  }
}

/**
 * Closes report builder.
 * No more addition allowed.
 */
private void close() {
  if (writer!=null) {
    try {
      writer.close();
    } catch (IOException ex) {
    }
  }
}

/**
 * Gets report statistics.
 * @return report statistics
 */
private String getReportStats() {
  StringBuilder sb = new StringBuilder();
  sb.append("<report>");
  sb.append("<saveOutput>OFF</saveOutput>");
  sb.append("<validate>OFF</validate>");
  sb.append("<publish>ON</publish>");
  sb.append("<harvestEnd>").append(getEndTime().toString()).append("</harvestEnd>");
  sb.append((maxDocToHarvest != null ? "<maxDocsToHarvest>" + maxDocToHarvest + "</maxDocsToHarvest>" : ""));
  sb.append("<docsHarvested>").append(harvested).append("</docsHarvested>");
  sb.append("<docsPublished>").append(added + updated).append("</docsPublished>");
  sb.append("<docsAdded>").append(added).append("</docsAdded>");
  sb.append("<docsUpdated>").append(updated).append("</docsUpdated>");
  sb.append("<docsFailed>").append(harvested - (added + updated)).append("</docsFailed>");
  sb.append("<docsDeleted>").append(deleted).append("</docsDeleted>");
  if (exception!=null) {
    sb.append("<exception>").append(exception.toString()).append("</exception>");
  }
  sb.append("<duration>").append(new TimePeriod(getDuration()).toString()).append("</duration>");
  sb.append("<average>").append(getPerformance()).append("rec/min</average>");
  if (maxRepRecords!=null && harvested>maxRepRecords) {
    sb.append("<recordsLimitation>").append(maxRepRecords).append("</recordsLimitation>");
  }
  if (maxRepErrors!=null && (harvested - (added + updated))>maxRepErrors) {
    sb.append("<errorsLimitation>").append(maxRepErrors).append("</errorsLimitation>");
  }
  sb.append("</report>");
  return sb.toString();
}

/**
 * Report records stream.
 */
private class ReportRecordsInputStream extends InputStream {

/** record reader */
private RecordReader reader;
/** maximum number of bytes */
private int maxBytes;
/** bytes read */
private int readBytes = 0;
/** current string */
private String currentString;
/** index of the current character in current string */
private int currentIndex = -1;

/**
 * Creates instance of the stream.
 * @param reader record reader
 */
public ReportRecordsInputStream(RecordReader reader, int maxBytes) {
  this.reader = reader;
  this.maxBytes = maxBytes;
}

@Override
public int read() throws IOException {
  // check there is current string and that string has not been exhausted
  // if there is no current string (no reading attempt undertaken yet) or the
  // current string has been consumed entirelly, try to get next record
  if (currentString == null || currentIndex + 1 >= currentString.length()) {
    // if there is no reader (last record read), exit with end of stream code
    if (reader == null) {
      return -1;
    }
    // read next record
    ReportRecord record = reader.readRecord();
    if (record == null) {
      // no more records, close and destroy reader
      reader.close();
      reader = null;
      currentString = null;
      return -1;
    } else {
      String snippet = record.toXmlSnippet();
      int snippetLength = snippet.getBytes("UTF-8").length;
      if (snippetLength + readBytes>= maxBytes) {
        reader.close();
        reader = null;
        currentString = null;
        return -1;
      } else {
        readBytes += snippetLength;
        currentString = snippet;
      }
    }
    // always reset current index since it's a new string
    currentIndex = -1;
  }
  // increase current index at return character at that index from the current string
  return currentString.charAt(++currentIndex);
}
}
}

package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.SearchResultRecords;
import com.esri.gpt.framework.jsf.MessageBroker;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 * The Class CsvWriter.
 */
public class CsvWriter extends AFeedWriter2 {

// class variables =============================================================
/** The class logger. */
private final static Logger LOG = 
  Logger.getLogger(CsvWriter.class.getCanonicalName());

private static final String CHARACTERS_THAT_MUST_BE_QUOTED = ",\"\n";
private static final String QUOTE = "\"";
private static final String ESCAPED_QUOTE = "\"\"";
private static final String DELIMETER = ",";


// methods =====================================================================
/**
 * Writes the records
 * 
 * @param records list of records
 */
@Override
public void write(IFeedRecords records) {
  PrintWriter writer = null;
  try {
    
    HttpServletResponse response = this.getResponse();
    response.setContentType("application/csv;charset=UTF-8");
    response.setHeader("Content-Disposition", "filename="+ 
        System.currentTimeMillis() + ".csv");
    
    writer = new PrintWriter( new OutputStreamWriter(response.getOutputStream(), 
        "UTF8"), true);
    //writer = response.getWriter();
    String sRow = this.readHeader();
    writer.print(sRow);   
    
    for (IFeedRecord record : records) {
      sRow = this.readRow(record);
      writer.print(sRow);
    }
  } catch (Exception e) {
    LOG.log(Level.WARNING, "Error while ", e);
 
  } finally {
    IOUtils.closeQuietly(writer);
  }

}

/**
 * Read escaped csv field.
 * 
 * @param field the field
 * 
 * @return the string (never null)
 */
private String readEscapedCsvField(String field) {

  if(field == null || "".equals(field)) {
    return "";
  }
  if(field.contains(QUOTE)) {
    field = field.replaceAll(QUOTE, ESCAPED_QUOTE);
  }
 
  for(char ch: CHARACTERS_THAT_MUST_BE_QUOTED.toCharArray()) {
    if(field.contains(Character.toString(ch)) == true) {
      field =  QUOTE + " " + field + QUOTE ;
      break;
    }
  }
  
  return  field;
}

/**
 * Read csv header.
 *
 * @return the string
 */
protected String readHeader() {
  StringBuffer row = new StringBuffer();
  MessageBroker mBrok = this.getMessageBroker();
  row
    .append(readEscapedCsvField(mBrok.retrieveMessage(
      "catalog.searchresult.csv.header.contentType")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
      "catalog.searchresult.csv.header.urlToMetadata")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
      "catalog.searchresult.csv.header.urlToliveData")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
      "catalog.searchresult.csv.header.urlToSiteOrDownLoad")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
        "catalog.searchresult.csv.header.title")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
        "catalog.searchresult.csv.header.abstract")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
        "catalog.searchresult.csv.header.west")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
        "catalog.searchresult.csv.header.east")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
        "catalog.searchresult.csv.header.north")))
    .append(DELIMETER)
    .append(readEscapedCsvField(mBrok.retrieveMessage(
        "catalog.searchresult.csv.header.south")))
    .append(DELIMETER)
    .append("\n");
  return row.toString();
}

/**
 * Read csv row.
 *
 * @param record the record
 * @return the string
 */
protected String readRow(IFeedRecord record) {
  StringBuffer row = new StringBuffer();
  MessageBroker mBrok = this.getMessageBroker();
  String contentType = mBrok.retrieveMessage(
      "catalog.search.filterContentTypes." + 
      record.getContentType());
  String resourceUrl = record.getResourceUrl();
  String webSite = record.getResourceLinks().findUrlByTag(ResourceLink.TAG_WEBSITE);
  if(!record.getContentType().toLowerCase().equals("livedata")) {
    resourceUrl = "";
  }
  if(record.getContentType().toLowerCase().contains("download")) {
    webSite = record.getResourceLinks().findUrlByTag(ResourceLink.TAG_OPEN);
  }
  
  row
    .append(readEscapedCsvField(contentType))
    .append(DELIMETER)  
    .append(readEscapedCsvField(record.getViewMetadataUrl()))
    .append(DELIMETER)  
    .append(readEscapedCsvField(resourceUrl))
    .append(DELIMETER) 
    .append(readEscapedCsvField(webSite))
    .append(DELIMETER)
    .append(readEscapedCsvField(record.getTitle()))
    .append(DELIMETER)
    .append(readEscapedCsvField(record.getAbstract()))
    .append(DELIMETER)
    .append(record.getEnvelope().getMinX())
    .append(DELIMETER)
    .append(record.getEnvelope().getMaxX())
    .append(DELIMETER)
    .append(record.getEnvelope().getMaxY())
    .append(DELIMETER)
    .append(record.getEnvelope().getMinY())
    .append(DELIMETER)
    .append("\n");
  
  return row.toString();
}

@Override
public void writeError(Throwable err) {
  try {
    this.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, 
        err.getMessage());
  } catch (Exception e) {
    LOG.log(Level.WARNING, "Error", e);
  }
  
}


}

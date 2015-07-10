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

import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.search.OpenSearchProperties;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.control.AbstractFeedRecords;
import static com.esri.gpt.control.georss.FieldMetaLoader.loadLuceneMeta;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;


/**
 * ATOM feed writer.
 */
public class AtomFeedWriter implements FeedWriter {

/** The LOGGER. */
private static Logger LOGGER = Logger.getLogger(AtomFeedWriter.class.getName());

/** The request */
private final HttpServletRequest request;

/** The _writer. */
private PrintWriter _writer;

/** The _entry base url. */
private String _entryBaseUrl = null;

/** The _target. */
private RecordSnippetWriter.Target _target = RecordSnippetWriter.Target.blank;

/** The _message broker. */
private MessageBroker _messageBroker = null;

/** line separator */
private String lineSeparator;

private String rootName = "feed";

// constructors ================================================================
/**
 * Constructor.
 * 
 * @param request request
 * @param writer the writer
 */
public AtomFeedWriter(HttpServletRequest request, PrintWriter writer) {
  this.request = request;
  _writer = writer;
  lineSeparator = System.getProperty("line.separator");
}

/**
 * Constructor.
 * 
 * @param request request
 * @param writer the writer
 * @param entryBaseUrl provider URL
 */
public AtomFeedWriter(HttpServletRequest request, PrintWriter writer, String entryBaseUrl) {
  this.request = request;
  _writer = writer;
  _entryBaseUrl = entryBaseUrl;
  lineSeparator = System.getProperty("line.separator");
}

// properties
/**
 * Sets the entry base url.
 * 
 * @param url the new entry base url
 */
public void setEntryBaseUrl(String url) {
  _entryBaseUrl = url;
}

/**
 * Sets a root name.
 * @param rootName root name
 */
public void setRootName(String rootName) {
    this.rootName = Val.chkStr(rootName, this.rootName);
}

// ==================================================================
/**
 * Gets the entry base url.
 * 
 * @return Base URL
 */
public String getEntryBaseUrl() {
  return _entryBaseUrl;
}

/**
 * Gets links target.
 * 
 * @return links targets
 */
public RecordSnippetWriter.Target getTarget() {
  return _target;
}

/**
 * Sets links target.
 * 
 * @param target
 *          links target
 */
public void setTarget(RecordSnippetWriter.Target target) {
  _target = target;
}

/**
 * Gets the Message Broker.
 * 
 * @return message broker
 */
public MessageBroker getMessageBroker() {
  _messageBroker = (_messageBroker != null) ? _messageBroker
      : new MessageBroker();
  return _messageBroker;
}

/**
 * Sets the Message Broker.
 * 
 * @param broker the new _message broker
 */
public void set_messageBroker(MessageBroker broker) {
  _messageBroker = broker;
}

// methods
// ==================================================================
public void write(RequestContext requestContext, IFeedRecord singleRecord) throws CatalogIndexException {
    final OpenSearchProperties osProps = new OpenSearchProperties();
    osProps.setShortName(getMessageBroker().retrieveMessage("catalog.openSearch.shortName"));
    osProps.setNumberOfHits(1);
    osProps.setStartRecord(1);
    osProps.setRecordsPerPage(1);
    
    final ArrayList<IFeedRecords.FieldMeta> fields = new ArrayList<IFeedRecords.FieldMeta>();
    
    loadLuceneMeta(requestContext, fields);
    
    List<IFeedRecord> records = Arrays.asList(singleRecord);
    
    AbstractFeedRecords feedRecords = new AbstractFeedRecords(records) {

        @Override
        public OpenSearchProperties getOpenSearchProperties() {
            return osProps;
        }

        @Override
        public List<IFeedRecords.FieldMeta> getMetaData() {
            return fields;
        }
        
    };
    
    write(feedRecords);
}

/**
 * Write Atom Feed.
 * 
 * @param records records to write
 */
@Override
public void write(IFeedRecords records) {
  if (_writer == null)
    return;
  AtomFeed af = new AtomFeed(this.rootName);
  
  String sTitle = _messageBroker.retrieveMessage("catalog.rest.title");
  String sDescription = _messageBroker.retrieveMessage("catalog.rest.description");
  String sCopyright = _messageBroker.retrieveMessage("catalog.rest.copyright");
  String sGenerator = _messageBroker.retrieveMessage("catalog.rest.generator");
  
  if (sTitle.startsWith("???")) sTitle = "";
  if (sDescription.startsWith("???")) sDescription = "";
  if (sCopyright.startsWith("???")) sCopyright = "";
  if (sGenerator.startsWith("???")) sGenerator = "";
  
  af.setTitle(sTitle);
  af.setDescription(sDescription);
  af.setAuthor(sGenerator);
  af.setCopyright(sCopyright);
  
  af.setLink(getEntryBaseUrl());
  af.setId(getEntryBaseUrl());
  af.setUpdated(new Date());
  af.setOsProps(records.getOpenSearchProperties());
  for (IFeedRecord record : records) {
    AtomEntry ae = new AtomEntry();
    ae.setData(record);
    af.addEntry(ae);
  }
  af.WriteTo(_writer);
}


/**
 * Represents an Atom Feed.
 */
public class AtomFeed {

/** The ATO m_ header. */
private final String ATOM_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

/** The ATO m_ roo t_ ope n_ tag. */
private final String ATOM_ROOT_OPEN_TAG_PATTERN = "<%s xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:georss10=\"http://www.georss.org/georss/10\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">";
private String ATOM_ROOT_OPEN_TAG = "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:georss10=\"http://www.georss.org/georss/10\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">";

/** The ATO m_ roo t_ clos e_ tag. */
private final String ATOM_ROOT_CLOSE_TAG_PATTERN = "</%s>";
private String ATOM_ROOT_CLOSE_TAG = "</feed>";

/** The COMMEN t_ ope n_ tag. */
private final String COMMENT_OPEN_TAG = "<!--";

/** The COMMEN t_ clos e_ tag. */
private final String COMMENT_CLOSE_TAG = "-->";

/** The TITLE open tag. */
private final String TITLE_OPEN_TAG = "<title>";

/** The TITLE close tag. */
private final String TITLE_CLOSE_TAG = "</title>";

/** The AUTHOR open tag. */
private final String AUTHOR_OPEN_TAG = "<author><name>";

/** The AUTHOR clos e_ tag. */
private final String AUTHOR_CLOSE_TAG = "</name></author>";

/** The LINK tag. */
private final String SELF_LINK_TAG = "<link rel=\"self\" href=\"?\" type=\"application/atom+xml\"/>";
private final String FIRST_LINK_TAG = "<link rel=\"first\" href=\"?\" type=\"application/atom+xml\"/>";
private final String LAST_LINK_TAG = "<link rel=\"last\" href=\"?\" type=\"application/atom+xml\"/>";
private final String PREV_LINK_TAG = "<link rel=\"prev\" href=\"?\" type=\"application/atom+xml\"/>";
private final String NEXT_LINK_TAG = "<link rel=\"next\" href=\"?\" type=\"application/atom+xml\"/>";
private final String OSDDLINK_TAG = "<link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"?\" title=\"Content Search\"/>";
private final String OSDDCSWLINK_TAG = "<link rel=\"search\" type=\"application/opensearchdescription+xml\" href=\"?\" title=\"Content Search through CSW 3.0\"/>";

/** The UPDATED open tag. */
private final String UPDATED_OPEN_TAG = "<updated>";

/** The UPDATED close tag. */
private final String UPDATED_CLOSE_TAG = "</updated>";

/** The ID open tag. */
private final String ID_OPEN_TAG = "<id>";

/** The ID close_ tag. */
private final String ID_CLOSE_TAG = "</id>";

/** The TOTAL results open_ tag. */
private final String TOTAL_RESULTS_OPEN_TAG = "<opensearch:totalResults>";

/** The TOTA l_ result s_ clos e_ tag. */
private final String TOTAL_RESULTS_CLOSE_TAG = "</opensearch:totalResults>";

/** The STAR t_ inde x_ ope n_ tag. */
private final String START_INDEX_OPEN_TAG = "<opensearch:startIndex>";

/** The STAR t_ inde x_ clos e_ tag. */
private final String START_INDEX_CLOSE_TAG = "</opensearch:startIndex>";

/** The ITEM s_ pe r_ pag e_ ope n_ tag. */
private final String ITEMS_PER_PAGE_OPEN_TAG = "<opensearch:itemsPerPage>";

/** The ITEM s_ pe r_ pag e_ clos e_ tag. */
private final String ITEMS_PER_PAGE_CLOSE_TAG = "</opensearch:itemsPerPage>";

/** The _copyright. */
private String _copyright = null;

/** The _description. */
private String _description = null;

/** The _title. */
private String _title = null;

/** The _link. */
private String _link = null;

/** The _updated. */
private Date _updated = null;

/** The _author. */
private String _author = null;

/** The _id. */
private String _id = null;

/** The os props. */
private OpenSearchProperties osProps = null;

/** The _entries. */
private LinkedList<AtomEntry> _entries = null;

/**
 * Instantiates a new atom feed.
 */
public AtomFeed() {
    this("feed");
}

/**
 * Instantiates a new atom feed.
 * @param  rootName root element name
 */
public AtomFeed(String rootName) {
  _entries = new LinkedList<AtomEntry>();
  this.ATOM_ROOT_OPEN_TAG = String.format(this.ATOM_ROOT_OPEN_TAG_PATTERN, rootName);
  this.ATOM_ROOT_CLOSE_TAG = String.format(this.ATOM_ROOT_CLOSE_TAG_PATTERN, rootName);
}

/**
 * Adds the entry.
 * 
 * @param ae the ae
 */
public void addEntry(AtomEntry ae) {
  if (ae != null) {
    _entries.add(ae);
  }
}

/**
 * Gets the copyright.
 * 
 * @return the copyright
 */
public String getCopyright() {
  return _copyright;
}

/**
 * Gets the description.
 * 
 * @return the description
 */
public String getDescription() {
  return _description;
}

/**
 * Gets the author.
 * 
 * @return the author
 */
public String getAuthor() {
  return _author;
}

/**
 * Entries.
 * 
 * @return the linked list
 */
public LinkedList<AtomEntry> Entries() {
  return _entries;
}

/**
 * Gets the title.
 * 
 * @return the title
 */
public String getTitle() {
  return _title;
}

/**
 * Gets the link.
 * 
 * @return the link
 */
public String getLink() {
  return _link;
}

/**
 * Gets the updated.
 * 
 * @return the updated
 */
public Date getUpdated() {
  return _updated;
}

/**
 * Gets the id.
 * 
 * @return the id
 */
public String getId() {
  return _id;
}

/**
 * Gets the os props.
 * 
 * @return the os props
 */
public OpenSearchProperties getOsProps() {
  return osProps;
}

/**
 * Sets the copyright.
 * 
 * @param copyright the new copyright
 */
public void setCopyright(String copyright) {
  this._copyright = copyright;
}

/**
 * Sets the description.
 * 
 * @param description the new description
 */
public void setDescription(String description) {
  this._description = description;
}

/**
 * Sets the author.
 * 
 * @param author the new author
 */
public void setAuthor(String author) {
  this._author = author;
}

/**
 * Sets the title.
 * 
 * @param title the new title
 */
public void setTitle(String title) {
  this._title = title;
}

/**
 * Sets the link.
 * 
 * @param link the new link
 */
public void setLink(String link) {
  this._link = link;
}

/**
 * Sets the updated.
 * 
 * @param updated the new updated
 */
public void setUpdated(Date updated) {
  this._updated = updated;
}

/**
 * Sets the id.
 * 
 * @param id the new id
 */
public void setId(String id) {
  this._id = id;
}

/**
 * Sets the os props.
 * 
 * @param osProps the new os props
 */
public void setOsProps(OpenSearchProperties osProps) {
  this.osProps = osProps;
}

/**
 * Gets the info.
 * 
 * @return the info
 */
public String getInfo() {
  StringBuffer sb = new StringBuffer();
  if (getDescription() != null) {
    sb.append("Description:  " + getDescription() + "\n");
  }
  if (getCopyright() != null) {
    sb.append("Copyright:  " + getCopyright() + "\n");
  }
  return sb.toString();
}

/**
 * Write preamble.
 * 
 * @param writer the writer
 * @throws IOException Signals that an I/O exception has occurred.
 */
public void writePreamble(java.io.Writer writer) throws IOException {
  String data = null;
  if (writer == null)
    return;
  writer.append(ATOM_HEADER+lineSeparator);
  writer.append(ATOM_ROOT_OPEN_TAG+lineSeparator);
  if (getInfo().length() > 0) {
    try {
      data = Val.escapeXml(getInfo());
      writer.append(COMMENT_OPEN_TAG +lineSeparator+ data + COMMENT_CLOSE_TAG+lineSeparator);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "", e);
    }
  }
  if (getId() != null) {
    try {
      data = Val.escapeXml(getId());
      writer.append(ID_OPEN_TAG + data + ID_CLOSE_TAG+lineSeparator);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "", e);
    }
  }
  if (getTitle() != null) {
    try {
      data = Val.escapeXml(getTitle());
      writer.append(TITLE_OPEN_TAG + data + TITLE_CLOSE_TAG+lineSeparator);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "", e);
    }
  }
  if (getEntryBaseUrl() != null) {
    try {
      data = OSDDLINK_TAG.replace("?", Val.escapeXml(getEntryBaseUrl()+"/openSearchDescription"));
      writer.append(data+lineSeparator);
      
      data = OSDDCSWLINK_TAG.replace("?", Val.escapeXml(getEntryBaseUrl()+"/openSearchDescriptionCsw30"));
      writer.append(data+lineSeparator);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "", e);
    }
  }
  
  
  if (getAuthor() != null) {
    try {
      data = Val.escapeXml(getAuthor());
      writer.append(AUTHOR_OPEN_TAG + data + AUTHOR_CLOSE_TAG+lineSeparator);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "", e);
    }
  }
  if (getUpdated() != null) {
    try {
      SimpleDateFormat format = new SimpleDateFormat(AtomEntry.DATE_FORMAT_PATTERN);
      data = format.format(getUpdated());
      format = new SimpleDateFormat(AtomEntry.TIME_FORMAT_PATTERN);
      data = data + "T" + format.format(getUpdated()) + "Z";
      data = UPDATED_OPEN_TAG + data + UPDATED_CLOSE_TAG;
      writer.append(data+lineSeparator);

    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "", e);
    }
  }
 
  if (getOsProps() != null) {
    _writer.println(TOTAL_RESULTS_OPEN_TAG + getOsProps().getNumberOfHits()
        + TOTAL_RESULTS_CLOSE_TAG);
    _writer.println(START_INDEX_OPEN_TAG + getOsProps().getStartRecord()
        + START_INDEX_CLOSE_TAG);
    _writer.println(ITEMS_PER_PAGE_OPEN_TAG + getOsProps().getRecordsPerPage()
        + ITEMS_PER_PAGE_CLOSE_TAG);
    
    String selfLink = getOsProps().getSelfUrl();
    if (!selfLink.isEmpty()) {
      data = SELF_LINK_TAG.replace("?", Val.escapeXml(selfLink));
      writer.append(data+lineSeparator);
    }
    
    String firstLink = getOsProps().getFirstUrl();
    if (!firstLink.isEmpty()) {
      data = FIRST_LINK_TAG.replace("?", Val.escapeXml(firstLink));
      writer.append(data+lineSeparator);
    }
    
    String lastLink = getOsProps().getLastUrl();
    if (!lastLink.isEmpty()) {
      data = LAST_LINK_TAG.replace("?", Val.escapeXml(lastLink));
      writer.append(data+lineSeparator);
    }
    
    String prevLink = getOsProps().getPrevUrl();
    if (!prevLink.isEmpty()) {
      data = PREV_LINK_TAG.replace("?", Val.escapeXml(prevLink));
      writer.append(data+lineSeparator);
    }
    
    String nextLink = getOsProps().getNextUrl();
    if (!nextLink.isEmpty()) {
      data = NEXT_LINK_TAG.replace("?", Val.escapeXml(nextLink));
      writer.append(data+lineSeparator);
    }
  }
  
  writer.append("<opensearch:Query role=\"request\"");
  if (request!=null) {
    String searchText = Val.chkStr(request.getParameter("searchText"));
    String uuid = Val.chkStr(request.getParameter("uuid"));
    if (!searchText.isEmpty()) {
      writer.append(" searchText=\"").append(Val.escapeXml(searchText)).append("\"");
    }
    if (!uuid.isEmpty()) {
      writer.append(" uuid=\"").append(Val.escapeXml(uuid)).append("\"");
    }
  }
  writer.append("/>");
}

/**
 * Write end.
 * 
 * @param writer the writer
 * @throws IOException Signals that an I/O exception has occurred.
 */
public void writeEnd(java.io.Writer writer) throws IOException {
  writer.write(ATOM_ROOT_CLOSE_TAG+lineSeparator);
}

/**
 * Write to.
 * 
 * @param writer the writer
 */
public void WriteTo(java.io.Writer writer) {
  if (writer == null)
    return;
  try {
    writePreamble(writer);
    for (AtomEntry ae : Entries()) {
      ae.WriteTo(writer);
    }
    writeEnd(writer);

  } catch (Exception e) {
    LOGGER.log(Level.SEVERE, "", e);
  }
}


public void addStringToXmlHeader(String replace) {
  ATOM_ROOT_OPEN_TAG = ATOM_ROOT_OPEN_TAG.replace(">" , replace + ">");
 
}

}


}

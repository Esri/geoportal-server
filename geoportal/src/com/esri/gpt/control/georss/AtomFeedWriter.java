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
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.SearchResultRecord;
import com.esri.gpt.catalog.search.SearchResultRecords;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.BufferedWriter;


/**
 * ATOM feed writer.
 */
public class AtomFeedWriter implements FeedWriter {

/** The LOGGER. */
private static Logger LOGGER = Logger.getLogger(AtomFeedWriter.class.getName());

/** The DAT e_ forma t_ pattern. */
private final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

/** The TIM e_ forma t_ pattern. */
private final String TIME_FORMAT_PATTERN = "kk:mm:ss";

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

// constructors ================================================================
/**
 * Constructor.
 * 
 * @param writer the writer
 */
public AtomFeedWriter(PrintWriter writer) {
  _writer = writer;
  lineSeparator =	(String) java.security.AccessController.doPrivileged(
              new sun.security.action.GetPropertyAction("line.separator"));
}

/**
 * Constructor.
 * 
 * @param writer the writer
 * @param entryBaseUrl provider URL
 */
public AtomFeedWriter(PrintWriter writer, String entryBaseUrl) {
  _writer = writer;
  _entryBaseUrl = entryBaseUrl;
  lineSeparator =	(String) java.security.AccessController.doPrivileged(
              new sun.security.action.GetPropertyAction("line.separator"));
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
/**
 * Write Atom Feed.
 * 
 * @param records records to write
 */
@Override
public void write(IFeedRecords records) {
  if (_writer == null)
    return;
  AtomFeed af = new AtomFeed();
  
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
    ae.setId(record.getUuid());
    ae.setPublished(record.getModfiedDate());
    ae.setTitle(record.getTitle());
    ae.setSummary(record.getAbstract());
    for (ResourceLink link : record.getResourceLinks()) {
      ae.addResourceLink(link);
    }
    ae.addResourceLink(record.getResourceLinks().getThumbnail());
    if (record.getEnvelope() != null) {
      ae.setMinx(record.getEnvelope().getMinX());
      ae.setMiny(record.getEnvelope().getMinY());
      ae.setMaxx(record.getEnvelope().getMaxX());
      ae.setMaxy(record.getEnvelope().getMaxY());
    }
    af.addEntry(ae);
  }
  af.WriteTo(_writer);
}

// Private classes
// ==================================================================
/**
 * Represents an Atom Entry.
 */
public class AtomEntry {

/** The ENTIT y_ ope n_ tag. */
private final String ENTITY_OPEN_TAG = "<entry>";

/** The ENTIT y_ clos e_ tag. */
private final String ENTITY_CLOSE_TAG = "</entry>";

/** The TITL e_ ope n_ tag. */
private final String TITLE_OPEN_TAG = "<title>";

/** The TITL e_ clos e_ tag. */
private final String TITLE_CLOSE_TAG = "</title>";
// private final String LINK_TAG = "<link type=\"text/html\" href=\"?\"/>";
/** The LIN k_ tag. */
private final String LINK_TAG = "<link href=\"?\"/>";

/** The I d_ ope n_ tag. */
private final String ID_OPEN_TAG = "<id>";

/** The I d_ clos e_ tag. */
private final String ID_CLOSE_TAG = "</id>";

/** The UPDATE d_ ope n_ tag. */
private final String UPDATED_OPEN_TAG = "<updated>";

/** The UPDATE d_ clos e_ tag. */
private final String UPDATED_CLOSE_TAG = "</updated>";

/** The SUMMAR y_ ope n_ tag. */
private final String SUMMARY_OPEN_TAG = "<summary>";

/** The SUMMAR y_ clos e_ tag. */
private final String SUMMARY_CLOSE_TAG = "</summary>";

/** The BO x_ ope n_ tag. */
private final String BOX_OPEN_TAG = "<georss:box>";

/** The BO x_ clos e_ tag. */
private final String BOX_CLOSE_TAG = "</georss:box>";

/** The RES t_ fin d_ pattern. */
private final String REST_FIND_PATTERN = "/rest/document/";

/** The _id. */
private String _id = null;

/** The _title. */
private String _title = null;

/** The _link. */
private LinkedList<String> _link = null;

/** The _published. */
private Date _published = null;

/** The _summary. */
private String _summary = null;

/** The _minx. */
private double _minx = 0;

/** The _miny. */
private double _miny = 0;

/** The _maxx. */
private double _maxx = 0;

/** The _maxy. */
private double _maxy = 0;

/** The custom elements. */
private String customElements;

// methods
// ==================================================================
/**
 * Gets the minx.
 * 
 * @return the minx
 */
public double getMinx() {
  return _minx;
}

/**
 * Gets the miny.
 * 
 * @return the miny
 */
public double getMiny() {
  return _miny;
}

/**
 * Gets the maxx.
 * 
 * @return the maxx
 */
public double getMaxx() {
  return _maxx;
}

/**
 * Gets the maxy.
 * 
 * @return the maxy
 */
public double getMaxy() {
  return _maxy;
}

/**
 * Sets the minx.
 * 
 * @param _minx the new minx
 */
public void setMinx(double _minx) {
  this._minx = _minx;
}

/**
 * Sets the miny.
 * 
 * @param _miny the new miny
 */
public void setMiny(double _miny) {
  this._miny = _miny;
}

/**
 * Sets the maxx.
 * 
 * @param _maxx the new maxx
 */
public void setMaxx(double _maxx) {
  this._maxx = _maxx;
}

/**
 * Sets the maxy.
 * 
 * @param _maxy the new maxy
 */
public void setMaxy(double _maxy) {
  this._maxy = _maxy;
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
 * Gets the title.
 * 
 * @return the title
 */
public String getTitle() {
  return _title;
}

/**
 * Gets the links.
 * 
 * @return the links
 */
public LinkedList<String> getLinks() {
  return _link;
}

/**
 * Gets the published.
 * 
 * @return the published
 */
public Date getPublished() {
  return _published;
}

/**
 * Gets the summary.
 * 
 * @return the summary
 */
public String getSummary() {
  return _summary;
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
 * Sets the title.
 * 
 * @param title the new title
 */
public void setTitle(String title) {
  _title = title;
}

/**
 * Sets the published.
 * 
 * @param published the new published
 */
public void setPublished(Date published) {
  _published = published;
}

/**
 * Sets the summary.
 * 
 * @param summary the new summary
 */
public void setSummary(String summary) {
  _summary = summary;
}

/**
 * Gets the custom elements.
 * 
 * @return the custom elements
 */
public String getCustomElements() {
  return customElements;
}

/**
 * Sets the custom elements.
 * 
 * @param customElements the new custom elements
 */
public void setCustomElements(String customElements) {
  this.customElements = customElements;
}

/**
 * Adds the resource link.
 * 
 * @param resourcelink the resourcelink
 */
public void addResourceLink(ResourceLink resourcelink) {
  if (resourcelink == null)
    return;
  if (_link == null)
    _link = new LinkedList<String>();
  if (resourcelink.getUrl() != null && resourcelink.getUrl().length() > 0)
    _link.add(resourcelink.getUrl());
}

/**
 * Write to.
 * 
 * @param writer the writer
 */
public void WriteTo(java.io.Writer writer) {
  String data = "";
  if (writer == null)
    return;
  try {
    writer.append(ENTITY_OPEN_TAG+lineSeparator);
    if (getTitle() != null) {
      try {
        data = TITLE_OPEN_TAG + Val.escapeXml(getTitle()) + TITLE_CLOSE_TAG;
        writer.append("\t"+data+lineSeparator);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "", e);
      }
    }
    // add the rest of links if they exist
    if (getLinks() != null) {

      for (String lnk : getLinks()) {
        try {
          data = LINK_TAG.replace("?", Val.escapeXml(lnk));
          writer.append("\t"+data+lineSeparator);
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "", e);
        }
      }
    }
    if (getId() != null) {
      try {
        data = Val.escapeXml(getId());
        data = ID_OPEN_TAG + "urn:uuid:" + data.substring(1, data.length() - 1)
            + ID_CLOSE_TAG;
        writer.append("\t"+data+lineSeparator);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "", e);
      }
    }
    if (getPublished() != null) {
      try {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        data = format.format(getPublished());
        format = new SimpleDateFormat(TIME_FORMAT_PATTERN);
        data = data + "T" + format.format(getPublished()) + "Z";
        data = UPDATED_OPEN_TAG + data + UPDATED_CLOSE_TAG;
        writer.append("\t"+data+lineSeparator);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "", e);
      }
    }
    if (getSummary() != null) {
      try {
        data = SUMMARY_OPEN_TAG + Val.escapeXml(getSummary())
            + SUMMARY_CLOSE_TAG;
        writer.append("\t"+data+lineSeparator);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "", e);
      }
    }
    if (hasEnvelope()) {
      try {
        data = BOX_OPEN_TAG + getMiny() + " " + getMinx() + " " + getMaxy()
            + " " + getMaxx() + BOX_CLOSE_TAG;
        writer.append("\t"+data+lineSeparator);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "", e);
      }
    }
    if( getCustomElements() != null) {
      writer.append("\t"+Val.chkStr(getCustomElements())+lineSeparator);
    }
    writer.append(ENTITY_CLOSE_TAG+lineSeparator);
  } catch (Exception e) {
    LOGGER.log(Level.WARNING, "", e);
  }
}

/**
 * Checks for envelope.
 * 
 * @return true, if successful
 */
private boolean hasEnvelope() {
  return !(getMinx() == 0 && getMiny() == 0 && getMaxx() == 0 && getMaxy() == 0);
}
}

/**
 * Represents an Atom Feed.
 */
public class AtomFeed {

/** The ATO m_ header. */
private final String ATOM_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

/** The ATO m_ roo t_ ope n_ tag. */
private String ATOM_ROOT_OPEN_TAG = "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\">";

/** The ATO m_ roo t_ clos e_ tag. */
private final String ATOM_ROOT_CLOSE_TAG = "</feed>";

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

/** The LIN k_ tag. */
private final String LINK_TAG = "<link rel=\"self\" href=\"?\"/>";

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
  _entries = new LinkedList<AtomEntry>();
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
  if (getLink() != null) {
    try {
      data = LINK_TAG.replace("?", Val.escapeXml(getEntryBaseUrl()));
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
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
      data = format.format(getUpdated());
      format = new SimpleDateFormat(TIME_FORMAT_PATTERN);
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
  }
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
  String data = null;
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

/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.server.erosfeed;

import com.esri.gpt.catalog.search.*;
import com.esri.gpt.control.georss.AtomFeedWriter;
import com.esri.gpt.control.georss.IFeedRecord;
import com.esri.gpt.control.georss.IFeedRecords;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Eros atom feed writer.
 */
public class ErosAtomFeedWriter extends AtomFeedWriter {

  private static String namespace = "sdi";
  /**
   * The LOGGER.
   */
  private static final Logger LOGGER = Logger.getLogger(AtomFeedWriter.class.getName());
  /**
   * The DAT e_ forma t_ pattern.
   */
  private final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
  /**
   * The TIM e_ forma t_ pattern.
   */
  private final String TIME_FORMAT_PATTERN = "kk:mm:ss";
  private PrintWriter _writer;
  ErosEmailFinder emailFinder;
  private ResourceIdentifier resourceIdentifier;

  public ErosAtomFeedWriter(ErosEmailFinder emailFinder, ResourceIdentifier resourceIdentifier, PrintWriter writer) {
    super(writer);
    this.emailFinder = emailFinder;
    this.resourceIdentifier = resourceIdentifier;
    this._writer = writer;
  }

  public ErosAtomFeedWriter(ErosEmailFinder emailFinder, ResourceIdentifier resourceIdentifier, PrintWriter writer, String entryBaseUrl) {
    super(writer, entryBaseUrl);
    this.emailFinder = emailFinder;
    this.resourceIdentifier = resourceIdentifier;
    this._writer = writer;
  }

  @Override
  public void write(IFeedRecords records) {
    AtomFeed af = new AtomFeed();

    af.addStringToXmlHeader(" xmlns:" + namespace + "=\"http://www.geodata.gov/" + namespace + "_atom\"");

    String sTitle = getMessageBroker().retrieveMessage("catalog.rest.title");
    String sDescription = getMessageBroker().retrieveMessage("catalog.rest.description");
    String sCopyright = getMessageBroker().retrieveMessage("catalog.rest.copyright");
    String sGenerator = getMessageBroker().retrieveMessage("catalog.rest.generator");

    if (sTitle.startsWith("???")) {
      sTitle = "";
    }
    if (sDescription.startsWith("???")) {
      sDescription = "";
    }
    if (sCopyright.startsWith("???")) {
      sCopyright = "";
    }
    if (sGenerator.startsWith("???")) {
      sGenerator = "";
    }

    af.setTitle(sTitle);
    af.setDescription(sDescription);
    af.setAuthor(sGenerator);
    af.setCopyright(sCopyright);

    af.setLink(getEntryBaseUrl());
    af.setId(getEntryBaseUrl());
    af.setUpdated(new Date());
    af.setOsProps(records.getOpenSearchProperties());
    for (IFeedRecord record : records) {
      ErosAtomEntry ae = new ErosAtomEntry();
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
      // Eros specific
      ae.setResourceUrl(record.getResourceUrl());
      ae.setEmailAddress(findEmailAddress(record));
      ae.setServiceType(findServiceType(record));
      ae.setMetadataUrl(findMetadataUrl(record));
      af.addEntry(ae);
    }
    af.WriteTo(_writer);
  }

  private String findEmailAddress(IFeedRecord record) {
    return emailFinder.findEmail(record.getUuid());
  }

  private String findServiceType(IFeedRecord record) {
    String serviceType = record.getServiceType().isEmpty() ? resourceIdentifier.guessServiceTypeFromUrl(record.getResourceUrl()) : record.getServiceType();
    if (serviceType.equals("ags") ) {
      if (record.getResourceUrl().endsWith("MapServer")) {
        serviceType += "mapserver";
      }
      if (record.getResourceUrl().endsWith("FeatureServer")) {
        serviceType += "featureserver";
      }
      if (record.getResourceUrl().endsWith("ImageServer")) {
        serviceType += "imageserver";
      }
    }
    return serviceType;
  }

  private String findMetadataUrl(IFeedRecord record) {
    ResourceLinks rLinks = record.getResourceLinks();
    return rLinks != null ? Val.chkStr(rLinks.findUrlByTag(ResourceLink.TAG_METADATA)) : "";
  }

  public class ErosAtomEntry extends AtomEntry {

    /**
     * The ENTIT y_ ope n_ tag.
     */
    private final String ENTITY_OPEN_TAG = "<entry>";
    /**
     * The ENTIT y_ clos e_ tag.
     */
    private final String ENTITY_CLOSE_TAG = "</entry>";
    /**
     * The TITL e_ ope n_ tag.
     */
    private final String TITLE_OPEN_TAG = "<title>";
    /**
     * The TITL e_ clos e_ tag.
     */
    private final String TITLE_CLOSE_TAG = "</title>";
// private final String LINK_TAG = "<link type=\"text/html\" href=\"?\"/>";
    /**
     * The LIN k_ tag.
     */
    private final String LINK_TAG = "<link href=\"?\"/>";
    /**
     * The I d_ ope n_ tag.
     */
    private final String ID_OPEN_TAG = "<id>";
    /**
     * The I d_ clos e_ tag.
     */
    private final String ID_CLOSE_TAG = "</id>";
    /**
     * The UPDATE d_ ope n_ tag.
     */
    private final String UPDATED_OPEN_TAG = "<updated>";
    /**
     * The UPDATE d_ clos e_ tag.
     */
    private final String UPDATED_CLOSE_TAG = "</updated>";
    /**
     * The SUMMAR y_ ope n_ tag.
     */
    private final String SUMMARY_OPEN_TAG = "<summary>";
    /**
     * The SUMMAR y_ clos e_ tag.
     */
    private final String SUMMARY_CLOSE_TAG = "</summary>";
    /**
     * The BO x_ ope n_ tag.
     */
    private final String BOX_OPEN_TAG = "<georss:box>";
    /**
     * The BO x_ clos e_ tag.
     */
    private final String BOX_CLOSE_TAG = "</georss:box>";
    /**
     * The RES t_ fin d_ pattern.
     */
    private final String REST_FIND_PATTERN = "/rest/document/";
    private String resourceUrl;
    private String serviceType;
    private String emailAddress;
    private String metadataUrl;

    public String getResourceUrl() {
      return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
      this.resourceUrl = Val.chkStr(resourceUrl);
    }

    public String getEmailAddress() {
      return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
    }

    public String getServiceType() {
      return serviceType;
    }

    public void setServiceType(String serviceType) {
      this.serviceType = serviceType;
    }

    public String getMetadataUrl() {
      return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
      this.metadataUrl = metadataUrl;
    }

    @Override
    public void WriteTo(Writer writer) {
      String data = "";
      if (writer == null) {
        return;
      }
      try {
        writer.append(ENTITY_OPEN_TAG);
        if (getTitle() != null) {
          try {
            data = TITLE_OPEN_TAG + Val.escapeXml(getTitle()) + TITLE_CLOSE_TAG;
            writer.append(data);
          } catch (Exception e) {
            LOGGER.log(Level.WARNING, "", e);
          }
        }
        // add the rest of links if they exist
//        if (getLinks() != null) {
//
//          for (String lnk : getLinks()) {
//            try {
//              data = LINK_TAG.replace("?", Val.escapeXml(lnk));
//              writer.append(data);
//            } catch (Exception e) {
//              LOGGER.log(Level.WARNING, "", e);
//            }
//          }
//        }
        if (getId() != null) {
          try {
            data = Val.escapeXml(getId());
            data = ID_OPEN_TAG + data + ID_CLOSE_TAG;
            writer.append(data);
          } catch (Exception e) {
            LOGGER.log(Level.WARNING, "", e);
          }
        }
//        if (getPublished() != null) {
//          try {
//            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
//            data = format.format(getPublished());
//            format = new SimpleDateFormat(TIME_FORMAT_PATTERN);
//            data = data + "T" + format.format(getPublished()) + "Z";
//            data = UPDATED_OPEN_TAG + data + UPDATED_CLOSE_TAG;
//            writer.append(data);
//          } catch (Exception e) {
//            LOGGER.log(Level.WARNING, "", e);
//          }
//        }
//        if (getSummary() != null) {
//          try {
//            data = SUMMARY_OPEN_TAG + Val.escapeXml(getSummary())
//                    + SUMMARY_CLOSE_TAG;
//            writer.append(data);
//          } catch (Exception e) {
//            LOGGER.log(Level.WARNING, "", e);
//          }
//        }
//        if (hasEnvelope()) {
//          try {
//            data = BOX_OPEN_TAG + getMiny() + " " + getMinx() + " " + getMaxy()
//                    + " " + getMaxx() + BOX_CLOSE_TAG;
//            writer.append(data);
//          } catch (Exception e) {
//            LOGGER.log(Level.WARNING, "", e);
//          }
//        }
//        if (getCustomElements() != null) {
//          writer.append(Val.chkStr(getCustomElements()));
//        }

        // Eros specific
        writer.append("<" + namespace + ":serviceUrl>" + Val.escapeXml(getResourceUrl()) + "</" + namespace + ":serviceUrl>");
        writer.append("<" + namespace + ":serviceType>" + Val.escapeXml(getServiceType()) + "</" + namespace + ":serviceType>");
        writer.append("<" + namespace + ":emailAddress>" + Val.escapeXml(getEmailAddress()) + "</" + namespace + ":emailAddress>");
//        writer.append("<" + namespace + ":emailAddress>" + "" + "</" + namespace + ":emailAddress>");
        writer.append("<" + namespace + ":metadataUrl>" + Val.escapeXml(getMetadataUrl()) + "</" + namespace + ":metadataUrl>");

        writer.append(ENTITY_CLOSE_TAG);
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
}

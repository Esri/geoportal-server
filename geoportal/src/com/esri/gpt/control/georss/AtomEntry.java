/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.framework.util.Val;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// Private classes

// ==================================================================
/**
 * Represents an Atom Entry.
 */
public class AtomEntry {

    /** The LOGGER. */
    private static Logger LOGGER = Logger.getLogger(AtomEntry.class.getName());
    
    /** The DAT e_ forma t_ pattern. */
    static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    /** The TIM e_ forma t_ pattern. */
    static final String TIME_FORMAT_PATTERN = "kk:mm:ss";
    /** The ENTIT y_ ope n_ tag. */
    private final String ENTITY_OPEN_TAG_NS_PATTERN = "<%s  xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:georss10=\"http://www.georss.org/georss/10\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">";
    private final String ENTITY_OPEN_TAG_NS;
    private final String ENTITY_OPEN_TAG_PATTERN = "<%s>";
    private final String ENTITY_OPEN_TAG;
    /** The ENTIT y_ clos e_ tag. */
    private final String ENTITY_CLOSE_TAG_PATTERN = "</%s>";
    private final String ENTITY_CLOSE_TAG;
    /** The TITL e_ ope n_ tag. */
    private final String TITLE_OPEN_TAG = "<title>";
    /** The TITL e_ clos e_ tag. */
    private final String TITLE_CLOSE_TAG = "</title>";
    // private final String LINK_TAG = "<link type=\"text/html\" href=\"?\"/>";
    /** The LIN k_ tag. */
    private final String LINK_TAG = "<link href=\"?\"{rel}/>";
    /** The I d_ ope n_ tag. */
    private final String ID_OPEN_TAG = "<id>";
    /** The I d_ clos e_ tag. */
    private final String ID_CLOSE_TAG = "</id>";
    /** DC Identifier open tag. */
    private final String DCIDENTIFIER_OPEN_TAG = "<dc:identifier>";
    /** DC Identifier closing tag. */
    private final String DCIDENTIFIER_CLOSE_TAG = "</dc:identifier>";
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
    /** The BO x_ ope n_ tag. */
    private final String BOX10_OPEN_TAG = "<georss10:box>";
    /** The BO x_ clos e_ tag. */
    private final String BOX10_CLOSE_TAG = "</georss10:box>";
    /** The _id. */
    private String _id = null;
    /** The _title. */
    private String _title = null;
    /** The _link. */
    private LinkedList<ResourceLink> _link = null;
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
    /** line separator */
    private String lineSeparator;

    // methods
    // ==================================================================
    public AtomEntry() {
        this("entry");
    }
    
    public AtomEntry(String entryName) {
        this.ENTITY_OPEN_TAG = String.format(this.ENTITY_OPEN_TAG_PATTERN, entryName);
        this.ENTITY_OPEN_TAG_NS = String.format(this.ENTITY_OPEN_TAG_NS_PATTERN, entryName);
        this.ENTITY_CLOSE_TAG = String.format(this.ENTITY_CLOSE_TAG_PATTERN, entryName);
        this.lineSeparator = System.getProperty("line.separator");
    }
    
    public void setData(IFeedRecord record) {
        this.setId(record.getUuid());
        this.setPublished(record.getModfiedDate());
        this.setTitle(record.getTitle());
        this.setSummary(record.getAbstract());
        for (ResourceLink link : record.getResourceLinks()) {
            this.addResourceLink(link);
        }
        this.addResourceLink(record.getResourceLinks().getThumbnail());
        if (record.getEnvelope() != null) {
            this.setMinx(record.getEnvelope().getMinX());
            this.setMiny(record.getEnvelope().getMinY());
            this.setMaxx(record.getEnvelope().getMaxX());
            this.setMaxy(record.getEnvelope().getMaxY());
        }
    }

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
    public LinkedList<ResourceLink> getLinks() {
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
        if (resourcelink == null) {
            return;
        }
        if (_link == null) {
            _link = new LinkedList<ResourceLink>();
        }
        if (resourcelink.getUrl() != null && resourcelink.getUrl().length() > 0) {
            _link.add(resourcelink);
        }
    }

    public void WriteTo(java.io.Writer writer) {
        WriteTo(this.ENTITY_OPEN_TAG, writer);
    }
    
    public void WriteNsTo(java.io.Writer writer) {
        WriteTo(this.ENTITY_OPEN_TAG_NS, writer);
    }
    
    public void AppendTo(Element elFeed) {
        String atomns = "http://www.w3.org/2005/Atom";
        String dcns = "http://purl.org/dc/elements/1.1/";
        String georss = "http://www.georss.org/georss";
        String georss10 = "http://www.georss.org/georss/10";
        Document parent = elFeed.getOwnerDocument();
        
        Element elEntry = parent.createElementNS(atomns,"entry");
        elFeed.appendChild(elEntry);
        
        if (getTitle()!=null) {
            Element elTitle = parent.createElementNS(atomns, "title");
            elTitle.appendChild(parent.createTextNode(Val.chkStr(getTitle())));
            elEntry.appendChild(elTitle);
        }
        
        if (getId()!=null) {
            Element elId = parent.createElementNS(atomns, "id");
            elId.appendChild(parent.createTextNode("urn:uuid:"+Val.chkStr(getId())));
            elEntry.appendChild(elId);


            Element elIdentifier = parent.createElementNS(dcns, "identifier");
            elIdentifier.appendChild(parent.createTextNode(Val.chkStr(getId())));
            elEntry.appendChild(elIdentifier);
        }
        
        if (getSummary()!=null) {
            Element elSummary = parent.createElementNS(atomns, "summary");
            elSummary.appendChild(parent.createTextNode(Val.chkStr(getSummary())));
            elEntry.appendChild(elSummary);
        }
        
        if (getPublished() != null) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
                String data = format.format(getPublished());
                format = new SimpleDateFormat(TIME_FORMAT_PATTERN);
                data = data + "T" + format.format(getPublished()) + "Z";
                Element elPublished = parent.createElementNS(atomns, "updated");
                elPublished.appendChild(parent.createTextNode(data));
                elEntry.appendChild(elPublished);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "", e);
            }
        }
        
        if (getLinks() != null) {
            for (ResourceLink lnk : getLinks()) {
                String [] rels = null;
                if (ResourceLink.TAG_DETAILS.equals(lnk.getTag())) {
                  rels = new String[]{"describedBy", "alternate"};
                }
                if (ResourceLink.TAG_METADATA.equals(lnk.getTag())) {
                  rels = new String[]{"via"};
                }
                if (ResourceLink.TAG_OPEN.equals(lnk.getTag())) {
                  rels = new String[]{"enclosure"};
                }
                if (ResourceLink.TAG_THUMBNAIL.equals(lnk.getTag())) {
                  rels = new String[]{"icon"};
                }
                
                if (rels!=null) {
                  for (String rel: rels) {
                    Element elLink = parent.createElementNS(atomns, "link");
                    elLink.setAttribute("href", lnk.getUrl());
                    elLink.setAttribute("rel", rel);
                    elEntry.appendChild(elLink);
                  }
                }
            }
        }
        
        Element elBox = parent.createElementNS(georss, "box");
        elBox.appendChild(parent.createTextNode(makeGeorssEnvelope()));
        elEntry.appendChild(elBox);
        
        Element elBox10 = parent.createElementNS(georss10, "box");
        elBox10.appendChild(parent.createTextNode(makeGeorssEnvelope()));
        elEntry.appendChild(elBox10);
    }
    
    /**
     * Write to.
     *
     * @param writer the writer
     */
    private void WriteTo(String openTag, java.io.Writer writer) {
        String data = "";
        if (writer == null) {
            return;
        }
        try {
            writer.append(openTag + lineSeparator);
            if (getTitle() != null) {
                try {
                    data = TITLE_OPEN_TAG + Val.escapeXml(getTitle()) + TITLE_CLOSE_TAG;
                    writer.append("\t" + data + lineSeparator);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "", e);
                }
            }
            // add the rest of links if they exist
            if (getLinks() != null) {
                for (ResourceLink lnk : getLinks()) {
                    try {
                        String [] rels = null;
                        if (ResourceLink.TAG_DETAILS.equals(lnk.getTag())) {
                          rels = new String[]{" rel=\"describedBy\"", " rel=\"alternate\""};
                        }
                        if (ResourceLink.TAG_METADATA.equals(lnk.getTag())) {
                          rels = new String[]{" rel=\"via\""};
                        }
                        if (ResourceLink.TAG_OPEN.equals(lnk.getTag())) {
                          rels = new String[]{" rel=\"enclosure\""};
                        }
                        if (ResourceLink.TAG_THUMBNAIL.equals(lnk.getTag())) {
                          rels = new String[]{" rel=\"icon\""};
                        }
                        if (rels!=null) {
                          for (String rel: rels) {
                            data = LINK_TAG.replace("?", Val.escapeXml(lnk.getUrl())).replace("{rel}", rel);
                            writer.append("\t" + data + lineSeparator);
                          }
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "", e);
                    }
                }
            }
            if (getId() != null) {
                try {
                    data = Val.escapeXml(getId());
                    data = ID_OPEN_TAG + "urn:uuid:" + Val.chkStr(data).replaceAll("&\\{|\\}$", "") + ID_CLOSE_TAG;
                    writer.append("\t" + data + lineSeparator);
                    
                    data = Val.escapeXml(getId());
                    data = DCIDENTIFIER_OPEN_TAG + data + DCIDENTIFIER_CLOSE_TAG;
                    writer.append("\t" + data + lineSeparator);
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
                    writer.append("\t" + data + lineSeparator);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "", e);
                }
            }
            if (getSummary() != null) {
                try {
                    data = SUMMARY_OPEN_TAG + Val.escapeXml(getSummary()) + SUMMARY_CLOSE_TAG;
                    writer.append("\t" + data + lineSeparator);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "", e);
                }
            }
            if (hasEnvelope()) {
                try {
                    data = BOX_OPEN_TAG + makeGeorssEnvelope() + BOX_CLOSE_TAG;
                    writer.append("\t" + data + lineSeparator);
                    
                    data = BOX10_OPEN_TAG + makeGeorssEnvelope() + BOX10_CLOSE_TAG;
                    writer.append("\t" + data + lineSeparator);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "", e);
                }
            }
            if (getCustomElements() != null) {
                writer.append("\t" + Val.chkStr(getCustomElements()) + lineSeparator);
            }
            writer.append(ENTITY_CLOSE_TAG + lineSeparator);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "", e);
        }
    }
    
    /**
     * Makes envelope string for georss.
     * @return envelope string
     */
    private String makeGeorssEnvelope() {
      return getMiny() + " " + getMinx() + " " + getMaxy() + " " + getMaxx();
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

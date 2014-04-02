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

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;

/**
 * HTML snippet writer.
 */
public class RecordSnippetWriter {

  // class variables =============================================================
  
  /** clip text length (255) */
  private static final int CLIP_TEXT_LENGTH = 255;
  
  /** snippet style class */
  private static final String SNIPPET_STYLE_CLASS = "snippet";
  
  /** title style class */
  private static final String TITLE_STYLE_CLASS = "title";
  
  /** abstract style class */
  private static final String ABSTRACT_STYLE_CLASS = "abstract";
  
  /** links style class */
  private static final String LINKS_STYLE_CLASS = "links";
  
  /** thumbnail style class */
  private static final String THUMBNAIL_STYLE_CLASS = "thumbnail";
  
  // instance variables ==========================================================
  
  /** Message broker */
  private MessageBroker _messageBroker;
  
  /** print writer */
  private final PrintWriter _writer;
  
  /** allows to show title. Default: <code>false</code>. */
  private boolean _showTitle = false;
  
  /** allows to show content type icon. Default: <code>false</code>. */
  private boolean _showIcon = false;
  
  /** allows to show description. Default: <code>true</code>. */
  private boolean _showDescription = true;
  
  /** allows to show links. Default: <code>true</code>. */
  private boolean _showLinks = true;
  
  /** text clipping. Default: <code>false</code>. */
  private boolean _clipText = false;
    
  /** links target */
  private Target _target = Target.blank;
  
  // constructors ================================================================
  /**
   * Creates instance of the writer.
   * @param messageBroker message broker
   * @param writer underlying print writer
   */
  public RecordSnippetWriter(MessageBroker messageBroker, PrintWriter writer) {
    _messageBroker = messageBroker;
    if (_messageBroker == null) {
      throw new IllegalArgumentException("A MessageBroker is required.");
    }
    _writer = writer;
  }
  
  // properties ==================================================================
  
  private MessageBroker getMessageBroker(){ 
    return this._messageBroker;
  }
  
  /**
   * Checks if showing title allowed.
   * @return <code>true</code> if showing title allowed
   */
  public boolean getShowTitle() {
    return _showTitle;
  }
  
  /**
   * Sets showing title flag.
   * @param showTitle <code>true</code> to show title
   */
  public void setShowTitle(boolean showTitle) {
    _showTitle = showTitle;
  }
  
  /**
   * Checks if showing icon allowed.
   * @return <code>true</code> if showing icon allowed
   */
  public boolean getShowIcon() {
    return _showIcon;
  }
  
  /**
   * Sets showing icon flag.
   * @param showIcon <code>true</code> to show icon
   */
  public void setShowIcon(boolean showIcon) {
    _showIcon = showIcon;
  }
  
  /**
   * Checks if showing description allowed.
   * @return <code>true</code> if showing description allowed
   */
  public boolean getShowDescription() {
    return _showDescription;
  }
  
  /**
   * Sets showing description flag.
   * @param showDescription <code>true</code> to show description
   */
  public void setShowDescription(boolean showDescription) {
    _showDescription = showDescription;
  }
  
  /**
   * Checks if showing links allowed.
   * @return <code>true</code> if showing links allowed
   */
  public boolean getShowLinks() {
    return _showLinks;
  }
  
  /**
   * Sets showing links flag.
   * @param showLinks <code>true</code> to show links
   */
  public void setShowLinks(boolean showLinks) {
    _showLinks = showLinks;
  }
  
  /**
   * Checks if text shoud be clipped.
   * @return <code>true</code> if text shoud be clipped
   */
  public boolean getClipText() {
    return _clipText;
  }
  
  /**
   * Sets clipping text flag.
   * @param clipText <code>true</code> to clip text
   */
  public void setClipText(boolean clipText) {
    _clipText = clipText;
  }
  
  /**
   * Gets links target.
   * @return links target
   */
  public Target getTarget() {
    return _target;
  }
  
  /**
   * Sets links target.
   * @param target links target
   */
  public void setTarget(Target target) {
    _target = target;
  }
  
  // methods =====================================================================
  /**
   * Writes HTML snippet with metadata description.
   * @param record record to write as snippet
   */
  public void write(IFeedRecord record) {
  
    // write
    if (_writer != null) {
      _writer.println("<div class=\"" + SNIPPET_STYLE_CLASS + "\">");
  
      // title
      if (getShowTitle()) {
        writeTitle(record);
      }
  
      // abstract
      if (getShowDescription()) {
        writeAbstract(record);
      }
  
      // links
      if (getShowLinks()) {
        writeLinks(record);
      }
  
      _writer.println("</div>");
      _writer.flush();
    }
  }
  
  /**
   * Writes title section.
   * @param record record
   */
  private void writeTitle(IFeedRecord record) {
    String sUuid = Val.chkStr(record.getUuid());
    String sTitle = Val.chkStr(record.getTitle());
    
    String sNodeId = Val.escapeXmlForBrowser(sUuid);
    _writer.println("<div id=\""+sNodeId+"\" class=\"" + TITLE_STYLE_CLASS + "\">");
    
    // content type icon
    ResourceLink icon = record.getResourceLinks().getIcon();
    if (icon != null) {
      String sUrl = Val.chkStr(icon.getUrl());
      if ((sUrl.length() > 0) && getShowIcon()) {
        _writer.print("<img src=\"");
        _writer.print(Val.escapeXmlForBrowser(sUrl));
        _writer.print("\" alt=\"");
        _writer.print(Val.escapeXmlForBrowser(icon.getLabel()));
        _writer.print("\" title=\"");
        _writer.print(Val.escapeXmlForBrowser(icon.getLabel()));
        _writer.print("\"/>");
      }
    }
    
    // title (or uuid if no title)
    sTitle = sTitle.length() > 0 ? sTitle : sUuid;
    _writer.println(Val.escapeXmlForBrowser(sTitle));
    
    _writer.println("</div>");
  }
  
  /**
   * Writes abstract section.
   * @param record record
   */
  private void writeAbstract(IFeedRecord record) {
    String sAbstract = Val.chkStr(record.getAbstract());
  
    if (sAbstract.length() > 0) {
      sAbstract = !getClipText()
        ? sAbstract : sAbstract.length() > CLIP_TEXT_LENGTH
        ? sAbstract.substring(0, CLIP_TEXT_LENGTH) + "..."
        : sAbstract;
        
      String divStyle = "overflow: auto;";
      _writer.println("<div class=\""+ABSTRACT_STYLE_CLASS+"\" style=\""+divStyle+"\" >");
      
      String url = Val.chkStr(record.getResourceLinks().getThumbnail().getUrl());
      if (url.length() > 0) {
        url = Val.escapeXml(url);
        String imgStyle = "float:right; margin-left:0.5em; width:64px; height:64px; border:1px solid #000000;";
        //imgStyle = "border:1px solid #000000;";
        _writer.println("<img class=\""+THUMBNAIL_STYLE_CLASS+"\" "+ 
            " alt=\""+ this.getMessageBroker().retrieveMessage("catalog.rest.thumbNail")+ "\" " +
            " src=\""+ url+"\" style=\""+imgStyle+"\"/>");
      }
      _writer.println(Val.escapeXmlForBrowser(sAbstract));
      _writer.println("</div>");
    }
  }
  
  /**
   * Writes links section.
   * @param record record
   */
  private void writeLinks(IFeedRecord record) {
    _writer.println("<div class=\"" + LINKS_STYLE_CLASS + "\">");
    
    ResourceLinks links = record.getResourceLinks();
    writeLinks(links);
    
    _writer.println("</div>");
  }
  
  /**
   * Write links.
   * 
   * @param links the links
   */
  public void writeLinks(ResourceLinks links) {
    if(links == null) {
      return;
    }
    for (ResourceLink link: links) {
      writeLink(link.getUrl(),link.getLabel());
    }
  }

  /**
   * Writes link.
   * @param url link URL
   * @param text link text
   */
  private void writeLink(String url, String text) {
    url = Val.chkStr(url);
    text = Val.chkStr(text);
    if (url.length() > 0) {
      if(url.startsWith("javascript:")) {
        _writer.print("<A href=\"javascript:void(0)\" onclick=\"");
      } else {
        _writer.print("<A HREF=\"");
      }
      _writer.print(Val.escapeXmlForBrowser(url));
      _writer.print("\" target=\"" + _target.toHtmlValue() + "\">");
      _writer.print(Val.escapeXmlForBrowser(text));
      _writer.println("</A>");
    }
  }

  // enums =======================================================================

  /** links target */
  public enum Target {
  
    /** blank */
    blank,
    /** parent */
    parent,
    /** self */
    self,
    /** top */
    top;
    
    /**
     * Checks value of the string.
     * @param value value
     * @return corresponding target or {@link Target#blank} if value is invalid
     */
    public static Target checkValueOf(String value) {
      value = Val.chkStr(value);
      for (Target t: values()) {
        if (t.name().equalsIgnoreCase(value)) {
          return t;
        }
      }
      return blank;
    }
    
    /**
     * Returns HTML value of the target.
     * @return HTML value of the target
     */
    public String toHtmlValue() {
      return "_" + name();
    }
    
  }
  
}

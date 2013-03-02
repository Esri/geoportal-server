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
package com.esri.gpt.catalog.schema;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.UIGraphic;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.model.SelectItem;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Section component associated with a metadata schema. 
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * A section contains a label and a collection of parameters. The
 * parameters will appear within the user interface in the order in
 * which they appear within the schema configuration XML.
 * <p/>
 * Example: 
 * <br/>&lt;section key="description"&gt;
 * <br/>&lt;/section&gt;
 */
public class Section extends UiComponent {

// class variables =============================================================
  
/** Mutually exclusive section = "exclusive" */
public static final String OBLIGATION_EXCLUSIVE = "exclusive";
  
/** Mandatory section = "mandatory" (this is the default) */
public static final String OBLIGATION_MANDATORY = "mandatory";

/** Optional section = "optional" */
public static final String OBLIGATION_OPTIONAL = "optional";
  
// instance variables ==========================================================
private String     _delete = "";
private Label      _label;
private String     _obligation = Section.OBLIGATION_MANDATORY;
private boolean    _open = true;
private Parameters _parameters;
private Section    _parent;
private Sections   _sections;
private String     _select = "";
private boolean    _useSelectForDelete = false;
private boolean    _visibleOnDetails = true;
private boolean    _wasUnbound = false;
  
// constructors ================================================================

/** Default constructor. */
public Section() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Section(Section objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate == null) {
    setParameters(new Parameters());
    setSections(new Sections());
  } else {
    setObligation(objectToDuplicate.getObligation());
    setOpen(objectToDuplicate.getOpen());
    setSelect(objectToDuplicate.getSelect());
    setDelete(objectToDuplicate.getDelete());
    setVisibleOnDetails(objectToDuplicate.getVisibleOnDetails());
    setUseSelectForDelete(objectToDuplicate.getUseSelectForDelete());
    if (objectToDuplicate.getLabel() != null) {
      setLabel(objectToDuplicate.getLabel().duplicate());
    }
    setParameters(new Parameters(objectToDuplicate.getParameters()));
    setSections(new Sections(objectToDuplicate.getSections(),this));
  } 
}

// properties ==================================================================

/**
 * Gets the XPath expression used to select nodes for deletion when an optional
 * section has not been filled (i.e. not checked within the editor).
 * @return the delete expression
 */
public String getDelete() {
  return _delete;
}
/**
 * Sets the XPath expression used to select nodes for deletion when an optional
 * section has not been filled (i.e. not checked within the editor).
 */
public void setDelete(String expression) {
  _delete = Val.chkStr(expression);
}

/**
 * Gets the mutually exclusive siblings associated with this section
 * @return the siblings (the collection includes this section if it is exclusive)
 */
private Sections getExclusiveSiblings(Schema schema) {
  Sections exclusive = new Sections();
  Sections siblings = schema.getSections();
  if (getParent() != null) {
    siblings = getParent().getSections();
  }
  for (Section sibling: siblings.values()) {
    if (sibling.getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE)) {
      exclusive.add(sibling);
    }
  }
  return exclusive;
}

/**
 * Gets the Faces ID for the section.
 * <br/>The Faces ID is based upon the section key.
 * <br/>All "." characters are replaced with "_".
 * @return the Faces ID
 */
public String getFacesId() {
  return getKey().replace('.','_');
}

/**
 * Gets the label component.
 * @return the label component
 */
public Label getLabel() {
  return _label;
}
/**
 * Sets the label component.
 * @param label the label component
 */
public void setLabel(Label label) {
  _label = label;
}

/**
 * Gets the obligation for this section.
 * @return the obligation
 */
public String getObligation() {
  return _obligation;
}
/**
 * Sets the obligation for this section.
 * @param obligation the obligation
 */
public void setObligation(String obligation) {
  obligation = Val.chkStr(obligation);
  if (obligation.equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE)) {
    _obligation = Section.OBLIGATION_EXCLUSIVE;
  } else if (obligation.equalsIgnoreCase(Section.OBLIGATION_MANDATORY)) {
    _obligation = Section.OBLIGATION_MANDATORY;
  } else if (obligation.equalsIgnoreCase(Section.OBLIGATION_OPTIONAL)) {
    _obligation = Section.OBLIGATION_OPTIONAL;
  } else {
    _obligation = Section.OBLIGATION_MANDATORY;
  }
}

/**
 * Gets the open status.
 * @return true if the section is open
 */
public boolean getOpen() {
  return _open;
}
/**
 * Sets the open status.
 * @param open true if the section is open
 */
public void setOpen(boolean open) {
  _open = open;
}

/**
 * Gets the parameters for the schema.
 * @return the schema parameters
 */
public Parameters getParameters() {
  return _parameters;
}

/**
 * Sets the parameters for the schema.
 * @param parameters the schema parameters
 */
protected void setParameters(Parameters parameters) {
  _parameters = parameters;
  if (_parameters == null) _parameters = new Parameters();
}

/**
 * Gets the parent section.
 * @return the parent section (null for a top level section)
 */
public Section getParent() {
  return _parent;
}
/**
 * Sets the parent section.
 */
protected void setParent(Section parent) {
  _parent = parent;
}

/**
 * Gets the sub-sections for this section.
 * @return the sub-sections
 */
public Sections getSections() {
  return _sections;
}
/**
 * Sets the sub-sections for this section.
 * @param sections the sub-sections
 */
protected void setSections(Sections sections) {
  _sections = sections;
  if (_sections == null) _sections = new Sections();
}

/**
 * Gets the XPath selection expression for selecting the section.
 * @return the selection expression
 */
public String getSelect() {
  return _select;
}
/**
 * Sets the XPath selection expression for selecting the section.
 * @param expression the selection expression
 */
public void setSelect(String expression) {
  _select = Val.chkStr(expression);
}

/**
 * Gets the status indicating if the selection expression should
 * be used to select nodes for deletion when an optional
 * section has not been filled (i.e. not checked within the editor).
 * @return true if the selection expression should be used for deletion
 */
public boolean getUseSelectForDelete() {
  return _useSelectForDelete;
}
/**
 * Sets the status indicating if the selection expression should
 * be used to select nodes for deletion when an optional
 * section has not been filled (i.e. not checked within the editor).
 * @param useSelectForDelete true if the selection expression 
 *        should be used for deletion
 */
public void setUseSelectForDelete(boolean useSelectForDelete) {
  _useSelectForDelete = useSelectForDelete;
}

/**
 * Gets the status indicating if the section should be displayed on the 
 * view details page.
 * @return true if the section should be displayed on the view details page
 */
public boolean getVisibleOnDetails() {
  return _visibleOnDetails;
}
/**
 * Sets the status indicating if the section should be displayed on the 
 * view details page.
 * @param visible  true if the section should be displayed on the view details page
 */
public void setVisibleOnDetails(boolean visible) {
  _visibleOnDetails = visible;
}

// methods =====================================================================

/**
 * Appends Faces display components for this section of the metadata details page.
 * @param schema the parent schema
 * @param context the UI context
 * @param parentComponent the parent component for this section
 */
public boolean appendDetailComponents(Schema schema,
                                      UiContext context,
                                      UIComponent parentComponent) {
  if (!getVisibleOnDetails()) return false;
  
  // initialize
  boolean bHadParameters = false;
  String sSectionId = getFacesId();
  String sCheckboxId = sSectionId+"-chk";
  String sRadioId = sSectionId+"-radio";
  String sImgId = sSectionId+"-img";
  String sBodyId = sSectionId+"-body";
  String sParamsId = sSectionId+"-params";
  boolean bIsExclusive = getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE);
  boolean bIsOptional = getObligation().equalsIgnoreCase(Section.OBLIGATION_OPTIONAL);
 
  HtmlPanelGroup panel = new HtmlPanelGroup();
  panel.setStyleClass("section");
  MessageBroker msgBroker = context.extractMessageBroker();
  
  // return if there is nothing to display
  if ((bIsExclusive || bIsOptional) && !getOpen()) {
    return false;
  }
  
  // determine the caption
  String sCaption = "";
  if (getLabel() != null) {
    sCaption = msgBroker.retrieveMessage(getLabel().getResourceKey());
  }
  if (sCaption.length() == 0) {
    sCaption = getKey();
  }
  
  // make the header
  StringBuffer sbOnclick = new StringBuffer();
  sbOnclick.append("mddOnSectionClicked('").append(sSectionId).append("')"); 
  
  HtmlPanelGrid headerTable = new HtmlPanelGrid();
  headerTable.setColumns(3);
  headerTable.setStyleClass("sectionHeader");
  headerTable.setSummary(msgBroker.retrieveMessage("catalog.general.designOnly"));
  headerTable.setOnclick(sbOnclick.toString());
  
  HtmlSelectBooleanCheckbox checkBox = new HtmlSelectBooleanCheckbox();
  checkBox.setId(sCheckboxId); 
  checkBox.setSelected(getOpen());
  checkBox.setStyle("display:none;");
  headerTable.getChildren().add(checkBox);
  
  HtmlGraphicImage img = new HtmlGraphicImage();
  img.setId(sImgId); 
  setGraphicUrl(img);
  headerTable.getChildren().add(img);
  
  HtmlOutputText caption = new HtmlOutputText();
  caption.setValue(sCaption);
  caption.setStyleClass("sectionCaption");
  headerTable.getChildren().add(caption);
  if (getLabel() != null) {
    panel.getChildren().add(headerTable);
  } else {
    panel.setStyleClass("");
  }
  
  // make the section body
  boolean bContainerOnly = (getLabel() == null);
  String sBodyClass = "sectionBody";
  if (bContainerOnly) sBodyClass= "";
  HtmlPanelGrid sectionBody = new HtmlPanelGrid();
  sectionBody.setId(sBodyId);
  sectionBody.setStyleClass(sBodyClass);
  setBodyDisplay(sectionBody);
  panel.getChildren().add(sectionBody);
  
  // make the table to hold section parameters, add all parameters
  HtmlPanelGrid parametersTable = new HtmlPanelGrid();
  parametersTable.setId(sParamsId);
  parametersTable.setColumns(2);
  parametersTable.setStyleClass("parameters");
  parametersTable.setSummary(msgBroker.retrieveMessage("catalog.general.designOnly"));
  boolean hasMap = false;
  for (Parameter parameter: getParameters().values()) {
    HtmlOutputText cmpLabel = null;
    UIComponent cmpValue = null;
    
    // check for a binary thumbnail
    if (parameter.getMeaningType().equalsIgnoreCase(Meaning.MEANINGTYPE_THUMBNAIL_URL)) {
      if (schema.getMeaning().getThumbnailUrl().length() == 0) {
        String uuid = Val.chkStr(context.extractHttpServletRequest().getParameter("uuid"));
        if ((uuid.length() > 0) && (schema.getMeaning().getThumbnailBinary().length() > 0)) {
          try {
            String thumbUrl = "/thumbnail?uuid="+URLEncoder.encode(uuid,"UTF-8");
            parameter.getContent().getSingleValue().setValue(thumbUrl);
          } catch (UnsupportedEncodingException e) {}
        }
      }
    }
    
    boolean bDisplay = parameter.getVisible() && parameter.getVisibleOnDetails() &&
                       !parameter.getContent().isValueEmpty() &&
                       (parameter.getInput() != null);
    if ((parameter.getInput() instanceof InputMap)) {
      cmpValue = parameter.getInput().makeOutputComponent(context,this,parameter);
      if (cmpValue != null) {
        parametersTable.getChildren().add(cmpValue);
        hasMap = true;
      }
    } else if (bDisplay) {
      cmpValue = parameter.getInput().makeOutputComponent(context,this,parameter);
      if (cmpValue != null) {
        cmpLabel = new HtmlOutputText();
        cmpLabel.setValue(msgBroker.retrieveMessage(parameter.getLabel().getResourceKey()));
        parametersTable.getChildren().add(cmpLabel);
        parametersTable.getChildren().add(cmpValue);
      }
    }
  }
  parametersTable.setColumnClasses(hasMap? ",parameterValue": "parameterLabel,parameterValue");
  if (parametersTable.getChildCount() > 0) {
    bHadParameters = true;
    sectionBody.getChildren().add(parametersTable);
  }
  
  // append all sub-sections
  for (Section section: getSections().values()) {
    boolean bHadSubParams = section.appendDetailComponents(schema,context,sectionBody);
    if (bHadSubParams) bHadParameters = true;
  }
  if (!bHadParameters) panel.setRendered(false);
  if ((panel != null) && (panel.getChildCount() > 0) && panel.isRendered()) {
    parentComponent.getChildren().add(panel);
  }
  
  return bHadParameters;
}

/**
 * Appends Faces editing components for this section of the metadata editor
 * @param schema the parent schema
 * @param context the UI context
 * @param parentComponent the parent component for this section
 */
public void appendEditorComponents(Schema schema,
                                   UiContext context,
                                   UIComponent parentComponent) {
  
  //appendDetailComponents(schema,context,parentComponent);
  //if (true) return;
  
  // initialize
  String sSectionId = getFacesId();
  String sCheckboxId = sSectionId+"-chk";
  String sRadioId = sSectionId+"-radio";
  String sImgId = sSectionId+"-img";
  String sBodyId = sSectionId+"-body";
  String sParamsId = sSectionId+"-params";
  boolean bIsExclusive = getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE);
  boolean bIsOptional = getObligation().equalsIgnoreCase(Section.OBLIGATION_OPTIONAL);
  
  HtmlPanelGroup panel = new HtmlPanelGroup();
  panel.setStyleClass("section");
  MessageBroker msgBroker = context.extractMessageBroker();
  
  // determine the caption
  String sCaption = "";
  if (getLabel() != null) {
    sCaption = msgBroker.retrieveMessage(getLabel().getResourceKey());
  }
  if (sCaption.length() == 0) {
    sCaption = getKey();
  }
  
  StringBuffer sbOnclick = new StringBuffer();
  sbOnclick.append("mdeOnSectionClicked(");
  sbOnclick.append("this,").append(bIsOptional).append(",").append(bIsExclusive);
  sbOnclick.append(",'").append(sSectionId).append("')"); 
  
  // add the caption
  if (bIsExclusive) {
    
    Sections exclusive = getExclusiveSiblings(schema);
    boolean bIsFirstExclusive = (exclusive.size() > 0) && 
                                (this == exclusive.values().iterator().next());
    if (bIsFirstExclusive) {
      HtmlSelectOneRadio radio = new HtmlSelectOneRadio();
      radio.setId(sRadioId);
      radio.setStyleClass("optionalSectionHeader");
      radio.setOnclick(sbOnclick.toString());
      ArrayList<SelectItem> radioItems = new ArrayList<SelectItem>();
      UISelectItems uiItems = new UISelectItems();
      uiItems.setValue(radioItems);
      radio.getChildren().add(uiItems);
      panel.getChildren().add(radio);
      int nIdx = 0;
      for (Section sibling: exclusive.values()) {
        String sItemLabel = "";
        String sItemId = sibling.getFacesId();
        if (sibling.getLabel() != null) {
          sItemLabel = msgBroker.retrieveMessage(sibling.getLabel().getResourceKey());
        }
        if (sItemLabel.length() == 0) {
          sItemLabel = sibling.getKey();
        }
        radioItems.add(new SelectItem(sItemId,sItemLabel));
        if (sibling.getOpen()) {
          radio.setValue(sItemId);
        }
        nIdx++;
      }
    }
    
  } else if (bIsOptional) {  
    
    HtmlPanelGroup captionPanel = new HtmlPanelGroup();
    captionPanel.setStyleClass("optionalSectionHeader");
    
    HtmlPanelGrid headerTable = new HtmlPanelGrid();
    headerTable.setColumns(2);
    headerTable.setStyleClass("optionalSectionHeader");
    headerTable.setSummary(msgBroker.retrieveMessage("catalog.general.designOnly"));
    
    HtmlSelectBooleanCheckbox checkBox = new HtmlSelectBooleanCheckbox();
    checkBox.setId(sCheckboxId); 
    checkBox.setSelected(getOpen());
    checkBox.setOnclick(sbOnclick.toString());
    headerTable.getChildren().add(checkBox);
    
    HtmlOutputLabel caption = new HtmlOutputLabel();
    // even label has to have unique id (for GlassFish)
    caption.setId(sCheckboxId+"label");
    caption.setFor(sCheckboxId);
    caption.setValue(sCaption);
    caption.setStyleClass("sectionCaption");
    headerTable.getChildren().add(caption);
    panel.getChildren().add(headerTable);    
    
  } else {
    
    HtmlPanelGrid headerTable = new HtmlPanelGrid();
    headerTable.setColumns(3);
    headerTable.setStyleClass("mandatorySectionHeader");
    headerTable.setSummary(msgBroker.retrieveMessage("catalog.general.designOnly"));
    headerTable.setOnclick(sbOnclick.toString());
    
    HtmlSelectBooleanCheckbox checkBox = new HtmlSelectBooleanCheckbox();
    checkBox.setId(sCheckboxId); 
    checkBox.setSelected(getOpen());
    //checkBox.setOnclick(sbOnclick.toString());
    checkBox.setStyle("display:none;");
    headerTable.getChildren().add(checkBox);
    
    HtmlGraphicImage img = new HtmlGraphicImage();
    img.setId(sImgId); 
    setGraphicUrl(img);
    headerTable.getChildren().add(img);
    
    HtmlOutputText caption = new HtmlOutputText();
    caption.setValue(sCaption);
    caption.setStyleClass("sectionCaption");
    
    headerTable.getChildren().add(caption);
    if (getLabel() != null) {
      panel.getChildren().add(headerTable);
    } else {
      panel.setStyleClass("");
    }
     
  }
  
  // make the section body
  boolean bContainerOnly = (getLabel() == null);
  String sBodyClass = "sectionBody";
  if (bContainerOnly) sBodyClass= "";
  HtmlPanelGrid sectionBody = new HtmlPanelGrid();
  sectionBody.setId(sBodyId);
  sectionBody.setStyleClass(sBodyClass);
  setBodyDisplay(sectionBody);
  panel.getChildren().add(sectionBody);
  
  
  // make the table to hold section parameters, add all parameters
  HtmlPanelGrid parametersTable = new HtmlPanelGrid();
  parametersTable.setId(sParamsId);
  parametersTable.setColumns(2);
  parametersTable.setStyleClass("parameters");
  parametersTable.setColumnClasses("parameterLabel,parameterInput");
  parametersTable.setSummary(msgBroker.retrieveMessage("catalog.general.designOnly"));
  for (Parameter parameter: getParameters().values()) {
    if (parameter.getVisible()) {
      UIComponent cmpLabel = null;
      if (parameter.getLabel() != null) {
        cmpLabel = parameter.getLabel().makeEditorLabel(context,this,parameter);
      }
      if (cmpLabel == null) cmpLabel = new HtmlOutputText();
      parametersTable.getChildren().add(cmpLabel);
      if (parameter.getInput() != null) {
        UIComponent cmp = parameter.getInput().makeInputComponent(context,this,parameter);
        if (cmp != null) parametersTable.getChildren().add(cmp);
      }
    } else {
      
      // non-visible hidden text
      if ((parameter.getInput() != null)  && (parameter.getInput() instanceof InputText)) {
        HtmlInputHidden cmpLabel = new HtmlInputHidden();
        cmpLabel.setId(parameter.getInput().getFacesId()+"-plcHldr");
        HtmlInputHidden cmp = new HtmlInputHidden();
        cmp.setId(parameter.getInput().getFacesId());
        parameter.getInput().setComponentValue(context,cmp,parameter);
        parametersTable.getChildren().add(cmpLabel);
        parametersTable.getChildren().add(cmp);
      }
    }
  }
  if (parametersTable.getChildCount() > 0) {
    sectionBody.getChildren().add(parametersTable);
  }
  
  // append all sub-sections
  for (Section section: getSections().values()) {
    section.appendEditorComponents(schema,context,sectionBody);
  }
  
  if ((panel != null) && (panel.getChildCount() > 0) && panel.isRendered()) {
    parentComponent.getChildren().add(panel);
  }
}

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>key open obligation select delete visibleOnDetails
 * <p/>
 * The following child nodes are configured:
 * <br/>label parameter[] section[]
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  
  // configure attributes
  setOpen(Val.chkBool(DomUtil.getAttributeValue(attributes,"open"),true));
  setObligation(DomUtil.getAttributeValue(attributes,"obligation"));
  setSelect(DomUtil.getAttributeValue(attributes,"select"));
  setDelete(DomUtil.getAttributeValue(attributes,"delete"));
  setUseSelectForDelete(Val.chkBool(DomUtil.getAttributeValue(attributes,"useSelectForDelete"),true));
  if (getUseSelectForDelete() && (getDelete().length() == 0)) {
    setDelete(getSelect());
  }
  setVisibleOnDetails(Val.chkBool(DomUtil.getAttributeValue(attributes,"visibleOnDetails"),true));
  
  // configure the label component
  setLabel(context.getFactory().newLabel(context,DomUtil.findFirst(node,"label")));
  
  // configure the parameter components
  Node[] aryParameters = DomUtil.findChildren(node,"parameter");
  for (Node ndParameter: aryParameters) {
    getParameters().add(context.getFactory().newParameter(context,ndParameter));
  }
  
  // configure the sub-section components
  Node[] arySections = DomUtil.findChildren(node,"section");
  for (Node ndSection: arySections) {
    Section child = context.getFactory().newSection(context,ndSection);
    if (child != null) {
      child.setParent(this);
      getSections().add(child);
    }
  }
  getSections().checkExclusiveOpenStatus();
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new Section(this);
 */
public Section duplicate() {
  return new Section(this);
}

/**
 * Appends property information for the component to a StringBuffer.
 * <br/>The method is intended to support "FINEST" logging.
 * <br/>super.echo should be invoked prior appending any local information.
 * @param sb the StringBuffer to use when appending information
 */
@Override
public void echo(StringBuffer sb) {
  super.echo(sb);
  sb.append(" open=\"").append(getOpen()).append("\"");
  sb.append(" obligation=\"").append(getObligation()).append("\"");
  //sb.append(" useSelectForDelete=\"").append(getUseSelectForDelete()).append("\"");
  sb.append(" visibleOnDetails=\"").append(getVisibleOnDetails()).append("\"");
  if (getSelect().length() > 0) {
    sb.append("\n select=\"").append(getSelect()).append("\"");
  }
  if (getDelete().length() > 0) {
    sb.append("\n delete=\"").append(getDelete()).append("\"");
  }
  
  if (getLabel() != null) sb.append("\n").append(getLabel());
  if (getParameters().size() > 0) sb.append("\n").append(getParameters());
  if (getSections().size() > 0) sb.append("\n").append(getSections());
}

/**
 * Evaluates a section based upon the supplied metadata document.
 * <p/>
 * The default behavior is to invoke the "evaluate" method
 * for each parameter within the section as well as each sub-section.
 * @param schema the schema being evaluated
 * @param dom the metadata document
 * @param xpath an XPath object configured with an appropriate 
 *        Namespace context for the schema
 * @throws XPathExpressionException if an evaluation expression fails 
 */
public void evaluate(Schema schema, Document dom, XPath xpath)
  throws XPathExpressionException {
  
  // determine if an optional section is present
  boolean bIsExclusive = getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE);
  boolean bIsOptional = getObligation().equalsIgnoreCase(Section.OBLIGATION_OPTIONAL);
  if (bIsExclusive || bIsOptional) {
    setOpen(false);
    if (getSelect().length() > 0) {
      NodeList nl = (NodeList)xpath.evaluate(getSelect(),dom,XPathConstants.NODESET);
      setOpen(nl.getLength() > 0);
    }
  }
  
  // evaluate parameters and sub-sections
  for (Parameter parameter: getParameters().values()) {
    parameter.evaluate(schema,dom,xpath);
  }
  for (Section section: getSections().values()) {
    section.evaluate(schema,dom,xpath);
  }
  getSections().checkExclusiveOpenStatus();
}

/**
 * Force a section open within the editor.
 * @param editorForm the Faces HtmlForm for the metadata editor
 */
public void forceOpen(UIComponent editorForm) {
  String sSectionId = getFacesId();
  String sCheckboxId = sSectionId+"-chk";
  String sRadioId = sSectionId+"-radio";
  String sImgId = sSectionId+"-img";
  String sBodyId = sSectionId+"-body";
  boolean bIsExclusive = getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE);
  boolean bIsOptional = getObligation().equalsIgnoreCase(Section.OBLIGATION_OPTIONAL);
  
  setOpen(true);
  UIComponent component = editorForm.findComponent(sCheckboxId);
  if ((component != null) && (component instanceof UISelectBoolean)) {
    UISelectBoolean checkbox = (UISelectBoolean)component;
    checkbox.setSelected(getOpen());
  }
  if (!bIsExclusive && !bIsOptional) {
    component = editorForm.findComponent(sImgId);
    if ((component != null) && (component instanceof UIGraphic)) {
      setGraphicUrl((UIGraphic)component);
    }
  }
  component = editorForm.findComponent(sBodyId);
  if ((component != null) && (component instanceof HtmlPanelGrid)) {
    setBodyDisplay((HtmlPanelGrid)component);
  }
  if (getParent() != null) {
    getParent().forceOpen(editorForm);
  }
}

/**
 * Sets the display style for the grid holding the section content.
 * <br/>(open=display:block, closed=display:none)
 * @param grid the grid component
 */
private void setBodyDisplay(HtmlPanelGrid grid) {
  if (getOpen()) {
    grid.setStyle("display:block;");
  } else {
    grid.setStyle("display:none;");
  }
}

/**
 * Sets the url for the section graphic (open/closed).
 * @param graphic the graphic component
 */
private void setGraphicUrl(UIGraphic graphic) {
  if (getOpen()) {
    graphic.setUrl("/catalog/images/section_open.gif");
  } else {
    graphic.setUrl("/catalog/images/section_closed.gif");
  }
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * The default behavior is to invoke the "unBind" method for
 * the each parameter as well as each sub-section. 
 * @param schema the active schema
 * @param context the UI context
 * @param editorForm the Faces HtmlForm for the metadata editor
 * @throws SchemaException if an associated Faces UIComponent cannot be located
 */
public void unBind(Schema schema,UiContext context, UIComponent editorForm) 
  throws SchemaException {
  _wasUnbound = true;
  String sSectionId = getFacesId();
  String sCheckboxId = sSectionId+"-chk";
  String sRadioId = sSectionId+"-radio";
  String sImgId = sSectionId+"-img";
  String sBodyId = sSectionId+"-body";
  boolean bIsExclusive = getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE);
  boolean bIsOptional = getObligation().equalsIgnoreCase(Section.OBLIGATION_OPTIONAL);
  UIComponent component;
  
  // determine whether or not this section is open
  if (bIsExclusive) {
    Sections exclusive = getExclusiveSiblings(schema);
    boolean bIsFirstExclusive = (exclusive.size() > 0) && 
                                (this == exclusive.values().iterator().next());
    if (bIsFirstExclusive) {
      component = editorForm.findComponent(sRadioId);
      if ((component != null) && (component instanceof HtmlSelectOneRadio)) {
        HtmlSelectOneRadio radio = (HtmlSelectOneRadio)component;
        if ((radio.getValue() != null) && (radio.getValue() instanceof String)) {
          String sChosenId = Val.chkStr((String)radio.getValue());
          for (Section sibling: exclusive.values()) {
            boolean bOpen = sChosenId.equals(sibling.getFacesId());
            sibling.setOpen(bOpen);
          }
        }
      }
    }
  } else {
    component = editorForm.findComponent(sCheckboxId);
    if ((component != null) && (component instanceof UISelectBoolean)) {
      UISelectBoolean checkbox = (UISelectBoolean)component;
      setOpen(checkbox.isSelected());
    }
  }
  
  // set the graphic for regular sections upon the open status
  if (!bIsExclusive && !bIsOptional) {
    component = editorForm.findComponent(sImgId);
    if ((component != null) && (component instanceof UIGraphic)) {
      setGraphicUrl((UIGraphic)component);
    }
  }
  
  // set the body display style based upon the open status
  component = editorForm.findComponent(sBodyId);
  if ((component != null) && (component instanceof HtmlPanelGrid)) {
    setBodyDisplay((HtmlPanelGrid)component);
  }
  
  // un-bind all parameters and sub-sections
  for (Parameter parameter: getParameters().values()) {
    parameter.unBind(context,editorForm);
  }
  for (Section section: getSections().values()) {
    section.unBind(schema,context,editorForm);
  }
}

/**
 * Updates the metadata document template based upon entered parameter value(s).
 * <p/>
 * The default behavior is to invoke the "update" method
 * for each parameter within the section as well as each sub-section.
 * @param dom the metadata document template for the schema
 * @param xpath an XPath object configured with an appropriate 
 *        Namespace context for the schema
 * @throws XPathExpressionException if an expression fails 
 * @throws SchemaException if the update fails
 */
public void update(Document dom, XPath xpath) 
  throws XPathExpressionException, SchemaException {
  
  // don't update if the section is optional and is not open within the editor
  boolean bProcessChildren = true;
  boolean bIsExclusive = getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE);
  boolean bIsOptional = getObligation().equalsIgnoreCase(Section.OBLIGATION_OPTIONAL);
  if ((bIsExclusive || bIsOptional) && !getOpen()) {
    bProcessChildren = false;
    if (getDelete().length() > 0) {
      Content.deleteNodes(dom,xpath,getDelete());
    }
  }
  
  // update parameters and sub-sections
  if (bProcessChildren) {
    for (Parameter parameter: getParameters().values()) {
      parameter.update(dom,xpath);
    }
    for (Section section: getSections().values()) {
      section.update(dom,xpath);
    }
  }
}

/**
 * Validates a section.
 * <p/>
 * The default behavior is to invoke the "validate" method
 * for each parameter within the section as well as each sub-section.
 * <p/>
 * Encountered errors should be appended to the 
 * schema.getValidationErrors() collection.
 * <p/>
 * Parameter values associated with meaning should be used to populate
 * schema.getMeaning() values.
 * @param schema the schema being validated
 */
public void validate(Schema schema) {
  
  // don't validate if the section is optional and is not open within the editor
  boolean bProcessChildren = true;
  boolean bIsExclusive = getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE);
  boolean bIsOptional = getObligation().equalsIgnoreCase(Section.OBLIGATION_OPTIONAL);
  if ((bIsExclusive || bIsOptional) && !getOpen()) {
    bProcessChildren = false;
  }
  
  // validate parameters and sub-sections
  if (bProcessChildren) {
    for (Parameter parameter: getParameters().values()) {
      parameter.validate(schema,this);
    }
    for (Section section: getSections().values()) {
      section.validate(schema);
    }
  }
}

/**
 * Selects all parameters conforming to the condiditions defined by predicate.
 * @param predicate predicate
 * @return list of selected parameters
 */
public List<Parameter> selectParameters(Predicate predicate) {
  return getParameters().selectParameters(predicate);
}
}

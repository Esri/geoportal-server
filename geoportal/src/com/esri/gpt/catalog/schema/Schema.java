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
import com.esri.gpt.catalog.discovery.PropertyMeaning;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.gxe.GxeContext;
import com.esri.gpt.catalog.gxe.GxeDefinition;
import com.esri.gpt.catalog.gxe.GxeLoader;
import com.esri.gpt.catalog.schema.indexable.IndexableContext;
import com.esri.gpt.catalog.schema.indexable.Indexables;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.components.LiveDataTag;
import com.esri.gpt.framework.jsf.components.UILiveData;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XsltTemplates;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Defines a metadata schema. 
 */
public class Schema extends Component {
  
  //  class variables =============================================================
  private static Logger LOGGER = Logger.getLogger(Schema.class.getName());
  private static XsltTemplates XSLTTEMPLATES = new XsltTemplates();

  // instance variables ==========================================================
  private String            _activeDocumentXml;
  private String            _cswBriefXslt = "";
  private String            _cswOutputSchema = "";
  private String            _cswSummaryXslt = "";
  private String            _detailsXslt = "";
  private boolean           _editable = true;
  private EsriTags          _evaluatedEsriTags = null;
  private GxeDefinition     _gxeEditorDefinition;
  private Indexables        _indexables;
  private Interrogation     _interrogation;
  private Label             _label;
  private Meaning           _meaning;
  private Namespaces        _namespaces;
  private PropertyMeanings  _propertyMeanings = new PropertyMeanings();
  private String            _schematronXslt = "";
  private Sections          _sections;
  private String            _templateFile = "";
  private String            _toEsriXslt = "";
  private ValidationErrors  _validationErrors;
  private String            _xsdLocation = "";
  private String            _toEsriItemInfoXslt = "";

  // constructors ================================================================
  
  /** Default constructor. */
  public Schema() {
    this(null);
  }

  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public Schema(Schema objectToDuplicate) {
    super(objectToDuplicate);
    if (objectToDuplicate == null) {
      setMeaning(new Meaning(this._propertyMeanings));
      setNamespaces(new Namespaces());
      setInterrogation(new Interrogation());
      setSections(new Sections());
    } else {
      setPropertyMeanings(objectToDuplicate.getPropertyMeanings());
      setEditable(objectToDuplicate.getEditable());
      setTemplateFile(objectToDuplicate.getTemplateFile());
      setCswBriefXslt(objectToDuplicate.getCswBriefXslt());
      setCswOutputSchema(objectToDuplicate.getCswOutputSchema());
      setCswSummaryXslt(objectToDuplicate.getCswSummaryXslt());
      setDetailsXslt(objectToDuplicate.getDetailsXslt());
      setToEsriXslt(objectToDuplicate.getToEsriXslt());
      setXsdLocation(objectToDuplicate.getXsdLocation());
      setMeaning(new Meaning(this._propertyMeanings));
      setNamespaces(new Namespaces(objectToDuplicate.getNamespaces()));
      setInterrogation(objectToDuplicate.getInterrogation().duplicate());
      setSchematronXslt(objectToDuplicate.getSchematronXslt());
      setSections(new Sections(objectToDuplicate.getSections(), null));
      if (objectToDuplicate.getLabel() != null) {
        setLabel(objectToDuplicate.getLabel().duplicate());
      }
      if (objectToDuplicate.getIndexables() != null) {
        setIndexables(new Indexables(objectToDuplicate.getIndexables()));
      }
      if (objectToDuplicate.getGxeEditorDefinition() != null) {
        setGxeEditorDefinition(objectToDuplicate.getGxeEditorDefinition());
      }
      setToEsriItemInfoXslt(objectToDuplicate.getToEsriItemInfoXslt());
    }
  }

  // properties ==================================================================
  
  /**
   * Gets the XML associated with the metadata document that was most recently 
   * evaluated for this schema.
   * @return the XML
   */
  public String getActiveDocumentXml() {
    return _activeDocumentXml;
  }
  /**
   * Sets the XML associated with the metadata document that was most recently 
   * evaluated for this schema.
   * @param xml the XML
   */
  public void setActiveDocumentXml(String xml) {
    _activeDocumentXml = xml;
  }
  
  /**
   * Gets the XSLT (file path) for generating the brief CSW response (optional).
   * <br/>Only applies if the document can generate non Dublin Core responses.
   * @return the file path to the brief XSLT
   */
  public String getCswBriefXslt() {
    return _cswBriefXslt;
  }
  /**
   * Sets the XSLT (file path) for generating the brief CSW response (optional).
   * <br/>Only applies if the document can generate non Dublin Core responses.
   * @param xslt the file path to the brief XSLT
   */
  public void setCswBriefXslt(String xslt) {
    _cswBriefXslt = Val.chkStr(xslt);
  }
  
  /**
   * Gets the CSW output schema (namespace) associated with the document.
   * <br/>Only applies if the document can generate non Dublin Core responses.
   * @return the CSW output schema
   */
  public String getCswOutputSchema() {
    return _cswOutputSchema;
  }
  /**
   * Sets the CSW output schema (namespace) associated with the document.
   * <br/>Only applies if the document can generate non Dublin Core responses.
   * @param namespace the CSW output schema
   */
  public void setCswOutputSchema(String namespace) {
    _cswOutputSchema = Val.chkStr(namespace);
  }
  
  /**
   * Gets the XSLT (file path) for generating the summary CSW response (optional).
   * <br/>Only applies if the document can generate non Dublin Core responses.
   * @return the file path to the summary XSLT
   */
  public String getCswSummaryXslt() {
    return _cswSummaryXslt;
  }
  /**
   * Sets the XSLT (file path) for generating the summary CSW response (optional).
   * <br/>Only applies if the document can generate non Dublin Core responses.
   * @param xslt the file path to the summary XSLT
   */
  public void setCswSummaryXslt(String xslt) {
    _cswSummaryXslt = Val.chkStr(xslt);
  }
  
  /**
   * Gets the XSLT (file path) for displaying metadata details (as HTML).
   * @return the file path to the details XSLT
   */
  public String getDetailsXslt() {
    return _detailsXslt;
  }
  /**
   * Sets the XSLT (file path) for displaying metadata details (as HTML).
   * @param xslt the file path to the details XSLT
   */
  public void setDetailsXslt(String xslt) {
    _detailsXslt = Val.chkStr(xslt);
  }
  
  /**
   * Gets the editable status.
   * @return true if this schema is editable within the metadata editor
   */
  public boolean getEditable() {
    return _editable;
  }
  /**
   * Sets the editable status.
   * @param editable true if this schema is editable within the metadata editor
   */
  public void setEditable(boolean editable) {
    _editable = editable;
  }
  
  /**
   * Gets the Geoportal XML editor definition (optional).
   * @return the editor definition
   */
  public GxeDefinition getGxeEditorDefinition() {
    return _gxeEditorDefinition;
  }
  /**
   * Sets the Geoportal XML editor definition (optional).
   * @param definition the editor definition
   */
  public void setGxeEditorDefinition(GxeDefinition definition) {
    _gxeEditorDefinition = definition;
  }
  
  /**
   * Gets the indexable properties.
   * @return the indexable properties
   */
  public Indexables getIndexables() {
    return this._indexables;
  }
  /**
   * Sets the indexable properties.
   * @param indexables the indexable properties
   */
  public void setIndexables(Indexables indexables) {
    this._indexables = indexables;
  }

  /**
   * Gets the interrogation component.
   * @return the interrogation component
   */
  public Interrogation getInterrogation() {
    return _interrogation;
  }
  /**
   * Sets the interrogation component.
   * @param interrogation the interrogation component
   */
  protected void setInterrogation(Interrogation interrogation) {
    _interrogation = interrogation;
    if (_interrogation == null) {
      _interrogation = new Interrogation();
    }
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
  protected void setLabel(Label label) {
    _label = label;
  }

  /**
   * Gets the meaning component for the schema.
   * @return the meaning component
   */
  public Meaning getMeaning() {
    return _meaning;
  }
  /**
   * Sets the meaning component for the schema.
   * @param meaning the meaning component
   */
  private void setMeaning(Meaning meaning) {
    _meaning = meaning;
  }

  /**
   * Gets the namespaces for the schema.
   * @return the schema namespaces
   */
  public Namespaces getNamespaces() {
    return _namespaces;
  }
  /**
   * Sets the namespaces for the schema.
   * @param namespaces the schema namespaces
   */
  protected void setNamespaces(Namespaces namespaces) {
    _namespaces = namespaces;
    if (_namespaces == null) {
      _namespaces = new Namespaces();
    }
  }

  /**
   * Gets the configured property meanings.
   * @return the property meanings
   */
  protected PropertyMeanings getPropertyMeanings() {
    return _propertyMeanings;
  }
  /**
   * Sets the configured property meanings.
   * @param meanings the property meanings
   */
  protected void setPropertyMeanings(PropertyMeanings meanings) {
    this._propertyMeanings = meanings;
  }
  
  /**
   * Gets the XSLT (file path) used to perform Schematron validation.
   * <p/>
   * If supplied, the metadata document will be validated prior to publication.
   * <br/>The XSLT must produce an SVRL document (Schematron Validation Report Language). 
   * @return the file path to the XSLT
   */
  public String getSchematronXslt() {
    return this._schematronXslt;
  }
  /**
   * Sets the XSLT (file path) used to perform Schematron validation.
   * <p/>
   * If supplied, the metadata document will be validated prior to publication.
   * <br/>The XSLT must produce an SVRL document (Schematron Validation Report Language). 
   * @param xslt the file path to the XSLT
   */
  public void setSchematronXslt(String xslt) {
    this._schematronXslt = Val.chkStr(xslt);
  }

  /**
   * Gets XSLT (file path) used to perform to ESRI_ItemInformation transformation.
   * @return XSLT file path
   */
  public String getToEsriItemInfoXslt() {
    return _toEsriItemInfoXslt;
  }

  /**
   * Sets XSLT (file path) used to perform to ESRI_ItemInformation transformation.
   * @param xslt XSLT file path
   */
  public void setToEsriItemInfoXslt(String xslt) {
    this._toEsriItemInfoXslt = Val.chkStr(xslt);
  }

  /**
   * Gets the sections for the schema.
   * @return the schema sections
   */
  public Sections getSections() {
    return _sections;
  }
  /**
   * Sets the sections for the schema.
   * @param sections the schema sections
   */
  protected void setSections(Sections sections) {
    _sections = sections;
    if (_sections == null) {
      _sections = new Sections();
    }
  }

  /**
   * Gets the template file path.
   * <p/>
   * The template is an XML file used for the creation and editing of
   * metadata documents for this schema.
   * @return the template file
   */
  public String getTemplateFile() {
    return _templateFile;
  }
  /**
   * Sets the template file path.
   * <p/>
   * The template is an XML file used for the creation and editing of
   * metadata documents for this schema.
   * @param templateFile the template file
   */
  public void setTemplateFile(String templateFile) {
    _templateFile = Val.chkStr(templateFile);
  }

  /**
   * Gets the XSLT (file path) for translating to an ESRI format.
   * <p/>
   * When a schema is neither FGDC or EsriIso, the document must
   * be translated to an ESRI format enclosed as a binary node prior
   * to publishing to the ArcIMS metdata server.
   * @return the file path to the XSLT
   */
  public String getToEsriXslt() {
    return _toEsriXslt;
  }
  /**
   * Sets the XSLT (file path) for translating to an ESRI format.
   * <p/>
   * When a schema is neither FGDC or EsriIso, the document must
   * be translated to an ESRI format enclosed as a binary node prior
   * to publishing to the ArcIMS metdata server.
   * @param xslt the file path to the XSLT
   */
  public void setToEsriXslt(String xslt) {
    _toEsriXslt = Val.chkStr(xslt);
  }

  /**
   * Gets the validation errors.
   * @return the schema sections
   */
  public ValidationErrors getValidationErrors() {
    return _validationErrors;
  }
  /**
   * Sets the validation errors.
   * @param errors validation errors
   */
  protected void setValidationErrors(ValidationErrors errors) {
    _validationErrors = errors;
    if (_validationErrors == null) {
      _validationErrors = new ValidationErrors();
    }
  }

  /**
   * Gets the location of the XML Schema definition file for the metadata standard.
   * <p/>
   * Not all metadata standards will have an XSD. If supplied, the 
   * metadata document will be validated against the XSD pror to
   * publication.
   * @return the XSD location (URL)
   */
  public String getXsdLocation() {
    return _xsdLocation;
  }
  /**
   * Sets the location of the XML Schema definition file for the metadata standard.
   * <p/>
   * Not all metadata standards will have an XSD. If supplied, the 
   * metadata document will be validated against the XSD pror to
   * publication.
   * @param url the XSD location (URL)
   */
  public void setXsdLocation(String url) {
    _xsdLocation = Val.chkStr(url);
  }

  // methods =====================================================================
  /**
   * Appends all sections of the schema to the sections component of the
   * metadata details page.
   * @param context the UI context
   * @param sectionsComponent the sections component of the metadata details page
   */
  public void appendDetailSections(UiContext context,
    UIComponent sectionsComponent) {
    appendEnvelopeArray(sectionsComponent);
    boolean liveDataSection = false;
    for (Section section : getSections().values()) {
      section.appendDetailComponents(this, context, sectionsComponent);
      if (section.getKey().equalsIgnoreCase("identification") && !liveDataSection) {
        liveDataSection = true;
        //appendLiveDataSection(sectionsComponent);
      }
    }
    if (!liveDataSection) {
      liveDataSection = true;
      //appendLiveDataSection(sectionsComponent);
    }
    assureComponentsIds(sectionsComponent);
  }

  /**
   * Appends all sections of the schema to the metadata editor sections component.
   * @param context the UI context
   * @param sectionsComponent the sections component of the metadata editor
   */
  public void appendEditorSections(UiContext context,
    UIComponent sectionsComponent) {
    appendEnvelopeArray(sectionsComponent);
    for (Section section : getSections().values()) {
      section.appendEditorComponents(this, context, sectionsComponent);
    }
    assureComponentsIds(sectionsComponent);
  }

  /**
   * Appends the Array of envelope coodinates as a script.
   * @param sectionsComponent the active UI component for the section
   */
  @SuppressWarnings("unchecked")
  private void appendEnvelopeArray(UIComponent sectionsComponent) {
    String[] aEnvIds = {"", "", "", ""};
    findEnvelopeIds(getSections(), aEnvIds);
    String sEnvIds = "'" + aEnvIds[0] + "','" + aEnvIds[1] + "','" + aEnvIds[2] + "','" + aEnvIds[3] + "'";
    String sScript = "<script>var mdeEnvelopeIds = new Array(" + sEnvIds + ");</script>";
    HtmlOutputText script = new HtmlOutputText();
    script.setEscape(false);
    script.setValue(sScript);
    sectionsComponent.getChildren().add(script);
  }

  /**
   * Appends live data section.
   * @param sectionsComponent the active UI component for the section
   */
  @SuppressWarnings("unchecked")
  private void appendLiveDataSection(UIComponent sectionsComponent) {
    FacesContextBroker broker = new FacesContextBroker();
    String contextPath = broker.extractHttpServletRequest().getContextPath();
    String imgOpen = contextPath + "/catalog/images/section_open.gif";
    String imgClosed = contextPath + "/catalog/images/section_closed.gif";
    String caption = broker.extractMessageBroker().retrieveMessage("catalog.search.viewMetadataDetails.liveData");

    String sScript =
      "<script>" +
      "function onCreatePlaceholder(node) {" +
      "node.innerHTML = \"<span class=\\\"section\\\">" +
      "<table class=\\\"sectionHeader\\\" summary=\\\"This table is for design purposes only.\\\" onclick=\\\"gpt.LiveData.onClickHandler('\" +node.id+ \"','" +imgOpen+ "','" +imgClosed+ "')\\\">" +
      "<tr>" +
      "<td>" +
      "<input type=\\\"checkbox\\\" style=\\\"display: none;\\\" id=\\\"\" +node.id+\"-chk\\\"/>"+
      "</td>"+
      "<td>"+
      "<img src=\\\"" +imgOpen+ "\\\" id=\\\"\" +node.id+\"-img\\\"/>"+
      "</td>"+
      "<td>"+
      "<span class=\\\"sectionCaption\\\" >"+
      caption +
      "</span>"+
      "</td>"+
      "</tr>"+
      "</table>"+
      "<table class=\\\"sectionBody\\\" summary=\\\"This table is for design purposes only.\\\" id=\\\"\" +node.id+\"-body\\\">"+
      "<tr><td id=\\\"\" +node.id+\"-cell\\\"></td></tr>" +
      "</table>"+
      "</span>\";"+
      "return dojo.byId(node.id+\"-cell\"); }" +
      "</script>";
    
    HtmlOutputText script = new HtmlOutputText();
    script.setEscape(false);
    script.setValue(sScript);
    sectionsComponent.getChildren().add(script);

    LiveDataTag liveDataTag = new LiveDataTag();
    liveDataTag.setUrl(getMeaning().getResourceUrl());
    liveDataTag.setOnCreatePlaceholder("onCreatePlaceholder");

    UILiveData uiLiveData = new UILiveData();
    liveDataTag.setProperties(uiLiveData);

    sectionsComponent.getChildren().add(uiLiveData);
  }
  
  /**
   * Configures the object based upon a node loaded from a 
   * schema configuration XML.
   * <br/>The super.configure method should be invoked prior to any
   * sub-class configuration.
   * <p/>
   * The following attributes are configured:
   * <br/>key editable templateFile detailsXslt cswOutputSchema cswBriefXslt cswSummaryXslt xsdLocation
   * <p/>
   * The following child nodes are configured:
   * <br/>label namespace[] interrogation section[]
   * @param context the configuration context
   * @param node the configuration node
   * @param attributes the attributes of the configuration node
   */
  @Override
  public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
    super.configure(context, node, attributes);

    // configure attributes
    setEditable(Val.chkBool(DomUtil.getAttributeValue(attributes, "editable"), true));
    setTemplateFile(DomUtil.getAttributeValue(attributes, "templateFile"));
    setDetailsXslt(DomUtil.getAttributeValue(attributes, "detailsXslt"));
    setCswOutputSchema(DomUtil.getAttributeValue(attributes, "cswOutputSchema"));
    setCswBriefXslt(DomUtil.getAttributeValue(attributes, "cswBriefXslt"));
    setCswSummaryXslt(DomUtil.getAttributeValue(attributes, "cswSummaryXslt"));
    setToEsriXslt(DomUtil.getAttributeValue(attributes, "toEsriXslt"));
    setXsdLocation(DomUtil.getAttributeValue(attributes, "xsdLocation"));
    setSchematronXslt(DomUtil.getAttributeValue(attributes, "schematronXslt"));
    setToEsriItemInfoXslt(DomUtil.getAttributeValue(attributes, "toEsriItemInfoXslt"));

    // configure the label component
    setLabel(context.getFactory().newLabel(context, DomUtil.findFirst(node, "label")));

    // configure the namespace components
    Node[] aryNamespaces = DomUtil.findChildren(node, "namespace");
    for (Node ndNamespace : aryNamespaces) {
      getNamespaces().add(context.getFactory().newNamespace(context, ndNamespace));
    }

    // configure the interrogation component
    setInterrogation(context.getFactory().newInterrogation(
      context, DomUtil.findFirst(node, "interrogation")));

    // configure the section components
    Node[] arySections = DomUtil.findChildren(node, "section");
    for (Node ndSection : arySections) {
      getSections().add(context.getFactory().newSection(context, ndSection));
    }
    getSections().checkExclusiveOpenStatus();
    
    // indexable properties, XML editor settings
    NodeList nl = node.getChildNodes();
    for (int i=0;i<nl.getLength();i++) {
      Node nd = nl.item(i);
      if (nd.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = Val.chkStr(nd.getNodeName());
        
        if (nodeName.equalsIgnoreCase("indexables")) {
          Indexables idxables = new Indexables();
          idxables.configure(context,nd,nd.getAttributes());
          if (idxables.hasPropertiesOrSiblings()) {
            if (this.getIndexables() == null) {
              this.setIndexables(idxables);
            } else {
              this.getIndexables().addSibling(idxables);
            }
          }
          
        } else if (nodeName.equalsIgnoreCase("editor")) { 
          String sLoc = Val.chkStr(DomUtil.getAttributeValue(nd.getAttributes(),"fileName"));
          if (sLoc.length() > 0) {
            GxeDefinition gxeDefinition = new GxeDefinition();
            gxeDefinition.setKey(this.getKey());
            gxeDefinition.setFileLocation(sLoc);
            this.setGxeEditorDefinition(gxeDefinition);
            
            GxeContext gxeContext = new GxeContext();
            GxeLoader gxeLoader = new GxeLoader();
            try {
              gxeLoader.loadDefinition(gxeContext,gxeDefinition);
            } catch (Exception e) {
              LOGGER.log(Level.CONFIG,"Error loading GXE XML Editor definition.",e);
            }
          }
     
        }
      }
    }
    if (this.getIndexables() != null) {
      if (!this.getIndexables().hasPropertiesOrSiblings()) {
        this.setIndexables(null);
      }
    }
    
  }

  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new Schema(this);
   */
  public Schema duplicate() {
    return new Schema(this);
  }

  /**
   * Ensures the the minimal components of a schema.
   * <p/>
   * The schema should have been evaluated at some point prior to executing this method.
   * <br/>At the moment, this method doesn't throw exceptions. An empty title is set to "Untitled",
   * an invalid envelope is set to the extent of the world.
   */
  public void ensureMinimals() {
    if (getMeaning().getTitle().length() == 0) {
      getMeaning().setTitle("Untitled");
    }
    if (!getMeaning().getEnvelope().isValid()) {
      PropertyMeaning meaning = this.getPropertyMeanings().get("geometry");
      if ((meaning != null) && (meaning instanceof PropertyMeaning.Geometry)) {
        Envelope def = ((PropertyMeaning.Geometry)meaning).getDefaultEnvelope();
        if ((def != null) && def.isValid()) {
          getMeaning().getEnvelope().put(def.getMinX(),def.getMinY(),def.getMaxX(),def.getMaxY());
        }
      }
    }
    String contentType = Val.chkStr(getMeaning().getArcIMSContentType());
    if (contentType.length() == 0) {
      getMeaning().setArcIMSContentType("unknown");
    }
  }

  /**
   * Evaluates a schema based upon the supplied metadata document.
   * <p/>
   * The default behavior is to invoke the "evaluate" method
   * for each section within the schema.
   * @param dom the metadata document
   * @throws XPathExpressionException if an evaluation expression fails 
   */
  public void evaluate(Document dom) throws XPathExpressionException {
    NamespaceContextImpl ns = new NamespaceContextImpl(getNamespaces());
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(ns);
    for (Section section : getSections().values()) {
      section.evaluate(this,dom,xpath);
    }
    getSections().checkExclusiveOpenStatus();
    _evaluatedEsriTags = new EsriTags();
    _evaluatedEsriTags.evaluate(this,dom);
    getMeaning().applyEsriTags(this,_evaluatedEsriTags);
    
    if (this.getIndexables() != null) {
      IndexableContext ictx = new IndexableContext(this.getPropertyMeanings());
      this.getIndexables().setIndexableContext(ictx);
      this.getIndexables().evaluate(this,ictx,dom,xpath);
      ictx.resolve(this,dom,_evaluatedEsriTags);
    }
    
    /*
    try {
      com.esri.gpt.catalog.classification.ClsConfig ccfg = new com.esri.gpt.catalog.classification.ClsConfig();
      ccfg.configure();
      com.esri.gpt.catalog.classification.ClsContext cctx = new com.esri.gpt.catalog.classification.ClsContext();
      cctx.setConfig(ccfg);
      cctx.classify(dom,this);
    } catch (Exception e) {
      e.printStackTrace();
    }
    */
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
    sb.append(" editable=\"").append(getEditable()).append("\"");
    sb.append(" templateFile=\"").append(getTemplateFile()).append("\"");
    if (this.getCswOutputSchema().length() > 0) {
      sb.append(" cswOutputSchema=\"").append(this.getCswOutputSchema()).append("\"");
    }
    if (this.getCswBriefXslt().length() > 0) {
      sb.append(" cswBriefXslt=\"").append(this.getCswBriefXslt()).append("\"");
    }
    if (this.getCswSummaryXslt().length() > 0) {
      sb.append(" cswSummaryXslt=\"").append(this.getCswSummaryXslt()).append("\"");
    }
    if (getDetailsXslt().length() > 0) {
      sb.append(" detailsXslt=\"").append(getDetailsXslt()).append("\"");
    }
    if (getToEsriXslt().length() > 0) {
      sb.append(" toEsriXslt=\"").append(getToEsriXslt()).append("\"");
    }
    if (getXsdLocation().length() > 0) {
      sb.append(" xsdLocation=\"").append(getXsdLocation()).append("\"");
    }
    if (getSchematronXslt().length() > 0) {
      sb.append(" schematronXslt=\"").append(getSchematronXslt()).append("\"");
    }
    if (getToEsriItemInfoXslt().length() > 0) {
      sb.append(" toEsriItemInfoXslt=\"").append(getToEsriItemInfoXslt()).append("\"");
    }
    if (getLabel() != null) {
      sb.append("\n").append(getLabel());
    }
    if ((getNamespaces() != null) && (getNamespaces().size() > 0)) {
      sb.append("\n").append(getNamespaces());
    }
    sb.append("\n").append(getInterrogation());
    sb.append("\n").append(getSections());
  }

  /**
   * Finds the faces ids associated with the envelope input components.
   * @param sections the sections to check
   * @param ids (xmin-id,ymin-id,xmax-id,ymax-id)
   */
  private void findEnvelopeIds(Sections sections, String[] ids) {
    for (Section section : sections.values()) {
      for (Parameter parameter : section.getParameters().values()) {
        Input input = parameter.getInput();
        if (input != null) {
          String sMeaningType = parameter.getMeaningType();
          if (sMeaningType.equals("")) {
          } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_EAST)) {
            ids[2] = input.getFacesId();
          } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_NORTH)) {
            ids[3] = input.getFacesId();
          } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_SOUTH)) {
            ids[1] = input.getFacesId();
          } else if (sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_ENVELOPE_WEST)) {
            ids[0] = input.getFacesId();
          }
        }
      }
      findEnvelopeIds(section.getSections(), ids);
    }
  }

  /**
   * Interrogates a schema based upon the supplied metadata document.
   * <p/>
   * The default behavior is to invoke the "interrogate" method
   * on the Interrogation object associated with the schema.
   * @param dom the metadata document
   * @return the interrogation object
   * @throws XPathExpressionException if an evaluation expression fails 
   */
  public Interrogation interrogate(Document dom)
    throws XPathExpressionException {
    NamespaceContextImpl ns = new NamespaceContextImpl(getNamespaces());
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(ns);
    getInterrogation().interrogate(this, dom, xpath);
    return getInterrogation();
  }

  /**
   * Loads the template for this schema.
   * @return the XML document
   * @throws ParserConfigurationException if a configuration exception occurs
   * @throws SAXException if an exception occurs during XML parsing
   * @throws IOException if an i/o exception occurs
   */
  public Document loadTemplate()
    throws ParserConfigurationException, SAXException, IOException {
    return DomUtil.makeDomFromResourcePath(getTemplateFile(), true);
  }

  /**
   * Updates the metadata document template based upon entered parameter value(s).
   * <p/>
   * The default behavior is to invoke the "update" method
   * for each section within the schema.
   * @param dom the metadata document template for the schema
   * @throws XPathExpressionException if an expression fails 
   * @throws SchemaException if the update fails
   */
  public void update(Document dom)
    throws XPathExpressionException, SchemaException {
    NamespaceContextImpl ns = new NamespaceContextImpl(getNamespaces());
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(ns);
    for (Section section : getSections().values()) {
      section.update(dom, xpath);
    }
        
    // update the rdf:about node based upon the resource url
    try {
      String rdfPfx = Val.chkStr(ns.getPrefix("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
      if (rdfPfx.length() > 0) {
        Node ndAbout = (Node)xpath.evaluate("/rdf:RDF/rdf:Description/@rdf:about",dom,XPathConstants.NODE);
        if (ndAbout != null) {
          String resUrl = "";
          Node ndResUrl = (Node)xpath.evaluate("/rdf:RDF/rdf:Description/dct:references",dom,XPathConstants.NODE); 
          if (ndResUrl != null) {
            resUrl = Val.chkStr(ndResUrl.getTextContent());
          }
          if (resUrl.length() > 0) {
            ndAbout.setNodeValue(resUrl);
          } else {
            ((Attr)ndAbout).getOwnerElement().removeAttributeNode((Attr)ndAbout);
          }
        }
      }
    } catch (Exception eTmp) {}
    
  }

  /**
   * Triggered on the save event from the metadata editor.
   * <p/>
   * The default behavior is to invoke the "unBind" method for
   * the each section. 
   * @param context the UI context
   * @param editorForm the Faces HtmlForm for the metadata editor
   * @throws SchemaException if an associated Faces UIComponent cannot be located
   */
  public void unBind(UiContext context, UIComponent editorForm)
    throws SchemaException {
    for (Section section : getSections().values()) {
      section.unBind(this, context, editorForm);
    }
  }

  /**
   * Validates the schema.
   * <p/>
   * The default behavior is to invoke the "validate" method
   * for each section within the schema.
   * @throws ValidationException if errors occur during validation
   */
  public void validate() throws ValidationException {
    setMeaning(new Meaning(this._propertyMeanings));
    setValidationErrors(new ValidationErrors());
    ValidationErrors errors = getValidationErrors();
    for (Section section : getSections().values()) {
      section.validate(this);
    }
    if (_evaluatedEsriTags != null) {
      getMeaning().applyEsriTags(this, _evaluatedEsriTags);
      
      if (this.getIndexables() != null) {
        IndexableContext ictx = this.getIndexables().getIndexableContext();
        if (ictx != null) {
          ictx.resolve(this,null,_evaluatedEsriTags);
        }
      }
      
    }
    if (errors.size() == 0) {
      ensureMinimals();
    }

    // throw an exception if errors were encountered
    if (errors.size() > 0) {
      throw new ValidationException(getKey(), "Invalid metadata document.", errors);
    }
  }

  /**
   * Selects all parameters conforming to the condiditions defined by predicate.
   * @param predicate predicate
   * @return list of selected parameters
   */
  public List<Parameter> selectParameters(Predicate predicate) {
    return getSections().selectParameters(predicate);
  }

  /**
   * Assures components ids.
   * @param parentComponent parent component
   */
  private void assureComponentsIds(UIComponent parentComponent) {
    // find components with no id assigned
    ArrayList<UIComponent> components = new ArrayList<UIComponent>();
    findComponentsWithNoId(components, parentComponent);

    // assign generated ids
    for (int i=0; i<components.size(); i++) {
      components.get(i).setId(parentComponent.getId()+"_genid_"+Integer.toString(i+1));
    }
  }

  /**
   * Recursively finds components with no id assigned.
   * @param components collection of all components with unassigned id
   * @param parentComponent parent component
   */
  private void findComponentsWithNoId(ArrayList<UIComponent> components, UIComponent parentComponent) {
    List children = parentComponent.getChildren();
    for (int i=0; i<children.size(); i++) {
      UIComponent child = (UIComponent) children.get(i);
      if (Val.chkStr(child.getId()).length()==0) {
        components.add(child);
      }
      findComponentsWithNoId(components, child);
    }
  }
}

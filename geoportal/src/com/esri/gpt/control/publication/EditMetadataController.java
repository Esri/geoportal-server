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
package com.esri.gpt.control.publication;
import com.esri.gpt.catalog.schema.Parameter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.w3c.dom.Document;

import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.gxe.GxeDefinition;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.publication.EditorRequest;
import com.esri.gpt.catalog.publication.ValidationRequest;
import com.esri.gpt.catalog.schema.Meaning;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Predicate;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.catalog.schema.Schemas;
import com.esri.gpt.catalog.schema.UiContext;
import com.esri.gpt.catalog.schema.ValidationError;
import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.control.harvest.HarvestContext;
import com.esri.gpt.control.view.SelectablePublishers;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Controller for the online metadata editor.
 */
public class EditMetadataController extends BaseActionListener {

// class variables =============================================================

// instance variables ==========================================================
private String               _createSchemaKey = "";
private String               _onBehalfOf  = "";
private String               _openDocumentUuid = "";
private String               _openSchemaKey = "";
private HtmlPanelGroup       _sectionsPanelGroup;
private SelectablePublishers _selectablePublishers;
private String               _openDocumentStatus = "";
private String               _operation = "register";
private HarvestContext       _harvestContext;

// constructors ================================================================

/** Default constructor. */
public EditMetadataController() {
  _sectionsPanelGroup = new HtmlPanelGroup();
  _selectablePublishers = new SelectablePublishers();
  FacesContext fc = FacesContext.getCurrentInstance();
  if (fc!=null) {
    ExternalContext ec = fc.getExternalContext();

    if (ec!=null && ec.getRequest() instanceof HttpServletRequest) {
      HttpSession session = ((HttpServletRequest)ec.getRequest()).getSession();
      Object hpg = session.getAttribute("EditMetadataController:HtmlPanelGroup");
      if (hpg instanceof HtmlPanelGroup) {
        _sectionsPanelGroup = (HtmlPanelGroup)hpg;
      } else {
        session.setAttribute("EditMetadataController:HtmlPanelGroup", _sectionsPanelGroup);
      }
    }
  }
}
// properties ==================================================================

/**
 * Gets the configured schemas.
 * @return the configured schemas
 */
private Schemas getConfiguredSchemas() {
  return this.extractRequestContext().getCatalogConfiguration().getConfiguredSchemas();
}

/**
 * Gets the list of schema's that can be created with the editor.
 * @return the list of schema's
 */
public List<SelectItem> getCreateSchemaItems() {
  MessageBroker msgBroker = extractMessageBroker();
  ArrayList<SelectItem> list = new ArrayList<SelectItem>();
  Schemas schemas = getConfiguredSchemas();
  for (Schema schema: schemas.values()) {
    if (schema.getEditable()) {
      String sKey = schema.getKey();
      String sLabel = schema.getKey();
      if (schema.getLabel() != null) {
        String sResKey = schema.getLabel().getResourceKey();
        if (sResKey.length() > 0) {
          sLabel = msgBroker.retrieveMessage(sResKey);
        }
      }
      list.add(new SelectItem(sKey,sLabel));
    }
  }
  return list;
}

/**
 * Gets the schema key selected from the create metadata page.
 * @return the key associated with the schema to create
 */
public String getCreateSchemaKey() {
  return _createSchemaKey;
}
/**
 * Sets the schema key selected from the create metadata page.
 * @param key the key associated with the schema to create
 */
public void setCreateSchemaKey(String key) {
  _createSchemaKey = Val.chkStr(key);
}

/**
 * Creates a new UUID.
 * <p/>
 * This method is typically used when binding a default parameter value from
 * the schema definition XML. Example: <br/>
 * <&lt;input type="text" defaultValue="#{EditMetadataController.newUuid}"/&gt;
 * @return then new UUID
 */
public String getNewUuid() {
  return UuidUtil.makeUuid(true);
}

/**
 * Returns the current data in yyyy-MM-dd format.
 * <p/>
 * This method is typically used when binding a default parameter value from
 * the schema definition XML. Example: <br/>
 * <&lt;input type="text" defaultValue="#{EditMetadataController.now}"/&gt;
 * @return then new UUID
 */
public String getNow() {
  return DateProxy.formatDate(new Timestamp(System.currentTimeMillis()));
}

/**
 * Gets the key of publisher this document is being created on behalf of.
 * @return the publisher key
 */
public String getOnBehalfOf() {
  return _onBehalfOf;
}
/**
 * Sets the key of publisher this document is being created on behalf of.
 * @param key the publisher key
 */
public void setOnBehalfOf(String key) {
  _onBehalfOf = Val.chkStr(key);
}

/**
 * Gets the open document status.
 * <br/>This value is empty for documents being created.
 * @return the current document status (trimmed, never null)
 */
public String getOpenDocumentStatus() {
   return Val.chkStr(_openDocumentStatus);
}
/**
 * Sets the open document status.
 * <br/>This value is empty for documents being created.
 * @param status the currently open document status
 */
public void setOpenDocumentStatus(String status) {
  _openDocumentStatus = status;
}

/**
 * Gets the UUID of the document currently open within the editor.
 * <br/>This value is empty for documents being created.
 * @return the currently open document UUID
 */
public String getOpenDocumentUuid() {
  return _openDocumentUuid;
}
/**
 * Sets the UUID of the document currently open within the editor.
 * <br/>This value is empty for documents being created.
 * @param uuid the currently open document UUID
 */
public void setOpenDocumentUuid(String uuid) {
  _openDocumentUuid = Val.chkStr(uuid);
}

/**
 * Gets the key of the schema currently open within the editor.
 * @return the currently open schema key
 */
public String getOpenSchemaKey() {
  return _openSchemaKey;
}
/**
 * Sets the key of the schema currently open within the editor.
 * @param key the currently open schema key
 */
public void setOpenSchemaKey(String key) {
  _openSchemaKey = Val.chkStr(key);
}

/**
 * Gets the bound HtmlPanelGroup for the sections panel.
 * <br/>This object is used during the Faces component binding process.
 * @return the bound HtmlPanelGroup
 */
public HtmlPanelGroup getSectionsPanelGroup() {
  return _sectionsPanelGroup;
}
/**
 * Sets the bound HtmlPanelGroup for the sections panel.
 * <br/>This object is used during the Faces component binding process.
 * @param htmlPanelGroup the bound HtmlPanelGroup
 */
public void setSectionsPanelGroup(HtmlPanelGroup htmlPanelGroup) {
  _sectionsPanelGroup = htmlPanelGroup; 
}

/**
 * Gets the list of selectable publishers.
 * @return the list of selectable publishers
 */
public SelectablePublishers getSelectablePublishers() {
  return _selectablePublishers;
}

/**
 * Gets 'add' operation type.
 * @return add operation type
 */
public String getOperation() {
  return _operation;
}

/**
 * Sets 'add' operation type.
 * @param addOperation add operation type
 */
public void setOperation(String addOperation) {
  this._operation = Val.chkStr(addOperation);
}

/**
 * Gets the active user's profile.
 * @return the user profile
 */
public UserAttributeMap getUserProfile() {
  return getContextBroker().extractRequestContext().getUser().getProfile();
}

// methods =====================================================================

/**
 * Builds the HtmlPanelGroup for the metadata editor sections component.
 * @param uiContext the UI context
 * @param panel the panel group to build
 * @param schema the schema to build
 */
private void buildSectionsPanelGroup(UiContext uiContext,
                                     HtmlPanelGroup panel,
                                     Schema schema) {
  
  // clear existing children, populate the panel group
  if (panel != null) {
    panel.getChildren().clear();
  }
  if ((panel != null) && (schema != null)) {
    schema.appendEditorSections(uiContext,panel);
  }
}

/**
 * Creates a document for editing.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void executeCreate(ActionEvent event, RequestContext context) 
  throws Exception {
  
  // determine the schema to create
  String sCreateSchemaKey = getCreateSchemaKey();
  setOpenSchemaKey("");
  setOpenDocumentUuid("");
  if (sCreateSchemaKey.length() == 0) {
    MessageBroker msgBroker = extractMessageBroker();
    msgBroker.addErrorMessage("publication.createMetadata.err.noSchemaSelected");
  } else {
    
    // create the new schema
    MetadataDocument document = new MetadataDocument();
    Schema schema = document.prepareForCreate(context,sCreateSchemaKey);
    setOpenSchemaKey(schema.getKey());
    
    // check for a defined GXE based Geoportal XML editor
    GxeDefinition gxeDefinition = schema.getGxeEditorDefinition();
    if (gxeDefinition != null) {
      context.getServletRequest().setAttribute("gxeDefinitionKey",gxeDefinition.getKey());
      context.getServletRequest().setAttribute("gxeDefinitionLocation",gxeDefinition.getFileLocation());
      context.getServletRequest().setAttribute("gxeOpenDocumentId",this.getOpenDocumentUuid());
      setNavigationOutcome("catalog.publication.gxeEditor");
      return;
    }
    
    // build the sections panel group, navigate to the edit metadata page 
    UiContext uiContext = new UiContext();
    uiContext.setIsCreateDocument(true);
    buildSectionsPanelGroup(uiContext,_sectionsPanelGroup,schema);
    setNavigationOutcome("catalog.publication.editMetadata");
  }
}

/**
 * Opens a document for editing.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void executeOpen(ActionEvent event, RequestContext context) 
  throws Exception {
  
  // initialize
  setOpenSchemaKey("");
  setNavigationOutcome("catalog.publication.editMetadata");
   
  // determine the uuid for the document to open
  String sOpenDocumentUuid = getOpenDocumentUuid();
  if (sOpenDocumentUuid.length() == 0) {
    String sMsg = "Programming error: The openDocumentUuid was not specified.";
    throw new SchemaException(sMsg);
  }
  
  // determine the owner
  ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(context);
  setOnBehalfOf(adminDao.queryOwnerDN(sOpenDocumentUuid));
  getLogger().finer("Opening editor for document="+sOpenDocumentUuid+", ownerDn="+getOnBehalfOf());
    
  // prepare the publisher
  getSelectablePublishers().setSelectedKey(getOnBehalfOf());

  // Modified by Esri Italy: set to true to overcome control on owner/publisher useful for enableEditForAdministrator flag
  // Publisher publisher = getSelectablePublishers().selectedAsPublisher(context,false);
  Publisher publisher = getSelectablePublishers().selectedAsPublisher(context,true);

  // prepare the schema for edit
  MetadataDocument document = new MetadataDocument();
  Schema schema = document.prepareForEdit(context,publisher,sOpenDocumentUuid);
  setOpenSchemaKey(schema.getKey());
  
  // check for a defined GXE based Geoportal XML editor
  GxeDefinition gxeDefinition = schema.getGxeEditorDefinition();
  if (gxeDefinition != null) {
    context.getServletRequest().setAttribute("gxeDefinitionKey",gxeDefinition.getKey());
    context.getServletRequest().setAttribute("gxeDefinitionLocation",gxeDefinition.getFileLocation());
    context.getServletRequest().setAttribute("gxeOpenDocumentId",this.getOpenDocumentUuid());
    setNavigationOutcome("catalog.publication.gxeEditor");
    return;
  }
  
  // build the sections panel group
  UiContext uiContext = new UiContext();
  buildSectionsPanelGroup(uiContext,_sectionsPanelGroup,schema);
}

/**
 * Uploads single metadata.
 * @param event event
 * @param context request context
 * @throws Exception if an exception occurs
 */
private void executeUpload(ActionEvent event, RequestContext context)
  throws Exception {
  // upload
  setOpenSchemaKey("");
  setNavigationOutcome("catalog.publication.uploadMetadata");
}

/**
 * Register resource.
 * @param event event
 * @param context request context
 * @throws Exception if an exception occurs
 */
private void executeRegister(ActionEvent event, RequestContext context)
  throws Exception {
  // register
  getHarvestContext().getHarvestCriteria().getActionCriteria().setUuid("");
  setOpenSchemaKey("");
  setNavigationOutcome("catalog.harvest.manage.create");
}

/**
 * Register resource.
 * @param event event
 * @param context request context
 * @throws Exception if an exception occurs
 */
private void executeOpenRegister(ActionEvent event, RequestContext context)
  throws Exception {
  // register
  setOpenSchemaKey("");
  setNavigationOutcome("catalog.harvest.manage.edit");
}

/**
 * Use metadata editor.
 * @param event event
 * @param context request context
 * @throws Exception if an exception occurs
 */
private void executeEdit(ActionEvent event, RequestContext context)
  throws Exception {
  // edit
  setOpenSchemaKey("");
  setNavigationOutcome("catalog.publication.createMetadata");
}

/**
 * Shows synchronizatio history.
 * @param event event
 * @param context request context
 * @throws Exception if an exception occurs
 */
private void executeHistory(ActionEvent event, RequestContext context)
  throws Exception {
  // history
  getHarvestContext().getHistoryCriteria().getQueryCriteria().setEventUuid("");
  getHarvestContext().getHistoryCriteria().getActionCriteria().setUuid(getOpenDocumentUuid());
  setOpenSchemaKey("");
  setNavigationOutcome("catalog.harvest.manage.history");
}

/**
 * Saves the document.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @param asDraft if true, save this a s a draft
 * @param validateOnly if true, then do no update the data base
 * @throws Exception if an exception occurs
 */
private void executeSave(ActionEvent event, 
                         RequestContext context, 
                         boolean asDraft, 
                         boolean validateOnly) 
  throws Exception  {
  
  // initialize
  MessageBroker msgBroker = extractMessageBroker();
  Schema schema = null;
  
  // find the metadata editor form
  UIViewRoot root = getContextBroker().extractViewRoot();
  UIComponent editorForm = root.findComponent("mdEditor");
  if (editorForm == null) {
    String sMsg = "Programming error: The mdEditor form connot be located.";
    throw new SchemaException(sMsg);
  }
  
  // determine the currently open schema
  String sOpenSchemaKey = getOpenSchemaKey();
  if (sOpenSchemaKey.length() == 0) {
    String sMsg = "Programming error: The openSchemaKey was not specified.";
    throw new SchemaException(sMsg);
  } else {
    MetadataDocument document = new MetadataDocument();
    schema = document.prepareForCreate(context,sOpenSchemaKey);
  }
  
  // un-bind editor values, validate the input
  UiContext uiContext = new UiContext();
  schema.unBind(uiContext,editorForm);

  try {
    if (!asDraft) schema.validate();
  } catch (ValidationException e) {
    for (ValidationError error: e.getValidationErrors()) {
      if (error.getSection() != null) {
        if (!error.getSection().getOpen()) {
          error.getSection().forceOpen(editorForm);
        }
      }
    }
    throw e;
  }
    
  // update the document
  Document dom = schema.loadTemplate();
  schema.update(dom);
  String sXml = XmlIoUtil.domToString(dom);
  getLogger().finer("Updated template xml:\n"+sXml);
  
  // prepare the publisher
  getSelectablePublishers().setSelectedKey(getOnBehalfOf());
  // Modified by Esri Italy: set to true to overcome control on owner/publisher useful for enableEditForAdministrator flag
  // Publisher publisher = getSelectablePublishers().selectedAsPublisher(context,false);
  Publisher publisher = getSelectablePublishers().selectedAsPublisher(context,true);
  //  the document
  
  if (validateOnly) {
    
    // handle a validation only request
    ValidationRequest request = new ValidationRequest(context,null,sXml);
    request.verify();
    msgBroker.addSuccessMessage("catalog.publication.success.validated");
    
  } else {
    
    // publish the document
    EditorRequest request = new EditorRequest(context,publisher,sXml);
    request.getPublicationRecord().setUuid(getOpenDocumentUuid());
    if (asDraft) {
      request.getPublicationRecord().setApprovalStatus(MmdEnums.ApprovalStatus.draft.toString());
    }
    request.publish();
    setOpenDocumentUuid(request.getPublicationRecord().getUuid());
    if (asDraft) {
      msgBroker.addSuccessMessage("catalog.publication.success.draftSaved");
    } else {
      if (request.getPublicationRecord().getWasDocumentReplaced()) {
        msgBroker.addSuccessMessage("catalog.publication.success.replaced");
      } else {
        msgBroker.addSuccessMessage("catalog.publication.success.created");
      }  
    }
  }

}

/**
 * Fired when the getPrepareView() property is accessed.
 * <br/>This event is triggered from the page during the 
 * render response phase of the JSF cycle. 
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
@Override
protected void onPrepareView(RequestContext context) throws Exception {
  getSelectablePublishers().build(context,false);
}

/**
 * Handles events from the metadata editor.
 * <br/>This is the default entry point for a sub-class of BaseActionListener.
 * <br/>This BaseActionListener handles the JSF processAction method and
 * invokes the processSubAction method of the sub-class.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws AbortProcessingException if processing should be aborted
 * @throws Exception if an exception occurs
 */
@Override
protected void processSubAction(ActionEvent event, RequestContext context) 
  throws AbortProcessingException, Exception {
  
  // determine the command
  UIComponent component = event.getComponent();
  String sCommand = Val.chkStr((String)component.getAttributes().get("command"));
  
  // execute the appropriate action
  try {
    if (sCommand.equalsIgnoreCase("create")) {
      setOnBehalfOf(getSelectablePublishers().getSelectedKey());
      executeCreate(event,context); 
    } else if (sCommand.equalsIgnoreCase("open")) {
      executeOpen(event,context);
    } else if (sCommand.equalsIgnoreCase("register")) {
      executeOpenRegister(event,context);
    } else if (sCommand.equalsIgnoreCase("save")) {
      executeSave(event,context,false,false);
    } else if (sCommand.equalsIgnoreCase("saveAsDraft")) {
      executeSave(event,context,true,false);
    } else if (sCommand.equalsIgnoreCase("validate")) {
      executeSave(event,context,false,true);
    } else if (sCommand.equalsIgnoreCase("select")) {
      if (getOperation().equals("register")) {
        executeRegister(event, context);
      } else if (getOperation().equals("upload")) {
        executeUpload(event,context);
      } else if (getOperation().equals("edit")) {
        executeEdit(event, context);
      }
    } else if (sCommand.equalsIgnoreCase("history")) {
      executeHistory(event, context);
    }
  } catch (ValidationException e) {
    String sKey = e.getKey();
    if (sKey.length() > 0) {
      String sMsg = sKey;
      Schema schema = getConfiguredSchemas().get(sKey);
      if (schema != null) {
        if (schema.getLabel() != null) {
          String sResKey = schema.getLabel().getResourceKey();
          if (sResKey.length() > 0) {
            sMsg = extractMessageBroker().retrieveMessage(sResKey)+" ("+sKey+")";
          }
        }
      }
      FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN,sMsg,null);
      extractMessageBroker().addMessage(fm);
    }
    e.getValidationErrors().buildMessages(extractMessageBroker(),false);
  }
}

  /**
   * @return the _harvestContext
   */
  public HarvestContext getHarvestContext() {
    return _harvestContext;
  }

  /**
   * @param harvestContext the _harvestContext to set
   */
  public void setHarvestContext(HarvestContext harvestContext) {
    this._harvestContext = harvestContext;
  }

  private class FileIdentifierPredicate implements Predicate {

    public boolean eligible(Parameter parameter) {
      return parameter.getMeaningType().equalsIgnoreCase(Meaning.MEANINGTYPE_FILEIDENTIFIER);
    }

  }
}

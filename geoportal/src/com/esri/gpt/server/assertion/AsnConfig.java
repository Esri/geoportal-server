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
package com.esri.gpt.server.assertion;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.lucene.LuceneConfig;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnAuthPolicy;
import com.esri.gpt.server.assertion.components.AsnConstants;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.components.AsnOperations;
import com.esri.gpt.server.assertion.components.AsnPredicate;
import com.esri.gpt.server.assertion.components.AsnPrincipals;
import com.esri.gpt.server.assertion.components.AsnProperty;
import com.esri.gpt.server.assertion.components.AsnAssertionSet;
import com.esri.gpt.server.assertion.components.AsnSubject;
import com.esri.gpt.server.assertion.components.AsnSupportedValues;
import com.esri.gpt.server.assertion.components.AsnUIResource;
import com.esri.gpt.server.assertion.components.AsnValue;
import com.esri.gpt.server.assertion.components.AsnValueType;
import com.esri.gpt.server.assertion.index.AsnIndexReference;
import com.esri.gpt.server.assertion.index.AsnIndexReferences;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Assertion configuration. 
 */
public class AsnConfig {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(AsnConfig.class.getName());
  
  /** instance variables ====================================================== */
  private boolean            allowNonLocalResourceIds = false;
  private boolean            areAssertionsEnabled = false;
  private AsnIndexReferences indexReferences;
  private AsnOperations      operations;
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnConfig() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the status indicating whether or not assertions are enabled.
   * @return <code>true</code> if assertions are enabled
   */
  public boolean getAreAssertionsEnabled() {
    return this.areAssertionsEnabled;
  }
  
  /**
   * Gets the the configured index references.
   * @return the configured index references
   */
  public AsnIndexReferences getIndexReferences() {
    return this.indexReferences;
  }
  
  /**
   * Gets the configured assertion operations.
   * @return the configured assertion operations
   */
  public AsnOperations getOperations() {
    return this.operations;
  }
  
  /** methods ================================================================= */
  
  /**
   * Builds the configuration. 
   */
  public void configure() {
    
    // initialize
    this.indexReferences = new AsnIndexReferences();
    this.operations = new AsnOperations();
    AsnIndexReferences indexRefs = this.indexReferences;
    ApplicationConfiguration appCfg = ApplicationContext.getInstance().getConfiguration();
    CatalogConfiguration catCfg = appCfg.getCatalogConfiguration();
    StringAttributeMap catParams = catCfg.getParameters();
    LuceneConfig luceneCfg = catCfg.getLuceneConfig();
    
    // API operations
    AsnOperation op = new AsnOperation();
    op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnApiHandler");
    op.setSubject(new AsnSubject(AsnConstants.APP_URN_PREFIX));
    op.getSubject().setRequiresValuePart(false);
    op.setPredicate(new AsnPredicate(AsnConstants.APP_URN_PREFIX+":assertion:operations"));
    op.setAuthPolicy(new AsnAuthPolicy());
    op.getAuthPolicy().setAuthenticationRequired(false);
    op.getAuthPolicy().setQueryPrincipals(new AsnPrincipals());
    op.getAuthPolicy().getQueryPrincipals().add(AsnConstants.PRINCIPAL_ANY);
    this.operations.add(op); 
    
    // root index reference
    String val = Val.chkStr(catParams.getValue("assertion.index.allowNonLocalResourceIds"));
    this.allowNonLocalResourceIds = val.equalsIgnoreCase("true");
    AsnIndexReference rootIndexRef = new AsnIndexReference();
    val = Val.chkStr(catParams.getValue("assertion.index.enabled"));
    rootIndexRef.setEnabled(!val.equalsIgnoreCase("false"));
    val = Val.chkStr(catParams.getValue("assertion.index.location"));
    rootIndexRef.setIndexLocation(val);
    rootIndexRef.setUseNativeFSLockFactory(luceneCfg.getUseNativeFSLockFactory());
    rootIndexRef.setWriteLockTimeout(luceneCfg.getWriteLockTimeout());
    if (!rootIndexRef.getEnabled()) {
      LOGGER.config("assertion.index.enabled=false");
      return;
    } else if (rootIndexRef.getIndexLocation().length() == 0) {
      String msg = "The configured assertion.index.location parameter is invalid.";
      msg += " Assertions will be disabled.";
      LOGGER.warning(msg);
      return;
    }
    this.areAssertionsEnabled = true;
 
    // ratings
    AsnIndexReference ratingIndexRef = this.makeIndexRef(catCfg,rootIndexRef,"rating");
    if (ratingIndexRef.getEnabled()) {
      indexRefs.add(ratingIndexRef);
      this.configureRatings(catCfg,ratingIndexRef,ratingIndexRef.getName());
    }
    
    // comments
    AsnIndexReference commentIndexRef = this.makeIndexRef(catCfg,rootIndexRef,"comment");
    if (commentIndexRef.getEnabled()) {
      indexRefs.add(commentIndexRef);
      this.configureComments(catCfg,commentIndexRef,commentIndexRef.getName());
    } 

  }
  
  /**
   * Configures comment operations.
   * @param catCfg the catalog configuration
   * @param indexRef the comment index reference
   * @param name the assertion set name
   */
  private void configureComments(CatalogConfiguration catCfg, 
                                 AsnIndexReference indexRef,
                                 String name) {
    StringAttributeMap catParams = catCfg.getParameters();
    AsnOperations ops =  this.operations;
    AsnOperation op;
    
    if (indexRef.getEnabled()) {
      
      int maxLength = Val.chkInt(catParams.getValue("assertion."+name+".maxLength"),4000);
      String valueFilterClass = Val.chkStr(catParams.getValue("assertion."+name+".valueFilterClass"));
      if (valueFilterClass.length() == 0) {
        valueFilterClass = null;
      }
      
      AsnAuthPolicy authForRead = new AsnAuthPolicy();
      authForRead.setAllowNonLocalResourceIds(this.allowNonLocalResourceIds);
      authForRead.setAuthenticationRequired(false);
      authForRead.setQueryPrincipals(new AsnPrincipals());
      authForRead.getQueryPrincipals().add(AsnConstants.PRINCIPAL_ANY);
      
      // create the assertion set
      AsnAssertionSet asnSet = new AsnAssertionSet(name);
      String actionPfx = asnSet.getURNPrefix();
      
      // set the index reference
      asnSet.setIndexReference(indexRef.duplicate());
     
      // set the default authentication/authorization policy
      asnSet.setAuthPolicy(new AsnAuthPolicy());
      asnSet.getAuthPolicy().setAllowNonLocalResourceIds(this.allowNonLocalResourceIds);
      asnSet.getAuthPolicy().setAuthenticationRequired(true);
      asnSet.getAuthPolicy().setCreatePrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getCreatePrincipals().add(AsnConstants.PRINCIPAL_ANY);
      asnSet.getAuthPolicy().setDeletePrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getDeletePrincipals().add(AsnConstants.PRINCIPAL_OWNER); 
      asnSet.getAuthPolicy().getDeletePrincipals().add(AsnConstants.PRINCIPAL_ADMINISTRATOR); 
      asnSet.getAuthPolicy().setEnableDisablePrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getEnableDisablePrincipals().add(AsnConstants.PRINCIPAL_ADMINISTRATOR); 
      asnSet.getAuthPolicy().setUpdatePrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getUpdatePrincipals().add(AsnConstants.PRINCIPAL_OWNER);
      asnSet.getAuthPolicy().setQueryPrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getQueryPrincipals().add(AsnConstants.PRINCIPAL_ANY);
      asnSet.getAuthPolicy().setMultiplePerUserSubjectPredicate(false);
      
      // value type
      asnSet.setValueType(new AsnValueType());
      asnSet.getValueType().setAnalyzePriorToIndexing(true);
      asnSet.getValueType().setMaxCharacters(maxLength);
      asnSet.getValueType().setRdfPredicate(asnSet.getURNPrefix());
      asnSet.getValueType().setRdfValueField("rdf.comment.value");
      asnSet.getValueType().setRequired(true);
      asnSet.getValueType().setValueFilterClass(valueFilterClass);
      asnSet.getValueType().setValueTypeName(AsnValueType.VALUE_TYPENAME_FREE);
      
      // value
      AsnValue asnValue = new AsnValue();
      asnValue.setValueType(asnSet.getValueType().duplicate());
                  
      // query a comment by assertion id
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnCommentHandler");
      op.setSubject(new AsnSubject(asnSet.getAssertionIdPrefix()));
      op.setPredicate(new AsnPredicate(actionPfx+":query"));
      op.setAuthPolicy(authForRead.duplicate());
      ops.add(op);
      
      // query comments associated with a resource
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnCommentHandler");
      op.setSubject(new AsnSubject(AsnConstants.SUBJECT_PREFIX_RESOURCEID));
      op.setPredicate(new AsnPredicate(actionPfx+":query"));
      op.setAuthPolicy(authForRead.duplicate());
      ops.add(op);
      
      // create      
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnCreateHandler");
      op.setSubject(new AsnSubject(AsnConstants.SUBJECT_PREFIX_RESOURCEID));
      op.setPredicate(new AsnPredicate(actionPfx+":create"));
      op.getAuthPolicy().setMultiplePerUserSubjectPredicate(true);
      op.setValue(asnValue.duplicate());
      ops.add(op);

      // update
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnUpdateHandler");
      op.setSubject(new AsnSubject(asnSet.getAssertionIdPrefix()));
      op.setPredicate(new AsnPredicate(actionPfx+":update"));
      op.setValue(asnValue.duplicate());
      ops.add(op);
      
      // delete
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnDeleteHandler");
      op.setSubject(new AsnSubject(asnSet.getAssertionIdPrefix()));
      op.setPredicate(new AsnPredicate(actionPfx+":delete"));
      ops.add(op);
      
      // enable
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnEnableHandler");
      op.setSubject(new AsnSubject(asnSet.getAssertionIdPrefix()));
      op.setPredicate(new AsnPredicate(actionPfx+":enable"));
      ops.add(op);
      
      // disable
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnDisableHandler");
      op.setSubject(new AsnSubject(asnSet.getAssertionIdPrefix()));
      op.setPredicate(new AsnPredicate(actionPfx+":disable"));
      ops.add(op);
      
      // UI resources
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnUIResourcesHandler");
      op.setSubject(new AsnSubject(asnSet.getURNPrefix()));
      op.getSubject().setRequiresValuePart(false);
      op.setPredicate(new AsnPredicate(actionPfx+":uiresources"));
      op.setAuthPolicy(authForRead.duplicate());
      ops.add(op); 
      AsnProperty uiResources = new AsnProperty(
          asnSet.getURNPrefix(),asnSet.getURNPrefix()+":uiresources",null);
      op.setUIResources(uiResources);
      
      String pfx = asnSet.getURNPrefix()+":uiresource";
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":maxLength",null,""+maxLength)); 
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":caption","catalog.asn.comment.caption","Comments"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":addComment","catalog.asn.comment.addComment","Add a comment:"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":postComment","catalog.asn.comment.postComment","Post"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":disabledComment","catalog.asn.comment.disabledComment","Disabled"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":emptyComment","catalog.asn.comment.emptyComment","Please enter a comment."));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":enableTip","catalog.asn.comment.enableTip","Enable"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":disableTip","catalog.asn.comment.disableTip","Disable"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":editTip","catalog.asn.comment.editTip","Edit"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":editedTip","catalog.asn.comment.editedTip","Edited"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":deleteTip","catalog.asn.comment.deleteTip","Delete"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":deletePrompt","catalog.asn.comment.deletePrompt",
          "Are you sure you want to delete this comment?"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":more","catalog.asn.comment.more","more"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":editIcon",null,"asn-edit.png")); 
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":deleteIcon",null,"asn-delete.png")); 
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":enableIcon",null,"asn-enable.png")); 
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":disableIcon",null,"asn-disable.png")); 
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":editIcon",null,"asn-edit.png")); 
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":deleteIcon",null,"asn-delete.png")); 
      
    }
    
  }
  
  /**
   * Configures rating operations.
   * @param catCfg the catalog configuration
   * @param indexRef the rating index reference
   * @param name the assertion set name
   */
  private void configureRatings(CatalogConfiguration catCfg, 
                                AsnIndexReference indexRef,
                                String name) {
    AsnOperations ops =  this.operations;
    AsnOperation op;
    
    if (indexRef.getEnabled()) {
      
      AsnAuthPolicy authForRead = new AsnAuthPolicy();
      authForRead.setAllowNonLocalResourceIds(this.allowNonLocalResourceIds);
      authForRead.setAuthenticationRequired(false);
      authForRead.setQueryPrincipals(new AsnPrincipals());
      authForRead.getQueryPrincipals().add(AsnConstants.PRINCIPAL_ANY);
      
      // create the assertion set
      AsnAssertionSet asnSet = new AsnAssertionSet(name);
      String actionPfx = asnSet.getURNPrefix();
      
      // set the index reference
      asnSet.setIndexReference(indexRef.duplicate());
     
      // set the default authentication/authorization policy
      asnSet.setAuthPolicy(new AsnAuthPolicy());
      asnSet.getAuthPolicy().setAllowNonLocalResourceIds(this.allowNonLocalResourceIds);
      asnSet.getAuthPolicy().setAuthenticationRequired(true);
      asnSet.getAuthPolicy().setCreatePrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getCreatePrincipals().add(AsnConstants.PRINCIPAL_ANY);
      asnSet.getAuthPolicy().setDeletePrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getDeletePrincipals().add(AsnConstants.PRINCIPAL_OWNER);
      asnSet.getAuthPolicy().setEnableDisablePrincipals(null);
      asnSet.getAuthPolicy().setUpdatePrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getUpdatePrincipals().add(AsnConstants.PRINCIPAL_OWNER);
      asnSet.getAuthPolicy().setQueryPrincipals(new AsnPrincipals());
      asnSet.getAuthPolicy().getQueryPrincipals().add(AsnConstants.PRINCIPAL_ANY);
      asnSet.getAuthPolicy().setMultiplePerUserSubjectPredicate(false);
      
      // value type
      asnSet.setValueType(new AsnValueType());
      asnSet.getValueType().setAnalyzePriorToIndexing(false);
      asnSet.getValueType().setRequired(true);
      asnSet.getValueType().setRdfPredicate(asnSet.getURNPrefix());
      asnSet.getValueType().setRdfValueField("rdf.rating.value");
      asnSet.getValueType().setValueTypeName(AsnValueType.VALUE_TYPENAME_CONSTRAINED);
      AsnSupportedValues supported = new AsnSupportedValues();
      supported.add("urn:esri:geoportal:rating:value:up");
      supported.add("urn:esri:geoportal:rating:value:down");
      asnSet.getValueType().setSupportedValues(supported);
      
      // value
      AsnValue asnValue = new AsnValue();
      asnValue.setValueType(asnSet.getValueType().duplicate());
            
      // query ratings associated with a resource
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnRatingHandler");
      op.setSubject(new AsnSubject(AsnConstants.SUBJECT_PREFIX_RESOURCEID));
      op.setPredicate(new AsnPredicate(actionPfx+":query"));
      op.setAuthPolicy(authForRead.duplicate());
      ops.add(op);
      
      // create
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnCreateHandler");
      op.setSubject(new AsnSubject(AsnConstants.SUBJECT_PREFIX_RESOURCEID));
      op.setPredicate(new AsnPredicate(actionPfx+":create"));
      op.setValue(asnValue.duplicate());
      ops.add(op);
      
      // update
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnUpdateHandler");
      op.setSubject(new AsnSubject(asnSet.getAssertionIdPrefix()));
      op.setPredicate(new AsnPredicate(actionPfx+":update"));
      op.setValue(asnValue.duplicate());
      ops.add(op);
      
      // delete
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnDeleteHandler");
      op.setSubject(new AsnSubject(asnSet.getAssertionIdPrefix()));
      op.setPredicate(new AsnPredicate(actionPfx+":delete"));
      ops.add(op); 
      
      // UI resources
      op = new AsnOperation(asnSet);
      op.setHandlerClass("com.esri.gpt.server.assertion.handler.AsnUIResourcesHandler");
      op.setSubject(new AsnSubject(asnSet.getURNPrefix()));
      op.getSubject().setRequiresValuePart(false);
      op.setPredicate(new AsnPredicate(actionPfx+":uiresources"));
      op.setAuthPolicy(authForRead.duplicate());
      ops.add(op); 
      AsnProperty uiResources = new AsnProperty(
          asnSet.getURNPrefix(),asnSet.getURNPrefix()+":uiresources",null);
      op.setUIResources(uiResources);
      
      String pfx = asnSet.getURNPrefix()+":uiresource";
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":caption","catalog.asn.rating.caption","User ratings for this resource:"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":totalUpTip","catalog.asn.rating.totalUpTip","Up votes"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":totalDownTip","catalog.asn.rating.totalDownTip","Down votes"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":upTip","catalog.asn.rating.upTip","Vote up"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":downTip","catalog.asn.rating.downTip","Vote down"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":youVoted","catalog.asn.rating.youVoted","You voted:"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":youVotedUpTip","catalog.asn.rating.youVotedUpTip","Up"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":youVotedDownTip","catalog.asn.rating.youVotedDownTip","Down"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":youCan","catalog.asn.rating.youCan","You can:"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":deleteTip","catalog.asn.rating.deleteTip","Delete your vote"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":switchTip","catalog.asn.rating.switchTip","Switch your vote"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":upIcon",null,"asn-vote-up.png"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":downIcon",null,"asn-vote-down.png"));
      uiResources.getChildren().add(new AsnUIResource(
          pfx+":deleteIcon",null,"asn-delete.png"));
      
    }
  }
  
  /**
   * Makes an index reference.
   * @param catCfg the catalog configuration
   * @param rootIndexRef the root index reference
   * @param name the index reference name
   */
  private AsnIndexReference makeIndexRef(CatalogConfiguration catCfg,
                                         AsnIndexReference rootIndexRef,
                                         String name) {
    StringAttributeMap catParams = catCfg.getParameters();
    AsnIndexReference indexRef = new AsnIndexReference();
    String indexAdapterClass = "com.esri.gpt.server.assertion.index.AsnIndexAdapter";
    String val = Val.chkStr(catParams.getValue("assertion."+name+".enabled"));
    indexRef.setEnabled(!val.equalsIgnoreCase("false"));
    if (!indexRef.getEnabled()) {
      LOGGER.config("assertion."+name+".enabled=false");
    } else {
      val = Val.chkStr(catParams.getValue("assertion."+name+".location"));
      indexRef.setIndexLocation(rootIndexRef,name,val);
      indexRef.setIndexAdatperClass(indexAdapterClass);
      indexRef.setUseNativeFSLockFactory(rootIndexRef.getUseNativeFSLockFactory());
      indexRef.setWriteLockTimeout(rootIndexRef.getWriteLockTimeout());
      try {
        indexRef.makeIndexAdapter(null).touch();
        //this.indexReferences.add(indexRef);
      } catch (Exception e) {
        indexRef.setEnabled(false);
        String msg = "Problem accessing assertion."+name+".location="+indexRef.getIndexLocation();
        msg += " Assertions will be disabled.";
        LOGGER.log(Level.SEVERE,msg,e);
      }
    }
    return indexRef;
  }

}

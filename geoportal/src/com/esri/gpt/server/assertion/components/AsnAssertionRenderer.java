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
package com.esri.gpt.server.assertion.components;
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.server.assertion.index.Assertion;
import java.sql.Timestamp;

/**
 * Creates an assertion based property for response rendering.
 */
public class AsnAssertionRenderer {

  /** instance variables ====================================================== */
  boolean includeOwnerName = true;
  boolean includeUserCapabilities = true;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnAssertionRenderer() {}
      
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnAssertionRenderer(AsnAssertionRenderer objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setIncludeOwnerName(objectToDuplicate.getIncludeOwnerName());
      this.setIncludeUserCapabilities(objectToDuplicate.getIncludeUserCapabilities());
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the flag indicating whether or not the assertion owner 
   * name should be rendered.
   * @return <code>true</code> if the owner name should be rendered
   */
  public boolean getIncludeOwnerName() {
    return this.includeOwnerName;
  }
  /**
   * Sets the flag indicating whether or not the assertion owner 
   * name should be rendered.
   * @param include <code>true</code> if the owner name should be rendered
   */
  public void setIncludeOwnerName(boolean include) {
    this.includeOwnerName = include;
  }
  
  /**
   * Gets the flag indicating whether or not capabilities for the active 
   * user should be rendered.
   * @return <code>true</code> if user capabilities should be rendered
   */
  public boolean getIncludeUserCapabilities() {
    return this.includeUserCapabilities;
  }
  /**
   * Sets the flag indicating whether or not capabilities for the active
   * user should be rendered.
   * @param include <code>true</code> if user capabilities should be rendered
   */
  public void setIncludeUserCapabilities(boolean include) {
    this.includeUserCapabilities = include;
  }

  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnAssertionRenderer(this);
   * @return the duplicated object
   */
  public AsnAssertionRenderer duplicate() {
    return new AsnAssertionRenderer(this);
  }
  
  /**
   * Makes a renderable property based upon an indexed assertion.
   * @param context the assertion operation context
   * @param assertion the assertion
   * @return the property
   */
  public AsnProperty makeProperty(AsnContext context, Assertion assertion) {
    
    // initialize
    AsnOperation operation = context.getOperation();
    AsnAssertionSet asnSet = operation.getAssertionSet();
    AsnValueType vType = asnSet.getValueType();
    String urnPfx =  asnSet.getURNPrefix();

    // root property
    String asnId = assertion.getSystemPart().getAssertionId();
    String asnSubject = asnSet.getAssertionIdPrefix()+":"+asnId;
    AsnProperty rootProp = new AsnProperty(asnSubject,vType.getRdfPredicate(),null);
    
    // system part
    Timestamp ts = assertion.getSystemPart().getTimestamp();
    Timestamp tsEdit = assertion.getSystemPart().getEditTimestamp();
    rootProp.getChildren().add(new AsnProperty(
        null,urnPfx+":enabled",""+assertion.getSystemPart().getEnabled()));
    rootProp.getChildren().add(new AsnProperty(
        null,urnPfx+":date",DateProxy.formatDate(ts)));
    rootProp.getChildren().add(new AsnProperty(
        null,urnPfx+":timestamp",DateProxy.formatIso8601Timestamp(ts)));
    if (tsEdit != null) {
      rootProp.getChildren().add(new AsnProperty(
          null,urnPfx+":edit:date",DateProxy.formatDate(tsEdit)));
      rootProp.getChildren().add(new AsnProperty(
          null,urnPfx+":edit:timestamp",DateProxy.formatIso8601Timestamp(tsEdit)));
    }

    // user part 
    if (this.getIncludeOwnerName()) {
      rootProp.getChildren().add(new AsnProperty(
          null,urnPfx+":username",assertion.getUserPart().getName()));
    }
    
    // RDF part
    String rdfValue = null;
    if (assertion.getSystemPart().getEnabled()) {
      rdfValue = assertion.getRdfPart().getValue();
    }
    rootProp.getChildren().add(new AsnProperty(
        assertion.getRdfPart().getSubject(),urnPfx+":value",rdfValue));
    
    // capabilities for the active user
    if (this.getIncludeUserCapabilities()) {
      AsnAuthorizer auth = context.getAuthorizer();
      AsnAuthPolicy policy = asnSet.getAuthPolicy();
      String activeUserPfx = asnSet.getURNPrefix()+":activeUser";
      AsnProperty userCapProp = new AsnProperty(null,activeUserPfx+":capabilities",null);
      rootProp.getChildren().add(userCapProp);
      userCapProp.getChildren().add(new AsnProperty(null,activeUserPfx+":canUpdate",
          ""+auth.canUpdate(context,policy,assertion)));
      userCapProp.getChildren().add(new AsnProperty(null,activeUserPfx+":canDelete",
          ""+auth.canDelete(context,policy,assertion)));
      userCapProp.getChildren().add(new AsnProperty(null,activeUserPfx+":canEnable",
          ""+auth.canEnable(context,policy,assertion)));
      userCapProp.getChildren().add(new AsnProperty(null,activeUserPfx+":canDisable",
          ""+auth.canDisable(context,policy,assertion)));
    }
    
    return rootProp;
  }
    
}

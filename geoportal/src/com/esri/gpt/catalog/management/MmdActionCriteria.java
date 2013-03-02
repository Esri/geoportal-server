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
package com.esri.gpt.catalog.management;
import com.esri.gpt.framework.request.ActionCriteria;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Maintains the action criteria for a manage metadata request.
 */
public class MmdActionCriteria extends ActionCriteria {

  // instance variables ==========================================================
  private String            _accessToGroupDN = null;
  private ArrayList<String> _metadataAccessPolicy = new ArrayList<String>();
  private String            _metadataAccessPolicyType = "";
  private String            _sharingCollectionUuid = "";
  private String            _toggleMetadataAccessPolicy = "";
  private String            _transferToOwner = "";

  // constructors ================================================================
  
  /** Default constructor. */
  public MmdActionCriteria() {}

  /**
   * Construct by duplicating a supplied action criteria.
   * @param criteriaToDuplicate the criteria to duplicate
   */
  public MmdActionCriteria(MmdActionCriteria criteriaToDuplicate) {
    super(criteriaToDuplicate);
    if (criteriaToDuplicate != null) {
      setTransferToOwner(criteriaToDuplicate.getTransferToOwner());
      setMetadataAccessPolicy(criteriaToDuplicate.getMetadataAccessPolicy());
      setMetadataAccessPolicyType(criteriaToDuplicate.getMetadataAccessPolicyType());
      setToggleMetadataAccessPolicy(criteriaToDuplicate.getToggleMetadataAccessPolicy());
      setAccessToGroupDN(criteriaToDuplicate.getAccessToGroupDN());
      
      setSharingCollectionUuid(criteriaToDuplicate.getSharingCollectionUuid());
    }
  }

  // properties ==================================================================

  /**
   * Gets the group access dn.
   * @return the group access dn
   */
  public String getAccessToGroupDN() {
		return _accessToGroupDN;
	}
  /**
   * Sets the group access dn.
   * @param toGroupDN the group access dn
   */
  public void setAccessToGroupDN(String toGroupDN) {
		_accessToGroupDN = toGroupDN;
	}
    
  /**
   * Gets metadata access policy.
   * @return metadata access policy
   */
  public ArrayList<String> getMetadataAccessPolicy() {
      return _metadataAccessPolicy;
  }
  /**
   * Sets metadata access policy.
   * @param metadataAccessPolicy accessPolicy access policy.
   */
  public void setMetadataAccessPolicy(ArrayList<String> metadataAccessPolicy) {
  	_metadataAccessPolicy = metadataAccessPolicy!=null? metadataAccessPolicy: new ArrayList<String>();
  }
  
  /**
   * Gets metadata access policy array.
   * @return metadata access policy array
   */
  public String[] getMetadataAccessPolicyArray() {
    ArrayList<String> list = getMetadataAccessPolicy();
    String [] array = new String[list.size()];
    return list.toArray(array);
  }
  /**
   * Sets metadata access policy array.
   * @param array metadata access policy array
   */
  public void setMetadataAccessPolicyArray(String [] array) {
    ArrayList<String> list = new ArrayList();
    list.addAll(Arrays.asList(array));
    setMetadataAccessPolicy(list);
  }
  
  /**
   * Gets metadata access policy array.
   * @return metadata access policy array as coma separated string of values
   */
  public String getMetadataAccessPolicyString() {
    StringBuilder sb = new StringBuilder();
    String [] arr = getMetadataAccessPolicyArray();
    if (arr!=null) {
      for (String p : arr) {
        if (sb.length()>0) sb.append("0x1E");
        sb.append(p);
      }
    }
    return sb.toString();
  }
  /**
   * Sets metadata access policy array.
   * @param str metadata access policy array as coma separated string of values
   */
  public void setMetadataAccessPolicyString(String str) {
    setMetadataAccessPolicyArray(Val.chkStr(str).split("0x1E"));
  }
  
  /**
   * Gets Metadata Access Policy type
   * @return metadataAccessPolicyType
   */
  public String getMetadataAccessPolicyType() {
    return _metadataAccessPolicyType;
  }
  /**
   * Sets metadata access policy type
   * @param accessPolicyType
   */
  public void setMetadataAccessPolicyType(String accessPolicyType) {
    _metadataAccessPolicyType = accessPolicyType;
  }
  
  /**
   * Gets the collection UUID selected for sharing.
   * @return the collection UUID
   */
  public String getSharingCollectionUuid() {
    return _sharingCollectionUuid;
  }
  /**
   * Sets the collection UUID selected for sharing.
   * @param collectionUuid the collection UUID
   */
  public void setSharingCollectionUuid(String collectionUuid) {
    _sharingCollectionUuid = collectionUuid;
  }
  
  /**
   * Gets the toggle metadata access policy.
   * @return the toggle metadata access policy
   */
  public String getToggleMetadataAccessPolicy() {
    return _toggleMetadataAccessPolicy;
  }
  /**
   * Sets the toggle metadata access policy.
   * @param metadataAccessPolicy the toggle metadata access policy
   */
  public void setToggleMetadataAccessPolicy(String metadataAccessPolicy) {
    _toggleMetadataAccessPolicy = metadataAccessPolicy;
  }
  
  /**
   * Gets the owner to transfer documents to.
   * @return the new document owner
   */
  public String getTransferToOwner() {
      return _transferToOwner;
  }
  /**
   * Sets the owner to transfer documents to.
   * @param owner the new document owner
   */
  public void setTransferToOwner(String owner) {
      _transferToOwner = Val.chkStr(owner);
  }

}

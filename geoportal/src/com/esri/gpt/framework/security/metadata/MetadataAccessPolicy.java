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
package com.esri.gpt.framework.security.metadata;

import java.io.Serializable;

/**
 * This class is used to store metadata access policy configuration.
 */
public class MetadataAccessPolicy implements Serializable {

  /** class variables ========================================================= */
  
  /** Document access control type public-protected field (single-value) = "public-protected" */
  public static final String TYPE_PUBLIC_PROTECTED = "public-protected";
  
  /** Document access control type restricted field (single-value) = "restricted" */
  public static final String TYPE_RESTRICTED  = "restricted";
  
  /** Document access control type unrestricted field (single-value) = "unrestricted" */
  public static final String TYPE_UNRESTRICTED = "unrestricted";

  /** instance variables ====================================================== */
  private String _accessPolicyType = null;
  private String _accessToGroupDN  = null;

  /** Default constructor. */
  public MetadataAccessPolicy() {}
  
  /**
   * Gets the access policy type
   * @return access policy type
   */
  public String getAccessPolicyType() {
    return _accessPolicyType;
  }
  /**
   * Sets the access policy type
   * @param policyType policy type
   * @throws Exception if setting access policy type fails
   */
  public void setAccessPolicyType(String policyType) throws Exception {
    if (policyType != null) {
      if (policyType.trim().length() == 0) {
        _accessPolicyType = TYPE_UNRESTRICTED;
      } else if (policyType.equalsIgnoreCase(TYPE_UNRESTRICTED)
          || policyType.equalsIgnoreCase(TYPE_RESTRICTED) || policyType.equalsIgnoreCase(TYPE_PUBLIC_PROTECTED)) {
        _accessPolicyType = policyType.toLowerCase();
      } else {
        throw new Exception(
          "Invalid policy type.Policy type must 'restricted' or 'unrestricted' or 'public-protected'");
      }
    }
  }
  
  /**
   * Gets the group access distributed name
   * @return group access distributed name
   */
  public String getAccessToGroupDN() {
    return _accessToGroupDN;
  }
  
  /**
   * Sets the group access distributed name
   * @param toGroupDN access distributed name
   */
  public void setAccessToGroupDN(String toGroupDN) {
    String policyType = getAccessPolicyType(); 
    if (policyType != null && policyType.trim().length() > 0) {
      if (policyType.equalsIgnoreCase(TYPE_PUBLIC_PROTECTED)) {
        _accessToGroupDN = toGroupDN;
      } else{
        _accessToGroupDN = null;
      }
    }
  }
  
  /**
   * Indicates if the application metadata access policy is unrestricted.
   * @return <code>true</code> if the policy is unrestricted
   */
  public boolean isPolicyUnrestricted() {
    return (_accessPolicyType == null)
        || _accessPolicyType.equalsIgnoreCase(TYPE_UNRESTRICTED);
  }
  
  /**
   * Checks if has protected group DN is configured
   * @return <code>true</code> if protected group DN is configured
   */
  public boolean isProtectedGroupDN(){
    return this.getAccessToGroupDN() != null? true:false;
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
    sb.append(" accessPolicyType=").append(getAccessPolicyType()).append("\n");
    sb.append(" accessToGroupDN=").append(getAccessToGroupDN()).append("\n");
    sb.append(") ===== end ").append(getClass().getName());
    return sb.toString();
  }

}

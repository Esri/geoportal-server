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
package com.esri.gpt.sdisuite;
import java.util.Calendar;

/**
 * Represents brief license information properties.
 */
public class BriefLicenseInfo {

  /** instance variables ====================================================== */
  private boolean  mActive = false;
  private String   mLicenseId;
  private String   mLicenseManagerUrl = null;
  private Calendar mNotBefore = null;
  private Calendar mNotOnOrAfter = null;
  private String   mOrderId = null;
  private String   mProductId = null;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public BriefLicenseInfo() {}
  
  /** properties ============================================================== */
  
  public boolean isActive() {
    return mActive;
  }
  public void setActive(final boolean pActive) {
    mActive = pActive;
  }

  public String getLicenseId() {
    return mLicenseId;
  }
  public void setLicenseId(final String pLicenseId) {
    mLicenseId = pLicenseId;
  }

  public String getLicenseManagerUrl() {
    return mLicenseManagerUrl;
  }
  public void setLicenseManagerUrl(final String pLicenseManagerUrl) {
    mLicenseManagerUrl = pLicenseManagerUrl;
  }
  public boolean isSetLicenseManagerUrl() {
    return getLicenseManagerUrl() != null;
  }
  
  public Calendar getNotBefore() {
    return mNotBefore;
  }
  public void setNotBefore(final Calendar pNotBefore) {
    mNotBefore = pNotBefore;
  }
  public boolean isSetNotBefore() {
    return getNotBefore() != null;
  }
  
  public Calendar getNotOnOrAfter() {
    return mNotOnOrAfter;
  }
  public void setNotOnOrAfter(final Calendar pNotOnOrAfter) {
    mNotOnOrAfter = pNotOnOrAfter;
  }
  public boolean isSetNotOnOrAfter() {
    return getNotOnOrAfter() != null;
  }
  
  public String getOrderId() {
    return mOrderId;
  }
  public void setOrderId(final String pOrderId) {
    mOrderId = pOrderId;
  }
  public boolean isSetOrderId() {
    return getOrderId() != null;
  }

  public String getProductId() {
    return mProductId;
  }
  public void setProductId(final String pProductId) {
    mProductId = pProductId;
  }
  public boolean isSetProductId() {
    return getProductId() != null;
  }

}
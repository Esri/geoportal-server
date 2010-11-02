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
package com.esri.gpt.control.harvest;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocol.ProtocolType;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolResource;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolArcIms;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolCsw;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolOai;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolWaf;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISProtocol;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.jsf.FacesMap;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.faces.model.SelectItem;

/**
 * Provides harvest repository editing functionality.
 * It assures that each harvest repository can be viewed in the same, unified 
 * way regardless the protocol. It also provides smooth values translation
 * from/to native form as stored within the object and to/from string value as
 * required in most cases to display on the form.
 */
public class HarvestEditor {

// class variables =============================================================
/** CSW profiles manager */
private static CswProfilesManager _cswProfilesManager = new CswProfilesManager();

// instance variables ==========================================================
  
/** validation status */  
private boolean _valid;
  
/** Edited repository. */
private HrRecord _harvestRepository;

/** all possible protocols */
private TreeMap<String,Protocol> protocols = new TreeMap<String,Protocol>(String.CASE_INSENSITIVE_ORDER);
{
  ApplicationContext appCtx = ApplicationContext.getInstance();
  ApplicationConfiguration appCfg = appCtx.getConfiguration();
  for (Map.Entry<String,ProtocolFactory> e : appCfg.getProtocolFactories().entrySet()) {
    protocols.put(e.getKey(), e.getValue().newProtocol());
  }
}

// constructors ================================================================
/**
 * Creates instance of editor.
 * @param harvestRepository repository to be edited
 */
public HarvestEditor(HrRecord harvestRepository) {
  _harvestRepository = harvestRepository;
  protocols.put(harvestRepository.getProtocol().getKind(), harvestRepository.getProtocol());
}
// properties ==================================================================
/**
 * Gets edited repository.
 * @return edited repository
 */
public HrRecord getRepository() {
  return _harvestRepository;
}

/**
 * Gets owner id as string.
 * @return owner id
 */
public String getOwnerId() {
  return Integer.toString(_harvestRepository.getOwnerId());
}

/**
 * Sets owner id from string.
 * @param value owner id
 */
public void setOwnerId(String value) {
  _harvestRepository.setOwnerId(Val.chkInt(value, -1));
}

/**
 * Gets map of attributes.
 * @return map of attributes
 */
public Map<String,String> getAttrs() {
  return new FacesMap<String>() {

      @Override
      public String get(Object key) {
        StringAttributeMap map = _harvestRepository.getProtocol().getAttributeMap();
        String value = map.getValue(key.toString());
        return Val.chkStr(value);
      }

      @Override
      public String put(String key, String value) {
        StringAttributeMap map = _harvestRepository.getProtocol().getAttributeMap();
        map.set(key.toString(), Val.chkStr(value));
        _harvestRepository.getProtocol().setAttributeMap(map);
        return value;
      }

  };
}

/**
 * Gets protocol name.
 * @return protocol name
 */
public String getType() {
  return _harvestRepository.getProtocol().getKind().toLowerCase();
}

/**
 * Sets protocol name.
 * @param name protocol name name
 */
public void setType(String name) {
  _harvestRepository.setProtocol(protocols.get(name));
}

/**
 * Checks if 'deep harvest' is enabled.
 * @return <code>true</code> if 'deep harvest' is enabled
 */
public boolean getUpdateContent() {
  return ProtocolInvoker.getUpdateContent(_harvestRepository.getProtocol());
}

/**
 * Enables/disables 'deep harvest'
 * @param enabled <code>true</code> to enable 'deep harvest'
 */
public void setUpdateContent(boolean enabled) {
  ProtocolInvoker.setUpdateContent(_harvestRepository.getProtocol(), enabled);
}

/**
 * Checks if 'xml generation' is enabled.
 * @return <code>true</code> if 'xml generation' is enabled
 */
public boolean getUpdateDefinition() {
  return ProtocolInvoker.getUpdateDefinition(_harvestRepository.getProtocol());
}

/**
 * Enables/disables 'xml generation'
 * @param enabled <code>true</code> to enable 'xml generation'
 */
public void setUpdateDefinition(boolean enabled) {
  ProtocolInvoker.setUpdateDefinition(_harvestRepository.getProtocol(), enabled);
}

/**
 * Checks if 'auto-approve' is enabled.
 * @return <code>true</code> if 'auto-approve' is enabled
 */
public boolean getAutoApprove() {
  return ProtocolInvoker.getAutoApprove(_harvestRepository.getProtocol());
}

/**
 * Enables/disables 'auto-approve'
 * @param enabled <code>true</code> to enable 'auto-approve'
 */
public void setAutoApprove(boolean enabled) {
  ProtocolInvoker.setAutoApprove(_harvestRepository.getProtocol(), enabled);
}

/**
 * Checks if 'lock-title' is enabled.
 * If a flag is set, it means a title is locked and synchronizer is not allowed
 * to update it, although all the rest of information is allowed to be updated.
 * @return <code>true</code> if 'lock-title' is enabled
 */
public boolean getLockTitle() {
  return ProtocolInvoker.getLockTitle(_harvestRepository.getProtocol());
}

/**
 * Enables/disables 'lock-title'
 * If a flag is set, it means a title is locked and synchronizer is not allowed
 * to update it, although all the rest of information is allowed to be updated.
 * @param lockTitle <code>true</code> to enable 'lock-title'
 */
public void setLockTitle(boolean lockTitle) {
  ProtocolInvoker.setLockTitle(_harvestRepository.getProtocol(), lockTitle);
}

/**
 * Gets harvest frequency.
 * @return harvest frequency
 */
public String getHarvestFrequency() {
  return _harvestRepository.getHarvestFrequency().name().toLowerCase();
}

/**
 * Sets harvest frequency.
 * @param harvestFrequency harvest frequency
 */
public void setHarvestFrequency(String harvestFrequency) {
  _harvestRepository.setHarvestFrequency(
     HrRecord.HarvestFrequency.checkValueOf(harvestFrequency));
}

/**
 * Gets sending notification switch flag.
 * @return sending notification switch flag
 */
public String getSendNotification() {
  return Boolean.toString(_harvestRepository.getSendNotification());
}

/**
 * Sets sending notification switch flag.
 * @param sendNotification sending notification switch flag
 */
public void setSendNotification(String sendNotification) {
  _harvestRepository.setSendNotification(Val.chkBool(sendNotification, false));
}

/**
 * Gets repository name.
 * @return name
 */
public String getName() {
  return _harvestRepository.getName();
}

/**
 * Sets repository name.
 * @param name name
 */
public void setName(String name) {
  _harvestRepository.setName(name);
}

/**
 * Gets host URL.
 * @return host URL
 */
public String getHostUrl() {
  return _harvestRepository.getHostUrl();
}

/**
 * Sets host URL.
 * @param hostUrl host URL
 */
public void setHostUrl(String hostUrl) {
  _harvestRepository.setHostUrl(hostUrl);
}

/**
 * Gets SOAP URL.
 * @return SOAP URL
 */
public String getSoapUrl() {
  return _harvestRepository.getProtocol() instanceof ArcGISProtocol?
    ((ArcGISProtocol)_harvestRepository.getProtocol()).getSoapUrl():
    "";
}

/**
 * Sets SOAP URL.
 * @param url SOAP URL
 */
public void setSoapUrl(String url) {
  if (_harvestRepository.getProtocol() instanceof ArcGISProtocol) {
    ((ArcGISProtocol)_harvestRepository.getProtocol()).setSoapUrl(url);
  }
}

/**
 * Gets ArcIMS properties.
 * @return ArcIMS properties
 */
public HarvestProtocolArcIms getArcIms() {
  return (HarvestProtocolArcIms) protocols.get(ProtocolType.ArcIms.name());
}

/**
 * Gets OAI properties.
 * @return OAI properties
 */
public HarvestProtocolOai getOai() {
  return (HarvestProtocolOai) protocols.get(ProtocolType.OAI.name());
}

/**
 * Gets WAF properties.
 * @return WAF properties
 */
public HarvestProtocolWaf getWaf() {
  return (HarvestProtocolWaf) protocols.get(ProtocolType.WAF.name());
}

/**
 * Gets CSW properties.
 * @return CSW properties
 */
public HarvestProtocolCsw getCsw() {
  return (HarvestProtocolCsw) protocols.get(ProtocolType.CSW.name());
}


/**
 * Gets CSW properties.
 * @return CSW properties
 */
public HarvestProtocolResource getArcGis() {
  return (HarvestProtocolResource) protocols.get(ProtocolType.RES.name());
}

/**
 * Gets all CSW profiles.
 * @return array list of all CSW profiles id's
 */
public ArrayList<SelectItem> getAllProfiles() {
  return _cswProfilesManager.getAllItems();
}

/**
 * Gets approval status.
 * @return approval status
 */
public String getApprovalStatus() {
  return getRepository().getApprovalStatus().name();
}

/**
 * Approval status.
 * @param status approval status
 */
public void setApprovalStatus(String status) {
  getRepository().setApprovalStatus(ApprovalStatus.checkValue(status));
}
// methods =====================================================================

/**
 * Validates entered content.
 * @param mb message broker
 * @return <code>true</code> if data is valid
 */
public boolean validate(MessageBroker mb) {
  _valid = true;
  
  if (getHostUrl().length()==0) {
    mb.addErrorMessage("catalog.harvest.manage.edit.err.hostUrlReq");
    _valid = false;
  }

  String kind = _harvestRepository.getProtocol().getKind();
  if (kind.equalsIgnoreCase(ProtocolType.ArcIms.name())) {
    if (getArcIms().getPortNoAsString().length()==0) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.portNumberReq");
      _valid = false;
    } else {
      try {
        int portNo = Integer.parseInt(getArcIms().getPortNoAsString());
        if (!(portNo >= 0 && portNo < 65536)) {
          mb.addErrorMessage("catalog.harvest.manage.edit.err.portNumberInv");
          _valid = false;
        }
      } catch (NumberFormatException ex) {
        mb.addErrorMessage("catalog.harvest.manage.edit.err.portNumberInv");
        _valid = false;
      }
    }
    if (getArcIms().getServiceName().length()==0) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.serviceNameReq");
      _valid = false;
    }
  } else if (kind.equalsIgnoreCase(ProtocolType.OAI.name())) {
    if (getOai().getPrefix().length()==0) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.prefixReq");
      _valid = false;
    }
  } else if (kind.equalsIgnoreCase("arcgis")) {
    ArcGISProtocol p = (ArcGISProtocol) protocols.get("arcgis");
    if (p.getSoapUrl().length()==0) {
      mb.addErrorMessage("catalog.harvest.manage.edit.err.soapUrl");
      _valid = false;
    }
  }
  
  return _valid;
}

/**
 * Gets string representation of the object.
 * @return string representation
 */
@Override
public String toString() {
  return _harvestRepository.toString();
}
}

/*
 * Copyright 2015 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.client.ckan;

import com.esri.gpt.agp.client.AgpItem;
import com.esri.gpt.agp.client.AgpProperty;
import com.esri.gpt.framework.ckan.CkanResource;

/**
 * Converts CKAN information into AGP information.
 */
public class CkanToAgpConverter {
  
  public AgpItem makeItem(CkanResource res) {
    AgpItem item = new AgpItem();
    
    initProp(item, "id", extractId(res));
    initProp(item, "title", extractTitle(res));
    initProp(item, "name", extractName(res));
    initProp(item, "url", extractUrl(res));
    initProp(item, "description", extractDescription(res));
    initProp(item, "thumbnailurl", extractThumbnailUrl(res));
    initProp(item, "text", extractText(res));
    initProp(item, "extent", extractExtent(res));
    initProp(item, "type", extractType(res));
    initProp(item, "typeKeywords", extractTypeKeywords(res));
    initProp(item, "modified", extractModified(res));
    
    return isValid(item)? item: null;
  }
  
  protected boolean isValid(AgpItem item) {
    return true;
  }
  
  protected void initProp(AgpItem item, String attrName, String attrValue) {
    if (attrValue!=null) {
      item.getProperties().add(new AgpProperty(attrName, attrValue));
    }
  }
  
  protected String extractId(CkanResource res) {
    return res.getId();
  }
  
  protected String extractTitle(CkanResource res) {
    return res.getName();
  }
  
  protected String extractName(CkanResource res) {
    return res.getName();
  }
  
  protected String extractUrl(CkanResource res) {
    return res.getUrl().toExternalForm();
  }
  
  protected String extractDescription(CkanResource res) {
    return res.getDescription();
  }
  
  protected String extractThumbnailUrl(CkanResource res) {
    return null;
  }
  
  protected String extractText(CkanResource res) {
    return null;
  }
  
  protected String extractExtent(CkanResource res) {
    return null;
  }
  
  protected String extractType(CkanResource res) {
    return res.getFormat();
  }
  
  protected String extractTypeKeywords(CkanResource res) {
    return null;
  }
  
  protected String extractModified(CkanResource res) {
    return res.getUpdateDate()!=null? ""+res.getUpdateDate().getTime(): null;
  }
}

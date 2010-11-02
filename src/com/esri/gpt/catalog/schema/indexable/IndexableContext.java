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
package com.esri.gpt.catalog.schema.indexable;
import com.esri.gpt.catalog.discovery.IStoreable;
import com.esri.gpt.catalog.discovery.IStoreables;
import com.esri.gpt.catalog.discovery.PropertyMeaning;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.lucene.Storeables;
import com.esri.gpt.catalog.schema.EsriTags;
import com.esri.gpt.catalog.schema.Meaning;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.framework.util.Val;

import java.util.ArrayList;
import org.w3c.dom.Document;

/**
 * Provides a context for the indexing of properties associated with a metadata schema.
 */
public class IndexableContext {

  /** instance variables ====================================================== */
  private PropertyMeanings   propertyMeanings;
  private ResourceIdentifier resourceIdentifier;
  private IStoreables        storables;
  
  /** constructors ============================================================ */
  
  /**
   * Construct with a configured collection of property meanings.
   * propertyMeanings the configured property meanings
   */
  public IndexableContext(PropertyMeanings propertyMeanings) {
    this.propertyMeanings = propertyMeanings;
    this.storables = new Storeables(this.propertyMeanings,true);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the configured property meanings.
   * @return the property meanings
   */
  public PropertyMeanings getPropertyMeanings() {
    return this.propertyMeanings;
  }
  
  /**
   * Gets storeable properties.
   * @return the storeable properties
   */
  public IStoreables getStoreables() {
    return this.storables;
  }
  

  
  /** methods ================================================================= */
  
  /**
   * Adds a storeable value.
   * @param meaning the associated property meaning
   * @param value the value to add
   */
  public void addStoreableValue(PropertyMeaning meaning, Object value) {
    if (value != null) {
      this.addStorableValues(meaning, new Object[]{value});
    }
  }
  
  /**
   * Adds a collection storeable values.
   * @param meaning the associated property meaning
   * @param values the values to add
   */
  public void addStorableValues(PropertyMeaning meaning, Object[] values) {
    if ((meaning != null) && (values != null) && (values.length > 0)) {
      IStoreable storeable = this.getStoreables().get(meaning.getName());
      if (storeable == null) {
        storeable = this.getStoreables().connect(meaning);
        if (storeable != null) {
          ((Storeables)this.getStoreables()).add(storeable);
        }
      }
      if (storeable == null) {
        // TODO warn
      } else {
        
        Object[] existingValues = storeable.getValues();
        if ((existingValues != null) && (existingValues.length > 0)) {
          java.util.ArrayList<Object> alValues = new ArrayList<Object>();
          for (Object value: existingValues) alValues.add(value);
          for (Object value: values) alValues.add(value);
          storeable.setValues(alValues.toArray());
        } else {
          storeable.setValues(values);
        }
        
      }
    }
  }
  
  /**
   * Ensures the existence of a resource identifier.
   * @return the resource identifier
   */
  public ResourceIdentifier ensureResourceIdentifier() {
    if (this.resourceIdentifier == null) {
      this.resourceIdentifier = ResourceIdentifier.newIdentifier(null);
    }
    return this.resourceIdentifier;
  }
  
  /**
   * Gets the first storeable string value associated with a property meaning name.
   * @param name the property meaning name
   * @return the first storeable value (can be null)
   */
  private String getFirstStoreableString(String name) {
    IStoreable storeable = this.getStoreables().get(name);
    if (storeable != null) {
      Object[] values = storeable.getValues();
      if ((values != null) && (values.length > 0)) {
        for (Object value: values) {
          if ((value != null) && (value instanceof String)) {
            String sValue = Val.chkStr((String)value);
            if (sValue.length() > 0) {
              return sValue;
            }
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Gets the first storeable value associated with a property meaning name.
   * @param name the property meaning name
   * @return the first storeable value (can be null)
   */
  private String getFirstStoreableValue(String name) {
    IStoreable storeable = this.getStoreables().get(name);
    if (storeable != null) {
      Object[] values = storeable.getValues();
      if ((values != null) && (values.length > 0) && (values[0] != null)) {
        return values[0].toString();
      }
    }
    return null;
  }
  
  /**
   * Resolves any inconsistencies associated with 
   * @param schema the schema being evaluated
   * @param esriTags the evaluated ESRI tags (/metadata/Esri)
   * @param dom the metadata document
   */
  public void resolve(Schema schema, Document dom, EsriTags esriTags) {
    
    // check the title
    String sTitle = Val.chkStr(this.getFirstStoreableString(Meaning.MEANINGTYPE_TITLE));
    if (sTitle.length() > 0) {
      schema.getMeaning().setTitle(sTitle);
    }
    
    // check the file identifier
    String sFileId = Val.chkStr(this.getFirstStoreableString(Meaning.MEANINGTYPE_FILEIDENTIFIER));
    if (sFileId.length() > 0) {
      schema.getMeaning().setFileIdentifier(sFileId);
    }
    
    // check the thumbnail url
    String sThumbUrl = Val.chkStr(this.getFirstStoreableString(Meaning.MEANINGTYPE_THUMBNAIL_URL));
    if (sThumbUrl.length() > 0) {
      schema.getMeaning().setThumbnailUrl(sThumbUrl);
    }
    
    // ESRI tags: published doc id
    if (esriTags != null) {
      String esriDocID = schema.getMeaning().getEsriDocID();
      if ((esriDocID == null) || (esriDocID.length() == 0)) {
        esriDocID = esriTags.getPublishedDocId();
        if ((esriDocID != null) && (esriDocID.length() > 0)) {
          schema.getMeaning().setEsriDocID(esriDocID);
        }
      }
    }
    
    // ESRI tags: thumbnail attachment (base64)
    if (esriTags != null) {
      String base64Thumbnail = schema.getMeaning().getThumbnailBinary();
      if ((base64Thumbnail == null) || (base64Thumbnail.length() == 0)) {
        base64Thumbnail = esriTags.getThumbnailBinary();
        if ((base64Thumbnail != null) && (base64Thumbnail.length() > 0)) {
          schema.getMeaning().setThumbnailBinary(base64Thumbnail);
        }
      }
    }
    
    // ESRI tags: website URL
    if (esriTags != null) {
      String sWebsiteUrl = Val.chkStr(this.getFirstStoreableString(Meaning.MEANINGTYPE_WEBSITE_URL));
      if ((sWebsiteUrl == null) || (sWebsiteUrl.length() == 0)) {
        sWebsiteUrl = esriTags.getPrimaryOnlink();
        if ((sWebsiteUrl != null) && (sWebsiteUrl.length() > 0)) {
          PropertyMeaning meaning = this.getPropertyMeanings().get(Meaning.MEANINGTYPE_WEBSITE_URL);
          IStoreable storeable = this.getStoreables().get(Meaning.MEANINGTYPE_WEBSITE_URL);
          if ((meaning != null) && (storeable != null)) {
            storeable.setValue(sWebsiteUrl);
          } else if (meaning != null) {
            this.addStoreableValue(meaning,sWebsiteUrl);
          }
        }
      }
    }
    
    // ESRI tags: resource URL
    if (esriTags != null) {
      String sResourceUrl = Val.chkStr(this.getFirstStoreableString(Meaning.MEANINGTYPE_RESOURCE_URL));
      if ((sResourceUrl == null) || (sResourceUrl.length() == 0)) {
        ResourceIdentifier ri = ensureResourceIdentifier();
        sResourceUrl = esriTags.makeResourceUrl(ri);
        if ((sResourceUrl != null) && (sResourceUrl.length() > 0)) {
          PropertyMeaning meaning = this.getPropertyMeanings().get(Meaning.MEANINGTYPE_RESOURCE_URL);
          IStoreable storeable = this.getStoreables().get(Meaning.MEANINGTYPE_RESOURCE_URL);
          if ((meaning != null) && (storeable != null)) {
            storeable.setValue(sResourceUrl);
          } else if (meaning != null) {
            this.addStoreableValue(meaning,sResourceUrl);
          }
        }
      }
    }
    
    // classify the ArcIMS content type from the resource URL
    String aimsContentType = Val.chkStr(this.getFirstStoreableValue(Meaning.MEANINGTYPE_CONTENTTYPE));
    if (aimsContentType.length() == 0) {
      PropertyMeaning meaning = this.getPropertyMeanings().get(Meaning.MEANINGTYPE_CONTENTTYPE);
      IStoreable storeable = this.getStoreables().get(Meaning.MEANINGTYPE_RESOURCE_URL);
      if ((meaning != null) && (storeable != null)) {
        Object[] values = storeable.getValues();
        if (values != null) {
          for (Object value: values) {
            if ((value != null) && (value instanceof String)) {
              String url = Val.chkStr((String)value);
              if (url.length() > 0) {
                ResourceIdentifier ri = ensureResourceIdentifier();
                aimsContentType = Val.chkStr(ri.guessArcIMSContentTypeFromUrl(url));
                if (aimsContentType.length() > 0) {
                  this.addStoreableValue(meaning,aimsContentType);
                  break;
                }
              }
            }
          }
        }
      }
    }
    
  }
  
}

/*
 * Copyright 2014 Esri, Inc..
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
package com.esri.gpt.control.georss.dcatdef;

import com.esri.gpt.control.georss.DcatField;
import com.esri.gpt.control.georss.DcatFields;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import java.util.Map;
import java.util.Properties;

/**
 * Base dcat field.
 */
public abstract class BaseDcatField implements DcatFieldDefinition {
  protected final String fldName;
  protected FlagsProvider flags;

  /**
   * Creates instance of the class.
   * @param fldName field name
   */
  public BaseDcatField(String fldName) {
    this.fldName = fldName;
    this.flags = new DefaultFlagsProvider();
  }

  /**
   * Creates instance of the class.
   * @param fldName field name
   * @param flags flags
   */
  public BaseDcatField(String fldName, long flags) {
    this.fldName = fldName;
    this.flags = new DefaultFlagsProvider(flags);
  }

  /**
   * Creates instance of the class.
   * @param fldName field name
   * @param flags flags provider
   */
  public BaseDcatField(String fldName, FlagsProvider flags) {
    this.fldName = fldName;
    this.flags = flags!=null? flags: new DefaultFlagsProvider(); 
  }

  /**
   * Gets output field name.
   * @return field name
   */
  protected String getOutFieldName() {
    return fldName;
  }

  /**
   * Gets index.
   * @param r feed record
   * @return map of attributes
   */
  protected Map<String, IFeedAttribute> getIndex(IFeedRecord r) {
    Map<String, IFeedAttribute> index = r.getData(IFeedRecord.STD_COLLECTION_INDEX);
    return index;
  }

  /**
   * Gets schema key.
   * @param index map of attributes
   * @return schema key
   */
  protected String getSchemaKey(Map<String, IFeedAttribute> index) {
    IFeedAttribute schemaKeyAttr = index.get("sys.schema.key");
    String schemaKey = null;
    if (schemaKeyAttr != null) {
      schemaKey = schemaKeyAttr.simplify().getValue().toString();
    }
    return schemaKey;
  }

  /**
   * Gets dcat fields
   * @param dcatSchemas dcat schemas
   * @param schemaKey schema key
   * @return dcat fields
   */
  protected DcatFields getDcatFields(DcatSchemas dcatSchemas, String schemaKey) {
    DcatFields dcatFields = dcatSchemas.get(schemaKey);
    if (dcatFields == null) {
      dcatFields = dcatSchemas.getDefaultFields();
    }
    return dcatFields;
  }

  /**
   * Gets dcat field.
   * @param dcatFields dcat fields
   * @return dcat field
   */
  protected DcatField getDcatField(DcatFields dcatFields) {
    return getDcatField(dcatFields, fldName);
  }

  /**
   * Gets dcat field.
   * @param dcatFields dcat fields
   * @param _fieldName field name
   * @return dcat field
   */
  protected DcatField getDcatField(DcatFields dcatFields, String _fieldName) {
    DcatField field = dcatFields.getField(_fieldName);
    return field;
  }

  /**
   * Gets feed attribute.
   * @param index index
   * @param field field
   * @return attribute or <code>null</code> if not available
   */
  protected IFeedAttribute getFeedAttribute(Map<String, IFeedAttribute> index, DcatField field) {
    IFeedAttribute attr = index.get(field.getIndex());
    return attr;
  }

  /**
   * Gets feed attribute.
   * @param dcatSchemas dcat schemas
   * @param r feed record
   * @return feed attribute
   */
  protected IFeedAttribute getFeedAttribute(DcatSchemas dcatSchemas, IFeedRecord r) {
    return getFeedAttribute(dcatSchemas, r, fldName);
  }

  /**
   * Gets feed attribute.
   * @param dcatSchemas dcat schemas
   * @param r feed record
   * @param _fieldName field name
   * @return feed attribute
   */
  protected IFeedAttribute getFeedAttribute(DcatSchemas dcatSchemas, IFeedRecord r, String _fieldName) {
    Map<String, IFeedAttribute> index = getIndex(r);
    if (index == null) {
      return null;
    }
    String schemaKey = getSchemaKey(index);
    if (schemaKey == null) {
      return null;
    }
    DcatFields dcatFields = getDcatFields(dcatSchemas, schemaKey);
    if (dcatFields == null) {
      return null;
    }
    DcatField field = getDcatField(dcatFields, _fieldName);
    if (field == null) {
      return null;
    }
    IFeedAttribute attr = getFeedAttribute(index, field);
    return attr;
  }
  
  public static interface FlagsProvider {
    long provide(IFeedRecord r, IFeedAttribute attr, Properties properties);
  }
  
  private static class DefaultFlagsProvider implements FlagsProvider {
    private final long flags;

    public DefaultFlagsProvider(long flags) {
      this.flags = flags;
    }

    public DefaultFlagsProvider() {
      this.flags = 0;
    }

    @Override
    public long provide(IFeedRecord r, IFeedAttribute attr, Properties properties) {
      return flags;
    }
    
    
  }
  
}

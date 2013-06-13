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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.catalog.discovery.PropertyComparisonType;
import com.esri.gpt.catalog.discovery.IStoreable;
import com.esri.gpt.catalog.discovery.IStoreables;
import com.esri.gpt.catalog.discovery.PropertyMeaning;
import com.esri.gpt.catalog.discovery.PropertyMeaningType;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.discovery.PropertyValueType;
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import com.esri.gpt.framework.collection.StringSet;

import java.util.Collection;

import org.apache.lucene.document.Field;

/**
 * Essentially defines the storage schema for documents indexed within Lucene.
 */
public class Storeables implements IStoreables {
  
  /** class variables ========================================================= */
  
  /** Document access control list field (multi-value) = "acl" */
  public static final String FIELD_ACL = "acl";
  
  /** Document modification date field name = "datemodified" */
  public static final String FIELD_DATEMODIFIED = "dateModified";
  
  /** Document file identifier field name = "fileIdentifier" */
  public static final String FIELD_FID = "fileIdentifier";
  
  /** Document SCHEMA = "sys.schema", e.g. http://www.isotc211.org/2005/gmd */
  public static final String FIELD_SCHEMA = "sys.schema";
  
  /** Document SCHEMA = "sys.schema.key", e.g. fgdc */
  public static final String FIELD_SCHEMA_KEY = "sys.schema.key";
  
  /** Document SITEUUID, parent site uuid = "sys.siteuuid" */
  public static final String FIELD_SITEUUID = "sys.siteuuid";
  
  /** Document UUID (primary key) field name = "uuid" */
  public static final String FIELD_UUID = "uuid";
  
  /** Document XML = "sys.xml" */
  public static final String FIELD_XML = "sys.xml";
  
  /** CSW Brief Response XML = "sys.xml.brief" */
  public static final String FIELD_XML_BRIEF = "sys.xml.brief";
  
  /** CSW Summary Response XML = "sys.xml.brief" */
  public static final String FIELD_XML_SUMMARY = "sys.xml.summary";
     
  /** instance variables ====================================================== */
  private CaseInsensitiveMap<IStoreable> map = new CaseInsensitiveMap<IStoreable>(false);
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public Storeables() {}
  
  /**
   * Constructs with a configured collection of property meanings.
   * @param meanings the configured property meanings
   */
  public Storeables(PropertyMeanings meanings) { 
    this(meanings, false);
  }
  
  /**
   * Constructs with a configured collection of property meanings.
   * @param meanings the configured property meanings
   * @param systemOnly if true, only configure the system storeables
   */
  public Storeables(PropertyMeanings meanings, boolean systemOnly) { 
    if (systemOnly) {
      this.ensure(meanings,Storeables.FIELD_UUID);
      this.ensure(meanings,Storeables.FIELD_DATEMODIFIED);
    } else {
      for (PropertyMeaning meaning: meanings.values()) {
        IStoreable storeable = this.connect(meaning);
        this.add(storeable);
      }
    }
  }
  
  /** properties ============================================================== */
  
  /** methods ================================================================= */
  
  /**
   * Adds a store-able property to the collection.
   * <br/>The collection is keyed on IStoreable.name.
   * @param storable the store-able property to add.
   */
  public void add(IStoreable storable) {
    if (storable != null) {
      map.put(storable.getName(),storable);
    }
  }
  
  
  /**
   * Returns the storeables collection (for iteration).
   * @return the collection of storeables
   */
  public Collection<IStoreable> collection() {
    return this.map.values();
  }
  
  /**
   * Connects a meaning to a store-able property,
   * @param meaning the meaning associated with the property to connect
   * @return an appropriate store-able (null if none was located);
   */
  public IStoreable connect(PropertyMeaning meaning) {
    
    Storeable storable = null;
    
    String fieldName = meaning.getName();
    PropertyMeaningType meaningType = meaning.getMeaningType();
    PropertyComparisonType comparisonType = meaning.getComparisonType();
    PropertyValueType valueType = meaning.getValueType();
    
    if (meaning instanceof PropertyMeaning.AnyText) {
      PropertyMeaning.AnyText anyText = (PropertyMeaning.AnyText)meaning;
      storable = new AnyTextProperty(fieldName,anyText.getNamesToConsider());

    } else if (comparisonType.equals(PropertyComparisonType.ANYTEXT)) {
      storable = new AnyTextProperty(fieldName,new StringSet());
      
    } else if (fieldName.equalsIgnoreCase("body")) {
      storable = new Storeable(fieldName);
      DatastoreField field = new DatastoreField(fieldName,
          Field.Store.NO,Field.Index.ANALYZED,Field.TermVector.NO);
      storable.getFields().add(field);
            
    } else if (comparisonType.equals(PropertyComparisonType.KEYWORD)) {
      storable = new Storeable(fieldName);
      storable.setComparisonField(new LowerCaseField(fieldName));
      storable.setRetrievalField(new ReferenceField(fieldName+".ref"));
      storable.getFields().add(storable.getComparisonField());
      storable.getFields().add(storable.getRetrievalField());
      
    } else if (!meaningType.equals(PropertyMeaningType.DATEMODIFIED) &&
                valueType.equals(PropertyValueType.TIMESTAMP)) {
      storable = new Storeable(fieldName);
      storable.setComparisonField(new TimestampField(fieldName));
      storable.setTermsField(new TermsField(fieldName+".input"));
      storable.getFields().add(storable.getComparisonField());
      storable.getFields().add(storable.getTermsField());
      storable.setRetrievalField(storable.getComparisonField());
      
    } else if (comparisonType.equals(PropertyComparisonType.VALUE)) {
      DatastoreField field = null;
      
      if (valueType.equals(PropertyValueType.DOUBLE)) {
        field = new DoubleField(fieldName,DoubleField.DEFAULT_PRECISION);
        
      } else if (valueType.equals(PropertyValueType.GEOMETRY)) {
        storable = new GeometryProperty(fieldName);

      } else if (valueType.equals(PropertyValueType.TIMEPERIOD)) {
        storable = new TimeperiodProperty(fieldName);
        
      } else if (valueType.equals(PropertyValueType.LONG)) {
        field = new LongField(fieldName);
        
      } else if (valueType.equals(PropertyValueType.STRING)) {
        field = new DatastoreField(fieldName,
                    Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
        
      } else if (valueType.equals(PropertyValueType.TIMESTAMP)) {
        field = new TimestampField(fieldName);
        
      } else {
        
        // TODO issue warning here (value type not recognized)
      }
      if (field != null) {
        storable = new Storeable(fieldName);
        storable.setComparisonField(field);
        storable.setRetrievalField(field);
        storable.getFields().add(field);
      }
      
    } else if (comparisonType.equals(PropertyComparisonType.TERMS)) {
      storable = new Storeable(fieldName);
      storable.setTermsField(new TermsField(fieldName));
      storable.setComparisonField(new LowerCaseField(fieldName+".lc"));
      storable.setRetrievalField(storable.getTermsField());
      storable.getFields().add(storable.getTermsField());
      storable.getFields().add(storable.getComparisonField());
      
    } else {
      
      // TODO issue warning here (comparison type not recognized)
    }
    
    return storable;
    
  }
  
  /**
   * Ensures a storeable property within the collection.
   * @param propertyMeanings the configured property meanings
   * @param meaningName the property meaning name
   * @return the storeable property (null if no match was located)
   */
  public IStoreable ensure(PropertyMeanings propertyMeanings, String meaningName) {
    IStoreable storeable = this.get(meaningName);
    if (storeable == null) {
      PropertyMeaning meaning = propertyMeanings.get(meaningName);
      if (meaning != null) {
        storeable = this.connect(meaning);
        if (storeable != null) {
          this.add(storeable);
        }
      }
    }
    return storeable;
  }
  
  /**
   * Finds a store-able property within the collection.
   * @param name the name of the store-able property
   * @return the store-able property (null if no match was located)
   */
  public IStoreable get(String name) {
    return map.get(name);
  }
                                        
}

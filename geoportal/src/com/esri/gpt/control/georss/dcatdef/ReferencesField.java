/*
 * Copyright 2015 Esri, Inc..
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

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.control.georss.DcatField;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * References.
 */
public class ReferencesField  extends BaseDcatField {
  private final IResourceLinkPredicate predicate;

  public ReferencesField(String name) {
    super(name);
    this.predicate = new AcceptAllPredicate();
  }

  public ReferencesField(String name, String ... excludedTags) {
    super(name);
    this.predicate = new ExcludeSelectedTagsPredicate(excludedTags);
  }
  
  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    HashSet<String> selectedLinks = new HashSet<String>();
    
    appendResourceLinks(selectedLinks, r);
    appendSchemaLinks(selectedLinks, properties, dcatSchemas, r);
    
    if (!selectedLinks.isEmpty()) {
      jsonWriter.name(getOutFieldName()).beginArray();
      for (String url: selectedLinks) {
        jsonWriter.value(url);
      }
      jsonWriter.endArray();
    }
  }
  
  private void appendSchemaLinks(Set<String> links, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) {
    Map<String, IFeedAttribute> index = getIndex(r);
    if (index == null) {
      return;
    }
    DcatField field = getAttributeField(dcatSchemas, index, r, fldName);
    if (field == null) {
      return;
    }
    IFeedAttribute attr = getFeedAttribute(index, field);

    if (attr!=null) {
      for (String val: readValue(dcatSchemas, field, r, attr)) {
        val = validateValue(val);
        if (!val.isEmpty()){
          links.add(val);
        }
      }
    }
  }
  
  /**
   * Validates value.
   * @param value value to validate
   * @return <code>true</code> if value is valid
   */
  protected String validateValue(String value) {
    return Val.chkStr(value);
  }

  protected ArrayList<String> readValue(DcatSchemas dcatSchemas, DcatField dcatField, IFeedRecord r, IFeedAttribute attr) {
    ArrayList<String> value = new ArrayList<String>();
    if (attr.getValue() instanceof List) {
      try {
        for (IFeedAttribute o : (List<IFeedAttribute>) attr.getValue()) {
          value.add(dcatField.translate(o.simplify().getValue().toString()));
        }
      } catch (ClassCastException ex) {
      }
    } else {
      value.add(dcatField.translate(attr.simplify().getValue().toString()));
    }
    return value;
  }
  
  private void appendResourceLinks(Set<String> links, IFeedRecord r) {
    for (ResourceLink link: r.getResourceLinks()) {
      if (!Val.isUrl(link.getUrl())) {
          continue;
      }
      if (predicate==null || predicate.accept(link)) {
        links.add(link.getUrl());
      }
    }
  }

  private class ExcludeSelectedTagsPredicate implements IResourceLinkPredicate {
    private final List<String> excludedTags;
    
    public ExcludeSelectedTagsPredicate(String ... excludedTags) {
      this.excludedTags = Arrays.asList(excludedTags);
    }

    @Override
    public boolean accept(ResourceLink resourceLink) {
      return !excludedTags.contains(resourceLink.getTag());
    }
  }
  
  private class AcceptAllPredicate implements IResourceLinkPredicate {

    @Override
    public boolean accept(ResourceLink resourceLink) {
      return true;
    }
    
  }
  
  /**
   * Resource link predicate
   */
  public static interface IResourceLinkPredicate {
    boolean accept(ResourceLink resourceLink);
  }
}

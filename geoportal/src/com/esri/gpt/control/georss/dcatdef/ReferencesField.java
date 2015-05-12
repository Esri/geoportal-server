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
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedRecord;
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * References.
 */
public class ReferencesField  implements DcatFieldDefinition {
  private final String name;
  private final IResourceLinkPredicate predicate;

  public ReferencesField(String name) {
    this.name = name;
    this.predicate = new AcceptAllPredicate();
  }

  public ReferencesField(String name, String ... excludedTags) {
    this.name = name;
    this.predicate = new ExcludeSelectedTagsPredicate(excludedTags);
  }
  
  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    ArrayList<String> selectedLinks = new ArrayList<String>();
    for (ResourceLink link: r.getResourceLinks()) {
      if (!Val.isUrl(link.getUrl())) {
          continue;
      }
      if (predicate==null || predicate.accept(link)) {
        selectedLinks.add(link.getUrl());
      }
    }
    if (!selectedLinks.isEmpty()) {
      jsonWriter.name(name).beginArray();
      for (String url: selectedLinks) {
        jsonWriter.value(url);
      }
      jsonWriter.endArray();
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

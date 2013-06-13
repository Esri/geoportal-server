/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.framework.util.Val;
import java.util.List;

/**
 * Collection of IFeedRecord elements.
 */
public interface IFeedRecords extends List<IFeedRecord> {

  OpenSearchProperties getOpenSearchProperties();

  List<FieldMeta> getMetaData();
  
  /**
   * FieldMeta definition
   */
  public static class FieldMeta {

    private String name;
    private String type;
    private String alias;
    private Number length;

    public FieldMeta(String name, String type, String alias, Number length) {
      this.name = Val.chkStr(name);
      this.type = Val.chkStr(type);
      this.alias = Val.chkStr(alias);
      this.length = length;
    }

    public FieldMeta(String name, String type, String alias) {
      this.name = Val.chkStr(name);
      this.type = Val.chkStr(type);
      this.alias = Val.chkStr(alias);
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public String getAlias() {
      return alias;
    }

    public Number getLength() {
      return length;
    }
  }
}

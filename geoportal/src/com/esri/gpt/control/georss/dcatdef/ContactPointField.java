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
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.framework.util.Val.*;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Contact point field.
 */
public class ContactPointField implements DcatFieldDefinition {
  private static final ArrayList<DcatFieldDefinition> fieldDefinitions = new ArrayList<DcatFieldDefinition>();
  static {
    fieldDefinitions.add(new StringField("contactPoint",DcatFieldDefinition.OBLIGATORY){

      @Override
      protected String readValue(DcatSchemas dcatSchemas, DcatField dcatField, IFeedRecord r, IFeedAttribute attr) {
        StringBuilder sb = new StringBuilder();
        for (String s: attr.asList()) {
            if (!s.isEmpty()) {
              sb.append(sb.length()>0? ", ": "").append(s);
            }
        }
        return sb.toString();
      }
      
      @Override
      protected String getOutFieldName() {
        return "fn";
      }

      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        return chkStr(properties.getProperty(fldName),"?");
      }
    });
    fieldDefinitions.add(new StringField("mbox",DcatFieldDefinition.OBLIGATORY){

      @Override
      protected String readValue(DcatSchemas dcatSchemas, DcatField dcatField, IFeedRecord r, IFeedAttribute attr) {
        String mbox = super.readValue(dcatSchemas, dcatField, r, attr);
        mbox = validateEmail(mbox);
        return mbox;
      }
      
      @Override
      protected String getOutFieldName() {
        return "hasEmail";
      }

      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        String mbox = chkStr(properties.getProperty(fldName),"mailto:contactperson@example.org");
        mbox = validateEmail(mbox);
        return mbox;
      }
      
      protected String validateEmail(String email) {
        email = chkStr(email);
        if (!email.isEmpty()) {
          if (email.toLowerCase().startsWith("mailto:")) {
            email = email.substring("mailto:".length());
          }
          if (chkEmail(email)) {
            email = "mailto:"+email;
          } else {
            email = "";
          }
        }
        return email;
      }
    });
  }

  private final String name;

  /**
   * Creates instance of the class.
   * @param name field name
   */
  public ContactPointField(String name) {
    this.name = name;
  }
  
  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    jsonWriter.name(name).beginObject();
    jsonWriter.name("@type").value("vcard:Contact");
    for (DcatFieldDefinition dfd: fieldDefinitions) {
      dfd.print(jsonWriter, properties, dcatSchemas, r);
    }
    jsonWriter.endObject();
  }
  
}

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
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.util.Val;
import static com.esri.gpt.framework.util.Val.chkStr;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Record definition.
 */
public class DcatRecordDefinition {
  
  private static final IsoDateFormat ISODF = new IsoDateFormat();
  private static final ArrayList<DcatFieldDefinition> fieldDefinitions = new ArrayList<DcatFieldDefinition>();
  static {
    fieldDefinitions.add(new StringField("title",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        return chkStr(properties.getProperty(fldName),"?");
      }
    });
    fieldDefinitions.add(new StringField("description",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        return chkStr(properties.getProperty(fldName),"?");
      }
    });
    fieldDefinitions.add(new ArrayField ("keyword",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected List<String> getDefaultValue(Properties properties) {
        List<String> list = Arrays.asList(chkStr(properties.getProperty(fldName)).replaceAll("^\\p{Space}*\\[|\\]\\p{Space}*$", "").split(","));
        for (int i=0; i<list.size(); i++) {
          list.set(i, list.get(i).replaceAll("^\\p{Space}*\\\"|\\\"\\p{Space}*$", ""));
        }
        return list;
      }
    });
    fieldDefinitions.add(new DateField  ("modified",DcatFieldDefinition.OBLIGATORY){

      @Override
      protected Date getDefaultValue(Properties properties) {
        String sDate = properties.getProperty(fldName);
        try {
          return ISODF.parseObject(sDate);
        } catch (ParseException ex) {
          return new Date();
        }
      }
      
    });
    fieldDefinitions.add(new DateField  ("issued"){

      @Override
      protected Date getDefaultValue(Properties properties) {
        String sDate = properties.getProperty(fldName);
        try {
          return ISODF.parseObject(sDate);
        } catch (ParseException ex) {
          return new Date();
        }
      }
      
    });
    fieldDefinitions.add(new PublisherField("publisher"));
    fieldDefinitions.add(new ContactPointField("contactPoint"));
    fieldDefinitions.add(new IdentifierField("identifier",DcatFieldDefinition.OBLIGATORY));
    fieldDefinitions.add(new StringField ("accessLevel",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        return chkStr(properties.getProperty(fldName),"public");
      }
    });
    fieldDefinitions.add(new StringField("accessLevelComment"){
      private Boolean writeAlways;
      
      private boolean getWriteAlways() {
        if (writeAlways==null) {
          ApplicationContext appCtx = ApplicationContext.getInstance();
          ApplicationConfiguration appCfg = appCtx.getConfiguration();
          StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
          writeAlways = Val.chkBool(parameters.getValue("dcat.rights.writeAlways"), false);
        }
        return writeAlways;
      }

      @Override
      protected String getOutFieldName() {
        return "rights";
      }

      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        return Val.chkStr(properties.getProperty("fldName"), "?");
      }

      @Override
      protected String readValue(DcatSchemas dcatSchemas, DcatField dcatField, IFeedRecord r, IFeedAttribute attr) {
        Map<String, IFeedAttribute> index = getIndex(r);
        if (index == null) {
          return "";
        }
        DcatField field = getAttributeField(dcatSchemas, index, r, "accessLevel");
        if (field == null) {
          return "";
        }
        IFeedAttribute accessLevelAttribute = getFeedAttribute(index, field);
    
        List<String> accessLevelList = accessLevelAttribute!=null? accessLevelAttribute.asList(): new ArrayList<String>();
        
        boolean nonPublic = false;
        
        if (!getWriteAlways()) {
          for (String accessLevel: accessLevelList) {
            accessLevel = field.translate(accessLevel);
            if (!"public".equals(accessLevel)) {
              nonPublic = true;
              break;
            }
          }
        } else {
          nonPublic = true;
        }
        
        if (nonPublic) {
          List<String> attrAsList = attr.asList();
          StringBuilder sb = new StringBuilder();
          int maxLength = 255;
          for (String text: attrAsList) {
            if (sb.length()>0 && sb.length()+text.length()+2>maxLength) break;
            sb.append(sb.length()>0 && sb.charAt(sb.length()-1)!='.'? ". ": " ").append(Val.chkStr(text));
          }
          String value = sb.toString();
          return value.length()<=maxLength? value: value.substring(0, maxLength-3).trim()+"...";
        }
        
        return "";
      }
      
    });
    fieldDefinitions.add(new StringField ("accrualPeriodicity"));
    fieldDefinitions.add(new ReferencesField("references","contentType"));
    fieldDefinitions.add(new ArrayField("bureauCode",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected List<String> getDefaultValue(Properties properties) {
        List<String> list = Arrays.asList(chkStr(properties.getProperty(fldName)).replaceAll("^\\p{Space}*\\[|\\]\\p{Space}*$", "").split(","));
        for (int i=0; i<list.size(); i++) {
          list.set(i, list.get(i).replaceAll("^\\p{Space}*\\\"|\\\"\\p{Space}*$", ""));
        }
        return list;
      }
    });
    fieldDefinitions.add(new ArrayField("programCode",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected List<String> getDefaultValue(Properties properties) {
        List<String> list = Arrays.asList(chkStr(properties.getProperty(fldName)).replaceAll("^\\p{Space}*\\[|\\]\\p{Space}*$", "").split(","));
        for (int i=0; i<list.size(); i++) {
          list.set(i, list.get(i).replaceAll("^\\p{Space}*\\\"|\\\"\\p{Space}*$", ""));
        }
        return list;
      }
    });
    fieldDefinitions.add(new StringField ("license",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        return chkStr(properties.getProperty(fldName));
      }
    });
    fieldDefinitions.add(new ArrayField("language"));
    fieldDefinitions.add(new StringField("landingPage", new BaseDcatField.FlagsProvider() {
      @Override
      public long provide(IFeedRecord r, IFeedAttribute attr, Properties properties) {
        ApplicationContext appCtx = ApplicationContext.getInstance();
        ApplicationConfiguration appCfg = appCtx.getConfiguration();
        boolean detailsAsDefaultLandingPage = Val.chkBool(appCfg.getCatalogConfiguration().getParameters().getValue("dcat.landingPage.default.details"),false);
        return detailsAsDefaultLandingPage? DcatFieldDefinition.OBLIGATORY: 0;
      }
    }) {
      
      @Override
      protected String getDefaultValue(IFeedRecord r, Properties properties) {
        String root = properties.getProperty("@root");
        String uuid = r.getUuid();
        return root + "/catalog/search/resource/details.page?uuid=" + encode(uuid);
      }

      private String encode(String str) {
        try {
          return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
          return str;
        }
      }
      
      @Override
      protected boolean validateValue(String value) {
        return !Val.chkUrl(value).isEmpty();
      }
    });
    fieldDefinitions.add(new SpatialField("spatial"));
    fieldDefinitions.add(new TemporalField("temporal"));
    fieldDefinitions.add(new DcatDistributionField("distribution"));
    fieldDefinitions.add(new StringField("isPartOf"));
    fieldDefinitions.add(new ArrayField("theme"));
    fieldDefinitions.add(new BooleanField("dataQuality"));
    fieldDefinitions.add(new StringField("describedBy"));
    fieldDefinitions.add(new StringField("describedByType"));
  }
  
  /**
   * Print record according to the definition.
   * @param jsonWriter underlying json writer
   * @param properties proeprties
   * @param dcatSchemas dcat schemas
   * @param r record to print
   * @throws IOException if printing fails
   */
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    jsonWriter.beginObject();
    jsonWriter.name("@type").value("dcat:Dataset");
    for (DcatFieldDefinition fd: fieldDefinitions) {
      fd.print(jsonWriter, properties, dcatSchemas, r);
    }
    jsonWriter.endObject();
  }
}

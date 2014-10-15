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

import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedAttribute;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Esri, Inc.
 */
public class ArrayField extends BaseDcatField {

  public ArrayField(String fldName) {
    super(fldName);
  }

  public ArrayField(String fldName, long flags) {
    super(fldName, flags);
  }

  protected ArrayList<String> readValue(IFeedAttribute attr) {
    ArrayList<String> value = new ArrayList<String>();
    if (attr.getValue() instanceof List) {
      try {
        for (IFeedAttribute o : (List<IFeedAttribute>) attr.getValue()) {
          value.add(o.simplify().getValue().toString());
        }
      } catch (ClassCastException ex) {
      }
    } else {
      value.add(attr.simplify().getValue().toString());
    }
    return value;
  }

  protected List<String> getDefaultValue(Properties properties) {
    return new ArrayList<String>();
  }

  @Override
  public void print(DcatPrinter printer, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    IFeedAttribute attr = getFeedAttribute(dcatSchemas, r);

    ArrayList<String> value = new ArrayList<String>();
    if (attr == null) {
      if ((flags & OBLIGATORY) != 0) {
        value.addAll(getDefaultValue(properties));
      } else {
        return;
      }
    } else {
      value = readValue(attr);
    }

    printer.printAttribute(getOutFieldName(), value);
  }
}

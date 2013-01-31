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
package com.esri.gpt.framework.jsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.esri.gpt.framework.util.Val;


/**
 * 
 * 
 * The Class ConverterListToString.  Converts a list object into a string
 * object
 */
public class ConverterListToString implements Converter {

// instance variable ===========================================================
/** The DELIMETER. */
private static String DELIMETER = "|";

// methods =====================================================================
/**
 * Gets the String as a list object.
 * 
 * @param arg0 the arg0
 * @param arg1 the arg1
 * @param value the value
 * 
 * @return the as object
 */
public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
  String vals[] = value.split("\\" +  DELIMETER);
  List<String> list = new ArrayList<String>();
  String tmp = null;
  for (int i = 0; vals != null && vals.length > i; i++) {
    tmp = Val.chkStr(vals[i]);
    if(!"".equals(tmp)) {
      list.add(tmp.toString());
    }
  }
  // TODO Auto-generated method stub
  return list;
}

/**
 * Gets the list as a string.
 * 
 * @param arg0 the arg0
 * @param arg1 the arg1
 * @param arg2 the list
 * 
 * @return the as string
 */
public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
  StringBuffer sList = new StringBuffer();
  Object tmp = null;
  if (arg2 != null && (arg2 instanceof List)) {
    List list = (List) arg2;
    Iterator iter = list.iterator();
    while (iter.hasNext()) {
      tmp = iter.next();
      if(tmp == null) {
        continue;
      }
      sList.append(tmp.toString());
      if(iter.hasNext()) {
        sList.append(DELIMETER);
      }
   }
  }
  return sList.toString();
}

}

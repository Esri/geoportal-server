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
package com.esri.gpt.control.webharvest.protocol;

import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;
import java.util.List;

/**
 * Protocol serializer.
 */
public class ProtocolSerializer {
/**
 * Creates xml string representation of the protocol.
 * @param protocol protocol
 * @return xml string representation of the protocol
 */
public static String toXmlString(Protocol protocol) {
  StringBuilder sb = new StringBuilder();

  sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  if (protocol!=null) {
    String sDest = getDestinations(protocol);
    sb.append("<protocol type=\"").append(protocol.getKind()).append("\" flags=\"").append(protocol.getFlags()).append("\"").append(sDest!=null? " destinations=\"" +sDest+ "\"": "").append(" adHoc=\"" +protocol.getAdHoc()+ "\"").append(">");

    StringAttributeMap attributes = protocol.extractAttributeMap();
    for (String key : attributes.keySet()) {
      StringAttribute value = attributes.get(key);
      sb.append("<").append(key).append(">").append(value.getValue()).
          append("</").append(key).append(">");
    }

    sb.append("</protocol>");
  } else {
    sb.append("<protocol/>");
  }

  return sb.toString();
}

/**
 * Gets destinations as string.
 * @param protocol protocol
 * @return destinations or <code>null</code> if no destinations
 */
private static String getDestinations(Protocol protocol) {
    List<String> destinations = ProtocolInvoker.getDestinations(protocol);
    String sDest = null;
    if (destinations!=null) {
      StringBuilder sbDest = new StringBuilder();
      for (String dest : destinations) {
        dest = Val.chkStr(dest);
        if (dest.length()>0) {
          if (sbDest.length()>0) {
            sbDest.append(",");
          }
          sbDest.append(dest);
        }
        sDest = sbDest.toString();
      }
    }
    return sDest;
}

}

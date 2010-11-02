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
    sb.append("<protocol type=\"" + protocol.getKind() + "\" flags=\"" +protocol.getFlags()+ "\">");

    StringAttributeMap attributes = protocol.getAttributeMap();
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

}

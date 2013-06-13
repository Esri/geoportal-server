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
package com.esri.gpt.agp.client;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Provides a few static utilities.
 */
public class AgpUtil {
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpUtil() {}
  
  /** methods ================================================================= */
  
  /**
   * Appends a parameter to a URL buffer.
   * <br/>The value will be trimmed. Empty values will not be appended.
   * @param parameters the buffer
   * @param parameter the parameter name
   * @param value the parameter value
   * @param encodeValue if true the value will be URL encoded
   */
  public static void appendURLParameter(StringBuilder parameters, 
      String parameter, String value, boolean encodeValue) {
    if (value == null) value = "";
    else value = value.trim();
    if (value.length() > 0) {
      if (encodeValue) {
        try {
          value = URLEncoder.encode(value,"UTF-8");
        } catch (UnsupportedEncodingException e) {
          // should never occur
          e.printStackTrace();
        }
      }
      if (parameters.length() > 0) {
        if (!parameter.startsWith("&")) {
          parameters.append("&");
        }
      }
      parameters.append(parameter).append("=").append(value);
    }
  }
  
  /**
   * URL encodes a URI component.
   * @param value the value to encode
   * @return the encoded value
   */
  public static String encodeURIComponent(String value) {
    if (value == null) {
      return null;
    }
    try {
      value = URLEncoder.encode(value,"UTF-8");
    } catch (UnsupportedEncodingException e) {
      // should never occur
      e.printStackTrace();
    }
    return value;
  }
  
}
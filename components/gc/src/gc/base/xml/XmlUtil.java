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
package gc.base.xml;
import java.io.UnsupportedEncodingException;

/**
 * XML utilities.
 */
public class XmlUtil {
	
	/**
	 * Escapes a string.
	 * @param s the string
	 * @return the escaped string
	 */
  public static String escapeXml(String s) {
    return escapeXml(s,true);
  }
  
  /**
   * Escapes special xml characters within a string.
   * <br/> < > & " are escaped
   * <br/> ' is escaped if escapeApostrophe is supplied as true.
   * @param s the string to escape
   * @param escapeApostrophe if true apostrophes are escaped
   * @return the escaped string
   */
  private static String escapeXml(String s, boolean escapeApostrophe) {
  	if (s == null) {
  		return null;
  	} else if (s.length() == 0) {
      return s;
    } else {
      char c;
      String sApostrophe = "&apos;";
      if (!escapeApostrophe) sApostrophe= "'";
      StringBuilder sb = new StringBuilder(s.length()+20);
      for (int i=0; i<s.length(); i++) {
        c = s.charAt(i);
        if      (c == '&')  sb.append("&amp;");
        else if (c == '<')  sb.append("&lt;");
        else if (c == '>')  sb.append("&gt;");
        else if (c == '\'') sb.append(sApostrophe);
        else if (c == '"')  sb.append("&quot;");
        else                sb.append(c);
      }
      return sb.toString();
    }
  }
  
  /**
   * Removes a windows byte order mark if present.
   * @param s the string to check
   * @return the string absent the byte order mark
   */
  public static String removeBOM(String s) {
    if (s != null) {
      byte[] bom = new byte[3];
      bom[0] = (byte)0xEF;
      bom[1] = (byte)0xBB; 
      bom[2] = (byte)0xBF;
      try {
        String sbom = new String(bom,"UTF-8");
        s = s.trim();
        if (s.startsWith(sbom)) {
          s = s.substring(1).trim();
        }
      } catch(UnsupportedEncodingException e) {}
    }
    return s;
  }

}

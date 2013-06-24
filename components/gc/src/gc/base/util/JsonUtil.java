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
package gc.base.util;

/**
 * JSON utilities.
 */
public class JsonUtil {

	/**
	 * Escape a string.
	 * @param text the text
	 * @param quote if true, quote the text
	 * @return the escaped string
	 */
	public static String escapeJson(String text, boolean quote) {
    if (text == null) return null;

    StringBuilder sb = new StringBuilder();
    for(int i=0;i<text.length();i++){
      char ch = text.charAt(i);
      switch(ch){
      case '"':
        sb.append("\\\"");
        break;
      case '\\':
        sb.append("\\\\");
        break;
      case '\b':
        sb.append("\\b");
        break;
      case '\f':
        sb.append("\\f");
        break;
      case '\n':
        sb.append("\\n");
        break;
      case '\r':
        sb.append("\\r");
        break;
      case '\t':
        sb.append("\\t");
        break;
      case '/':
        sb.append("\\/");
        break;
      default:
        if ((ch >= '\u0000') && (ch <= '\u001F')) {
          String s2 =Integer.toHexString(ch);
          sb.append("\\u");
          for(int k=0;k<4-s2.length();k++){
            sb.append('0');
          }
          sb.append(s2.toUpperCase());
        } else{
          sb.append(ch);
        }
      }
    }
    if (quote) {
    	return "\""+sb.toString()+"\"";
    } else {
      return sb.toString();
    }
  }
}

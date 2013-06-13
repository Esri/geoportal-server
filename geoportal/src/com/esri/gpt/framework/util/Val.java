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
package com.esri.gpt.framework.util;
import com.esri.gpt.framework.context.ApplicationContext;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Provides basic validation and conversion support.
 */
public class Val {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================
  
/** Default constructor. */
private Val() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Converts a string to a boolean value.
 * @param s the string to convert
 * @param defaultVal the default value to return if the string is invalid
 * @return the converted value
 */
public static boolean chkBool(String s, boolean defaultVal) {
  boolean b = defaultVal;
  String  v = Val.chkStr(s).toLowerCase();
  if (v.length() > 0) {
    if      (v.equals("true"))  b = true;
    else if (v.equals("false")) b = false;
    else if (v.equals("y"))     b = true;
    else if (v.equals("n"))     b = false;
    else if (v.equals("yes"))   b = true;
    else if (v.equals("no"))    b = false;
    else if (v.equals("on"))    b = true;
    else if (v.equals("off"))   b = false;
    else if (v.equals("0"))     b = false;
    else if (v.equals("1"))     b = true;
    else if (v.equals("-1"))    b = false;
  }
  return b;
}

/**
 * Converts a string to a double value.
 * @param s the string to convert
 * @param defaultVal the default value to return if the string is invalid
 * @return the converted value
 */
public static double chkDbl(String s, double defaultVal) {
  double n = defaultVal;
  try {
    n = Double.parseDouble(s);
  } catch (Exception e) {
    n = defaultVal;
  }
  return n;
}

/**
 * Check a string to see if it matches an email address.
 * <br/>The check is against a regular expression defined within gpt.xml:
 * <br/>/gptConfig/mail/@emailAddressRegexp
 * @param s the string to check
 * @return true if the string matches an email address
 */
public static boolean chkEmail(String s) {
  if (s == null) {
    return false;
  } else {
    //String exp = "(\\w+)(\\.\\w+)*@(\\w+\\.)(\\w+)(\\.\\w+)*";
    String exp = ApplicationContext.getInstance().getConfiguration().getMailConfiguration().getEmailAddressRegexp();
    if ((exp != null) && (exp.length() > 0)) {
      return s.matches(exp);
    } else {
      return true;
    }
  }
}

/**
 * Checks a string if it's a alid URL.
 * @param url string to check
 * @return url if it's valid or empty string if it's not valid
 */
public static String chkUrl(String url) {
  url = chkStr(url);
  try {
    new URL(url);
    return url;
  } catch (MalformedURLException ex) {
    return "";
  }
}

/**
 * Converts a string to an int value.
 * @param s the string to convert
 * @param defaultVal the default value to return if the string is invalid
 * @return the converted value
 */
public static int chkInt(String s, int defaultVal) {
  int n = defaultVal;
  try {
    n = Integer.parseInt(s);
  } catch (Exception e) {
    n = defaultVal;
  }
  return n;
}

/**
 * Converts a string to a long value.
 * @param s the string to convert
 * @param defaultVal the default value to return if the string is invalid
 * @return the converted value
 */
public static long chkLong(String s, long defaultVal) {
  long n = defaultVal;
  try {
    n = Long.parseLong(s);
  } catch (Exception e) {
    n = defaultVal;
  }
  return n;
}

/**
 * Check a string value.
 * @param s the string to check
 * @return the checked string (trimmed, zero length if the supplied String was null)
 */
public static String chkStr(String s) {
  if (s == null) {
    return "";
  } else {
    return s.trim();
  }
}

/**
 * Check a string value.
 * @param s the string to check
   @param defaultVal the default value to return if the string is null or empty
 * @return the checked string (trimmed, default value if the
 *         supplied String was null or empty)
 */
public static String chkStr(String s, String defaultVal) {
  s = Val.chkStr(s);
  if (s.length() == 0) {
    return defaultVal;
  } else {
    return s;
  }
}

/**
 * Check a string value and ensures that it does not exceed a maximum length.
 * @param s the string to check
   @param maxLength the maximum length for the string
 * @return the checked string (substring to the max length if applicable)
 */
public static String chkStr(String s, int maxLength) {
  s = Val.chkStr(s);
  if ((maxLength > 0) && (s.length() > maxLength)) {
    return s.substring(0,maxLength);
  } else {
    return s;
  }
}

/**
 * Escapes single and quotes within a string.
 * <br/>The escape character inserted is a backslash.
 * @param s the string to escape
 * @return the escaped string
 */
public static String escapeSingleQuotes(String s) {
  if ((s == null) || (s.length() == 0)) {
    return "";
  } else {
    int nIdx = s.indexOf("'");
    if (nIdx != -1) {
      char c;
      StringBuffer sb = new StringBuffer(s.length()+20);
      for (int i=0; i<s.length(); i++) {
        c = s.charAt(i);
        if (c == '\'') sb.append("\\");
        sb.append(c);
      }
      return sb.toString();
    } else {
      return s;
    }
  }
}

/**
 * Escapes special xml characters within a string.
 * <br/> < > & " ' are escaped.
 * @param s the string to escape
 * @return the escaped string
 */
public static String escapeXml(String s) {
  return Val.execEscapeXml(s,true);
}

/**
 * Escapes special xml characters within a string.
 * <br/> < > & " are escaped. The single quote character is not escaped.
 * @param s the string to escape
 * @return the escaped string
 */
public static String escapeXmlForBrowser(String s) {
  return Val.execEscapeXml(s,false);
}

/**
 * Escapes arbitrary text into a JSON escaped string.
 * @param text text to escape
 * @return esaped text
 * @see <A href="http://json.org/">JSON specification</A>
 */
public static String escapeStrForJson(String text) {
    if (text == null) return null;

    StringBuffer sb = new StringBuffer();
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
    return sb.toString();
}

/**
 * Escapes arbitrary text into a ldap escaped string.
 * @param text text to escape
 * @return escaped text
 */
public static String escapeStrForLDAPSearch(String text) {
    if (text == null) return null;

    StringBuffer sb = new StringBuffer();
    for(int i=0;i<text.length();i++){
      char ch = text.charAt(i);
      switch(ch){
      case '\\':
        sb.append("\\\\");
        break;
      case '#':
        sb.append("\\#");
        break;
      case '+':
        sb.append("\\+");
        break;
      case '<':
        sb.append("\\<");
        break;
      case '>':
        sb.append("\\>");
        break;
      case ';':
        sb.append("\\;");
        break;
      case '"':
          sb.append("\\\"");
          break;
      default:       
          sb.append(ch);
      }
    }
    return sb.toString();
}
/**
 * Escapes special xml characters within a string.
 * <br/> < > & " are escaped
 * <br/> ' is escaped if escapeApostrophe is supplied as true.
 * @param s the string to escape
 * @param escapeApostrophe if true apostrophies are escapaped
 * @return the escaped string
 */
private static String execEscapeXml(String s, boolean escapeApostrophe) {
  if ((s == null) || (s.length() == 0)) {
    return "";
  } else {
    char c;
    String sApostrophe = "&apos;";
    if (!escapeApostrophe) sApostrophe= "'";
    StringBuffer sb = new StringBuffer(s.length()+20);
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
        s = Val.chkStr(s.substring(1));
      }
    } catch (UnsupportedEncodingException e) {}
  }
  return s;
}

/**
 * Tokenizes a delimited string into a string array.
 * @param tokens the delimited string to tokenize
 * @param delimiter the delimiter
 * @return the string array of tokens
 */
public static String[] tokenize(String tokens, String delimiter) {
  ArrayList<String> al = new ArrayList<String>();
  tokens = Val.chkStr(tokens);
  if (delimiter == null) {
    delimiter = "";
  } else if (!delimiter.equals(" ")) {
    delimiter = delimiter.trim();
  }
  StringTokenizer st = new StringTokenizer(tokens,delimiter);
  while (st.hasMoreElements()) {
    String s = Val.chkStr((String)st.nextElement());
    al.add(s);
  }
  return al.toArray(new String[0]);
}


}


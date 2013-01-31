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
package com.esri.gpt.framework.security.codec;

import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Digest authenticator.
 */
public class Digest {

// class variables =============================================================
/** digest signature */  
private static final String DIGEST_SIGNATURE = "Digest";
/** authenticate attribute */
private static final String AUTHENTICATE_ATTR = "WWW-Authenticate";
/** authorization attribute */
private static final String AUTHORIZATION_ATTR = "Authorization";
/** algorithm key */
private static final String ALGORITHM_KEY = "algorithm";
/** user name key */
private static final String USERNAME_KEY = "username";
/** cnonce key */
private static final String CNONCE_KEY = "cnonce";
/** uri key */
private static final String URI_KEY = "uri";
/** nc key */
private static final String NC_KEY = "nc";
/** nonce key */
private static final String NONCE_KEY = "nonce";
/** qop key */
private static final String QOP_KEY = "qop";
/** realm key */
private static final String REALM_KEY = "realm";
/** opaque key */
private static final String OPAQUE_KEY = "opaque";
/** response key */
private static final String RESPONSE_KEY = "response";
// instance variables ==========================================================
/** attributes */
private Map<String, String> _attrs = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
/** indicates if response is valid */
private boolean _valid;

// constructors ================================================================
/**
 * Creates instance of the digest.
 */
private Digest() {
}
// properties ==================================================================
/**
 * Checks if digest has been found and extracted from the connection.
 * @return <code>true</code> if digest digest has been extracted from the 
 * connection
 */
public boolean isValid() {
  return _valid;
}

/**
 * Sets <i>valid</i> flag.
 * @param valid valid flag
 */
private void setValid(boolean valid) {
  _valid = valid;
}

// methods =====================================================================
/**
 * Extracts digest from the connection.
 * If digest has been successfuly extracted, returned digest will have 
 * <i>valid</i> flag set to <code>true</code>.
 * @param connection HTTP connection
 * @return digest
 */
public static Digest extractFrom(HttpURLConnection connection) {
  Digest digResp = new Digest();

  if (connection != null) {
    String sAuthenticate = extractAuthenticateAttribute(connection);
    if (sAuthenticate.startsWith(DIGEST_SIGNATURE)) {
      digResp.setValid(true);
      sAuthenticate = sAuthenticate.substring(DIGEST_SIGNATURE.length()).trim();
      String[] sParams = sAuthenticate.split(",");
      for (String sParam : sParams) {
        String[] sNameValue = extractAttribute(sParam);
        if (sNameValue.length == 2) {
          digResp.put(sNameValue[0], sNameValue[1]);
        }
      }
    }
  }

  return digResp;
}

/**
 * Injects digest response into connection.
 * @param connection HTTP connection
 * @param credentials credentials to embede within digest request
 */
public void injectTo(HttpURLConnection connection,
                      UsernamePasswordCredentials credentials) {
  if (connection != null && credentials != null) {
    String uri = connection.getURL().getFile();
    int idx = uri.indexOf('?');
    if (idx != -1) {
      uri = uri.substring(0, idx);
    }
    String cnonce = Integer.toHexString(new java.util.Random().nextInt());

    // create complementory digest
    Digest digest = new Digest();

    // create obligatory attributes
    digest.put(USERNAME_KEY, credentials.getUsername());
    digest.put(CNONCE_KEY, cnonce);
    digest.put(URI_KEY, uri);
    digest.put(NC_KEY, "00000001");
    digest.put(QOP_KEY, "auth");

    // copy attributes from current digest
    digest.put(ALGORITHM_KEY, get(ALGORITHM_KEY));
    digest.put(NONCE_KEY, get(NONCE_KEY));
    digest.put(REALM_KEY, get(REALM_KEY));
    digest.put(OPAQUE_KEY, get(OPAQUE_KEY));

    // compute and attach response
    try {
      digest.calculateResponse(
        Val.chkStr(connection.getRequestMethod(),"GET"), credentials);
    } catch (NoSuchAlgorithmException ex) {
      LogUtil.getLogger().severe(
        "Invalid digest algorithm: " + ex.getMessage());
      digest.put(RESPONSE_KEY, "");
    }

    digest.storeAuthorization(connection);
  }
}

/**
 * Creates string reprezentation of the digest.
 * @return string reprezentation of the digest
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder();
  for (Map.Entry<String, String> entry : _attrs.entrySet()) {
    if (sb.length() > 0) {
      sb.append(", ");
    }
    if (entry.getKey().equalsIgnoreCase(NC_KEY)) {
      sb.append(entry.getKey());
      sb.append("=");
      sb.append(entry.getValue());
    } else {
      sb.append(entry.getKey());
      sb.append("=\"");
      sb.append(entry.getValue());
      sb.append("\"");
    }
  }
  return DIGEST_SIGNATURE + " " + sb.toString();
}

/**
 * Stores attribute.
 * @param attr attribute name
 * @param value attribute value
 */
private void put(String attr, String value) {
  _attrs.put(attr, value);
}

/**
 * Gets attribute.
 * @param attr attribute name
 * @return attribute value or empty string if not available
 */
private String get(String attr) {
  return Val.chkStr(_attrs.get(attr));
}

/**
 * Calculates response.
 * @param method HTTP method ("GET" or "POST")
 * @param credentials credentials used to calculate response attribute
 * @throws java.security.NoSuchAlgorithmException if invalid digest algorithm
 */
private void calculateResponse(String method, UsernamePasswordCredentials credentials)
  throws NoSuchAlgorithmException {
  String algorithm = Val.chkStr(get(ALGORITHM_KEY), "MD5").toUpperCase();
  MessageDigest md = MessageDigest.getInstance(algorithm);
  String HA1 = encrypt(md,
                       credentials.getUsername() + ":" +
                       get(REALM_KEY) + ":" +
                       credentials.getPassword());
  String HA2 = encrypt(md, method + ":" + get(URI_KEY));
  String content =
    HA1 + ":" +
    get(NONCE_KEY) + ":" +
    get(NC_KEY) + ":" +
    get(CNONCE_KEY) + ":" +
    get(QOP_KEY) + ":" +
    HA2;
  String response = encrypt(md, content);
  put(RESPONSE_KEY, response);
}

/**
 * Encrypt data using digest.
 * @param md message digest
 * @param data data to encrypt
 * @return encrypted data
 */
private String encrypt(MessageDigest md, String data) {
  md.reset();
  md.update(data.getBytes());
  byte[] bytes = md.digest();
  StringBuilder sb = new StringBuilder();

  for (byte b : bytes) {
    String bhex = Integer.toHexString(b >= 0 ? b : 256 + b);
    bhex = bhex.substring(bhex.length() > 2 ? bhex.length() - 2 : 0);
    while (bhex.length() < 2) {
      bhex = "0" + bhex;
    }
    sb.append(bhex);
  }

  return sb.toString();
}

/**
 * Extracts attribute name-value pair.
 * @param param param containing attribute definition
 * @return two dimensional array of strings having name-value pair
 */
private static String[] extractAttribute(String param) {
  param = Val.chkStr(param);
  String[] sNameValue = param.split("=");
  for (int i = 0; i < sNameValue.length; i++) {
    sNameValue[i] =
      sNameValue[i].replaceAll("^\\p{Blank}*\"|\"\\p{Blank}*$", "").trim();
  }
  return sNameValue;
}

/**
 * Extracts authentiate attribute from the connection.
 * @param connection HTTP connection
 * @return authenticate attribute or empty string if not availabale
 */
private static String extractAuthenticateAttribute(HttpURLConnection connection) {
  Map<String, List<String>> headerFields = 
          new TreeMap<String, List<String>>(new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
              if (o1==null) return -1;
              if (o2==null) return 1;
              return o1.compareToIgnoreCase(o2);
            }
          });
  headerFields.putAll(connection.getHeaderFields());
  List<String> wwwAuthenticateList = headerFields.get(AUTHENTICATE_ATTR);
  if (wwwAuthenticateList != null && wwwAuthenticateList.size() > 0) {
    return Val.chkStr(wwwAuthenticateList.get(0));
  }
  return "";
}

/**
 * Creates string reprezentation of the digest.
 * @return string reprezentation of the digest
 */
private String toDigest() {
  StringBuilder sb = new StringBuilder();
  for (Map.Entry<String, String> entry : _attrs.entrySet()) {
    if (sb.length() > 0) {
      sb.append(", ");
    }
    if (entry.getKey().equalsIgnoreCase(NC_KEY)) {
      sb.append(entry.getKey());
      sb.append("=");
      sb.append(entry.getValue());
    } else {
      sb.append(entry.getKey());
      sb.append("=\"");
      sb.append(entry.getValue());
      sb.append("\"");
    }
  }
  return DIGEST_SIGNATURE + " " + sb.toString();
}

/**
 * Stores itself as authorization
 * @param connection
 */
private void storeAuthorization(HttpURLConnection connection) {
  connection.setRequestProperty(AUTHORIZATION_ATTR, toDigest());
}
}

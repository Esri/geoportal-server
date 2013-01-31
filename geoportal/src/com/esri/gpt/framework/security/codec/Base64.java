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
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Handles base64 encoding/decoding.
 */
public class Base64 {

// class variables =============================================================
private static byte[] BASE64_DEC_MAP;
private static byte[] BASE64_ENC_MAP;

// instance variables ==========================================================

/**
 * Static initialization
 */
static {

  // rfc-2045: Base64 Alphabet
  byte[] map = {
    (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F',
    (byte)'G', (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L',
    (byte)'M', (byte)'N', (byte)'O', (byte)'P', (byte)'Q', (byte)'R',
    (byte)'S', (byte)'T', (byte)'U', (byte)'V', (byte)'W', (byte)'X',
    (byte)'Y', (byte)'Z',
    (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f',
    (byte)'g', (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l',
    (byte)'m', (byte)'n', (byte)'o', (byte)'p', (byte)'q', (byte)'r',
    (byte)'s', (byte)'t', (byte)'u', (byte)'v', (byte)'w', (byte)'x',
    (byte)'y', (byte)'z',
    (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
    (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/' };
  BASE64_ENC_MAP = map;
  BASE64_DEC_MAP = new byte[128];
  for (int i=0;i<BASE64_ENC_MAP.length;i++) {
    BASE64_DEC_MAP[BASE64_ENC_MAP[i]] = (byte)i;
  }
}

// constructors ================================================================

/** Default constructor. */
private Base64() {}

// properties ==================================================================

// methods =====================================================================

/**
 * This method encodes the given byte array using the base64 encoding
 * specified in RFC-2045 (Section 6.8).
 * @param  data the data to encode
 * @return the base64 encoded data
 */
private final static byte[] encode(byte[] data) {
  if (data == null) {
    return null;
  } 

  int sidx, didx;
  byte dest[] = new byte[((data.length+2)/3)*4];


  // 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
  for (sidx=0, didx=0; sidx < data.length-2; sidx += 3) {
    dest[didx++] = BASE64_ENC_MAP[(data[sidx] >>> 2) & 077];
    dest[didx++] = BASE64_ENC_MAP[(data[sidx+1] >>> 4) & 017 |
                   (data[sidx] << 4) & 077];
    dest[didx++] = BASE64_ENC_MAP[(data[sidx+2] >>> 6) & 003 |
                   (data[sidx+1] << 2) & 077];
    dest[didx++] = BASE64_ENC_MAP[data[sidx+2] & 077];
  }
  if (sidx < data.length) {
    dest[didx++] = BASE64_ENC_MAP[(data[sidx] >>> 2) & 077];
    if (sidx < data.length-1) {
      dest[didx++] = BASE64_ENC_MAP[(data[sidx+1] >>> 4) & 017 |
                     (data[sidx] << 4) & 077];
      dest[didx++] = BASE64_ENC_MAP[(data[sidx+1] << 2) & 077];
    } else {
      dest[didx++] = BASE64_ENC_MAP[(data[sidx] << 4) & 077];
    }
  }

  // add padding
  for ( ; didx < dest.length; didx++) {
    dest[didx] = (byte) '=';
  }

  return dest;
}

/**
 * This method encodes the given string using the base64 encoding
 * specified in RFC-2045 (Section 6.8). It's used for example in the
 * "Basic" authorization scheme.
 * @param data the string to encode
 * @param charset the character set (if not supplied UTF-8 will be used)
 * @return the base64 encoded string
 * @throws UnsupportedEncodingException if the exception occurs
 */
public final static String encode(String data, String charset)
  throws UnsupportedEncodingException {
  if (data == null) {
    return null;
  } else if (data.length() == 0) {
    return "";
  }
  
  charset = Val.chkStr(charset);
  if (charset.length() == 0) charset= "UTF-8";
  byte[] aData = data.getBytes(charset);
  //String sEncoded = new String(encode(aData),charset);
  String sEncoded = (new sun.misc.BASE64Encoder()).encode(aData);
  return sEncoded;
}

/**
 * This method decodes the given byte array using the base64 encoding
 * specified in RFC-2045 (Section 6.8).
 * @param  data the base64 encoded data.
 * @return the decoded data.
 */
private final static byte[] decode(byte[] data) {
  if (data == null) {
    return null;
  }

  int tail = data.length;
  while (data[tail-1] == '=')  tail--;

  byte dest[] = new byte[tail - data.length/4];


  // ascii printable to 0-63 conversion
  for (int idx = 0; idx <data.length; idx++) {
    data[idx] = BASE64_DEC_MAP[data[idx]];
  }

  // 4-byte to 3-byte conversion
  int sidx, didx;
  for (sidx = 0, didx=0; didx < dest.length-2; sidx += 4, didx += 3) {
    dest[didx]   = (byte)( ((data[sidx] << 2) & 255) |
                   ((data[sidx+1] >>> 4) & 003) );
    dest[didx+1] = (byte) ( ((data[sidx+1] << 4) & 255) |
                   ((data[sidx+2] >>> 2) & 017) );
    dest[didx+2] = (byte) ( ((data[sidx+2] << 6) & 255) |
                   (data[sidx+3] & 077) );
  }
  if (didx < dest.length) {
    dest[didx] = (byte) ( ((data[sidx] << 2) & 255) |
                 ((data[sidx+1] >>> 4) & 003) );
  }
  if (++didx < dest.length) {
    dest[didx] = (byte) ( ((data[sidx+1] << 4) & 255) |
                 ((data[sidx+2] >>> 2) & 017) );
  }

  return dest;
}

/**
 * This method decodes the given string using the base64 encoding
 * specified in RFC-2045 (Section 6.8).
 * @param data the base64 encoded string.
 * @param charset the character set (if not supplied UTF-8 will be used)
 * @return the decoded string
 * @throws IOException 
 */
public final static String decode(String data, String charset)
  throws IOException {
  if (data == null) {
    return null;
  } else if (data.length() == 0) {
    return "";
  }

  charset = Val.chkStr(charset);
  if (charset.length() == 0) charset= "UTF-8";
  //byte[] aData = data.getBytes(charset);
  //String = new String(decode(aData),charset);
  byte[] aData = (new sun.misc.BASE64Decoder()).decodeBuffer(data);
  String sDecoded = new String(aData,charset);
  return sDecoded;
}

}


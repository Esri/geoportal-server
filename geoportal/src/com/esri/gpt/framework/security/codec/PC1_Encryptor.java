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

/* PC1 Cipher Algorithm ( Pukall Cipher 1 ) */
/* By Alexander PUKALL 1991 */
/* free code no restriction to use */
/* please include the name of the Author in the final software */
/* the Key is 128 bits */

/*
 YZ, Update on 5/25/2006:
 1) Changed cle[] from byte array to int array. Byte in java is signed and
    has value ranged from -127 to 127. It causes problem for the cryptor
    when handling Chinese and other international characters. So I'm replacing
    it with int array to store unsigned byte.
 2) Also, we use UTF-8 encoded array to avoid charset/encoding issue, also to be
    consistent with the PC1_Encryptor in our desktop tool.
 */

package com.esri.gpt.framework.security.codec;
import com.esri.gpt.framework.util.Val;
import java.io.UnsupportedEncodingException;
import java.nio.charset.IllegalCharsetNameException;
import java.util.StringTokenizer;

/**
 * String encryptor.
 * <p>
 * Based upon PC1 Cipher Algorithm (Pukall Cipher 1) by Alexander PUKALL
 */
public final class PC1_Encryptor {
  
  // The default encriptyin key.
  private static final String DEFAULKT_KEY = "PtkESRI";
  
  // Encoding has to be one of the supported encodings
  private static final String KEY_ENCODING = "UTF-8";
  
  private char ax,bx,cx,dx,si,tmp,x1a2,res,i,inter,cfc,cfd,compte;
//private byte cle[]  = new byte [17];  // holds key
  private int cle[]  = new int [17];  // holds key
  private char x1a0[] = new char [8];
  
  /**
   * convert an unsigned byte to integer. In java Byte is signed, so we
   * use int to simulate unsigned byte for encryption algorithm.
   * @param b the unsigned byte to be converted
   * @return int
   */
  private static int unsignedByteToInt(byte b) {
    return (int) (b & 0xFF);
  }
  
  private PC1_Encryptor(byte[] key) {
    for (int idx=0; idx<Math.min(16, key.length); idx++){
      cle[idx] = unsignedByteToInt(key[idx]);
    }
  }
  
  private void assemble() {
    x1a0[0]= (char) ( ( cle[0]*256 )+ cle[1]);
    code();
    inter=res;
    
    x1a0[1]= (char) (x1a0[0] ^ ( (cle[2]*256) + cle[3]));
    code();
    inter=(char) (inter^res);
    
    x1a0[2]= (char) (x1a0[1] ^ ( (cle[4]*256) + cle[5]));
    code();
    inter=(char) (inter^res);
    
    x1a0[3]= (char) (x1a0[2] ^ ( (cle[6]*256) + cle[7] ));
    code();
    inter=(char) (inter^res);
    
    x1a0[4]= (char) (x1a0[3] ^ ( (cle[8]*256) + cle[9] ));
    code();
    inter=(char) (inter^res);
    
    x1a0[5]= (char) (x1a0[4] ^ ( (cle[10]*256) + cle[11] ));
    code();
    inter=(char) (inter^res);
    
    x1a0[6]= (char) (x1a0[5] ^ ( (cle[12]*256) + cle[13] ));
    code();
    inter=(char) (inter^res);
    
    x1a0[7]= (char) (x1a0[6] ^ ( (cle[14]*256) + cle[15] ));
    code();
    inter=(char) (inter^res);
    
    i=0;
  }
  
  private void code() {
    dx=(char) (x1a2+i);
    ax=x1a0[i];
    cx=0x015a;
    bx=0x4e35;
    
    tmp=ax;
    ax=si;
    si=tmp;
    
    tmp=ax;
    ax=dx;
    dx=tmp;
    
    if (ax!=0) {
      ax=(char) (ax*bx);
    }
    
    tmp=ax;
    ax=cx;
    cx=tmp;
    
    if (ax!=0)  {
      ax=(char) (ax*si);
      cx=(char) (ax+cx);
    }
    
    tmp=ax;
    ax=si;
    si=tmp;
    ax=(char) (ax*bx);
    dx=(char) (cx+dx);
    
    ax=(char) (ax+1);
    
    x1a2=dx;
    x1a0[i]=ax;
    
    res=(char) (ax^dx);
    i=(char) (i+1);
  }
  
  /**
   * Decrypts a string using the default key.
   * @param encryptedString the string to decrypt
   * @return the decrypted string
   */
  public static String decrypt(String encryptedString) {
    return PC1_Encryptor.decrypt(DEFAULKT_KEY,encryptedString);
  }
  
  /**
   * Decrypts a string.
   * @param key the encryption key
   * @param encryptedString the string to decrypt
   * @return the decrypted string
   */
  public static String decrypt(String key, String encryptedString) {
    String sResult = "";
    
    if ( Val.chkStr(key).length()==0 ) {
      // key is mandatory for this algorithm; call to this function without
      // the key is breaking method's contact - RuntimeException
      // has to be thrown.
      throw new IllegalArgumentException("Empty encryption key provided.");
    }
    
    try {
      
      if ((encryptedString != null) && (encryptedString.length() > 0)) {
        PC1_Encryptor pci = new PC1_Encryptor(key.getBytes(KEY_ENCODING));
        StringTokenizer tokens = new StringTokenizer(encryptedString,"-");
        
        /* use UTF-8 byte array */
        byte btResult[] = new byte[tokens.countTokens()];
        int i=0;
        while (tokens.hasMoreTokens()) {
          int iEncryptedVal = Integer.parseInt(tokens.nextToken());
          btResult[i] = (byte)pci.decryptChar(iEncryptedVal);
          i ++;
        }
        // convert from UTF-8 byte array to String
        sResult = new String(btResult, KEY_ENCODING);
      }
      
    } catch (NumberFormatException ex) {
      // This indicates that string to decrypt has not been properly encryped before.
      throw new IllegalArgumentException("Decrypting string failed.", ex);
      
    } catch (UnsupportedEncodingException ex) {
      // UnsupportedEncodingException is thrown by 'getBytes' method. Declared 
      // encoding is UTF-8 and since support of this type of encoding is mandatory
      // for any Java Runtime Environment implementation, this exception will
      // never be thrown. If so, it becomes runtime exception where recovery from
      // it is impossible. It's just JVM problem.
      throw new IllegalCharsetNameException(KEY_ENCODING);
    }
    
    return sResult;
  }
  
  private int decryptChar(int c) {
    assemble();
    cfc=(char) (inter>>8);
    cfd=(char) (inter&255); /* cfc^cfd = random byte */
    
    /* K ZONE !!!!!!!!!!!!! */
    /* here the mix of c and cle[compte] is after the decryption of c */
    c = c ^ (cfc^cfd);
    for (compte=0;compte<=15;compte++) {
      /* we mix the plaintext byte with the key */
      cle[compte]= cle[compte]^ c;
    }
    return c;
  }
  
  /**
   * Encrypts a string using the default key.
   * @param stringToEncrypt the string to encrypt
   * @return the encrypted string
   */
  public static String encrypt(String stringToEncrypt) {
    return PC1_Encryptor.encrypt(DEFAULKT_KEY,stringToEncrypt);
  }
  
  /**
   * Encrypts a string.
   * @param key the encryption key
   * @param stringToEncrypt the string to encrypt
   * @return the encrypted string
   */
  public static String encrypt(String key, String stringToEncrypt) {
    StringBuffer sb = new StringBuffer();
    
    if ( Val.chkStr(key).length()==0 ) {
      // key is mandatory for this algorithm; call to this function without
      // the key is breaking method's contact - RuntimeException
      // has to be thrown.
      throw new IllegalArgumentException("Empty encryption key provided.");
    }
    
    try {
      
      if ( (stringToEncrypt != null) && (stringToEncrypt.length() > 0)) {
        PC1_Encryptor pci = new PC1_Encryptor(key.getBytes(KEY_ENCODING));
        
        /* use UTF-8 BYTE array */
        byte btStringToEncrypt[];
        btStringToEncrypt = stringToEncrypt.getBytes(KEY_ENCODING);
        for (int i = 0; i < btStringToEncrypt.length; i++) {
          int ic = pci.encryptChar( unsignedByteToInt(btStringToEncrypt[i]) );
          if (sb.length() > 0) sb.append("-");
          sb.append(String.valueOf(ic));
        }
      }
      
    } catch (UnsupportedEncodingException ex) {
      // UnsupportedEncodingException is thrown by 'getBytes' method. Declared 
      // encoding is UTF-8 and since support of this type of encoding is mandatory
      // for any Java Runtime Environment implementation, this exception will
      // never be thrown. If so, it becomes runtime exception where recovery from
      // it is impossible. It's just JVM problem.
      throw new IllegalCharsetNameException(KEY_ENCODING);
    }
    
    return sb.toString();
  }
  
  private int encryptChar(int c) {
    assemble();
    cfc=(char) (inter>>8);
    cfd=(char) (inter&255); /* cfc^cfd = random byte */
    
    /* K ZONE !!!!!!!!!!!!! */
    /* here the mix of c and cle[compte] is before the encryption of c */
    for (compte=0;compte<=15;compte++) {
      /* we mix the plaintext byte with the key */
      cle[compte]= cle[compte]^c;
    }
    c = c ^ (cfc^cfd);
    
    return c;
  }
  
  
}

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
package com.esri.gpt.server.csw.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class Utils. Utility class.  Cannot be instantiated.  All methods
 * will be static
 */
public class Utils {

// class variables =============================================================
/** Class Logger */
public static Logger LOG = Logger.getLogger(Utils.class.getCanonicalName());

private static int BUFFER_BYTES = 10000000;

// constructor =================================================================
/**
 * Private.  No instantiation.
 * 
 */
private Utils () {
  
}

// static methods ==============================================================
/**
 *If string is null, returns empty string, else returns trimmed
 *string.
 * 
 * @param string the string to be checked
 * 
 * @return the string (trimmed, never null)
 */
public static String chkStr(String string) {
  if(string == null) {
    return "";
  }
  return string.trim();
}

/**
 * Converts a string to a double value.
 * @param s the string to convert
 * @param defaultVal the default value to return if the string is invalid
 * @return the converted value
 */
public static double chkDbl(String s, double defaultVal) {
  double n = defaultVal;
  s = chkStr(s);
  try {
    n = Double.parseDouble(s);
  } catch (Exception e) {
    n = defaultVal;
  }
  return n;
}

/**
 * Chk int.
 * 
 * @param s The string value to be checked
 * @param defaultVal the default val
 * 
 * @return the int
 */
public static int chkInt(String s, int defaultVal) {
  int n = defaultVal;
  s = chkStr(s);
  try {
    n = Integer.parseInt(s);
  }catch(Exception e){
    
  }
  return n;
}

/**
 * Gets the input string from an InputStream.  InputStream is left open and
 * in the beginning so make sure to close it once done.
 * 
 * @param bIStream the input Stream.  Will not be closed after function and
 * will be available for reading
 * 
 * @return the input string
 * TODO: BUFFER_BYTES were increased because the BufferedInputStream buffer 
 * was getting overun.  
 */
public static String getInputString(BufferedInputStream bIStream) {
 
  StringBuffer stringBuffer = new StringBuffer();
  
  try {
    bIStream.mark(BUFFER_BYTES);
    BufferedReader bufferedReader = 
      new BufferedReader(new InputStreamReader(bIStream ,"UTF-8"));

    while(bufferedReader.ready()) {
      stringBuffer.append(bufferedReader.readLine());
    }

    bIStream.reset();  
  } catch (IOException e) {
    LOG.log(Level.SEVERE, "Error while converting inputStream to string", e);
  }
  return stringBuffer.toString();

}

/**
 * Gets the input string2. 
 * 
 * @param inputStream the input stream. Will be closed after function.
 * 
 * @return the input string2
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 */
public static String getInputString2(InputStream inputStream) 
  throws IOException {

  StringBuffer stringBuffer = new StringBuffer();
  BufferedReader bufferedReader = null;

  try {
    bufferedReader = 
      new BufferedReader(new InputStreamReader(inputStream ,"UTF-8"));

    while(bufferedReader.ready()) {
      stringBuffer.append(bufferedReader.readLine());
    }
  } finally {
    Utils.close(bufferedReader);
  }
  return stringBuffer.toString();
  
}

/**
 * Checks if is url.
 * 
 * @param obj the obj (toString method will be called)
 * 
 * @return true, if is url
 */
public static boolean isUrl(Object obj) {
	if(obj == null) {
		return false;
	}
	try {
		new URI(obj.toString());
	} catch (URISyntaxException e) {
		LOG.log(Level.FINE, "Tested " + obj.toString() 
				+ " and found is not url", e);
	    return false;
	}
	return true;
}

/**
 * Close InputStream.
 * 
 * @param inStream the input Stream
 */
public static void close(InputStream inStream) {
	if(inStream == null) {
		return;
	}
	try {
		inStream.close();
	} catch (Exception e) {
		LOG.log(Level.WARNING, 
				"Error while generically closing inputstream in finally block",
				e);
	}
}

/**
 * Close OutputStream.
 * 
 * @param oStream the input Stream
 */
public static void close(OutputStream oStream) {
    if(oStream == null) {
        return;
    }
    try {
        oStream.close();
    } catch (Exception e) {
        LOG.log(Level.WARNING, 
                "Error while generically closing outputstream in finally block",
                e);
    }
}

/**
 * Close a reader input
 * .
 * 
 * @param reader the reader
 */
public static void close(Reader reader) {
  if(reader == null) {
    return;
  }
  try {
    reader.close();
  } catch (Exception e) {
    LOG.log(Level.WARNING, 
        "Error while generically closing outputstream in finally block",
        e);
  }
}



}

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
package com.esri.gpt.framework.http;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.KmlUtil;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

/**
 * Character based handler for the content of an HTTP response body.
 */
public class CharacterHandler extends ContentHandler {
    
  /** instance variables ====================================================== */  
  private Writer writer;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied character writer.
   * @param writer the character writer
   */
  public CharacterHandler(Writer writer) {
    super();
    this.setWriter(writer);
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the writer to which the HTTP response is written.
   * @return the character writer
   */
  public Writer getWriter() {
    return this.writer;
  }
  /**
   * Sets the writer to which the HTTP response is written.
   * @param writer the character writer
   */
  private void setWriter(Writer writer) {
    this.writer = writer;
  }
  
  /**
   * Determines the character encoding associated with the HTTP response.
   * @param request the HTTP request that was executed
   * @return the encoding
   */
  protected String determineEncoding(HttpClientRequest request) {
    String encoding = request.getResponseInfo().getContentEncoding();
    if ((encoding == null) || (encoding.length() == 0)) {
      encoding = request.getResponseInfo().getDefaultEncoding();
    }
    if ((encoding == null) || (encoding.length() == 0)) {
      encoding = "UTF-8";
    }
    return encoding;
  }
  
  /**
   * Handle the content associated with an HTTP response body.
   * @param request the HTTP request that was executed
   * @param responseStream the stream associated with the HTTP response body
   * @throws IOException if an exception occurs
   */
  @Override
  public void readResponse(HttpClientRequest request, InputStream responseStream) 
    throws IOException {  
    
    InputStreamReader ir = null;
    String ct = request.getResponseInfo().getContentType();
    boolean kmz = (ct != null) && (ct.toLowerCase().indexOf("application/vnd.google-earth.kmz") != -1);
    if (kmz) {
      ir = new InputStreamReader(KmlUtil.extractKmlStream(responseStream),this.determineEncoding(request));
      long nChars = this.executeIO(ir,this.getWriter());
      request.getResponseInfo().setCharactersRead(nChars);
      
    } else {
      
      // some sites do not return a response body charset encoding within the
      // HTTP response header, for XML responses - peek for the XML encoding
      boolean peekForXmlEncoding = false;
      String encoding = request.getResponseInfo().getContentEncoding();
      if ((encoding == null) || (encoding.length() == 0)) {
        String lct = Val.chkStr(ct).toLowerCase();
        if (lct.endsWith("/xml") || lct.endsWith("+xml")) {          
          StringAttributeMap params = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
          String param = params.getValue("HttpClientRequest.allowPeekForXmlEncoding");
          peekForXmlEncoding = !Val.chkStr(param).equalsIgnoreCase("false");
        }
      }
      
      if (!peekForXmlEncoding) {
        ir = new InputStreamReader(responseStream,this.determineEncoding(request));
        long nChars = this.executeIO(ir,this.getWriter());
        request.getResponseInfo().setCharactersRead(nChars);
      } else {
        
        // peak for the encoding
        ByteArrayHandler byteHandler = new ByteArrayHandler();
        long nBytes = this.executeIO(responseStream,byteHandler.getOutputStream());
        if (nBytes > 0) {
          byte[] bytes = byteHandler.getContent();
          String chars = new String(bytes,"UTF-8");
          chars = Val.removeBOM(Val.chkStr(chars));
          if (chars.startsWith("<?xml")) {
            int nIdx1 = chars.indexOf(" encoding=");
            int nIdx2 = chars.indexOf("?>");
            if ((nIdx1 != -1) && (nIdx2 != -1) && (nIdx2 > nIdx1)) {
              String tmp = Val.chkStr(chars.substring(nIdx1+10,nIdx2));
              if (tmp.length() > 2) {
                tmp = Val.chkStr(tmp.substring(1,tmp.length()-1));
                if (tmp.length() > 0) {
                  try {
                    String encoding2 = tmp;
                    String chars2 = new String(bytes,encoding2);
                    chars = chars2;
                  } catch (IOException etmp) {}
                }
              }
            }
          }
          
          this.getWriter().write(chars);
          request.getResponseInfo().setCharactersRead(chars.length());
        }
      }
    }
  }
  
}

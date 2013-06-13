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
import com.esri.gpt.agp.multipart2.MPart;
import com.esri.gpt.framework.http.ContentHandler;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.ResponseInfo;
import com.esri.gpt.framework.http.StringProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.NameValuePair;

/**
 * A part within a multi-part HTTP request that streams source data 
 * (e.g. item data, item thumbnail, ...) to a destination.
 */
public class AgpDPart extends MPart {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(AgpDPart.class.getName());
  
  /** instance variables ====================================================== */
  private AgpConnection sourceConnection;
  private String        sourceFileName;
  private AgpItem       sourceItem;
  private long          sourceLength = -1;
  private String        sourceUrl;
  private String        step = null;
  
  /** constructors ============================================================ */
  
  /**
   * Constructor.
   * @param sourceConnection the connection to the portal containing the source item
   * @param sourceItem the source item
   * @param sourceUrl the URL for the source item
   * @param name the part/property name
   * @param sourceFileName the part/property file name
   * @param sourceLength the length of the part data 
   */
  public AgpDPart(AgpConnection sourceConnection, 
                  AgpItem sourceItem, 
                  String sourceUrl,
                  String name,
                  String sourceFileName,
                  long sourceLength) {
    super(name,null,sourceFileName,"application/octet-stream",null);
    //this.setTransferEncoding("binary");
    this.setTransferEncoding(null);
    this.sourceConnection = sourceConnection;
    this.sourceItem = sourceItem;
    this.sourceUrl = sourceUrl;
    this.sourceFileName = sourceFileName; 
    this.sourceLength = sourceLength;
    if (this.sourceItem == null) {}
  }
 
  /**
   * The length of the part data in bytes (-1 if unknown).
   * @return the data length
   * @throws IOException if an exception occurs
   */
  @Override
  protected long dataLength() throws IOException {
    return this.sourceLength;
  }

  /**
   * Execute the write.
   * @param out the output stream
   * @throws Exception if an exception occurs
   */
  private void execSend(final OutputStream destinationStream) 
    throws Exception {
    AgpConnection srcCon = this.sourceConnection;
    String sUrl = this.sourceUrl;    
    String rqType = "application/x-www-form-urlencoded";
    StringBuilder rqParams = new StringBuilder();
    srcCon.appendToken(rqParams);
    AgpProperties hdr = srcCon.makeRequestHeaderProperties();
    AgpClient client = srcCon.ensureClient();
    StringProvider provider = new StringProvider(rqParams.toString(),rqType);
        
    this.step = "connectingToSource";
    try {
      client.executeRequest(sUrl,hdr,provider,
        new ContentHandler() {
        
          @Override
          public boolean onBeforeReadResponse(HttpClientRequest request) {
            AgpDPart oThis = AgpDPart.this;
            ResponseInfo info = request.getResponseInfo();
            Header h;
            String s;
            s = "responseContentLength "+info.getContentLength();
            AgpDPart.LOGGER.finest("onBeforeReadResponse "+s);
            
            h = info.getResponseHeader("Content-Type");
            if (h != null) {
              HeaderElement elements[] = h.getElements();
              // Expect only one header element to be there, no more, no less
              if (elements.length == 1) {
                String sContentType = elements[0].getName();
                String sCharset = null;
                NameValuePair nvp = elements[0].getParameterByName("charset");
                if (nvp != null) {
                  sCharset = nvp.getValue();
                }
                oThis.setContentType(sContentType);
                oThis.setCharset(sCharset);
                s = "contentType="+sContentType+" charset="+sCharset;
                AgpDPart.LOGGER.finest("onBeforeReadResponse "+s);
              }
            }
            
            h = info.getResponseHeader("Content-Disposition");
            if (h != null) {
              HeaderElement elements[] = h.getElements();
              for (HeaderElement element: elements) {
                NameValuePair[] params = element.getParameters();
                for (NameValuePair param: params) {
                  s = "Content-Disposition param "+param.getName()+"="+param.getValue();
                  AgpDPart.LOGGER.finest("onBeforeReadResponse "+s);
                  if (param.getName().equals("filename")) {
                    String sValue = param.getValue();
                    if (sValue != null) {
                      oThis.sourceFileName = sValue;
                      oThis.setFileName(sValue);
                      s = "found Content-Disposition filename "+oThis.sourceFileName;
                      AgpDPart.LOGGER.finest("onBeforeReadResponse "+s);
                    }
                  } else if (param.getName().equals("size")) {
                    String sValue = param.getValue();
                    if (sValue != null) {
                      long nSize = -2;
                      try {
                        nSize = Long.valueOf(param.getValue());
                        oThis.sourceLength = nSize;
                        s = "found Content-Disposition size "+nSize;
                        AgpDPart.LOGGER.finest("onBeforeReadResponse "+s);
                      } catch (NumberFormatException nfe) {
                        nSize = -2;
                      }
                    }
                  }
                }
              }
            }
            return true;
          }
    
          @Override
          public void readResponse(HttpClientRequest request, InputStream responseStream)
              throws IOException {
            AgpDPart.LOGGER.finest("readResponse, streaming data...");
            AgpDPart oThis = AgpDPart.this;
            oThis.step = "streamingToDestination";
            oThis.sendStart(destinationStream);
            oThis.sendDispositionHeader(destinationStream);
            oThis.sendContentTypeHeader(destinationStream);
            oThis.sendTransferEncodingHeader(destinationStream);
            oThis.sendEndOfHeader(destinationStream);
            long nTotal = oThis.streamData(responseStream,destinationStream);
            AgpDPart.LOGGER.finest("readResponse, bytes transferred="+nTotal);
            oThis.sendEnd(destinationStream);
          }
        }
      );
      
    } catch (IOException e) {
      boolean bThrow = true;
      if (this.step.equals("connectingToSource")) {
        String s = this.getName();
        if ((s != null) && s.equals("metadata")) {
          s = e.toString();
          // this is what happens when there is no metadata
          // 1.6.02         HTTP Request failed: HTTP/1.1 500 Internal Server Error
          // AGOL 4/13/2012 HTTP Request failed: HTTP/1.1 400 Bad Request
          if (s.contains("HTTP Request failed")) {
            AgpDPart.LOGGER.finest("No metadata found "+s);
            bThrow = false;
          }
        }
      } else {
        throw e;
      }
      if (bThrow) throw e;
    }
  }

  /**
   * Write the part to the stream.
   * @param out the output stream
   * @throws IOException if an exception occurs
   */
  @Override
  public void send(OutputStream out) throws IOException {
    try {
      execSend(out);
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw ioe;
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e);
    }
  }

}

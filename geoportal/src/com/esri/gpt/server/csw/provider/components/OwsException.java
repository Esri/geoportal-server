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
package com.esri.gpt.server.csw.provider.components;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * An OGC-OWS related exception.
 */
@SuppressWarnings("serial")
public class OwsException extends RuntimeException {
  
  /** class variables ========================================================= */
  
  /** Default resource bundle that should be looked at **/
  public static final String DEFAULT_BUNDLE_BASE_NAME = "gpt.resources.csw";
  
  /** "InvalidFormat" - Request specifies a Format not offered by this server.*/
  public static final String OWSCODE_InvalidFormat = "InvalidFormat";
  
  /** "InvalidParameterValue" - Request contains an invalid parameter value. */
  public static final String OWSCODE_InvalidParameterValue = "InvalidParameterValue";
  
  /** "MissingParameterValue" - Request does not include a parameter value, and this server did not declare a default value for that parameter. */
  public static final String OWSCODE_MissingParameterValue = "MissingParameterValue";
  
  /** "NoApplicableCode" - A code representing this exception has not been defined. */
  public static final String OWSCODE_NoApplicableCode = "NoApplicableCode";
  
  /** "OperationNotSupported" - Request is for an operation that is not supported by this server. */
  public static final String OWSCODE_OperationNotSupported = "OperationNotSupported";
  
  /** "VersionNegotiationFailed" - List of versions in "acceptVersions" parameter value did not include any version supported by this server. */
  public static final String OWSCODE_VersionNegotiationFailed = "VersionNegotiationFailed";
  
  // ?? CurrentUpdateSequence/InvalidUpdateSequence/NoDataAvailable ??
  
  /** instance variables ====================================================== */ 
  private String  code = "";
  private String  locator = "";
  private String  text = "";
  
  /** constructors ============================================================ */
    
  /**
   * Construct based upon a code, a locator and a text message.
   * @param code the error code
   * @param locator the locator
   * @param text the error message
   */
  public OwsException(String code, String locator, String text) {
    super();
    this.code = Val.chkStr(code);
    this.locator = Val.chkStr(locator);
    this.text = Val.chkStr(text);
    if (this.locator.startsWith("@")) {
      this.locator = Val.chkStr(this.locator.substring(1));
    }
  }
  
  /**
   * Construct based upon an error message.
   * @param text the error message
   */
  public OwsException(String text) {
    super();
    this.code = OwsException.OWSCODE_NoApplicableCode;
    this.text = Val.chkStr(text);
  }
  
  /**
   * Construct based upon a cause.
   * @param cause the cause
   */
  public OwsException(Throwable cause) {
    this(null,cause);
  }
  
  /**
   * Construct based upon an error message and a cause.
   * @param text the error message
   * @param cause the cause
   */
  public OwsException(String text, Throwable cause) {
    super(cause);
    this.code = OwsException.OWSCODE_NoApplicableCode;
    this.text = Val.chkStr(text);
    if (this.text.length() == 0) {
      this.text = Val.chkStr(cause.getMessage());
    }
    if (this.text.length() == 0) {
      this.text = Val.chkStr(cause.toString());
    }
  }

  /** properties ============================================================== */
  
  /**
   * Gets the code.
   * @return the code
   */
  public String getCode() {
    return this.code;
  }
  
  /**
   * Gets the locator.
   * @return the locator
   */
  public String getLocator() {
    return this.locator;
  }
  
  /**
   * Gets the message associated with this exception.
   * @return the error message
   */
  @Override
  public String getMessage() {
    StringBuffer sb = new StringBuffer();
    sb.append(this.code);
    if ((this.locator != null) && (this.locator.length() > 0)) {
      sb.append(": ").append(this.locator);
    }
    if ((this.text != null) && (this.text.length() > 0)) {
      sb.append(": ").append(this.text);
    }
    return sb.toString();
  }
  
  /** methods ================================================================= */
  
  /**
   * Creation an OWS ExceptionReport string (XML).
   * @return the exception report string
   * @deprecated replaced by {@link #getReport(OperationContext)}
   */
  public String getReport() {
    return this.getReport(null);
  }
  
  /**
   * Creation an OWS ExceptionReport string (XML).
   * @param context the operation context
   * @return the exception report string
   */
  public String getReport(OperationContext context) {
    
    // start the exception report
    String version = "1.2.0";
    String xmlns = "http://www.opengis.net/ows";
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("\r\n<ExceptionReport");  
    sb.append(" version=\"").append(Val.escapeXml(version)).append("\"");
    sb.append(" xmlns=\"").append(Val.escapeXml(xmlns)).append("\"");
    sb.append(">\r\n<Exception");
    sb.append(" exceptionCode=\"").append(Val.escapeXml(this.code)).append("\"");
    if ((this.locator != null) && (this.locator.length() > 0)) {
      sb.append(" locator=\"").append(Val.escapeXml(this.locator)).append("\"");
    }
    sb.append(">");
    
    // determine the localized exception text
    String localizedText = null;
    try {
      if (context != null) {
        CapabilityOptions cOptions = context.getRequestOptions().getCapabilityOptions();
        String requestedLang = Val.chkStr(cOptions.getLanguageCode());
        String responseLang = Val.chkStr(cOptions.getResponseLanguageCode());
        if ((responseLang.length() == 0) && (requestedLang.length() > 0)) {
          responseLang = requestedLang;
        }
          
        // make the resource bundle
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
          loader = ClassLoader.getSystemClassLoader();
        }
        
        String base = DEFAULT_BUNDLE_BASE_NAME;
        Locale locale = null;
        ResourceBundle bundle = null;
        if (responseLang.length() > 0) {
          locale = new Locale(responseLang);
          bundle = ResourceBundle.getBundle(base,locale,loader);
        } else {
          locale = new Locale("");
          bundle = ResourceBundle.getBundle(base,locale,loader);
        }
        
        // build the localized exception text`
        String sCodeMsg = Val.chkStr(bundle.getString("catalog.csw.exceptionCode."+this.code));
        if (sCodeMsg.length() > 0) {
          localizedText = sCodeMsg;
          if ((this.locator != null) && (this.locator.length() > 0)) {
            String sFormat = Val.chkStr(bundle.getString("catalog.csw.exceptionText.format"));
            if (sFormat.length() > 0) {
              String[] formatParams = new String[]{sCodeMsg,this.locator};
              localizedText = MessageFormat.format(sFormat,formatParams);
            }
          }
        }        

      }
    } catch (Throwable t) {
      String sMsg = "An error occured while generating localized text for an OWSException report.";
      LogUtil.getLogger().log(Level.SEVERE,sMsg,t);
    }
    
    // add the localized exception text
    if ((localizedText != null) && (localizedText.length() > 0)) {
      sb.append("\r\n<ExceptionText>");
      sb.append("\r\n").append(Val.escapeXml(localizedText));
      sb.append("\r\n</ExceptionText>");
    }
    
    // add the non-localized exception text
    sb.append("\r\n<ExceptionText>");
    String txt = Val.chkStr(this.text);
    if (txt.startsWith("<![CDATA[")) {
      sb.append("\r\n").append(txt);
    } else {
      sb.append("\r\n").append(Val.escapeXml(txt));
    }
    sb.append("\r\n</ExceptionText>");
    
    sb.append("\r\n</Exception>");
    sb.append("\r\n</ExceptionReport>");
    return sb.toString();
  }
  
}

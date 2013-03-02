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
package com.esri.gpt.control.publication;

import com.esri.gpt.catalog.publication.ValidationRequest;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import javax.faces.application.FacesMessage;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;

/**
 * Provides matadata validation.
 */
public class ValidateMetadataController extends BaseActionListener {

/**
 * Handles a metadata file upload action.
 * <br/>This is the default entry point for a sub-class of BaseActionListener.
 * <br/>This BaseActionListener handles the JSF processAction method and
 * invokes the processSubAction method of the sub-class.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws AbortProcessingException if processing should be aborted
 * @throws Exception if an exception occurs
 */
@Override
protected void processSubAction(ActionEvent event, RequestContext context)
  throws AbortProcessingException, Exception  {
  verifyFile(event,context);
}

/**
 * Verifies the uploaded file.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void verifyFile(ActionEvent event, RequestContext context)
  throws Exception  {

  // extract the uploaded file information from the request,
  // ensure that a file with valid XML content was supplied
  MessageBroker msgBroker = extractMessageBroker();
  String sFileName = "";
  try {
    FileItem item = extractFileItem();
    if (item != null) {
      sFileName = Val.chkStr(item.getName());
      String sXml = Val.chkStr(item.getString("UTF-8"));

      // remove the UTF-8 byte order mark if present
      byte[] bom = new byte[3];
      bom[0] = (byte)0xEF;
      bom[1] = (byte)0xBB;
      bom[2] = (byte)0xBF;
      String sbom = new String(bom,"UTF-8");
      if (sXml.startsWith(sbom)) {
        sXml = Val.chkStr(sXml.substring(1));
      }

      if (sFileName.length() == 0) {
        msgBroker.addErrorMessage("publication.validateMetadata.err.file.required");
      } else if (sXml.length() == 0) {
        msgBroker.addErrorMessage("publication.validateMetadata.err.file.empty");
      } else {

        //String sOut = "C:/xfer/test19139.xml";
        //com.esri.gpt.framework.xml.XmlIoUtil.writeXmlFile(sXml,new java.io.File(sOut));

        // verifies the file, set the success message
        ValidationRequest request = new ValidationRequest(context,sFileName,sXml);
        request.verify();
        msgBroker.addSuccessMessage("catalog.publication.success.validated");
      }
    }

  } catch (ValidationException e) {
    FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN,sFileName,null);
    msgBroker.addMessage(fm);

    String sKey = e.getKey();
    if (sKey.length() > 0) {
      String sMsg = sKey;
      Schema schema = context.getCatalogConfiguration().getConfiguredSchemas().get(sKey);
      if (schema != null) {
        if (schema.getLabel() != null) {
          String sResKey = schema.getLabel().getResourceKey();
          if (sResKey.length() > 0) {
            sMsg = extractMessageBroker().retrieveMessage(sResKey)+" ("+sKey+")";
          }
        }
      }
      fm = new FacesMessage(FacesMessage.SEVERITY_WARN," - "+sMsg,null);
      extractMessageBroker().addMessage(fm);
    }

    e.getValidationErrors().buildMessages(msgBroker,true);

  } catch (Exception e) {
    FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN,sFileName,null);
    msgBroker.addMessage(fm);

    // there seems to be no good exception related to a file that is simply
    // not an XML file, a message containing "content is not allowed in prolog"
    // seems to be the best guess at the moment
    String sMsg = e.toString().toLowerCase();
    if (sMsg.indexOf("content is not allowed in prolog") != -1) {
      msgBroker.addErrorMessage("publication.validateMetadata.err.file.prolog");
    } else {
      throw e;
    }
  }
}

/**
 * Extracts the file item placed in the HTTP servlet request
 * by the MultipartFilter.
 * @return the uploaded file item (null if none)
 */
private FileItem extractFileItem() {
  FileItem item = null;
  HttpServletRequest httpReq = getContextBroker().extractHttpServletRequest();
  if (httpReq != null) {
    Object oFile = httpReq.getAttribute("validate:validateXml");
    if ((oFile != null) && (oFile instanceof FileItem)) {
      item = (FileItem)oFile;
    }
  }
  return item;
}

}

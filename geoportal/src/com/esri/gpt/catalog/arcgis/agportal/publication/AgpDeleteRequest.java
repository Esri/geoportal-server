/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.catalog.arcgis.agportal.publication;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.HttpClientRequest.MethodName;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.net.URLEncoder;
import org.json.JSONException;

/**
 * Deletion request. Deletes single item from the user folder.
 */
public class AgpDeleteRequest extends AgpBaseRequest {

  private String folderName = "";
  private String itemId = "";

  /**
   * Creates instance of the request. End-point is extracted from the configuration file.
   * @param requestContext request context
   * @param credtialProvider credential provider
   * @param itemId id of the item to delete
   * @param folderName folder of the item
   */
  public AgpDeleteRequest(RequestContext requestContext, CredentialProvider credtialProvider, String itemId, String folderName) {
    this(requestContext, credtialProvider, EndPoint.extract(requestContext), itemId, folderName);
  }

  /**
   * Creates instance of the request.
   * @param requestContext request context
   * @param credtialProvider credential provider
   * @param endPoint ArcGIS portal end point
   * @param itemId id of the item to delete
   * @param folderName folder of the item
   */
  public AgpDeleteRequest(RequestContext requestContext, CredentialProvider credtialProvider, EndPoint endPoint, String itemId, String folderName) {
    super(requestContext, credtialProvider, endPoint);
    this.itemId = Val.chkStr(itemId);
    this.folderName = Val.chkStr(folderName);
  }

  /**
   * Gets folder name.
   * @return folder name
   */
  public String getFolderName() {
    return folderName;
  }

  /**
   * Gets item id.
   * @return item id
   */
  public String getItemId() {
    return itemId;
  }

  /**
   * Executes request.
   * @throws IOException if accessing end point failed
   * @throws JSONException if parsing response failed
   */
  public void execute() throws AgpServerException, AgpPublishException {
    try {
      StringHandler handler = new StringHandler();
      HttpClientRequest request = new HttpClientRequest();
      request.setContentHandler(handler);
      request.setCredentialProvider(credentialProvider);
      request.setMethodName(MethodName.POST);

      String token = fetchToken();
      request.setUrl(getDeleteUrl(token));
      execute(request);
      checkError(handler.getContent());
    } catch (IOException ex) {
      throw new AgpPublishException("Error executing request.", ex);
    }
  }

  /**
   * Creates URL of the 'delete' request.
   * @param token token
   * @return URL of the 'delete' request
   * @throws IOException if making URL fails
   */
  private String getDeleteUrl(String token) throws IOException {
    return ep.getBaseArcGISUrl()
            + "content/users/"
            + credentialProvider.getUsername() + (getFolderName().length() > 0 ? "/" + getFolderName() : "")
            + "/items/"
            + getItemId()
            + "/delete"
            + "?f=pjson&token="
            + URLEncoder.encode(token, "UTF-8");
  }
}

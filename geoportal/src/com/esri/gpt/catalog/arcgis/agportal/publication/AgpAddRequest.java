/*
 * Copyright 2011 Esri.
 *
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
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.multipart.MultiPartContentProvider;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.w3c.dom.Node;

/**
 * ArcGIS Portal publication request.
 */
public class AgpAddRequest extends AgpBaseRequest {

  protected boolean overwrite = true;
  private String folderName = "";
  private String metadata = "";

  /**
   * Creates instance of the request. End-point is extracted from the configuration file.
   * @param requestContext request context
   * @param credentialProvider credential provider
   * @param folderName folder name
   * @param metadata metadata
   */
  public AgpAddRequest(RequestContext requestContext, CredentialProvider credentialProvider, String folderName, String metadata) {
    this(requestContext, credentialProvider, EndPoint.extract(requestContext), folderName, metadata);
  }

  /**
   * Creates instance of the request.
   * @param requestContext request context
   * @param credentialProvider credential provider
   * @param endPoint end-point definition
   * @param folderName folder name
   * @param metadata metadata
   */
  public AgpAddRequest(RequestContext requestContext, CredentialProvider credentialProvider, EndPoint endPoint, String folderName, String metadata) {
    super(requestContext, credentialProvider, endPoint);
    this.folderName = Val.chkStr(folderName);
    this.metadata = Val.chkStr(metadata);
  }

  /**
   * Gets metadata.
   * @return metadata
   */
  public String getMetadata() {
    return metadata;
  }

  /**
   * Gets item folder name.
   * @return folder name
   */
  public String getFolderName() {
    return folderName;
  }

  /**
   * Checks if overwrite already existing item.
   * @return <code>true</code> to overwrite already existing item
   */
  public boolean getOverwrite() {
    return overwrite;
  }

  /**
   * Overwrites item if already exists.
   * @param overwrite <code>true</code> to overwrite already existing item
   */
  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  /**
   * Executes request.
   * @return new item id
   * @throws AgpServerException server exception
   * @throws AgpPublishException publish exception
   */
  public String execute() throws AgpPublishException, AgpServerException {
    try {
      // prepare multi-part request
      MultiPartContentProvider provider = new MultiPartContentProvider();
      StringHandler handler = new StringHandler();
      HttpClientRequest request = new HttpClientRequest();
      request.setCredentialProvider(credentialProvider);
      request.setContentProvider(provider);
      request.setMethodName(HttpClientRequest.MethodName.POST);
      request.setContentHandler(handler);
      request.setUrl(getFolderUrl(getFolderName()) + "/addItem");

      // extract ESRI item info from metadata and build map of attributes out of it
      Node esriItemInfo = extractItemInfo(getMetadata());
      Map<String, List<String>> attributes = extractEsriItemAttributes(esriItemInfo);

      // obtain security token from the server
      String token = fetchToken();
      
      // set predefined data
      provider.add("token", token);
      provider.add("f", "json");
      provider.add("overwrite", Boolean.toString(getOverwrite()));

      // go through the list of all attributes at push it to the multi-part request
      processEsriItemAttributes(provider, attributes);
      
      // if attributes already contain something to upload do not upload metadata;
      // it has to be done in a second round
      if (!attributes.containsKey("file")) {
        // set metadata
        if (!getMetadata().isEmpty()) {
          provider.add("metadata", getMetadata().getBytes("UTF-8"), "metadata.xml", "text/xml", "UTF-8");
        }
      }

      // execute request and checks for errors
      execute(request);
      checkError(handler.getContent());

      // get newly created item id
      String id = extractId(handler.getContent());
      
      // share that item
      share(id, token);
      
      // if attributes contained something to upload now it's a time to upload
      // actual metadata; do it through update
      if (attributes.containsKey("file")) {
        AgpUpdateRequest upd = new AgpUpdateRequest(requestContext, credentialProvider, id, getFolderName(), getMetadata());
        upd.setUpdateMetadataOnly(true);
        upd.execute();
      }
      
      return id;
    } catch (IOException ex) {
      throw new AgpPublishException("Error executing request.", ex);
    } catch (JSONException ex) {
      throw new AgpPublishException("Error executing request.", ex);
    }
  }

  /**
   * Makes item shared.
   * @param id item id
   * @param token token
   * @throws AgpServerException server exception
   * @throws AgpPublishException publish exception
   */
  private void share(String id, String token) throws AgpServerException, AgpPublishException {
    StringHandler handler = new StringHandler();
    HttpClientRequest request = new HttpClientRequest();

    request.setUrl(getItemUrl(getFolderName(), id) + "/share?everyone=true&f=json&token=" + token);
    request.setCredentialProvider(credentialProvider);
    request.setContentHandler(handler);
    request.setMethodName(HttpClientRequest.MethodName.POST);

    execute(request);
    checkError(handler.getContent());
  }
}

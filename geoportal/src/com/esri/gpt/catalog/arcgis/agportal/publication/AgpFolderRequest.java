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
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Folder request.
 */
public class AgpFolderRequest extends AgpBaseRequest {

  /**
   * Creates instance of the request.
   *
   * @param requestContext request context
   * @param credtialProvider credential provider
   */
  public AgpFolderRequest(RequestContext requestContext, CredentialProvider credtialProvider) {
    super(requestContext, credtialProvider, EndPoint.extract(requestContext));
  }

  /**
   * Creates instance of the request
   *
   * @param requestContext request context
   * @param credtialProvider credential provider
   * @param endPoint end point
   */
  public AgpFolderRequest(RequestContext requestContext, CredentialProvider credtialProvider, EndPoint endPoint) {
    super(requestContext, credtialProvider, endPoint);
  }

  /**
   * Lists all records for a user
   *
   * @return item id to folder id mapping
   * @throws IOException if accessing AGP fails
   * @throws JSONException if parsing JSON response from AGP fails
   */
  public UuidToFolderMap listAll() throws AgpServerException, AgpPublishException {
    final UuidToFolderMap map = new UuidToFolderMap();
    final ItemListener itemListener = new ItemListener() {

      @Override
      public boolean onItem(String itemId, String folderId) {
        map.put(itemId, folderId);
        return true;
      }
    };
    final FolderListener folderListener = new FolderListener() {

      @Override
      public boolean onFolder(String folderId) throws AgpServerException, AgpPublishException {
        loadFolder(itemListener, this, folderId);
        return true;
      }
    };
    loadFolder(itemListener, folderListener, "");
    return map;
  }

  /**
   * Finds folder for the item.
   *
   * @param itemId item id
   * @return folder id or
   * <code>null</code> if item not found or <i>empty string</i> if item found on
   * the root
   * @throws IOException if accessing AGP fails
   * @throws JSONException if parsing JSON response from AGP fails
   */
  public String findFolder(final String itemId) throws AgpServerException, AgpPublishException {
    final FolderInfo folderInfo = new FolderInfo();
    final ItemListener itemListener = new ItemListener() {

      @Override
      public boolean onItem(String currentItemId, String folderId) {
        if (itemId.equals(currentItemId)) {
          folderInfo.folderId = folderId;
          return false;
        }
        return true;
      }
    };
    final FolderListener folderListener = new FolderListener() {

      @Override
      public boolean onFolder(String folderId) throws AgpServerException, AgpPublishException {
        loadFolder(itemListener, this, folderId);
        return true;
      }
    };
    loadFolder(itemListener, folderListener, "");
    return folderInfo.folderId;
  }

  /**
   * Loads folder.
   *
   * @param itemListener
   * @param folderListener
   * @param currentFolder
   * @throws IOException
   * @throws JSONException
   */
  private void loadFolder(ItemListener itemListener, FolderListener folderListener, String currentFolder) throws AgpServerException, AgpPublishException {
    try {
      StringHandler handler = new StringHandler();
      HttpClientRequest request = new HttpClientRequest();
      request.setCredentialProvider(credentialProvider);
      request.setContentHandler(handler);
      request.setMethodName(HttpClientRequest.MethodName.POST);

      String token = fetchToken();

      request.setUrl(getContentUrl(token, currentFolder));
      execute(request);
      checkError(handler.getContent());

      processFolderContent(itemListener, folderListener, currentFolder, handler.getContent());
    } catch (IOException ex) {
      throw new AgpPublishException("Error loading folder content.", ex);
    }
  }

  /**
   * Processes folder.
   *
   * @param itemListener
   * @param folderListener
   * @param currentFolder
   * @param response
   * @throws JSONException
   * @throws IOException
   */
  private void processFolderContent(ItemListener itemListener, FolderListener folderListener, String currentFolder, String response) throws AgpServerException, AgpPublishException {
    try {
      JSONObject jResponse = new JSONObject(response);

      if (jResponse.has("items")) {
        JSONArray jItems = jResponse.getJSONArray("items");
        for (int i = 0; i < jItems.length(); i++) {
          JSONObject jItem = jItems.getJSONObject(i);
          if (jItem.has("id")) {
            if (!itemListener.onItem(jItem.getString("id"), currentFolder)) {
              return;
            }
          }
        }
      }

      if (jResponse.has("folders")) {
        JSONArray jFolders = jResponse.getJSONArray("folders");
        for (int i = 0; i < jFolders.length(); i++) {
          JSONObject jFolder = jFolders.getJSONObject(i);
          if (jFolder.has("id")) {
            if (!folderListener.onFolder(jFolder.getString("id"))) {
              return;
            }
          }
        }
      }
    } catch (JSONException ex) {
      throw new AgpPublishException("Error processing folder content.", ex);
    }
  }

  /**
   * Gets content URL.
   *
   * @param token token
   * @param currentFolder current folder id
   * @return URL of the content
   * @throws IOException if generating content URL failed.
   */
  private String getContentUrl(String token, String currentFolder) throws IOException {
    return ep.getBaseArcGISUrl()
            + "content/users/"
            + credentialProvider.getUsername() + (currentFolder.length() > 0 ? "/" + currentFolder : "")
            + "?f=pjson&token="
            + URLEncoder.encode(token, "UTF-8");
  }

  private static interface FolderListener {

    boolean onFolder(String folderId) throws AgpServerException, AgpPublishException;
  }

  private static interface ItemListener {

    boolean onItem(String itemId, String folderId);
  }

  private static class FolderInfo {

    public String folderId;
  }

  /**
   * Mapping between document UUIS and the folder.
   */
  public static class UuidToFolderMap extends HashMap<String, String> {
  }
}

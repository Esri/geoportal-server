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
package com.esri.gpt.catalog.arcgis.agportal.publication;

import com.esri.gpt.catalog.arcgis.agportal.itemInfo.ESRI_ItemInformation;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.HttpClientRequest.MethodName;
import com.esri.gpt.framework.http.StringProvider;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used to handle publication request to ArcGIS Portal instance.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 * @deprecated 
 */
@Deprecated
public class PublicationRequest {

  /**
   * instance variables
   * =========================================================
   */
  private CredentialProvider credentialProvider = null;
  private RequestContext requestContext = null;
  private EndPoint ep;
  private String addItemUrl = null;
  private String shareItemUrl = null;
  private String token = null;
  private String referer = null;
  private String itemId = null;
  private boolean publicationStatus = false;
  /** The logger. */
  private static final Logger LOGGER = Logger.getLogger(PublicationRequest.class.getName());

  /**
   * Creates instance of the request.
   * @param requestContext context
   * @param endPoint end-point
   * @param credtialProvider credential provider
   * @param httpReferer referer
   * @throws Exception if publication fails
   */
  public PublicationRequest(RequestContext requestContext, EndPoint endPoint, CredentialProvider credtialProvider, String httpReferer) throws Exception {

    if (credtialProvider == null) {
      throw new IllegalArgumentException("Null credentials provided.");
    }
    if (requestContext == null) {
      throw new IllegalArgumentException(" Null request context provided.");
    }
    if (Val.chkStr(httpReferer).length() == 0) {
      throw new IllegalArgumentException("Null http referer provided.");
    }

    this.credentialProvider = credtialProvider;
    this.requestContext = requestContext;
    this.ep = endPoint;
    this.addItemUrl = this.ep.getBaseArcGISUrl() + "content/users/{0}/addItem";
    this.shareItemUrl = this.ep.getBaseArcGISUrl() + "content/users/{0}/shareItems";
    this.referer = httpReferer;
  }

  /**
   * Constructs a request to publish an itemInfo to ArcGIS Portal instance.
   * 
   * @param requestContext
   *          the request context
   * @param credtialProvider
   *          the CredentialProvider
   * @param httpReferer
   *          the Http Referer
   * @throws Exception
   */
  public PublicationRequest(RequestContext requestContext,
      CredentialProvider credtialProvider, String httpReferer) throws Exception {
    this(requestContext, EndPoint.extract(requestContext), credtialProvider, httpReferer);
  }

  /**
   * methods =================================================================
   */
  
  private boolean hasMetadataViewTag(String str) {
    Pattern p = Pattern.compile("\\>Metadata\\</[aA]\\>[ \t\r\n]*$");
    return p.matcher(str).find();
  }
  
  private String makeMetadataViewTag(String id) {
    String viewUrl = makeMetadataViewUrl(id);
    if (viewUrl.length()>0) {
      return "</br><a href='"+viewUrl+"' target='_blank'>Metadata</a>";
    }
    return "";
  }
  
  private String makeMetadataViewUrl(String id) {
    StringAttributeMap params = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
    String basePath = Val.chkStr(params.getValue("reverseProxy.baseContextPath"));
    if (basePath.length()>0) {
      return basePath + "/catalog/search/resource/details.page?uuid=" + formatUuid(id);
    }
    return "";
  }
  
  
  private String formatUuid(String uuid) {
    uuid = Val.chkStr(uuid);
    String value = uuid.replaceAll("[^0-9a-fA-F]", "");
    if (value.length()==32) {
      StringBuilder sb = new StringBuilder();
      sb.append(value.substring(0, 8));
      sb.append("-");
      sb.append(value.substring(8, 12));
      sb.append("-");
      sb.append(value.substring(12, 16));
      sb.append("-");
      sb.append(value.substring(16, 20));
      sb.append("-");
      sb.append(value.substring(20));
      value = "{"+sb.toString()+"}";
    } else {
      value = uuid;
    }
    return value;
  }
  
  /**
   * Adds item to ArcGIS Portal instance.
   * 
   * @param itemInfo
   *          the ESRI_ItemInformation object to be published.
   * @throws IOException
   * @throws JSONException
   */
  private void addItem(ESRI_ItemInformation itemInfo) throws IOException, JSONException {
    String content = "";
    this.publicationStatus = false;
    if (this.token != null && this.token.length() > 0) {
      this.addItemUrl = this.addItemUrl.replace("{0}",
          this.credentialProvider.getUsername());

      content = "f=json&overwrite=" + "true" + "&token="
          + URLEncoder.encode(this.token, "UTF-8");

      // file
      content += "&URL="
          + URLEncoder.encode(itemInfo.getUrl(), "UTF-8");
      content += "&title=" + URLEncoder.encode(itemInfo.getTitle(), "UTF-8");
      content += "&type=" + URLEncoder.encode(itemInfo.getType(), "UTF-8");
      content += "&tags=" + URLEncoder.encode(itemInfo.getTagsAsString(), "UTF-8");

      if (itemInfo.getTags().isEmpty()) {
        if (itemInfo.getDescription().length() > 0) {
          content += "&tags=" + URLEncoder.encode(itemInfo.getDescription(), "UTF-8");

        } else if (itemInfo.getTitle().length() > 0) {
          content += "&tags=" + URLEncoder.encode(itemInfo.getTitle(), "UTF-8");
        }
      }

      if (itemInfo.getName().length() > 0) {
        content += "&name=" + URLEncoder.encode(itemInfo.getName(), "UTF-8");
      }

      if (itemInfo.getDescription().length() > 0) {
        String viewTag = !hasMetadataViewTag(itemInfo.getDescription())? makeMetadataViewTag(itemInfo.getId()): "";
        content += "&description="
            + URLEncoder.encode(itemInfo.getDescription()+viewTag, "UTF-8");
      } else {
        content += "&description="+URLEncoder.encode(makeMetadataViewTag(itemInfo.getId()),"UTF-8");
      }

      if (itemInfo.getThumbnailUrl().length() > 0) {
        content += "&thumbnailurl="
            + URLEncoder.encode(itemInfo.getThumbnailUrl(), "UTF-8");
      }

      Envelope extent = itemInfo.getExtent();
      if (extent != null && !extent.isEmpty()) {
        content += "&extent=" + extent.getMinX() + "," + extent.getMinY() + ","
            + extent.getMaxX() + "," + extent.getMaxY();
      }
      HttpClientRequest httpClient = new HttpClientRequest();
      httpClient.setContentProvider(new StringProvider(content,
          "application/x-www-form-urlencoded"));
      httpClient.setUrl(this.addItemUrl);
      httpClient.setRequestHeader("Referer", this.referer);
      httpClient.setMethodName(MethodName.POST);
      httpClient.execute();

      int nHttpResponseCode = httpClient.getResponseInfo().getResponseCode();
      if ((nHttpResponseCode < 200) || (nHttpResponseCode > 299)) {
        throw new JSONException("Add item request failed: HTTP "
            + nHttpResponseCode);
      }

      String resp = httpClient.readResponseAsCharacters();
      if (resp.length() > 0) {
        JSONObject jsoResponse;
        try {
          jsoResponse = new JSONObject(resp);
        } catch (JSONException e) {
          throw new JSONException(
              "Error occurred while trying publish item, cause :"
              + e.getMessage());
        }

        if (jsoResponse.has("id") && jsoResponse.has("success")
            && jsoResponse.getString("success").equalsIgnoreCase("true")) {
          this.itemId = jsoResponse.getString("id");
          this.publicationStatus = true;
        }
      }

    }
  }

  /**
   * Fetch ArcGIS portal token.
   * @throws IOException 
   * @throws JSONException 
   */
  private void fetchToken() throws IOException, JSONException {
    String content = "";
    this.publicationStatus = false;

    content = URLEncoder.encode("f", "UTF-8") + "="
        + URLEncoder.encode("json", "UTF-8") + "&"
        + URLEncoder.encode("username", "UTF-8") + "="
        + URLEncoder.encode(this.credentialProvider.getUsername(), "UTF-8")
        + "&" + URLEncoder.encode("password", "UTF-8") + "="
        + URLEncoder.encode(this.credentialProvider.getPassword(), "UTF-8");

    HttpClientRequest httpClient = new HttpClientRequest();
    // send the request
    content += "&expiration=525600&referer=" + this.referer;

    httpClient.setContentProvider(new StringProvider(content,
        "application/x-www-form-urlencoded"));

    httpClient.setRequestHeader("Referer", this.referer);
    httpClient.setUrl(this.ep.getGenerateTokenUrl());
    httpClient.setMethodName(MethodName.POST);

    // expiration
    httpClient.execute();

    int nHttpResponseCode = httpClient.getResponseInfo().getResponseCode();
    if ((nHttpResponseCode < 200) || (nHttpResponseCode > 299)) {
      throw new IOException("Request failed: HTTP " + nHttpResponseCode);
    }

    String resp = httpClient.readResponseAsCharacters();
    JSONObject jsoToken = new JSONObject(resp);
    if (jsoToken.has("token")) {
      this.token = jsoToken.getString("token");
      this.publicationStatus = true;
    } else {
      throw new IOException("Failed to generate token");
    }
  }

  /**
   * Processes the ArcGIS Portal publication request.
   * 
   * @param itemInfo
   *          the ESRI_ItemInformation object.
   * @throws Exception
   * @throws IOException
   * @throws JSONException
   */
  public boolean publish(ESRI_ItemInformation itemInfo) throws IOException, JSONException, Exception {
    this.publicationStatus = false;
    try {
      if (itemInfo == null) {
        throw new Exception("ItemInfo is null.");
      } else {
        fetchToken();
        if (this.publicationStatus) {
          addItem(itemInfo);
          if (this.publicationStatus) {
            shareItem(itemInfo);
          }
        }
      }
    } finally {
      if (this.requestContext != null) {
        this.requestContext.onExecutionPhaseCompleted();
      }
    }
    return this.publicationStatus;
  }

  /**
   * Sets access right in ArcGIS Portal for item.
   * 
   * @param itemInfo
   * @throws Exception
   */
  private void shareItem(ESRI_ItemInformation itemInfo) throws Exception {
    String content = "";
    this.publicationStatus = false;
    if (this.token != null && this.token.length() > 0) {
        if (itemId != null && itemId.length() > 0) {
          this.shareItemUrl = shareItemUrl.replace("{0}",
              this.credentialProvider.getUsername());
          content = "f=json&token=" + URLEncoder.encode(this.token, "UTF-8");
          content += "&everyone=true&items=" + this.itemId;
          HttpClientRequest httpClient = new HttpClientRequest();
          httpClient.setContentProvider(new StringProvider(content,
              "application/x-www-form-urlencoded"));
          httpClient.setRequestHeader("Referer", this.referer);
          httpClient.setUrl(this.shareItemUrl);
          httpClient.setMethodName(MethodName.POST);
          httpClient.execute();

          int nHttpResponseCode = httpClient.getResponseInfo().getResponseCode();
          if ((nHttpResponseCode < 200) || (nHttpResponseCode > 299)) {
            LOGGER.info("Share item request failed: HTTP " + nHttpResponseCode);
          } else {
            this.publicationStatus = true;
            LOGGER.info("Item shared with everyone." + nHttpResponseCode);
          }

        }
    }
  }
}

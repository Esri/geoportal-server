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
package com.esri.gpt.control.livedata;

import com.esri.gpt.control.livedata.selector.HttpRequestDefinition;
import com.esri.gpt.control.livedata.selector.IHttpResponseListener;
import com.esri.gpt.control.livedata.selector.IRegistry;
import com.esri.gpt.control.livedata.selector.ISetter;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.http.ResponseInfo;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;

/**
 * ArcGISRendererFactory
 */
/*packge*/ class ArcGISRendererFactory extends MapBasedRendererFactory {

  /**
   * Creates instance of the factory.
   * @param properties properties
   */
  public ArcGISRendererFactory(ILiveDataProperties properties) {
    super(properties);
  }

  @Override
  public boolean isDefinitive() {
    return true;
  }

  @Override
  public void register(IRegistry reg, final ISetter setter, final String url) {
    try {
      URL urlObj = new URL(url);
      // get just protocola name, host name and port number
      String fullHost = urlObj.getProtocol() + "://" + urlObj.getHost() + (urlObj.getPort() >= 0 && urlObj.getPort() != urlObj.getDefaultPort() ? ":" + urlObj.getPort() : "");
      // get path and find deployment context
      String path = urlObj.getPath();
      String ctx = "";
      Pattern ctxPattern = Pattern.compile("^/\\p{Alnum}+/");
      Matcher ctxMatcher = ctxPattern.matcher(path);
      if (ctxMatcher.find()) {
        ctx = path.substring(ctxMatcher.start(), ctxMatcher.end() - 1);
        path = path.substring(ctxMatcher.end() - 1);
      }
      String restServices = "";
      if (path.startsWith("/rest/services/")) {
        path = path.substring("/rest/services/".length() - 1);
        restServices = "/rest/services";
      } else if (path.startsWith("/services/")) {
        path = path.substring("/services/".length() - 1);
        restServices = "/services";
      }
      // create rest URL
      final String restUrl = fullHost + ctx + restServices + path + "?f=json";
      reg.register(new HttpRequestDefinition(restUrl), new IHttpResponseListener() {

        public void onResponse(ResponseInfo info, String strContent, Document docContent) {

          // extract singleFusedMapCache flag
          String singleFusedMapCache = readTopParam(strContent, "singleFusedMapCache");

          // extract service type; for image service it will be something beginning with "esriImageService"
          String serviceType = readTopParam(strContent, "serviceDataType");

          // it has to be at least either singleFusedMapCache or serviceType...
          if (singleFusedMapCache.length() != 0 || serviceType.length() != 0) {

            // extract envelope; depending on the service it ight be named "initialExtent" or just "extent"
            Envelope extent = readExtent(strContent, "initialExtent");
            if (extent == null) {
              extent = readExtent(strContent, "extent");
            }

            // prepare final values to create renderer
            final Envelope finalExtent = extent;
            final boolean isImageService = serviceType.startsWith("esriImageService");

            setter.set(new ArcGISRenderer() {

              @Override
              protected Envelope getExtent() {
                return finalExtent;
              }

              @Override
              protected String getUrl() {
                return restUrl;
              }

              @Override
              protected boolean isImageService() {
                return isImageService;
              }

              @Override
              protected int getMapHeightAdjustment() {
                return getProperties().getMapHeightAdjustment();
              }
            });
          }
        }
      });

    } catch (MalformedURLException ex) {
      Logger.getLogger(ArcGISRendererFactory.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Reads top-level parameter.
   * @param responseStream JSON response stream
   * @param topParamName top-level parameter name
   * @return value of the top-level parameter name or empty string if no top level parameter found
   */
  private String readTopParam(String responseStream, String topParamName) {
    JSONTokener tokener = new JSONTokener(responseStream);

    try {
      if (tokener.more()) {
        Object obj = tokener.nextValue();
        if (obj instanceof JSONObject) {
          JSONObject jObj = (JSONObject) obj;
          return Val.chkStr(jObj.getString(topParamName));
        }
      }
    } catch (JSONException ex) {
      // invalid JSON response
      return "";
    }

    return "";
  }

  /**
   * Reads extent from the JSON response.
   * @param responseStream response stream
   * @param extentName extent name
   * @return envelope or <code>null</code> if requested envelope not found
   * @throws org.json.JSONException if parsing JSON response failed
   */
  private Envelope readExtent(String responseStream, String extentName) {
    try {
      JSONObject jExt = findObject(responseStream, extentName);
      if (jExt != null) {
        double xmin = jExt.getDouble("xmin");
        double ymin = jExt.getDouble("ymin");
        double xmax = jExt.getDouble("xmax");
        double ymax = jExt.getDouble("ymax");
        JSONObject jSpatialRef = jExt.getJSONObject("spatialReference");
        int wkid = jSpatialRef.getInt("wkid");

        Envelope envelope = new Envelope(xmin, ymin, xmax, ymax);
        envelope.setWkid(Integer.toString(wkid));

        return envelope;
      }
    } catch (JSONException ex) {
      // invalid JSON response
    }

    return null;
  }

  /**
   * Finds named JSON object in the JSON response.
   * @param responseStream response stream
   * @param objectName object name
   * @return envelope or <code>null</code> if requested object not found
   * @throws org.json.JSONException if parsing JSON response failed
   */
  private JSONObject findObject(String responseStream, String objectName) throws JSONException {
    InputStreamReader reader = null;
    try {
      JSONTokener tokener = new JSONTokener(responseStream);

      while (tokener.more()) {
        Object obj = tokener.nextValue();
        if (obj instanceof JSONObject) {
          JSONObject jObj = (JSONObject) obj;
          try {
            JSONObject jNamedObject = jObj.getJSONObject(objectName);
            if (jNamedObject != null)
              return jNamedObject;
          } catch (JSONException ex) {
            // no extent in the current node; advance to the next node
          }
        }
      }
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
        }
      }
    }

    return null;
  }
}

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
package com.esri.gpt.control.download;

import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Map;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

/**
 * Download controller.
 */
public class DownloadController extends BaseActionListener {

/** email key */
private static final String KEY_EMAIL = "email";
/** map service url key */
private static final String KEY_MAPSERVICEURL = "mapServiceUrl";
/** extract data task url key */
private static final String KEY_EXTRACTDATATASKURL = "extractDataTaskUrl";
/** format key */
private static final String KEY_FORMAT = "format";
/** projection key */
private static final String KEY_PROJECTION = "projection";
/** raster key */
private static final String KEY_RASTER = "raster";
/** feature key */
private static final String KEY_FEATURE = "feature";
/** layers */
private static final String KEY_LAYERS = "layers";
/** User email if any. */
private String _userEmail = "";
/** Map service url */
private String _mapServiceUrl = "";
/** Geoprocessing task url */
private String _extractDataTaskUrl = "";
/** pre-selected layers */
private String _layers = "";
/** pre-selected format */
private String _selectedFormat = "";
/** pre-selected projection */
private String _selectedProjection = "";
/** pre-selected raster */
private String _selectedRaster = "";
/** pre-selected feature */
private String _selectedFeature = "";

/**
 * Prepares page to display list of harvest repositories.
 * @return empty string
 */
public String getDownloadView() {

  try {
    // start view preparation phase
    RequestContext context = onPrepareViewStarted();
    HttpServletRequest request =
      new FacesContextBroker().extractHttpServletRequest();

    // get donwload config
    DownloadConfiguration cfg = 
      com.esri.gpt.framework.context.RequestContext.extract(request).
      getApplicationConfiguration().getDownloadDataConfiguration();

    // get user email address
    setUserEmail(getParameterByKey(request, KEY_EMAIL));
    if (getUserEmail().length() == 0) {
      User user = context.getUser();
      setUserEmail(user != null ? user.getProfile().getEmailAddress() : "");
    }

    // get map service url
    setMapServiceUrl(getParameterByKey(request, KEY_MAPSERVICEURL));

    if (getMapServiceUrl().length() == 0) {
      setMapServiceUrl(cfg.getMapServiceUrl());
    }

    // get extract data geoprocessing task url
    setExtractDataTaskUrl(getParameterByKey(request, KEY_EXTRACTDATATASKURL));

    if (getExtractDataTaskUrl().length() == 0) {
      setExtractDataTaskUrl(cfg.getTaskUrl());
    }

    /** extract format */
    String sFormat = getParameterByKey(request, KEY_FORMAT);
    if (sFormat.length() > 0) {
      setSelectedOutputFormat(sFormat);
    }

    /** extract projection */
    String sProjection = getParameterByKey(request, KEY_PROJECTION);
    if (sProjection.length() > 0) {
      setSelectedProjection(sProjection);
    }

    /** extract feature */
    String sFeature = getParameterByKey(request, KEY_FEATURE);
    if (sFeature.length() > 0) {
      setSelectedFeature(sFeature);
    }

    /** extract raster */
    String sRaster = getParameterByKey(request, KEY_RASTER);
    if (sRaster.length() > 0) {
      setSelectedRaster(sRaster);
    }
    
    /** extract preselected layers */
    String sLayers = getParameterByKey(request, KEY_LAYERS);
    if (sLayers.length()>0) {
      setLayers(sLayers);
    }

  } catch (Throwable t) {
    handleException(t);
  } finally {
    onPrepareViewCompleted();
  }

  return "";
}

/**
 * Gets user email.
 * @return user email
 */
public String getUserEmail() {
  return _userEmail;
}

/**
 * Sets user email.
 * @param userEmail user email
 */
public void setUserEmail(String userEmail) {
  _userEmail = Val.chkStr(userEmail);
}

/**
 * Gets all output formats.
 * @return array of select items representing output formats
 */
public ArrayList<SelectItem> getAllOutputFormats() {
  return getAllSelectItems(getConfiguration().getOutputFormatInfo());
}

/**
 * Gets selected output format.
 * @return select item representing output format
 */
public String getSelectedOutputFormat() {
  return _selectedFormat;
}

/**
 * Sets selected output format.
 * @param item select item representing output format
 */
public void setSelectedOutputFormat(String item) {
  ItemInfo ii = getItem(getConfiguration().getOutputFormatInfo(), item);
  _selectedFormat = ii!=null? ii.getKey(): "";
}

/**
 * Gets all projections.
 * @return array of select items representing projections
 */
public ArrayList<SelectItem> getAllProjections() {
  return getAllSelectItems(getConfiguration().getProjectionInfo());
}

/**
 * Gets selected projection.
 * @return select item representing projection
 */
public String getSelectedProjection() {
  return _selectedProjection;
}

/**
 * Sets selected projection.
 * @param item select item representing projection
 */
public void setSelectedProjection(String item) {
  ItemInfo ii = getItem(getConfiguration().getProjectionInfo(), item);
  _selectedProjection = ii!=null? ii.getKey(): "";
}

/**
 * Gets all features.
 * @return features
 */
public ArrayList<SelectItem> getAllFeatures() {
  return getAllSelectItems(getConfiguration().getFeatureFormatInfo());
}

/**
 * Gets selected feature.
 * @return selected feature
 */
public String getSelectedFeature() {
  return _selectedFeature;
}

/**
 * Sets selected feature.
 * @param item selected feature
 */
public void setSelectedFeature(String item) {
  ItemInfo ii = getItem(getConfiguration().getFeatureFormatInfo(), item);
  _selectedFeature = ii!=null? ii.getKey(): "";
}

/**
 * Gets all rasters.
 * @return all rasters
 */
public ArrayList<SelectItem> getAllRasters() {
  return getAllSelectItems(getConfiguration().getRasterFormatInfo());
}

/**
 * Gets selected raster.
 * @return selected raster
 */
public String getSelectedRaster() {
  return _selectedRaster;
}

/**
 * Sets selected raster.
 * @param item selected raster
 */
public void setSelectedRaster(String item) {
  ItemInfo ii = getItem(getConfiguration().getRasterFormatInfo(), item);
  _selectedRaster = ii!=null? ii.getKey(): "";
}

/**
 * Gets map service URL.
 * @return map service URL
 */
public String getMapServiceUrl() {
  return _mapServiceUrl;
}

/**
 * Sets map service URL.
 * @param mapServiceUrl map service URL
 */
public void setMapServiceUrl(String mapServiceUrl) {
  _mapServiceUrl = Val.chkStr(mapServiceUrl);
}

/**
 * Gets geoprocessing task URL.
 * @return geoprocessing task URL
 */
public String getExtractDataTaskUrl() {
  return _extractDataTaskUrl;
}

/**
 * Sets geoprocessing task URL.
 * @param url geoprocessing task URL
 */
public void setExtractDataTaskUrl(String url) {
  _extractDataTaskUrl = Val.chkStr(url);
}

/**
 * Gets pre-layers.
 * @return layers indexes separated by coma (,)
 */
public String getLayers() {
  return _layers;
}

/**
 * Sets pre-selected layers.
 * @param layers layers indexes separated by coma (,)
 */
public void setLayers(String layers) {
  String [] indexes = layers.split(",");
  StringBuilder sb = new StringBuilder();
  for (String idx : indexes) {
    try {
      int index = Integer.parseInt(Val.chkStr(idx));
      if (sb.length()>0) {
        sb.append(",");
      }
      sb.append(Integer.toString(index));
    } catch (NumberFormatException ex) {
      
    }
  }
  _layers = sb.toString();
}

/**
 * Gets item by name.
 * @param itemInfos array of infos
 * @param itemName item name
 * @return rquested item or <ocde>null</code> if no item found
 */
private ItemInfo getItem(ArrayList<ItemInfo> itemInfos, String itemName) {
  itemName = Val.chkStr(itemName);
  for (ItemInfo ii : itemInfos) {
    if (ii.getKey().equalsIgnoreCase(itemName) || 
        ii.getAlias().equalsIgnoreCase(itemName)) {
      return ii;
    }
  }
  return null;
}
/**
 * Gets all select items.
 * @param itemInfos array of item infos
 * @return array of select items
 */
private ArrayList<SelectItem> getAllSelectItems(ArrayList<ItemInfo> itemInfos) {
  MessageBroker mb = new FacesContextBroker().extractMessageBroker();
  ArrayList<SelectItem> selectItems = new ArrayList<SelectItem>();
  for (ItemInfo ii : itemInfos) {
    selectItems.add(
      new SelectItem(ii.getKey(), mb.retrieveMessage(ii.getResourceKey())));
  }
  return selectItems;
}

/**
 * Gets download configuration.
 * @return download configuration
 */
private DownloadConfiguration getConfiguration() {
  ApplicationContext appCtx = ApplicationContext.getInstance();
  ApplicationConfiguration appCfg = appCtx.getConfiguration();
  return appCfg.getDownloadDataConfiguration();
}

/**
 * Gets parameter value.
 * @param request HTTP request
 * @param parameterKey parameter key
 * @return parameter name
 */
private String getParameterByKey(HttpServletRequest request,
                                 String parameterKey) {
  Map<String, String[]> parMap = request.getParameterMap();
  for (Map.Entry<String, String[]> e : parMap.entrySet()) {
    if (e.getKey().equalsIgnoreCase(parameterKey)) {
      if (e.getValue().length > 0) {
        return Val.chkStr(e.getValue()[0]);
      } else {
        return "";
      }
    }
  }
  return "";
}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.control.georss;

import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.http.HttpClientException;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Geometry service.
 */
public class GeometryService {
  private String geometryServiceURL;
  
  /**
   * Creates instance of the service object.
   * @param URL URL to the service
   */
  public GeometryService(String URL) {
    this.geometryServiceURL = URL;
  }
  
  /**
   * Creates instance as configured in gpt.xml.
   * @return instance of the service
   */
  public static GeometryService createDefaultInstance() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    return new GeometryService(appCfg.getInteractiveMap().getGeometryServiceUrl());
  }
  
  /**
   * Performs projection.
   * @param envelopes list of envelopes to project
   * @param outSR output spatial reference (WKID)
   * @return list of projected envelopes
   * @throws JSONException if reading response as JSON fails
   * @throws HttpClientException if HTTP communication fails
   * @throws IOException if data transmission fails
   */
  public List<Envelope> project(List<Envelope> envelopes, String outSR) throws JSONException, HttpClientException, IOException {
    Envelope [] envArray = new Envelope[envelopes.size()];
    
    Map<String,List<Envelope>> envMap = createEnvMap(envelopes, "4326");
    
    for (Map.Entry<String,List<Envelope>> e: envMap.entrySet()) {
      List<Envelope> inEnvs = e.getValue();
      List<Envelope> outEnvs = inEnvs;
      if (!e.getKey().equals(outSR)) {
        outEnvs = project(inEnvs, e.getKey(), outSR);
      }
      for (int i=0; i<outEnvs.size() && i<inEnvs.size(); i++) {
        int index = envelopes.indexOf(inEnvs.get(i));
        if (index>=0) {
          envArray[index] = outEnvs.get(i);
        }
      }
    }
    
    
    for (int i=0; i<envArray.length; i++) {
      if (envArray[i]==null) {
        envArray[i] = envelopes.get(i);
      }
    }
    
    return Arrays.asList(envArray);
  }
  
  /**
   * Performs projection.
   * @param inEnv list of envelopes
   * @param inSR input spatial reference (WKID)
   * @param outSR output spatial reference (WKID)
   * @return list of projected envelopes
   * @throws JSONException if reading response as JSON fails
   * @throws HttpClientException if HTTP communication fails
   * @throws IOException if data transmission fails
   */
  protected List<Envelope> project(List<Envelope> inEnv, String inSR, String outSR) throws JSONException, HttpClientException, IOException {
    List<Envelope> outEnv = new ArrayList<Envelope>();
    
    String geometries = envToJson(inEnv, inSR);
    
    StringBuilder params = new StringBuilder();
    params.append("f=").append("json");
    params.append("&geometries=").append(enc(geometries));
    params.append("&inSR=").append(inSR);
    params.append("&outSR=").append(outSR);
    
    HttpClientRequest request = new HttpClientRequest();
    request.setUrl(geometryServiceURL+"/project?"+params);
    StringHandler handler = new StringHandler();
    request.setContentHandler(handler);
    request.execute();

    JSONObject response = new JSONObject(handler.getContent());
    if (response.has("error")) {
      JSONObject error = response.getJSONObject("error");
      int code = error.getInt("code");
      String message = error.getString("message");
      throw new HttpClientException(code, message);
    }
    
    if (response.has("geometries")) {
      JSONArray geoms = response.getJSONArray("geometries");
      for (int i=0; i<geoms.length(); i++) {
        JSONObject geom = geoms.getJSONObject(i);
        outEnv.add(readEnvelope(geom));
      }
    }
    
    return outEnv;
  }
  
  /**
   * Read envelope from the JSON object.
   * @param geom JSON object representing envelope
   * @return envelope
   * @throws JSONException if input object is not a valid envelope
   */
  protected Envelope readEnvelope(JSONObject geom) throws JSONException {
    double xmin = geom.getDouble("xmin");
    double ymin = geom.getDouble("ymin");
    double xmax = geom.getDouble("xmax");
    double ymax = geom.getDouble("ymax");
    
    Envelope env = new Envelope(xmin, ymin, xmax, ymax);
    
    if (geom.has("spatialReference")) {
      JSONObject sr = geom.getJSONObject("spatialReference");
      String wkid = sr.getString("wkid");
      env.setWkid(wkid);
    }
    
    return env;
  }
  
  /**
   * Creates WKID to envelope mapping.
   * @param envelopes envelopes
   * @param defWkid default WKID
   * @return mapping
   */
  protected Map<String,List<Envelope>> createEnvMap(List<Envelope> envelopes, String defWkid) {
    Map<String,List<Envelope>> envMap = new HashMap<String, List<Envelope>>();
    for (Envelope env: envelopes) {
      String wkid = Val.chkStr(env.getWkid(), defWkid);
      List<Envelope> envList = envMap.get(wkid);
      if (envList==null) {
        envList = new ArrayList<Envelope>();
        envMap.put(wkid, envList);
      }
      envList.add(env);
    }
    return envMap;
  }
  
  /**
   * Turns list of envelopes into a JSON object.
   * @param envList list of envelopes
   * @param wkid WKID
   * @return string representation of JSON object
   */
  protected String envToJson(List<Envelope> envList, String wkid) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"geometryType\":\"esriGeometryEnvelope\",");
    sb.append("\"geometries\":");
    sb.append("[");
    
    StringBuilder sbEnv = new StringBuilder();
    for (Envelope env: envList) {
      if (sbEnv.length()>0) {
        sbEnv.append(",");
      }
      sbEnv.append(envToJson(env, wkid));
    }
    sb.append(sbEnv);
    
    sb.append("]");
    sb.append("}");
    return sb.toString();
  }
  
  /**
   * Turns a single envelope into a JSON object.
   * @param env envelope
   * @param wkid WKID
   * @return string representation of JSON object
   */
  protected String envToJson(Envelope env, String wkid) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"xmin\":").append(env.getMinX()).append(",");
    sb.append("\"ymin\":").append(env.getMinY()).append(",");
    sb.append("\"xmax\":").append(env.getMaxX()).append(",");
    sb.append("\"ymax\":").append(env.getMaxY()).append(",");
    sb.append("\"spatialReference\": { \"wkid\":").append(wkid).append("}");
    sb.append("}");
    return sb.toString();
  }
  
  /**
   * Encodes a string.
   * @param str string to encode
   * @return encoded string
   */
  protected String enc(String str) {
    try {
      return URLEncoder.encode(str, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return str;
    }
  }
}

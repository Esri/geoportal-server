/* Licensed under the Apache License, Version 2.0 (the "License");
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

import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.HttpClientRequest.MethodName;
import com.esri.gpt.framework.http.*;
import com.esri.gpt.framework.http.multipart.MultiPartContentProvider;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Base ArcGIS Portal request.
 */
public abstract class AgpBaseRequest {

  /** temporary storage file prefix */
  private static final String PREFIX = "file";
  /** temporary storage file suffix */
  private static final String SUFFIX = ".data";
  /** default sub-folder for the intermediate report files */
  private static final String SUBFOLDER = "/geoportal/downloads";
  
  protected static XsltTemplates XSLTTEMPLATES = new XsltTemplates();
  protected CredentialProvider credentialProvider = null;
  protected RequestContext requestContext = null;
  protected EndPoint ep;

  /**
   * Creates instance of the request.
   *
   * @param requestContext request context
   * @param endPoint ArcGIS Portal end point
   * @param credtialProvider credential provider
   */
  public AgpBaseRequest(RequestContext requestContext, CredentialProvider credtialProvider, EndPoint endPoint) {
    if (credtialProvider == null) {
      throw new IllegalArgumentException("Null credentials provided.");
    }
    if (requestContext == null) {
      throw new IllegalArgumentException("Null request context provided.");
    }
    if (endPoint == null) {
      throw new IllegalArgumentException("Null end point provided.");
    }

    this.credentialProvider = credtialProvider;
    this.requestContext = requestContext;
    this.ep = endPoint;
  }

  /**
   * Fetch ArcGIS portal token.
   * @throws IOException if communication with the server fails
   * @throws JSONException if unable to parse server response
   */
  protected String fetchToken() throws AgpServerException, AgpPublishException {
    try {
      String content = URLEncoder.encode("f", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8") + "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(this.credentialProvider.getUsername(), "UTF-8") + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(this.credentialProvider.getPassword(), "UTF-8");
      StringHandler handler = new StringHandler();
      HttpClientRequest httpClient = new HttpClientRequest();
      // send the request
      content += "&expiration=525600&referer=" + ep.getReferer();
      httpClient.setContentProvider(new StringProvider(content, "application/x-www-form-urlencoded"));
      httpClient.setContentHandler(handler);
      httpClient.setRequestHeader("Referer", ep.getReferer());
      httpClient.setUrl(this.ep.getGenerateTokenUrl());
      httpClient.setMethodName(MethodName.POST);

      // execute
      execute(httpClient);
      checkError(handler.getContent());

      String resp = handler.getContent();
      JSONObject jResp = new JSONObject(resp);
      return jResp.getString("token");
    } catch (AgpServerException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new AgpPublishException("Error fetching token.", ex);
    }
  }

  /**
   * Executes HTTP request.
   * @param request HTTP client request
   * @throws AgpServerException server exception
   * @throws AgpPublishException publish exception
   */
  protected void execute(HttpClientRequest request) throws AgpServerException, AgpPublishException {
    try {
      request.execute();
      int nHttpResponseCode = request.getResponseInfo().getResponseCode();
      if ((nHttpResponseCode < 200) || (nHttpResponseCode > 299)) {
        throw new AgpServerException(nHttpResponseCode, "Error accessing ArcGIS Portal.");
      }
    } catch (AgpServerException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new AgpPublishException("Error executing request.", ex);
    }
  }

  /**
   * Extracts credential provider for ArcGIS Portal from configuration via
   * context.
   *
   * @param ctx request context
   * @return credential provider
   */
  protected static CredentialProvider extractCredentialProvider(RequestContext ctx) {
    StringAttributeMap params = ctx.getCatalogConfiguration().getParameters();
    return new CredentialProvider(
            params.get("share.with.arcgis.username").getValue(),
            params.get("share.with.arcgis.password").getValue());
  }

  /**
   * Extracts ESRI Item Information from metadata.
   *
   * @param metadata metadata
   * @return DOM node of ESRI Item Information
   * @throws AgpPublishException if extracting ESRI Item Information fails
   */
  protected Node extractItemInfo(String metadata) throws AgpPublishException {
    try {
      // find the right schema
      MetadataDocument document = new MetadataDocument();
      Schema schema = document.prepareForView(requestContext, metadata);

      // get transformation file
      String toEsriItemInfoXslt = schema.getToEsriItemInfoXslt();
      if (toEsriItemInfoXslt.isEmpty()) {
        throw new AgpPublishException("Schema: " + schema.getKey() + " has no transformation to ItemInformation.");
      }

      // run the validation xsl
      XsltTemplate template = this.getCompiledTemplate(toEsriItemInfoXslt);
      String result = template.transform(metadata);

      // load the result SVRL document
      Document dom = DomUtil.makeDomFromString(result, true);

      // find ESRI item information
      XPath xPath = XPathFactory.newInstance().newXPath();
      return (Node) xPath.evaluate("ESRI_ItemInformation", dom, XPathConstants.NODE);
      
    } catch (AgpPublishException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new AgpPublishException("Error processing metadata.", ex);
    }

  }
  
  /**
   * Extracts ESRI item attributes.
   * @param esriItemInfo DOM node with ESRI item info
   * @return map of attributes
   */
  protected Map<String, List<String>> extractEsriItemAttributes(Node esriItemInfo) {
    AttrMap attributes = new AttrMap();
    ItemDesc itemDesc = null;
    // go through each child of ESRI item info
    for (Node nd : new NodeListAdapter(esriItemInfo.getChildNodes())) {
      // get name and value of the attribute
      String name = Val.chkStr(nd.getNodeName());
      String value = Val.chkStr(nd.getTextContent());
      
      if (!name.isEmpty() && !value.isEmpty()) {
        // if the attribute is url check a type of that url
        if ("url".equals(name)) {
          for (ItemEntry ie : itemEntries) {
            if (ie.matches(value)) {
              itemDesc = ie.getItemDesc();
              break;
            }
          }
          // if the type is downloadable, put 'file' attribute instead of 'url'
          if (itemDesc != null && itemDesc.getItemType() == ItemType.file) {
            attributes.put("file", value);
          } else {
            attributes.put(name, value);
          }
        } else {
          attributes.put(name, value);
        }
      }
    }
    // if 'type' attribute not found in the ESRI item information (which will be
    // the most common case), set that attribute from the item description
    if (itemDesc != null && !attributes.containsKey("type")) {
      attributes.put("type", itemDesc.getType());
    }
    if (itemDesc != null && !attributes.containsKey("typekeywords")) {
      attributes.put("typekeywords", itemDesc.getTypeKeywords());
    }
    return attributes;
  }

  /**
   * Process attributes.
   * @param provider
   * @param attributes
   * @throws AgpPublishException 
   */
  protected void processEsriItemAttributes(MultiPartContentProvider provider, Map<String, List<String>> attributes) throws AgpPublishException {
    try {
      // go through each attribute and add it to the multi-part provider
      for (Map.Entry<String, List<String>> e : attributes.entrySet()) {
        // for 'file' though download that file and push to the request
        if (!e.getValue().isEmpty()) {
          if ("file".equals(e.getKey())) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            StreamHandler handler = new StreamHandler(output);
            HttpClientRequest request = new HttpClientRequest();
            request.setUrl(e.getValue().get(0));
            request.setContentHandler(handler);
            request.execute();
            // two versions

            // 'in memory' version
            //provider.add("file", output.toByteArray(), URLDecoder.decode(getFileName(value), "UTF-8"), null, null);

            // 'on disk' version
            File tempFile = createTempFile();
            saveBytesToFile(tempFile, output.toByteArray());
            provider.add("file", tempFile, URLDecoder.decode(getFileName(e.getValue().get(0)), "UTF-8"), null, null, true);

            // possible third version operating directly on a stream
          } else {
            provider.add(e.getKey(), e.getValue().get(0));
          }
        }
      }
    } catch (IOException ex) {
      throw new AgpPublishException("Error processing item information.", ex);
    }
  }

  /**
   * Gets file name from the URL.
   * @param url URL
   * @return file name
   * @throws IOException if getting file name fails
   */
  private String getFileName(String url) throws IOException {
    URL u = new URL(url);
    File f = new File(u.getPath());
    return f.getName();
  }

  /**
   * Creates temporary file to store downloaded data.
   * @return file
   * @throws IOException if creating file fails
   */
  private File createTempFile() throws IOException {
    File directory = new File(System.getProperty("java.io.tmpdir") + SUBFOLDER);
    directory.mkdirs();
    File storage = File.createTempFile(PREFIX, SUFFIX, directory);
    return storage;
  }

  /**
   * Saves downloaded bytes to the temporary file.
   * @param file file
   * @param bytes bytes
   * @throws IOException if saving bytes fails
   */
  private void saveBytesToFile(File file, byte[] bytes) throws IOException {
    OutputStream output = null;
    try {
      output = new FileOutputStream(file);
      output.write(bytes);
      output.flush();
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException ex) {
        }
      }
    }
  }

  /**
   * Checks response for errors.
   *
   * @param response response
   * @throws IOException if error found in the response
   */
  protected void checkError(String response) throws AgpServerException {
    try {
      JSONObject jo = new JSONObject(response);
      if (jo.has("error")) {
        JSONObject error = jo.getJSONObject("error");
        String code = error.getString("code");
        String message = error.getString("message");
        throw new AgpServerException(Val.chkInt(code, -1), message);
      }
    } catch (JSONException ex) {
      // no error in the response; do not throw anything
    }
  }

  /**
   * Gets newly created document response
   *
   * @param response response
   * @return id
   */
  protected String extractId(String response) throws JSONException {
    JSONObject jo = new JSONObject(response);
    return jo.getString("id");
  }

  /**
   * Gets a compiled XSLT template.
   *
   * @param xsltPath the path to an XSLT
   * @return the compiled template
   * @throws IOException if an IO exception occurs
   * @throws TransformerException if a transformation exception occurs
   */
  protected synchronized XsltTemplate getCompiledTemplate(String xsltPath) throws TransformerException {
    String sKey = xsltPath;
    XsltTemplate template = XSLTTEMPLATES.get(sKey);
    if (template == null) {
      template = XsltTemplate.makeTemplate(xsltPath);
      XSLTTEMPLATES.put(sKey, template);
    }
    return template;
  }

  /**
   * Gets folder URL.
   * @param folderId folder id
   * @return folder URL
   */
  protected String getFolderUrl(String folderId) {
    folderId = Val.chkStr(folderId);
    return ep.getBaseArcGISUrl() + "/content/users/" + credentialProvider.getUsername() + (!folderId.isEmpty() ? "/" + folderId : "");
  }

  /**
   * Gets item URL.
   * @param folderId folder name
   * @param itemId item id
   * @return item URL
   */
  protected String getItemUrl(String folderId, String itemId) {
    return getFolderUrl(folderId) + "/items/" + Val.chkStr(itemId);
  }
  
  /**
   * Known item entries
   */
  private static final ArrayList<ItemEntry> itemEntries = new ArrayList<ItemEntry>();

  static {
    itemEntries.add(new ItemEntry(".*mapserver.*", new ItemDesc("Map Service", ItemType.url, "Data, Service, Map Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*imageserver.*", new ItemDesc("Image Service", ItemType.url, "Data, Service, Image Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*globeserver.*", new ItemDesc("Globe Service", ItemType.url, "Data, Service, Globe Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*gpserver.*", new ItemDesc("Geoprocessing Service", ItemType.url, "Tool, Service, Geoprocessing Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*geocodeserver.*", new ItemDesc("Geocoding Service", ItemType.url, "Tool, Service, Geocoding Service, Locator Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*geometryserver.*", new ItemDesc("Geometry Service", ItemType.url, "Tool, Service, Geometry Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*networkserver.*", new ItemDesc("Network Analysis Service", ItemType.url, "Tool, Service, Network Analysis Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*geodataserver.*", new ItemDesc("Geodata Service", ItemType.url, "Data, Service, Geodata Service, ArcGIS Server")));
    itemEntries.add(new ItemEntry(".*service=wms.*|.*wmsserver.*", new ItemDesc("WMS", ItemType.url, "Web Map Service")));
    itemEntries.add(new ItemEntry(".*service=wmts.*|.*wmtsserver.*", new ItemDesc("WMTS", ItemType.url, "Web Map Tile Service")));
    itemEntries.add(new ItemEntry(".*\\.kml$|.*\\.kmz$|.*f=kml.*", new ItemDesc("KML", ItemType.url, "Keyhole Markup Language")));

    itemEntries.add(new ItemEntry(".*\\.mxd$", new ItemDesc("Map Document", ItemType.file, "Map Document, Map, 2D, ArcMap, ArcGIS Server, mxd")));
    itemEntries.add(new ItemEntry(".*\\.nmf$", new ItemDesc("Explorer Map", ItemType.file, "Map, Explorer Map, Explorer Document, 2D, 3D, ArcGIS Explorer, nmf")));
    itemEntries.add(new ItemEntry(".*\\.3dd$", new ItemDesc("Globe Document", ItemType.file, "Map, Globe Document, 3D, ArcGlobe, ArcGIS Server, 3dd")));
    itemEntries.add(new ItemEntry(".*\\.sxd$", new ItemDesc("Scene Document", ItemType.file, "Map, Scene Document, 3D, ArcScene, sxd")));
    itemEntries.add(new ItemEntry(".*\\.ncfg$", new ItemDesc("Explorer Map", ItemType.file, "Map, Explorer Map, Explorer Document, 2D, 3D, ArcGIS Explorer, nmf")));
    itemEntries.add(new ItemEntry(".*\\.pmf$", new ItemDesc("Published Map", ItemType.file, "Published Map,2D,ArcReader,ArcMap,ArcGIS Server,pmf")));
    itemEntries.add(new ItemEntry(".*\\.mpk$", new ItemDesc("Map Package", ItemType.file, "Map, 2D, Map Package, ArcMap")));
    itemEntries.add(new ItemEntry(".*\\.gpk$", new ItemDesc("Geoprocessing Package", ItemType.file, "Tool, ArcGIS Desktop, ArcMap, ArcGlobe, ArcScene, Toolbox, Geoprocessing Package, Model, Script, Sharing, Result, gpk")));
    itemEntries.add(new ItemEntry(".*\\.apk$", new ItemDesc("Locator Package", ItemType.file, "Tool, ArcMap, ArcGIS Desktop, Locator Package, Geocoding, apk")));
    itemEntries.add(new ItemEntry(".*\\.wmpk$", new ItemDesc("Windows Mobile Package", ItemType.file, "ArcGIS Windows Mobile Package, ArcGIS Windows Mobile Map, ArcGIS Windows Mobile, wmpk")));
    itemEntries.add(new ItemEntry(".*\\.wpk$", new ItemDesc("Workflow Manager Package", ItemType.file, "Tool, ArcGIS Workflow Manager, Sharing, wpk, ArcGIS Desktop")));
    itemEntries.add(new ItemEntry(".*\\.zip$", new ItemDesc("Desktop Application Template", ItemType.file, "application, template, ArcGIS desktop")));
//    itemEntries.add(new ItemEntry(".*\\.zip$", new ItemDesc("Map Template", ItemType.file, "map, template, ArcMap, ArcGIS desktop")));
    itemEntries.add(new ItemEntry(".*\\.c$", new ItemDesc("Code Sample", ItemType.file, "code, sample, <optional, specified by the caller:  Java, C#, C++, C, Python, or Script>")));
    itemEntries.add(new ItemEntry(".*\\.cpp$", new ItemDesc("Code Sample", ItemType.file, "code, sample, <optional, specified by the caller:  Java, C#, C++, C, Python, or Script>")));
    itemEntries.add(new ItemEntry(".*\\.py$", new ItemDesc("Code Sample", ItemType.file, "code, sample, <optional, specified by the caller:  Java, C#, C++, C, Python, or Script>")));
    itemEntries.add(new ItemEntry(".*\\.java$", new ItemDesc("Code Sample", ItemType.file, "code, sample, <optional, specified by the caller:  Java, C#, C++, C, Python, or Script>")));
    itemEntries.add(new ItemEntry(".*\\.cs$", new ItemDesc("Code Sample", ItemType.file, "code, sample, <optional, specified by the caller:  Java, C#, C++, C, Python, or Script>")));
    itemEntries.add(new ItemEntry(".*\\.js$", new ItemDesc("Code Sample", ItemType.file, "code, sample, <optional, specified by the caller:  Java, C#, C++, C, Python, or Script>")));
    itemEntries.add(new ItemEntry(".*\\.lyr$", new ItemDesc("Layer", ItemType.file, "Data, Layer, ArcMap,ArcGlobe,ArcGIS Explorer, lyr")));
    itemEntries.add(new ItemEntry(".*\\.lpk$", new ItemDesc("Layer Package", ItemType.file, "Data, Layer Package,ArcMap, ArcGlobe, ArcGIS Explorer, lpk")));
    itemEntries.add(new ItemEntry(".*\\.nmc$", new ItemDesc("Explorer Layer", ItemType.file, "Data, Layer, Explorer Layer, ArcGIS Explorer,nmc")));
    itemEntries.add(new ItemEntry(".*\\.esriaddin$", new ItemDesc("Desktop Add In", ItemType.file, "Tool, Add In, Desktop Add In, ArcGIS Desktop, ArcMap, ArcGlobe, ArcScene, esriaddin")));
    itemEntries.add(new ItemEntry(".*\\.eaz$", new ItemDesc("Explorer Add In", ItemType.file, "Tool, Add In,Explorer Add In, ArcGIS Explorer, eaz")));
  }

  /**
   * Item entry
   */
  public static final class ItemEntry {

    private Pattern pattern;
    private ItemDesc itemDesc;

    public ItemEntry(String pattern, ItemDesc itemDesc) {
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
      this.itemDesc = itemDesc;
    }

    public ItemEntry(Pattern pattern, ItemDesc itemDesc) {
      this.pattern = pattern;
      this.itemDesc = itemDesc;
    }

    public ItemDesc getItemDesc() {
      return itemDesc;
    }

    public boolean matches(String url) {
      return pattern.matcher(url).matches();
    }
  }

  /**
   * Item description.
   */
  public static final class ItemDesc {

    private String type;
    private ItemType itemType;
    private String typeKeywords;

    public ItemDesc(String type, ItemType itemType, String typeKeywords) {
      this.type = Val.chkStr(type);
      this.itemType = itemType;
      this.typeKeywords = Val.chkStr(typeKeywords);
    }

    public String getType() {
      return type;
    }

    public ItemType getItemType() {
      return itemType;
    }
    
    public String getTypeKeywords() {
      return typeKeywords;
    }
  }

  /**
   * Item type.
   */
  public static enum ItemType {

    file,
    url,
    text
  }

  /**
   * Attributes map.
   */
  protected static class AttrMap extends HashMap<String, List<String>> {
    public void put(String name, String value) {
      name = Val.chkStr(name);
      value = Val.chkStr(value);
      if (!name.isEmpty() && !value.isEmpty()) {
        List<String> node = get(name);
        if (node==null) {
          node = new ArrayList<String>();
          put(name, node);
        }
        node.add(value);
      }
    }
  }
}

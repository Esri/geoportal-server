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
package com.esri.gpt.framework.util;

import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * KML/KMZ utility class.
 */
public class KmlUtil {

  /**
   * Creates a stream from the URL possibly referencing KML/KMZ. If this is
   * a KMZ, it will be interrogated for the enclosed KML, than that KML will be
   * reopened.
   * @param url KML/KMZ url
   * @throws IOException if creating stream failed
   */
  public static InputStream openKmlStream(String url)
    throws IOException {
    try {

      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      return openKmlStream(xPath, url);

    } catch (ParserConfigurationException ex) {
      throw new IOException("Error extracting kml stream.");
    } catch (SAXException ex) {
      throw new IOException("Error extracting kml stream.");
    } catch (XPathExpressionException ex) {
      throw new IOException("Error extracting kml stream.");
    }
  }

  /**
   * Gets KML input stream from KMZ stream.
   * @param kmzInputStream KMZ stream
   * @return encoded input stream containing KML data
   * @throws IOException extracting KML stream fails
   */
  public static InputStream extractKmlStream(InputStream kmzInputStream)
    throws IOException {

    try {
      
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      return extractKmlStream(xPath, kmzInputStream);

    } catch (ParserConfigurationException ex) {
      throw new IOException("Error extracting kml stream.");
    } catch (SAXException ex) {
      throw new IOException("Error extracting kml stream.");
    } catch (XPathExpressionException ex) {
      throw new IOException("Error extracting kml stream.");
    }
  }

  /**
   * Drills-down KML stream for "Placemark" and follows "NetworkLink". THis method
   * ALWAYS closes input stream passed as an argument.
   * @param kmlInputStream KML input stream
   * @param rootUrl root URL used when KML contains relative URL
   * @return first found feature stream.
   * @throws IOException if convertiong stream fails
   */
  public static InputStream convertToFeaturesStream(InputStream kmlInputStream, String rootUrl)
    throws IOException {

    rootUrl = Val.chkStr(rootUrl);

    try {

      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();
      return convertToFeaturesStream(xPath, kmlInputStream, rootUrl);

    } catch (ParserConfigurationException ex) {
      throw new IOException("Error extracting kml stream.");
    } catch (SAXException ex) {
      throw new IOException("Error extracting kml stream.");
    } catch (XPathExpressionException ex) {
      throw new IOException("Error extracting kml stream.");
    }
  }

  /**
   * Fully reads the characters from an input stream.
   * @param stream the input stream
   * @param charset the encoding of the input stream
   * @return the characters read
   * @throws IOException if an exception occurs
   */
  private static String readCharacters(InputStream stream, String charset)
    throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = null;
    InputStreamReader ir = null;
    try {
      if ((charset == null) || (charset.trim().length() == 0)) charset = "UTF-8";
      char cbuf[] = new char[2048];
      int n = 0;
      int nLen = cbuf.length;
      ir = new InputStreamReader(stream,charset);
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf,0,nLen)) > 0) sb.append(cbuf,0,n);
    } finally {
      try {if (br != null) br.close();} catch (Exception ef) {}
      try {if (ir != null) ir.close();} catch (Exception ef) {}
    }
    return sb.toString();
  }

  /**
   * Drills-down KML stream for "Placemark" and follows "NetworkLink". THis method
   * ALWAYS closes input stream passed as an argument.
   * @param xPath XPath
   * @param kmlInputStream KML input stream
   * @param rootUrl root URL used when KML contains relative URL
   * @return first found feature stream.
   * @throws IOException if reading stream fails
   * @throws XPathExpressionException if invalid XPath expression
   * @throws SAXException if error parsing document
   * @throws ParserConfigurationException if error obtaining parser
   */
  private static InputStream convertToFeaturesStream(XPath xPath, InputStream kmlInputStream, String root)
    throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {

    try {
      // read stream
      String kmlData = readCharacters(kmlInputStream, "UTF-8");

      // create XML document
      Document doc = DomUtil.makeDomFromString(Val.removeBOM(kmlData), false);

      // find placemarks
      NodeList ndPlacemarks = (NodeList) xPath.evaluate("//Placemark", doc, XPathConstants.NODESET);
      if (ndPlacemarks.getLength()>0)
        return new ByteArrayInputStream(kmlData.getBytes("UTF-8"));

      // find network links
      NodeList ndNetworkLinks = (NodeList) xPath.evaluate("//NetworkLink/Url/href", doc, XPathConstants.NODESET);
      if (ndNetworkLinks.getLength()==0) {
        ndNetworkLinks = (NodeList) xPath.evaluate("//NetworkLink/Link/href", doc, XPathConstants.NODESET);
        if (ndNetworkLinks.getLength()==0) {
          return null;
        }
      }

      for (int i=0; i<ndNetworkLinks.getLength(); i++) {
        Node ndNetworkLink = ndNetworkLinks.item(i);
        String value = (String) xPath.evaluate(".", ndNetworkLink, XPathConstants.STRING);
        if (root.length()>0) {
          try {
            URI valueUrl = new URI(value);
            if (!valueUrl.isAbsolute()) {
              URI rootUrl = new URI(root);
              value = rootUrl.resolve(valueUrl.normalize()).toString();
            }
          } catch (URISyntaxException ex) {
            continue;
          }
        }
        if (value.length()>0) {
          InputStream is = openKmlStream(xPath, value);
          if (is!=null) {
            is = convertToFeaturesStream(is, value);
            if (is!=null)
              return is;
          }
        }
      }
    } finally {
      try {
        kmlInputStream.close();
      } catch (IOException ex){}
    }

    return null;
  }

  /**
   * Gets encoded input stream from KMZ stream.
   * @param xPath XPath
   * @param kmzInputStream KMZ stream
   * @return encoded input stream containing KML data
   * @throws IOException KML input stream
   * @throws SAXException if error parsing document
   * @throws ParserConfigurationException if error obtaining parser
   * @throws XPathExpressionException if invalid XPath expression
   */
  private static InputStream extractKmlStream(XPath xPath, InputStream kmzInputStream)
    throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

    ZipInputStream zipStream = new ZipInputStream(kmzInputStream);
    for (ZipEntry ze=zipStream.getNextEntry(); ze!=null; ze=zipStream.getNextEntry()) {
      if (!ze.isDirectory() && ze.getName().endsWith(".kml")) {
        String kml = readCharacters(zipStream, "UTF-8");
        Document doc = DomUtil.makeDomFromString(Val.removeBOM(kml), false);
        Node kmlNode = (Node) xPath.evaluate("/kml", doc, XPathConstants.NODE);
        if (kmlNode!=null) {
          return new ByteArrayInputStream(kml.getBytes("UTF-8"));
        }
      }
    }
    return null;
  }

  /**
   * Creates a stream from the URL possibly referencing KML/KMZ. If this is
   * a KMZ, it will be interrogated for the enclosed KML, than that KML will be
   * reopened.
   * @param xPath XPath
   * @param url KML/KMZ url
   * @return KML stream or <code>null</code> server provides response code indicating error
   * @throws IOException if creating stream failed
   * @throws SAXException if error parsing document
   * @throws ParserConfigurationException if error obtaining parser
   * @throws XPathExpressionException if invalid XPath expression
   */
  private static InputStream openKmlStream(XPath xPath, String url)
    throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

    URL queryUrl = new URL(url);
    HttpURLConnection httpCon = (HttpURLConnection) queryUrl.openConnection();
    httpCon.setDoInput(true);
    httpCon.setRequestMethod("GET");
    InputStream responseStream = httpCon.getInputStream();
    if (httpCon.getResponseCode() != HttpURLConnection.HTTP_OK) {
      return null;
    }

    String ct = httpCon.getContentType();
    boolean kmz = (ct != null) && (ct.toLowerCase().indexOf("application/vnd.google-earth.kmz") != -1);
    InputStream wrappedStream = null;
    if (kmz) {
      wrappedStream = KmlUtil.extractKmlStream(xPath, responseStream);
    } else {
      wrappedStream = new BufferedInputStream(responseStream);
    }

    return wrappedStream;
  }
}

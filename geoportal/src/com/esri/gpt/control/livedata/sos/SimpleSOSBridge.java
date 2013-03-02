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
package com.esri.gpt.control.livedata.sos;

import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.util.Val;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.batik.ext.awt.image.codec.png.PNGDecodeParam;
import org.apache.batik.ext.awt.image.codec.png.PNGImageEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Simple Sensor observation service bridge. Provides image stream based on
 * service URL, offering, and feature.
 */
public class SimpleSOSBridge extends HttpServlet {

public static final int marginSize = 3;
public static final int maxReadingsDensity = 4;
private static final Color[] palette = {
  Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA, Color.YELLOW, Color.PINK
};

/** 
 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
 * @param request servlet request
 * @param response servlet response
 * @throws ServletException if a servlet-specific error occurs
 * @throws IOException if an I/O error occurs
 */
protected void processRequest(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
  OutputStream out = response.getOutputStream();
  try {
    SOSContext sosContext = new SOSContext();
    sosContext.setWidth(Val.chkInt(request.getParameter("width"), 200));
    sosContext.setHeight(Val.chkInt(request.getParameter("height"), 90));
    sosContext.setUrl(request.getParameter("url"));
    sosContext.setMethod(Method.parse(request.getParameter("method")));
    sosContext.setOfferingName(request.getParameter("offeringName"));
    sosContext.setResponseFormat(request.getParameter("responseFormat"));
    sosContext.setObservedProperty(request.getParameter("observedProperty"));
    sosContext.setFeatureOfInterest(request.getParameter("featureOfInterest"));
    sosContext.setBeginPosition(request.getParameter("beginPosition"));
    sosContext.setEndPosition(request.getParameter("endPosition"));

    response.setContentType("image/png");

    createImage(sosContext, out);
    //SosImageProducer imageProducer = new SosImageProducer(width, height, url, method);
    //imageProducer.createImage(offeringName, observedProperty, featureOfInterest, beginPeriod, endPeriod, out);
  } catch (ParserConfigurationException ex) {
    throw new ServletException("Error processing request", ex);
  } catch (SAXException ex) {
    throw new ServletException("Error processing request", ex);
  } catch (XPathExpressionException ex) {
    throw new ServletException("Error processing request", ex);
  } finally {
    out.close();
  }
}

/** 
 * Handles the HTTP <code>GET</code> method.
 * @param request servlet request
 * @param response servlet response
 * @throws ServletException if a servlet-specific error occurs
 * @throws IOException if an I/O error occurs
 */
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
  processRequest(request, response);
}

/** 
 * Handles the HTTP <code>POST</code> method.
 * @param request servlet request
 * @param response servlet response
 * @throws ServletException if a servlet-specific error occurs
 * @throws IOException if an I/O error occurs
 */
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
  processRequest(request, response);
}

/** 
 * Returns a short description of the servlet.
 * @return a String containing servlet description
 */
@Override
public String getServletInfo() {
  return "Simple SOS bridge";
}

/**
 * Creates image.
 * @param sosContext service context
 * @param output output stream to create image
 * @throws IOException I/O exception
 * @throws ParserConfigurationException throws if unable to obtain XML parser
 * @throws SAXException if error parsing data
 * @throws XPathExpressionException if error invoking XPath expression
 */
private void createImage(SOSContext sosContext, final OutputStream output) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
  ValueComponentsArray valArr = new ValueComponentsArray();

  InputStream responseStream = null;
  try {
    responseStream = createInputStream(sosContext);
    ValueComponentsArray simpleValArr = readSimpleValues(sosContext, responseStream);
    valArr.addAll(simpleValArr);
  } finally {
    try {
      if (responseStream != null) {
        responseStream.close();
      }
    } catch (Exception ef) {
    }
  }
//
//  // submit measurements request to the server
//  String response = submitRequest(sosContext);
//  // turn response into document
//  Document doc = DomUtil.makeDomFromString(response, false);
//
//  // create XPath accessories
//  XPathFactory xPathFactory = XPathFactory.newInstance();
//  XPath xPath = xPathFactory.newXPath();
//
//  // read simple values
//  ValueComponentsArray simpleValArr = readSimpleValues(sosContext, xPath, doc);
//
//  // read composite values
//  ValueComponentsArray compositeValArr = readCompositeValues(sosContext, xPath, doc);
//
//  simpleValArr.addAll(compositeValArr);
//  simpleValArr.normalize();

  valArr.normalize();

  // create image
  BufferedImage image = new BufferedImage(sosContext.getWidth(), sosContext.getHeight(), BufferedImage.TYPE_INT_RGB);
  Graphics2D gr = (Graphics2D) image.getGraphics();

  // draw axes
  drawAxes(sosContext, gr);

  // draw chart
  int i = 0;
  for (String name : valArr.getNames()) {
    if (i>=palette.length) break;
    gr.setColor(palette[i]);
    ArrayList<Double> vls = valArr.select(name);
    drawChart(sosContext, gr, vls);
    i++;
  }

  // encode into PNG
  PNGDecodeParam decodeParam = new PNGDecodeParam();
  decodeParam.setGenerateEncodeParam(true);

  PNGImageEncoder enc = new PNGImageEncoder(output, decodeParam.getEncodeParam());
  enc.encode(image);

  gr.dispose();
}

private ValueComponentsArray readSimpleValues(SOSContext sosContext, InputStream is) throws SAXException, IOException, ParserConfigurationException {
  ValueComponentsArray valArr = new ValueComponentsArray();
  SOSParser parser = new SOSParser(valArr, sosContext);
  parser.parseDocument(new InputSource(is));
  return valArr;
}

private ValueComponentsArray readSimpleValues(SOSContext sosContext, XPath xPath, Document doc) throws XPathExpressionException {
  ValueComponentsArray valArr = new ValueComponentsArray();

  // get separator definitions
  String tokenSeparator = (String) xPath.evaluate("/ObservationCollection/member/Observation/featureOfInterest/FeatureCollection/featureMember/SamplingPoint[@id='" + sosContext.getFeatureOfInterest() + "']/../../../../result/DataArray/encoding/TextBlock/@tokenSeparator", doc, XPathConstants.STRING);
  String blockSeparator = (String) xPath.evaluate("/ObservationCollection/member/Observation/featureOfInterest/FeatureCollection/featureMember/SamplingPoint[@id='" + sosContext.getFeatureOfInterest() + "']/../../../../result/DataArray/encoding/TextBlock/@blockSeparator", doc, XPathConstants.STRING);

  // get values
  String values = (String) xPath.evaluate("/ObservationCollection/member/Observation/featureOfInterest/FeatureCollection/featureMember/SamplingPoint[@id='" + sosContext.getFeatureOfInterest() + "']/../../../../result/DataArray/values", doc, XPathConstants.STRING);

  // transform values
  String[] readings = values.split(blockSeparator);
  for (int i = Math.max(readings.length - ((sosContext.getWidth() - 2 * marginSize) / maxReadingsDensity), 0); i < readings.length; i++) {
    String tokens[] = readings[i].split(tokenSeparator);
    if (tokens.length == 3) {
      ValueComponents vc = new ValueComponents();
      try {
        vc.put(sosContext.getObservedProperty(), Double.parseDouble(tokens[2]));
      } catch (NumberFormatException ex) {
        vc.put(sosContext.getObservedProperty(), Double.NaN);
      }
      valArr.add(vc);
    }
  }

  return valArr;
}

private Node searchForFirstQuantity(Node node) {
  Node n = node.getFirstChild();
  while (n!=null) {
    if (n.getNodeName().endsWith("Quantity")) {
      return n;
    }
    Node q = searchForFirstQuantity(n);
    if (q!=null) {
      return q;
    }
    n = n.getNextSibling();
  }
  return null;
}

private Node searchForEnclosingComposite(Node node) {
  if (node.getNodeName().endsWith("Composite")) {
    return node;
  }
  node = node.getParentNode();
  if (node!=null) {
    return searchForEnclosingComposite(node);
  }
  return null;
}

private void searchForChildComposites(ArrayList<Node> childComposites, Node parentNode) {
  Node n = parentNode.getFirstChild();
  while (n!=null) {
    String nodeName = n.getNodeName();
    if (nodeName.endsWith("Composite")) {
      childComposites.add(n);
    } else
      searchForChildComposites(childComposites, n);
    n = n.getNextSibling();
  }
}

private ValueComponentsArray readCompositeValues(SOSContext sosContext, XPath xPath, Document doc) {
  ValueComponentsArray values = new ValueComponentsArray();

  try {
    ArrayList<Node> childComposites = new ArrayList<Node>();

    Node fq = searchForFirstQuantity(doc);
    if (fq!=null) {
      fq = searchForEnclosingComposite(fq);
      if (fq!=null) {
        fq = fq.getParentNode();
        if (fq!=null) {
          searchForChildComposites(childComposites, fq);
        }
      }
    }

    for (Node comp : childComposites) {
      ValueComponents vc = new ValueComponents();
      Node quantity = searchForFirstQuantity(comp);
      while (quantity!=null) {
        if (quantity.getNodeName().endsWith("Quantity")) {
          String name = Val.chkStr(xPath.evaluate("@name", quantity));
          String value = Val.chkStr(xPath.evaluate(".", quantity));
          try {
            vc.put(name, Double.parseDouble(value));
          } catch (NumberFormatException ex) {
          }
        }
        quantity = quantity.getNextSibling();
      }
      values.add(vc);
    }

  } catch (XPathExpressionException ex) {}

  return values;
}

private void drawAxes(SOSContext sosContext, Graphics2D gr) {

  // set background to while
  gr.setColor(Color.WHITE);
  gr.fillRect(0, 0, sosContext.getWidth(), sosContext.getHeight());

  // draw a simple grid
  gr.setColor(Color.LIGHT_GRAY);

  int xstep = (sosContext.getWidth() - 2 * marginSize) / 8;
  for (int i = xstep; i < sosContext.getWidth() - 2 * marginSize; i += xstep) {
    gr.drawLine(marginSize + i, sosContext.getHeight() - marginSize, marginSize + i, marginSize);
  }

  int ystep = (sosContext.getHeight() - 2 * marginSize) / 4;
  for (int i = ystep; i < sosContext.getHeight() - 2 * marginSize; i += ystep) {
    gr.drawLine(marginSize, sosContext.getHeight() - marginSize - i, sosContext.getWidth() - marginSize, sosContext.getHeight() - marginSize - i);
  }

  // draw axis
  gr.setColor(Color.BLACK);

  gr.drawLine(marginSize, sosContext.getHeight() - marginSize, marginSize, marginSize);
  gr.drawLine(marginSize, sosContext.getHeight() - marginSize, sosContext.getWidth() - marginSize, sosContext.getHeight() - marginSize);
}

/**
 * Draws chart.
 * @param sosContext SOS context
 * @param gr graphics
 * @param vals array of values
 */
private void drawChart(SOSContext sosContext, Graphics2D gr, ArrayList<Double> vals) {

  // find minimum and maximum of the value
  Double min = null;
  Double max = null;

  for (Double v : vals) {
    if (min == null || (v!=null && !v.isNaN() && v.doubleValue() < min.doubleValue())) {
      min = v;
    }
    if (max == null || (v!=null && !v.isNaN() && v.floatValue() > max.floatValue())) {
      max = v;
    }
  }

  // if minimum and maximum found...
  if (min != null && max != null) {

    Float delta = max.floatValue() - min.floatValue();
    int clientWidth = sosContext.getWidth() - (2 * marginSize);
    int clientHeight = sosContext.getHeight() - (2 * marginSize);

    int lastX = marginSize;
    int lastY = marginSize;

    for (int i = 0; i < vals.size(); i++) {
      Double val = vals.get(i);
      if (val!=null) {
        int x = marginSize + (clientWidth * i) / vals.size();
        int y = (int) (marginSize + ((float) clientHeight * (val.floatValue() - min.floatValue())) / delta);
        if (i > 0) {
          gr.drawLine(lastX, sosContext.getHeight() - lastY, x, sosContext.getHeight() - y);
        }
        lastX = x;
        lastY = y;
      }
    }
  }
}

private InputStream createInputStream(SOSContext sosContext) throws MalformedURLException, IOException {
  StringBuilder postData = new StringBuilder();

  postData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  postData.append("<sos:GetObservation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosAll.xsd\" xmlns:sos=\"http://www.opengis.net/sos/1.0\" xmlns:om=\"http://www.opengis.net/om/1.0\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" service=\"SOS\" version=\"1.0.0\">");
  postData.append("<sos:offering>" + sosContext.getOfferingName() + "</sos:offering>");
  String beginPosition = sosContext.getBeginPosition();
  String endPosition = sosContext.getEndPosition();
  if (beginPosition.length() > 0) {
    if (endPosition.length() == 0) {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      endPosition = (new IsoDateFormat()).format(cal.getTime());
    }
    postData.append("<sos:eventTime>");
    postData.append("<ogc:TM_During>");
    postData.append("<ogc:PropertyName>om:samplingTime</ogc:PropertyName>");
    postData.append("<gml:TimePeriod>");
    postData.append("<gml:beginPosition>" + beginPosition + "</gml:beginPosition>");
    postData.append("<gml:endPosition>" + endPosition + "</gml:endPosition>");
    postData.append("</gml:TimePeriod>");
    postData.append("</ogc:TM_During>");
    postData.append("</sos:eventTime>");
  }
  postData.append("<sos:observedProperty>" + sosContext.getObservedProperty() + "</sos:observedProperty>");
  postData.append("<sos:responseFormat>" + sosContext.getResponseFormat() + "</sos:responseFormat>");
  postData.append("<sos:resultModel>om:Observation</sos:resultModel>");
  postData.append("<sos:responseMode>inline</sos:responseMode>");
  postData.append("</sos:GetObservation>");

  // open a connection to the targeted server
  URL URL = new URL(sosContext.getUrl());
  HttpURLConnection httpCon = (HttpURLConnection) URL.openConnection();
  httpCon.setDoInput(true);
  httpCon.setRequestMethod(sosContext.getMethod().name());

  // set request properties
  httpCon.setRequestProperty("Content-Type", "text/xml");
  httpCon.setRequestProperty("Content-Length", "" + postData.length());

  // post data to the targeted server
  httpCon.setDoOutput(true);
  OutputStream postStream = null;
  try {
    postStream = httpCon.getOutputStream();
    postStream.write(postData.toString().getBytes("UTF-8"));
    postStream.flush();
  } finally {
    try {
      if (postStream != null) {
        postStream.close();
      }
    } catch (Exception ef) {
    }
  }

  return httpCon.getInputStream();
}

/**
 * Submits request to the remote SOS service.
 * @param sosContext service context
 * @return response from the server
 * @throws MalformedURLException if invalid URL
 * @throws IOException if reading/writing to the connection streams failed
 */
private String submitRequest(SOSContext sosContext) throws MalformedURLException, IOException {
  // read the response from the targeted server
  String responseData = "";
  InputStream responseStream = null;
  try {
    responseStream = createInputStream(sosContext);
    responseData = readCharacters(responseStream, "UTF-8");
  } finally {
    try {
      if (responseStream != null) {
        responseStream.close();
      }
    } catch (Exception ef) {
    }
  }

  return responseData;
}

/**
 * Fully reads the characters from an input stream.
 * @param stream the input stream
 * @param charset the encoding of the input stream
 * @return the characters read
 * @throws IOException if an exception occurs
 */
private String readCharacters(InputStream stream, String charset)
  throws IOException {
  StringBuffer sb = new StringBuffer();
  BufferedReader br = null;
  InputStreamReader ir = null;
  try {
    if ((charset == null) || (charset.trim().length() == 0)) {
      charset = "UTF-8";
    }
    char cbuf[] = new char[2048];
    int n = 0;
    int nLen = cbuf.length;
    ir = new InputStreamReader(stream, charset);
    br = new BufferedReader(ir);
    while ((n = br.read(cbuf, 0, nLen)) > 0) {
      sb.append(cbuf, 0, n);
    }
  } finally {
    try {
      if (br != null) {
        br.close();
      }
    } catch (Exception ef) {
    }
    try {
      if (ir != null) {
        ir.close();
      }
    } catch (Exception ef) {
    }
  }
  return sb.toString();
}

/**
 * Method used to acces remote SOS service
 */
public static enum Method {

GET,
POST;

/**
 * Parses value.
 * @param method method name
 * @return method
 */
public static Method parse(String method) {
  method = Val.chkStr(method).toUpperCase();
  for (Method m : Method.values()) {
    if (m.name().equals(method)) {
      return m;
    }
  }
  return GET;
}
}
}

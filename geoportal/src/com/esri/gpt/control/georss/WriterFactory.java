package com.esri.gpt.control.georss;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;


/**
 * A factory for creating Writer objects.
 * 
 * @author TM
 */
public class WriterFactory {
// class variables =============================================================
/** class logger **/
private static Logger LOG = Logger.getLogger(
    WriterFactory.class.getCanonicalName());

// methods =====================================================================
/**
 * Creates a new Writer object.  
 * 
 *  *
 * @param format the format
 * @param messageBroker the message broker
 * @param query the query
 * @param request the request
 * @param response the response
 * @param context the context
 * @return the feed writer2
 * @throws Exception the exception
 */
public static FeedWriter2 createWriter(
    String format, 
    MessageBroker messageBroker, 
    RestQuery query, 
    HttpServletRequest request, 
    HttpServletResponse response,
    RequestContext context
    ) throws Exception {
  FeedWriter2 fW = null;
  
  StringAttributeMap attributeMap = 
    context.getCatalogConfiguration().getParameters();
  /*for(Entry<String, StringAttribute> entry :  attributeMap.entrySet()) {
    if(entry.getKey().toLowerCase()) {
      
    }
  }*/
  format = Val.chkStr(format);
  if("".equals(format)) {
    return null;
  }
  
  fW = createWriter(format);
  if( fW != null) {
    fW.setMessageBroker(messageBroker);
    fW.setQuery(query);
    fW.setResponse(response);
    fW.setRequest(request);
    fW.setContext(context);
  }
 
  return fW;
  
}

/**
 * Creates a new Writer object by looking at values in the gpt.xml under
 * the node searchResultFormat/format.
 *
 * @param format the format
 * @return the feed writer2
 * @throws XPathExpressionException the x path expression exception
 */
private static FeedWriter2 createWriter(String format) 
throws XPathExpressionException {
  
  XPath xpath = XPathFactory.newInstance().newXPath();
  Node node = 
    SearchConfig.getConfiguredInstance().getSearchConfigNode();
  NodeList nlFormat = (NodeList) xpath.evaluate("searchResultFormat/format", node,
      XPathConstants.NODESET);
  
  for(int i = 0; i < nlFormat.getLength(); i++) {
    try {
      Node ndFormat = nlFormat.item(i);
      String regexFormat = 
        ndFormat.getAttributes().getNamedItem("regexFormat").getNodeValue();
      if(regexFormat.equalsIgnoreCase(format) || format.matches(regexFormat)) {
        String className = ndFormat.getAttributes().getNamedItem("class")
          .getNodeValue();
        Class classDefinition = Class.forName(className);
        FeedWriter2 feedWriter = (FeedWriter2)classDefinition.newInstance();
        NodeList params = (NodeList) xpath.evaluate("parameter",
            ndFormat, XPathConstants.NODESET);
        Map<String, String> attributes = 
          new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (int k = 0; params != null && k < params.getLength(); k++) {
          String key = xpath.evaluate("@key", params.item(k));
          String value = xpath.evaluate("@value", params.item(k));
          attributes.put(Val.chkStr(key), Val.chkStr(value));
          
        }
        feedWriter.setConfigParameters(attributes);
        return feedWriter;
      }
    } catch(Throwable npex) {
      LOG.log(Level.WARNING, "Exception while parsing configuration", npex);
    }
  
  }
  return null;
  
}



}

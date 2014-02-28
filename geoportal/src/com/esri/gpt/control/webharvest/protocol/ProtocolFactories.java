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
package com.esri.gpt.control.webharvest.protocol;

import com.esri.gpt.control.webharvest.protocol.factories.Agp2AgpProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.Ags2AgpProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ArcGISProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ArcImsProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.AtomProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.CswProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.DCATProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.OaiProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ResourceProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.ThreddsProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.WafProtocolFactory;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Collection of protocol factories.
 * <p/>
 * Protocol factories is a collection (a map) initialized during application
 * configuration loading. The default behavior is to load all protocol factories
 * known at the compilation time. However, this can be altered by providing 
 * configuration information in <i>gpt.xml</i> configuration file.
 * <p/>
 * Example of the configuration:
 * <code><pre>
    &lt;gptConfig>
      ...
      &lt;protocols default="false">
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.ArcImsProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.arcims"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.CswProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.csw"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.OaiProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.oai"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.WafProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.waf"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.ResourceProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.resource"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.ArcGISProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.arcgis"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.ThreddsProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.thredds"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.AtomProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.atom"/>
        &lt;protocol factoryClass="com.esri.gpt.control.webharvest.protocol.factories.Agp2AgpProtocolFactory"
                  resourceKey="catalog.harvest.manage.edit.protocol.agp2agp"/>
      &lt;/protocols>
    &lt;/gptConfig>
 * </pre></code>
 * <p>
 * Description: <br/><br/>
 * default - if <code>true</code> a default configuration will be loaded first,<br/>
 * factoryClass - canonical name of the factory class,<br/>
 * resourceKey - resource key referring to the string in <i>gpt.resources</i> file.<br/>
 * 
 * @see com.esri.gpt.framework.context.ApplicationConfigurationLoader
 */
public class ProtocolFactories extends TreeMap<String, ProtocolFactory> {

private ArrayList<String> keys = new ArrayList<String>();  
private Map<String,String> resourceKeys = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

/**
 * Creates instance of the factories.
 */
public ProtocolFactories() {
  super(String.CASE_INSENSITIVE_ORDER);
}

/**
 * Initializes collection with default protocol factories.
 */
public void initDefault() {
  keys.clear();
  resourceKeys.clear();
  
  put("ArcIms" , new ArcImsProtocolFactory(),   "catalog.harvest.manage.edit.protocol.arcims");
  put("CSW"    , new CswProtocolFactory(),      "catalog.harvest.manage.edit.protocol.csw");
  put("OAI"    , new OaiProtocolFactory(),      "catalog.harvest.manage.edit.protocol.oai");
  put("WAF"    , new WafProtocolFactory(),      "catalog.harvest.manage.edit.protocol.waf");
  put("RES"    , new ResourceProtocolFactory(), "catalog.harvest.manage.edit.protocol.resource");
  put("ARCGIS" , new ArcGISProtocolFactory(),   "catalog.harvest.manage.edit.protocol.arcgis");
  put("THREDDS", new ThreddsProtocolFactory(),  "catalog.harvest.manage.edit.protocol.thredds");
  put("ATOM"   , new AtomProtocolFactory(),     "catalog.harvest.manage.edit.protocol.atom");
  put("AGP2AGP", new Agp2AgpProtocolFactory(),  "catalog.harvest.manage.edit.protocol.agp2agp");
  put("AGS2AGP", new Ags2AgpProtocolFactory(),  "catalog.harvest.manage.edit.protocol.ags2agp");
  put("DCAT"   , new DCATProtocolFactory(),     "catalog.harvest.manage.edit.protocol.dcat");
  /* NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
  put("AGP"    , new AgpProtocolFactory(),      "catalog.harvest.manage.edit.protocol.agp");
  */
}

@Override
public ProtocolFactory put(String key, ProtocolFactory value) {
  keys.add(key);
  return super.put(key, value);
}


/**
 * Stores a protocol value.
 * @param key protocol key
 * @param value protocol value
 * @param resourceKey resource key
 */
public ProtocolFactory put(String key, ProtocolFactory value, String resourceKey) {
  ProtocolFactory result = put(key,value);
  resourceKeys.put(key, resourceKey);
  return result;
}

/**
 * Gets all keys.
 * @return list of keys
 */
public List<String> getKeys() {
  return keys;
}

/**
 * Gets resource key of a given protocol.
 * @param protocolKey protocol key
 * @return resource key
 */
public String getResourceKey(String protocolKey) {
  return Val.chkStr(resourceKeys.get(protocolKey));
}

/**
 * Parses protocol.
 * @param xmlString protocol as XML string
 * @return protocol
 * @throws ProtocolParseException if error parsing protocol
 */
public Protocol parseProtocol(String xmlString) throws ProtocolParseException {
  try {
    Document doc = DomUtil.makeDomFromString(xmlString, false);

    String protocolName = "";
    long flags = 0;
    List<String> vDest = null;
    String sAddHoc = "";
    StringAttributeMap properties = new StringAttributeMap();
    NodeList protocolNL = doc.getElementsByTagName("protocol");

    if (protocolNL.getLength() >= 1) {
      Node protocolN = protocolNL.item(0);

      NamedNodeMap attributes = protocolN.getAttributes();

      Node protocolTypeN = attributes.getNamedItem("type");
      protocolName = Val.chkStr(protocolTypeN!=null? protocolTypeN.getNodeValue(): "");

      Node flagsN = attributes.getNamedItem("flags");
      flags = flagsN!=null? Val.chkLong(Val.chkStr(flagsN.getNodeValue()), 0): 0;
      
      Node destinationsN = attributes.getNamedItem("destinations");
      String sDest = destinationsN!=null? Val.chkStr(destinationsN.getNodeValue()): null;
      vDest = sDest!=null? Arrays.asList(sDest.split(",")): null;
      
      Node addHocN = attributes.getNamedItem("adHoc");
      sAddHoc = addHocN!=null? Val.chkStr(addHocN.getNodeValue()): "";

      NodeList propertiesNL = protocolN.getChildNodes();
      for (int i = 0; i < propertiesNL.getLength(); i++) {
        Node property = propertiesNL.item(i);
        String propertyName = property.getNodeName();
        String propertyValue = property.getTextContent();
        properties.set(propertyName, propertyValue);
      }
    }

    ProtocolFactory protocolFactory = get(protocolName);
    if (protocolFactory == null) {
      throw new ProtocolParseException("Unsupported protocol: " + protocolName);
    }

    Protocol protocol = protocolFactory.newProtocol();
    protocol.setFlags(flags);
    protocol.applyAttributeMap(properties);
    protocol.setAdHoc(sAddHoc);
    ProtocolInvoker.setDestinations(protocol, vDest);

    return protocol;
  } catch (ParserConfigurationException ex) {
    throw new ProtocolParseException("Error parsing protocol.", ex);
  } catch (SAXException ex) {
    throw new ProtocolParseException("Error parsing protocol.", ex);
  } catch (IOException ex) {
    throw new ProtocolParseException("Error parsing protocol.", ex);
  }
}

@Override
public String toString() {
  StringBuilder sb = new StringBuilder(getClass().getName()).append(" (\r\n");
  for (Map.Entry<String, ProtocolFactory> e : this.entrySet()) {
    sb.append("protocol: name=\"").append(e.getKey()).append("\", factoryClass=\"").append(e.getValue().getClass().getCanonicalName()).append("\"\r\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}
}

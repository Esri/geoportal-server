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
package com.esri.gpt.framework.security.identity.open;
import com.esri.gpt.framework.util.Val;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Property information associated with an Openid or oAuth provider.
 */
public class OpenProvider {

  /** instance variables ====================================================== */
  private String authenticationUrl;
  private String consumerKey;
  private String consumerSecret;
  private String iconUrl;
  private String name;
  private String resourceKey;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public OpenProvider() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the authentication URL.
   * @return the authentication URL
   */
  public String getAuthenticationUrl() {
    return this.authenticationUrl;
  }
  /**
   * Sets the authentication URL.
   * @param authenticationUrl the authentication URL
   */
  public void setAuthenticationUrl(String authenticationUrl) {
    this.authenticationUrl = authenticationUrl;
  }
  
  /**
   * Gets the consumer key (oAuth).
   * @return the consumer key
   */
  public String getConsumerKey() {
    return this.consumerKey;
  }
  /**
   * Sets the consumer key (oAuth).
   * @param consumerKey the name
   */
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }
  
  /**
   * Gets the consumer secret (oAuth).
   * @return the consumer secret
   */
  public String getConsumerSecret() {
    return this.consumerSecret;
  }
  /**
   * Sets the consumer secret (oAuth).
   * @param consumerSecret the secret
   */
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }
  
  /**
   * Gets the icon URL.
   * @return the icon URL
   */
  public String getIconUrl() {
    return this.iconUrl;
  }
  /**
   * Sets the icon URL.
   * @param iconUrl the icon URL
   */
  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }
  
  /**
   * Gets the provider name.
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the provider name.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Gets the UI property bundle resource key.
   * @return the resource key
   */
  public String getResourceKey() {
    return this.resourceKey;
  }
  
  /**
   * Sets the UI property bundle resource key.
   * @param resourceKey the resource key
   */
  public void setResourceKey(String resourceKey) {
    this.resourceKey = resourceKey;
  }
  
  /** methods ================================================================= */
  
  /**
   * Processes an open provider configuration node.
   * @param providers the container
   * @param node the openProvider configuration node
   * @throws Exception if an exception occurs
   */
  public void processConfgurationNode(OpenProviders providers, Node node) throws Exception {
    NamedNodeMap nnm = node.getAttributes();
    for (int i=0; i<nnm.getLength(); i++) {
      Node nd = nnm.item(i);
      String nodeName = nd.getNodeName();
      String nodeValue = nd.getNodeValue();
      if (nodeName.equals("name")) {
        this.setName(nodeValue);
      } else if (nodeName.equals("resourceKey")) {
        this.setResourceKey(nodeValue);
      } else if (nodeName.equals("authenticationUrl")) {
        this.setAuthenticationUrl(nodeValue);
      } else if (nodeName.equals("iconUrl")) {
        this.setIconUrl(nodeValue);
      } else if (nodeName.equals("consumerKey")) {
        this.setConsumerKey(nodeValue);
      } else if (nodeName.equals("consumerSecret")) {
        this.setConsumerSecret(nodeValue);
      }
    }
    if ((providers != null) && 
        (Val.chkStr(this.getName()).length() > 0) &&
        (Val.chkStr(this.getAuthenticationUrl()).length() > 0)) {
      if (this.getName().equalsIgnoreCase("Twitter")) {
        if ((Val.chkStr(this.getConsumerKey()).length() > 0) &&
            (Val.chkStr(this.getConsumerSecret()).length() > 0)) {
          providers.add(this);
        }
      } else {
        providers.add(this);
      }
    }
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
    sb.append(" name=\"").append(this.getName()).append("\"\n");
    sb.append(" resourceKey=\"").append(this.getResourceKey()).append("\"\n");
    sb.append(" authenticationUrl=\"").append(this.getAuthenticationUrl()).append("\"\n");
    sb.append(" iconUrl=\"").append(this.getIconUrl()).append("\"\n");
    if (this.getConsumerKey() != null) {
      String tmp = "";
      for (int i=0;i<this.getConsumerKey().length();i++) tmp += "*"; 
      sb.append(" consumerKey=\"").append(tmp).append("\"\n");
    }
    if (this.getConsumerSecret() != null) {
      String tmp = "";
      for (int i=0;i<this.getConsumerSecret().length();i++) tmp += "*";  
      sb.append(" consumerSecret=\"").append(tmp).append("\"\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
    return sb.toString();
  }
  
}

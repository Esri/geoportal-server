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
package gc.base.xmltypes;
import gc.base.xml.XmlNamespaces;

/**
 * Information for interrogating an XML document. 
 */
public class XmlInterrogationInfo {
	
	/** Instance variables. */
  private String        countExpression;
  private XmlNamespaces namespaces = new XmlNamespaces();
  //private String        pipelineXslt;
  
  /** Default constructor. */
  public XmlInterrogationInfo() {}

  /**
   * Gets the XPath expression for counting matching nodes.
   * @return the expression
   */
  public String getCountExpression() {
    return countExpression;
  }
  /**
   * Sets the XPath expression for counting matching nodes.
   * @param countExpression the expression
   */
  public void setCountExpression(String countExpression) {
    this.countExpression = countExpression;
  }
  
  /**
   * Gets the namespaces.
   * @return the namespaces
   */
	public XmlNamespaces getNamespaces() {
		return namespaces;
	}
	/**
	 * Sets the namespaces.
	 * @param namespaces the namespaces
	 */
	public void setNamespaces(XmlNamespaces namespaces) {
		this.namespaces = namespaces;
	}
  
}

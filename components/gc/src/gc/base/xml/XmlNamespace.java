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
package gc.base.xml;

/**
 * Holds a namespace prefix and URI.
 */
public class XmlNamespace {

	/** Instance variables. */
	private String prefix;
	private String uri;

	/** Default constructor. */
	public XmlNamespace() {}
	
	/**
	 * Constructs with a supplied prefix and uri.
	 * @param prefix the namespace prefix
	 * @param uri the namespace uri
	 */
	public XmlNamespace(String prefix, String uri) {
		setPrefix(prefix);
		setUri(uri);
	}

	/**
	 * Gets the prefix.
	 * @return the prefix
	 */
	public String getPrefix() {
	  return prefix;
	}
	/**
	 * Sets the prefix.
	 * @param prefix the prefix
	 */
	public void setPrefix(String prefix) {
	  this.prefix = prefix;
	}

	/**
	 * Gets the uri.
	 * @return the uri
	 */
	public String getUri() {
	  return uri;
	}
	/**
	 * Sets the uri.
	 * @param uri the uri
	 */
	public void setUri(String uri) {
	  this.uri = uri;
	}

}

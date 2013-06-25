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
 * A reference to an XSLT.
 */
public class XsltReference {
	
	/** Instance variables. */
	private String purpose;
	private String src;
	private String version;
	
  /** Default constructor. */
  public XsltReference() {}
  
  /**
   * Constructs with a purpose, source location and version.
   * @param purpose the purpose
   * @param src the source location
   * @param version the version
   */
  public XsltReference(String purpose, String src, String version) {
  	this.purpose = purpose;
  	this.src = src;
  	this.version = version;
  }
	
  /**
   * Gets the purpose.
   * @return the purpose
   */
	public String getPurpose() {
		return this.purpose;
	}
	/**
	 * Sets the purpose.
	 * @param purpose the purpose
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

  /**
   * Gets the source location.
   * @return the source location
   */
	public String getSrc() {
		return this.src;
	}
	/**
	 * Sets the source location.
	 * @param src the source location
	 */
	public void setSrc(String src) {
		this.src = src;
	}

  /**
   * Gets the version.
   * @return the version
   */
	public String getVersion() {
		return this.version;
	}
	/**
	 * Sets the version.
	 * @param version the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}
  
}


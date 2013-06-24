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
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Defines a collection of namespaces.
 */
public class XmlNamespaces {
	
	/** Instance variables. */
	private LinkedHashMap<String,XmlNamespace> members = new LinkedHashMap<String,XmlNamespace>();

	/** Default constructor. */
	public XmlNamespaces() {}
	
	/**
	 * Adds a member to the collection.
	 * @param member the member to add
	 */
	public void add(XmlNamespace member) {
	  members.put(member.getPrefix(),member);
	}

	/**
	 * Adds a member to the collection.
	 * @param prefix the namespace prefix
	 * @param uri the namespace URI
	 */
	public void add(String prefix, String uri) {
	  XmlNamespace member = new XmlNamespace(prefix,uri);
	  members.put(member.getPrefix(),member);
	}
	
  public Collection<XmlNamespace> values() {
  	return members.values();
  }

}


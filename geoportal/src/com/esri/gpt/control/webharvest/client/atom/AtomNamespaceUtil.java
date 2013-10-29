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
package com.esri.gpt.control.webharvest.client.atom;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import com.esri.gpt.catalog.schema.NamespaceContextImpl;
import com.esri.gpt.catalog.schema.Namespaces;

/**
 * Atom name space utility class.
 */
public class AtomNamespaceUtil {
	/**
	 * Makes ATOM name spaces.
	 * @return the ATOM name spaces
	 */
	private static Namespaces makeNamespaces() {
	  Namespaces namespaces = new Namespaces();
	  namespaces.add("atom","http://www.w3.org/2005/Atom");
	  namespaces.add("opensearch","http://a9.com/-/spec/opensearch/1.1/");
	  return namespaces;
	}
	/**
	 * Makes a context for Atom name spaces.
	 * @return the name space context
	 */
	private static NamespaceContext makeNamespaceContext() {
	  return new NamespaceContextImpl(makeNamespaces());
	}
	
	/**
	 * Makes xpath using namespace option
	 * @param isAtomNamespaceAware
	 * @return XPath
	 */
	public static XPath makeXPath(boolean isAtomNamespaceAware){
		XPath xPath = XPathFactory.newInstance().newXPath();
		if(isAtomNamespaceAware){
			xPath.setNamespaceContext(makeNamespaceContext());
		}
		return xPath;
	}
}

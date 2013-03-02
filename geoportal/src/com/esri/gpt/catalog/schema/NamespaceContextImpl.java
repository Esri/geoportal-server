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
package com.esri.gpt.catalog.schema;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Provides an implementation of NamespaceContext for a Namespaces collection.
 */
public class NamespaceContextImpl implements NamespaceContext {
  
// class variables =============================================================
  
// instance variables ==========================================================
 
/** Map of URI values keyed by prefix. */
private HashMap<String,String> _mapUriByPrefix;

/** Map of prefix values keyed by URI. */
private HashMap<String,String> _mapPrefixByUri;

// constructors ================================================================

/**
 * Constructs a namespace context for a namespace collection.
 */
public NamespaceContextImpl(Namespaces namespaces) {
  _mapUriByPrefix = new HashMap<String,String>();
  _mapPrefixByUri = new HashMap<String,String>();
  if (namespaces != null) {
    for (Namespace namespace: namespaces.values()) {
      String sPfx = namespace.getPrefix();
      String sUri = namespace.getUri();
      if ((sPfx.length() > 0) && (sUri.length() > 0)) {
        _mapUriByPrefix.put(sPfx,sUri);
        _mapPrefixByUri.put(sUri,sPfx);
      }
    }
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Gets the namespace URI associated with a prefix.
 * @param prefix the prefix
 */
public String getNamespaceURI(String prefix) {
  if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
    return XMLConstants.XML_NS_URI;
  } else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
    return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
  } else {
    String sUri = _mapUriByPrefix.get(prefix);
    if (sUri == null) {
      return XMLConstants.NULL_NS_URI;
    } else {
      return sUri;
    }
  }
}

/**
 * Gets the namespace prefix associated with a URI.
 * @param uri the URI
 */
public String getPrefix(String uri) {
  if (uri.equals(XMLConstants.XML_NS_URI)) {
    return XMLConstants.XML_NS_PREFIX;
  } else if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
    return XMLConstants.XMLNS_ATTRIBUTE;
  } else {
    return _mapPrefixByUri.get(uri);
  }
}

/**
 * Gets the namespace prefix iterator associated with a URI.
 * <br/>This method always returns null.
 * @param namespaceURI uri the URI
 */
public Iterator getPrefixes(String namespaceURI) {
  return null;
}

}

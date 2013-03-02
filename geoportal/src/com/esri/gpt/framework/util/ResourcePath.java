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
package com.esri.gpt.framework.util;
import java.io.IOException;
import java.net.URL;
import org.xml.sax.InputSource;

/**
 * Helps with the location of file resources relative to the deployed application.
 * <p>
 * This class has no static methods or members to ensure a proper class loader.
 */
public class ResourcePath {

// class variables =============================================================

// instance variables ==========================================================
private String _externalFolder = "";
private String _localFolder = "";

// constructors ================================================================

/** Default constructor. */
public ResourcePath() {
  //_externalFolder = "file:///C:/Program%20Files/ESRI/GPT9/gpt/";
  //_localFolder = "gpt/";
}

// properties ==================================================================

// methods =====================================================================

/**
 * Returns an InputSource for a resource.
 * @param path the path for the resource (relative to WEB-INF/classes)
 * @return an InputSource for the associated resource
 * @throws java.io.IOException if the URL associated with the resource path cannot be generated
 */
public InputSource makeInputSource(String path) throws IOException {
  return new InputSource(makeUrl(path).toString());
}

/**
 * Returns the URL associated with a resource.
 * @param path the path for the resource (relative to WEB-INF/classes)
 * @return the associated url
 * @throws java.io.IOException if the URL cannot be generated
 */
public URL makeUrl(String path) throws IOException {
  path = Val.chkStr(path);
  URL url = null;
  
  if ((_localFolder.length() > 0) && (_externalFolder.length() > 0) &&
      path.startsWith(_localFolder)) {
    path = _externalFolder+path.substring(_localFolder.length());
    //LogUtil.getLogger().finer("Making URL for resource: "+path);
    url = new URL(path); 
    
  } else {
    //url = this.getClass().getClassLoader().getResource(path);
    //LogUtil.getLogger().finer("Making URL for resource: "+path);
    url = Thread.currentThread().getContextClassLoader().getResource(path);
  }
  if (url == null) {
    throw new IOException("Unable to create resource URL for path: "+path);
  }
  return url;
}

}

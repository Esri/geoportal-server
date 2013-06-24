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
package gc.base.util;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.xml.sax.InputSource;

/**
 * Helps with the location of file resources relative to the deployed application.
 * <p/>
 * This class has no static methods or members to ensure a proper class loader.
 */
public class ResourcePath {

	/** Default constructor. */
	public ResourcePath() {}

	/**
	 * Returns an InputSource for a resource.
	 * @param path the path for the resource (relative to WEB-INF/classes)
	 * @return an InputSource for the associated resource
	 * @throws IOException if the URL associated with the resource path cannot be generated
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
		URL url = null;
		if ((path != null) && path.startsWith("gc-config/")) {
			String gcHome = System.getProperty("gc.home");
			if (gcHome != null) gcHome = gcHome.trim();
			if ((gcHome != null) && (gcHome.length() > 0)) {
				String s = gcHome+"/"+path;
				File f = new File(s);
				if (f.exists()) {
					url = f.toURI().toURL();
				}
			}
		}
		if (url == null) {
	    url = Thread.currentThread().getContextClassLoader().getResource(path);
		}
	  if (url == null) {
	    throw new IOException("Unable to create resource URL for path: "+path);
	  }
	  return url;
	}

}

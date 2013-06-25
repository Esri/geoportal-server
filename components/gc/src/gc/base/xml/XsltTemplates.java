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
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * A static collection of compiled XSLT templates.
 */
public class XsltTemplates {
	
	/** Class variables. */
	protected static Map<String,XsltTemplate> COMPILEDTEMPLATES = 
			Collections.synchronizedMap(new HashMap<String,XsltTemplate>());

  /** Default constructor. */
  public XsltTemplates() {}
  
  /**
   * Gets a compiled XSLT template.
   * @param xsltPath the path to an XSLT
   * @return the compiled template
   * @throws IOException if an IO exception occurs
   * @throws TransformerException if a transformation exception occurs
   * @throws SAXException if a SAX parsing exception occurs
   */
  public static synchronized XsltTemplate getCompiledTemplate(String xsltPath)
    throws TransformerException {
  	String sKey = xsltPath;
  	XsltTemplate template = null;
  	synchronized(COMPILEDTEMPLATES) {
	    template = COMPILEDTEMPLATES.get(sKey);
	    if (template == null) {
	      template = XsltTemplate.makeTemplate(xsltPath);
	      COMPILEDTEMPLATES.put(sKey,template);
	    }
  	}
    return template;
  }

}


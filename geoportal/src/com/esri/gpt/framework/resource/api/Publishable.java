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

package com.esri.gpt.framework.resource.api;

import com.esri.gpt.server.csw.client.NullReferenceException;
import java.io.IOException;
import java.util.Date;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Publishable resource.
 * This is specific type of the network accessble resource which can be
 * published.
 */
public interface Publishable extends Resource {
/**
 * Gets source URI.
 * @return source URI
 */
SourceUri getSourceUri();
/**
 * Gets content.
 * @return content
 * @throws IOException if getting content fails
 * @throws TransformerException if processing response fails
 * @throws SAXException if processing response fails
 * @throws NullReferenceException if null reference
 */
String getContent() throws IOException, TransformerException, SAXException, NullReferenceException;
/**
 * Gets update date if available.
 * @return update date or <code>null</code> if date not available
 */
Date getUpdateDate();
}

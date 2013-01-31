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
package com.esri.gpt.framework.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML handler.
 */
public class XmlHandler extends CharacterHandler {

  private boolean namespaceAware;
  private Document document;

  public XmlHandler(boolean namespaceAware) {
    super(new StringWriter());
    this.namespaceAware = namespaceAware;
  }

  @Override
  public void readResponse(HttpClientRequest request, InputStream responseStream)
    throws IOException {

    super.readResponse(request, responseStream);

    InputStream is = null;

    try {

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(namespaceAware);
      DocumentBuilder builder = factory.newDocumentBuilder();

      if (!namespaceAware) {
        builder.setEntityResolver(
          new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
              throws SAXException, IOException {
              return new InputSource(new StringReader(""));
            }
          }
        );
      }

      final ArrayList<SAXParseException> saxExceptions = new ArrayList<SAXParseException>();

      builder.setErrorHandler(new ErrorHandler() {
        public void warning(SAXParseException exception) throws SAXException {
          //saxExceptions.add(exception);
        }

        public void error(SAXParseException exception) throws SAXException {
          //saxExceptions.add(exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
          saxExceptions.add(exception);
        }
      });

      is = new ByteArrayInputStream(((StringWriter) this.getWriter()).getBuffer().toString().getBytes("UTF-8"));
      document = builder.parse(new InputSource(is));

      if (!saxExceptions.isEmpty()) {
        document = null;
        throw new IOException("Response does not appear as a valid XML stream.");
      }
    } catch (ParserConfigurationException ex) {
      throw new IOException("Response does not appear as a valid XML stream.");
    } catch (SAXException ex) {
      throw new IOException("Response does not appear as a valid XML stream.");
    } finally {
      if (is!=null) {
        try {
          is.close();
        } catch (IOException ex) {}
      }
    }
  }

  public Document getDocument() {
    return document;
  }
}

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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Redirecting resource resolver.
 */
public class RedirectingResourceResolver implements LSResourceResolver {

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    if (systemId != null) {
      try {
        URL url = new URL(systemId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection.getResponseCode() == 301 || connection.getResponseCode() == 302) {
          String location = connection.getHeaderField("Location");
          if (location != null && !location.isEmpty()) {
            url = new URL(location);
            connection = (HttpURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            String stringData = IOUtils.toString(reader);
            Input input = new Input();
            input.setStringData(stringData);
            // input.setCharacterStream(reader);
            // input.setPublicId(location);
            return input;
          }
        }
      } catch (IOException ex) {
      }
    }
    return null;
  }

  private final static class Input implements LSInput {
    private Reader characterStream;
    private InputStream byteStream;
    private String stringData;
    private String systemId;
    private String publicId;
    private String baseURI;
    private String encoding;
    private boolean certifiedText;

    @Override
    public Reader getCharacterStream() {
      return characterStream;
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
      this.characterStream = characterStream;
    }

    @Override
    public InputStream getByteStream() {
      return byteStream;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
      this.byteStream = byteStream;
    }

    @Override
    public String getStringData() {
      return stringData;
    }

    @Override
    public void setStringData(String stringData) {
      this.stringData = stringData;
    }

    @Override
    public String getSystemId() {
      return systemId;
    }

    @Override
    public void setSystemId(String systemId) {
      this.systemId = systemId;
    }

    @Override
    public String getPublicId() {
      return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    @Override
    public String getBaseURI() {
      return baseURI;
    }

    @Override
    public void setBaseURI(String baseURI) {
      this.baseURI = baseURI;
    }

    @Override
    public String getEncoding() {
      return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    @Override
    public boolean getCertifiedText() {
      return certifiedText;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
      this.certifiedText = certifiedText;
    }
  
  }
}
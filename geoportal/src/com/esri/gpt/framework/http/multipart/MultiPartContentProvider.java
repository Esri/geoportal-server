/*
 * Copyright 2011 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.http.multipart;

import com.esri.gpt.framework.http.ContentProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-part provider.
 * Use any of 'add' method to add suitable content part.
 */
public class MultiPartContentProvider extends ContentProvider {
  private List<Part> parts = new ArrayList<Part>();

  @Override
  public long getContentLength() {
    return 0; // that's all right; the actual content length will be provided by MultipartRequestEntity
  }

  @Override
  public String getContentType() {
    return "multipart/form-data";
  }

  @Override
  public boolean isRepeatable() {
    return true;
  }

  @Override
  public void writeRequest(HttpClientRequest request, OutputStream destination) throws IOException {
    // MultipartRequestEntity will provide that functionality
  }
  
  /**
   * Writes all parts.
   * @param partWriter part writer
   * @throws IOException if writing part fails
   */
  public void writeParts(PartWriter partWriter) throws IOException {
    for (Part part: parts) {
      part.write(partWriter);
    }
  }

  /**
   * Adds string attribute.
   *
   * @param name attribute name
   * @param value attribute value
   * @throws IOException if error setting attribute
   */
  public void add(final String name, final String value) throws IOException {
    parts.add(new Part() {
      @Override
      public void write(PartWriter writer) throws IOException {
        writer.write(name, value);
      }
    });
  }

  /**
   * Adds file attribute.
   *
   * @param name attribute name
   * @param file file
   * @param fileName disposition file name
   * @param contentType content type
   * @param charset character set
   * @param deleteAfterUpload <code>true</code> to delete file after being uploaded
   * @throws IOException if error setting attribute
   */
  public void add(final String name, final File file, final String fileName, final String contentType, final String charset, final boolean deleteAfterUpload) throws IOException {
    parts.add(new Part() {
      @Override
      public void write(PartWriter writer) throws IOException {
        writer.write(name, file, fileName, contentType, charset, deleteAfterUpload);
      }
    });
  }

  /**
   * Adds bytes attribute.
   *
   * @param name name
   * @param bytes bytes
   * @param fileName disposition file name
   * @param contentType content type
   * @param charset character set
   * @throws IOException if error setting attribute
   */
  public void add(final String name, final byte[] bytes, final String fileName, final String contentType, final String charset) throws IOException {
    parts.add(new Part() {
      @Override
      public void write(PartWriter writer) throws IOException {
        writer.write(name, bytes, fileName, contentType, charset);
      }
    });
  }
}

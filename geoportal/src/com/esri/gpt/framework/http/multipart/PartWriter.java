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

import java.io.File;
import java.io.IOException;

/**
 * Single part writer.
 */
public interface PartWriter {

  /**
   * Write ordinary attribute.
   * @param name part name.
   * @param value part value
   * @throws IOException if writing fails
   */
  void write(String name, String value) throws IOException;

  /**
   * Uploads a file.
   * @param name part name
   * @param file file to upload
   * @param fileName disposition file name
   * @param contentType content type
   * @param charset character set
   * @param deleteAfterUpload <code>true</code> to delete file after upload
   * @throws IOException if writing fails
   */
  void write(String name, File file, String fileName, String contentType, String charset, boolean deleteAfterUpload) throws IOException;

  /**
   * Uploads bytes.
   * @param name part name
   * @param bytes bytes to upload
   * @param fileName disposition file name
   * @param contentType content type
   * @param charset character set
   * @throws IOException if writing fails
   */
  void write(String name, byte[] bytes, String fileName, String contentType, String charset) throws IOException;
  
}

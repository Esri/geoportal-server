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

import java.io.Writer;

/**
 * Writer to {@link StringBuilder}.
 */
public class StringBuilderWriter extends Writer {
  private final StringBuilder stringBuilder;

  /**
   * Creates instance of the writer.
   * @param builder string builder
   */
  public StringBuilderWriter(StringBuilder builder) {
    this.stringBuilder = builder;
  }
  
  /**
   * Creates instance of the writer.
   * @param capacity initial capacity
   */
  public StringBuilderWriter(int capacity) {
    this(new StringBuilder(capacity));
  }
  
  /**
   * Creates instance of the writer.
   */
  public StringBuilderWriter() {
    this(new StringBuilder());
  }

  /**
   * Gets string builder.
   * @return string builder.
   */
  public StringBuilder getStringBuilder() {
    return stringBuilder;
  }

  @Override
  public void write(char[] cbuf, int off, int len) {
    stringBuilder.append(cbuf, off, len);
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
  }
}

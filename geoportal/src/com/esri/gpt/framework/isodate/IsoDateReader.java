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
package com.esri.gpt.framework.isodate;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

class IsoDateReader extends Reader {

private IsoDateInputStream inputStream;

public IsoDateReader(IsoDateInputStream inputStream) {
  super();
  this.inputStream = inputStream;
}

@Override
public int read(char[] cbuf, int off, int len) throws IOException {
  int i = 0;
  for (; i < Math.min(len, cbuf.length - off); i++) {
    int ch = inputStream.read();
    if (ch == -1) {
      if (i == 0) i = -1;
      break;
    }
    cbuf[off+i] = (char) ch;
  }
  return i;
}

@Override
public void close() throws IOException {
  inputStream.close();
}

public void filterOn() {
  inputStream.filterOn();
}

public void filterOff() {
  inputStream.filterOff();
}

public String read(int size) throws IOException {
  char[] cbuf = new char[size];
  Arrays.fill(cbuf, '\000');
  int num = read(cbuf, 0, cbuf.length);
  return num == -1? "": new String(cbuf, 0, num);
}

public void unread(int size) {
  inputStream.unread(size);
}

public String peek(int size) throws IOException {
  String next = read(size);
  unread(next.length());
  return next;
}

public boolean hasMore() {
  return inputStream.hasMore();
}
}

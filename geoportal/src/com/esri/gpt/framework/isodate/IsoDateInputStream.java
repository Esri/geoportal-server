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
import java.io.InputStream;

class IsoDateInputStream extends InputStream {

private String isoDate;
private IsoDateContext isoDateContext;
private boolean filterOn = true;

public IsoDateInputStream(String isoDate, IsoDateContext isoDateContext) {
  super();
  this.isoDate = isoDate;
  this.isoDateContext = isoDateContext;
}

public void filterOn() {
  this.filterOn = true;
}

public void filterOff() {
  this.filterOn = false;
}

@Override
public int read() throws IOException {
  int ch = hasMore() ? isoDate.charAt(incIndex()) : -1;
  if (filterOn) {
    while (ch == ':' || (ch == '-' && getIndex() > 0)) {
      ch = getIndex() < isoDate.length() ? isoDate.charAt(incIndex()) : -1;
    }
  }
  return ch;
}

private void unread() {
  if (getIndex()>=0) {
    decIndex();
    if (isoDate!=null) {
      if (filterOn) {
        while (getIndex()>0 && getIndex()<isoDate.length() && (isoDate.charAt(getIndex())==':' || isoDate.charAt(getIndex())=='-')) {
          decIndex();
        }
      }
    }
  }
}

public void unread(int size) {
  while (size-->0) unread();
}

int getIndex() {
  return isoDateContext.getIndex();
}

int decIndex() {
  isoDateContext.setIndex(isoDateContext.getIndex()-1);
  return isoDateContext.getIndex();
}

int incIndex() {
  isoDateContext.setIndex(isoDateContext.getIndex()+1);
  return isoDateContext.getIndex();
}

public boolean hasMore() {
  return isoDate != null && getIndex()+1 < isoDate.length();
}
}

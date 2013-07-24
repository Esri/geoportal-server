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
package com.esri.gpt.control.webharvest.client.csw;

import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.StringUri;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.NullReferenceException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * CSW publishable adapter.
 */
class CswPublishableAdapter extends CommonPublishable {
private static final IsoDateFormat ISODF = new IsoDateFormat();
private CswProxy proxy;
private SourceUri sourceUri;
private String updateDate;

public CswPublishableAdapter(CswProxy proxy, com.esri.gpt.server.csw.client.CswRecord record) {
  this.proxy = proxy;
//  this.sourceUri = new UuidUri(record.getId());
  // CSW specification doesn't say it has to be a GUID (UUID)
  this.sourceUri = new StringUri(record.getId());
  this.updateDate = Val.chkStr(record.getModifiedDate());
}

public SourceUri getSourceUri() {
  return sourceUri;
}

public String getContent() throws IOException, NullReferenceException {
  return proxy.read(sourceUri.asString());
}

@Override
public Date getUpdateDate() {
  if (updateDate.length() == 0)
    return null;
  try {
    return ISODF.parseObject(updateDate);
  } catch (ParseException ex) {
    return null;
  }
}

}

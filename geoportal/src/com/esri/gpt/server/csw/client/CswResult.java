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
package com.esri.gpt.server.csw.client;

import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.request.QueryResult;

/**
 * The Class CswResult.
 */
public class CswResult extends QueryResult<CswRecords> {


@Override
public PageCursor getPageCursor() {
  // TODO Auto-generated method stub
  return super.getPageCursor();
}

@Override
public CswRecords getRecords() {
  if(super.getRecords() == null) {
    super.setRecords(new CswRecords());
  }
  return super.getRecords();
}

@Override
public void reset() {
  // TODO Auto-generated method stub
  super.reset();
}

@Override
protected void setPageCursor(PageCursor cursor) {
  // TODO Auto-generated method stub
  super.setPageCursor(cursor);
}

@Override
public void setRecords(CswRecords records) {
  // TODO Auto-generated method stub
  super.setRecords(records);
}

}

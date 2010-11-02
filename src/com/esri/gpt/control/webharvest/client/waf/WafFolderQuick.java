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
package com.esri.gpt.control.webharvest.client.waf;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WAF folder (quick version).
 */
class WafFolderQuick extends WafFolder {

private static final Pattern HREF_PATTERN = Pattern.compile("href\\p{Space}*=\\p{Space}*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
private Set<String> processedFolders;

/**
 * Creates instance of the WAF folder.
 * @param context iteration context
 * @param info WAF info
 * @param proxy WAF proxy
 * @param processedFolders set of processed folders
 * @param url folder URL
 * @param criteria search criteria
 */
public WafFolderQuick(IterationContext context, WafInfo info, WafProxy proxy, Set<String> processedFolders, String url, Criteria criteria) {
  super(context, info, proxy, url, criteria);
  this.processedFolders = processedFolders;
}

/**
 * Parses WAF response.
 * @param response response
 * @return collection of resources found in the response
 * @throws IOException if unable to parse response
 */
protected Collection<Resource> parseResonse(String response) throws IOException {
  final ArrayList<Resource> directoryUrls = new ArrayList<Resource>();
  Matcher hrefMatcher = HREF_PATTERN.matcher(response);
  int hrefIdx = 0;
  while (hrefMatcher.find(hrefIdx)) {
    if (criteria == null || criteria.getMaxRecords() == null || criteria.getMaxRecords() == 0 || directoryUrls.size() < criteria.getMaxRecords()) {
      String documentUrl = hrefMatcher.group().replaceAll("^[^\"]*\"|\"[^\"]*$", "");
      try {
        URI urlUri = new URI(url);
        URI pathUri = urlUri.resolve(documentUrl);
        String pathExternalForm = pathUri.toURL().toExternalForm();
        if (documentUrl.endsWith("/")) {
          if (!url.startsWith(pathExternalForm)) {
            if (!processedFolders.contains(pathExternalForm.toLowerCase())) {
              directoryUrls.add(new WafFolderQuick(context, info, proxy, processedFolders, pathExternalForm, criteria));
              processedFolders.add(pathExternalForm.toLowerCase());
            }
          }
        } else if (documentUrl.toLowerCase().endsWith(".xml")) {
          directoryUrls.add(new WafFile(proxy, pathExternalForm));
        }
      } catch (URISyntaxException ex) {
        LOGGER.log(Level.FINER, "Erorr parsing document/folder URL: "+documentUrl, ex);
      }
    } else {
      break;
    }
    hrefIdx = hrefMatcher.end() + 1;
  }
  return directoryUrls;
}
}

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
import com.esri.gpt.framework.robots.Access;
import com.esri.gpt.framework.robots.Bots;
import com.esri.gpt.framework.robots.PathMatcher;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WAF folder (quick version).
 */
class WafFolderQuick extends WafFolder {

  private static final Pattern A_PATTERN = Pattern.compile("[<]a[^>]*[>]", Pattern.CASE_INSENSITIVE);
  private static final Pattern HREF_PATTERN = Pattern.compile("href\\p{Space}*=\\p{Space}*(\"[^\"]*\"|\'[^\']*\')", Pattern.CASE_INSENSITIVE);
  private static final String  HREF_VALUE_PATTERN = "^[^\"]*\"|\"[^\"]*$|^[^\']*\'|\'[^\']*$";
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
  @Override
  protected Collection<Resource> parseResonse(String response) throws IOException {
    final ArrayList<Resource> directoryUrls = new ArrayList<Resource>();
    final HashSet<String> processedFiles = new HashSet<String>();
    Matcher aMatcher = A_PATTERN.matcher(response);
    int aIdx = 0;
    URL baseUrl = new URL(url);
    while (aMatcher.find(aIdx)) {
      String a = aMatcher.group();
      Matcher hrefMatcher = HREF_PATTERN.matcher(a);
      int hrefIdx = 0;
      while (hrefMatcher.find(hrefIdx)) {
        if (criteria == null || criteria.getMaxRecords() == null || criteria.getMaxRecords() == 0 || directoryUrls.size() < criteria.getMaxRecords()) {
          String documentUrl = hrefMatcher.group().replaceAll(HREF_VALUE_PATTERN, "");
          URL pathUrl = new URL(baseUrl, documentUrl);
          if (baseUrl.getHost().equals(pathUrl.getHost())) {
            String pathExternalForm = pathUrl.toExternalForm();
            if (documentUrl.endsWith("/")) {
              if (pathExternalForm.startsWith(url)) {
                if (!processedFolders.contains(pathExternalForm.toLowerCase())) {
                  directoryUrls.add(new WafFolderQuick(context, info, proxy, processedFolders, pathExternalForm, criteria));
                  processedFolders.add(pathExternalForm.toLowerCase());
                }
              }
            } else if (documentUrl.toLowerCase().endsWith(".xml")) {
              if (!processedFiles.contains(pathExternalForm.toLowerCase())) {
                directoryUrls.add(new WafFile(proxy, pathExternalForm));
                processedFiles.add(pathExternalForm.toLowerCase());
              }
            }
          }
        } else {
          break;
        }
        hrefIdx = hrefMatcher.end() + 1;
      }
      aIdx = aMatcher.end() + 1;
    }
    return directoryUrls;
  }

  @Override
  protected Collection<Resource> handleDisallowedFolder(Bots bots, Access acc) {
    ArrayList<Resource> subResources = new ArrayList<Resource>();
    if (acc!=null) {
      List<Access> select = bots.select(acc.getPath(), new SubPathMatcher());
      for (Access acs: select) {
        if (acs.hasAccess() && acs!=acc) {
          String server = getServer(url);
          String stem = getStem(acs.getPath());
          
          String rest = acs.getPath().substring(stem.length()).replaceFirst("^/", "");
          if (!rest.contains("/") && !rest.contains("*")) {
            String relativePath = stem + "/" + rest;
            String fullPath = server + (!relativePath.startsWith("/")? "/": "") + relativePath;
            if (isFile(fullPath)) {
              if (fullPath.toLowerCase().endsWith(".xml")) {
                subResources.add(new WafFile(proxy, fullPath));
              }
            } else if (!rest.contains(".")) {
              subResources.add(new WafFolderQuick(context, info, proxy, processedFolders, fullPath, criteria));
              processedFolders.add(fullPath.toLowerCase());
            }
          }
        }
      }
    }
    return subResources;
  }
  
  private String getServer(String path) {
    try {
      URL baseUrl = new URL(path);
      if (baseUrl.getPort() >= 0) {
        return String.format("%s://%s:%d", baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort());
      } else {
        return String.format("%s://%s", baseUrl.getProtocol(), baseUrl.getHost());
      }
    } catch (Exception ex) {
      return path;
    }
  }
  
  private static String getStem(String s) {
    String stem = s;
    
    stem = stem.lastIndexOf("*")>=0? stem.substring(0,stem.lastIndexOf("*")): stem;
    stem = stem.lastIndexOf("/")>=0? stem.substring(0,stem.lastIndexOf("/")): stem;
    
    return stem;
  }

  private static boolean isFile(String path) {
    if (path!=null) {
      int lastSlashIdx = path.lastIndexOf("/");
      if (lastSlashIdx>=0) {
        path = path.substring(lastSlashIdx+1);
      }
      if (path!=null && path.contains(".")) {
        return true;
      }
    }
    return false;
  }
  
}

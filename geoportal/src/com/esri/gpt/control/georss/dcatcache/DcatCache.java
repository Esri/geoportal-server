/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.control.georss.dcatcache;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * DCAT cache.
 */
public class DcatCache {
  private static final Logger LOGGER = Logger.getLogger(DcatCache.class.getCanonicalName());
  private static final Pattern CACHE_NAME_PATTERN = Pattern.compile("cache\\-\\p{Digit}{4}\\-\\p{Digit}{2}\\-\\p{Digit}{2} \\p{Digit}{2}:\\p{Digit}{2}\\.dcat",Pattern.CASE_INSENSITIVE);
  
  private static DcatCache instance;
  private File root;
  
  /**
   * Gets instance of the cache.
   * @return singleton instance of the cache
   */
  public static DcatCache getInstance() {
    if (instance==null) {
      ApplicationContext appCtx = ApplicationContext.getInstance();
      ApplicationConfiguration appCfg = appCtx.getConfiguration();
      StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
      String dcatCachePath = parameters.getValue("dcat.cache.path");
      File root = new File(dcatCachePath);
      
      instance = new DcatCache(root);
    }
    return instance;
  }
  
  /**
   * Creates cache stream from the latest cache data.
   * @return input stream
   * @throws FileNotFoundException if cache data file not found
   */
  public InputStream createCacheStream() throws FileNotFoundException {
    File [] cacheFiles = listCacheFiles();
    File latestCache = findLatest(cacheFiles);
    return new FileInputStream(latestCache);
  }
  
  /**
   * Creates instance of the cache.
   * @param root root folder of the cache
   */
  private DcatCache(File root) {
    this.root = root;
  }
  
  /**
   * Lists all cache files.
   * @return array of cache files
   */
  private File[] listCacheFiles() {
    return root.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return CACHE_NAME_PATTERN.matcher(name).matches();
      }
    });
  }
  
  /**
   * Finds the latest file.
   * @param files array of files
   * @return latest file or <code>null</code> if no latest file available
   */
  private File findLatest(File [] files) {
    File latest = null;
    for (File f: files) {
      if (latest==null || f.lastModified()>latest.lastModified()) {
        latest = f;
      }
    }
    return latest;
  }
  
  private void purgeOutdatedFiles(File [] files, File latest) {
    for (File f: files) {
      if (!f.equals(latest)) {
        f.delete();
      }
    }
  }
}

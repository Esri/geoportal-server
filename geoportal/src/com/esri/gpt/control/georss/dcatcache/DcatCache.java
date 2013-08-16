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
import com.esri.gpt.framework.util.Val;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * DCAT cache.
 */
public class DcatCache {
  private static final Pattern CACHE_NAME_PATTERN = Pattern.compile("cache[^.]*\\.dcat",Pattern.CASE_INSENSITIVE);
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh-mm");
  
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
      String dcatCachePath = Val.chkStr(parameters.getValue("dcat.cache.path"),getDefaultDCATPath());
      File root = new File(dcatCachePath);
      
      instance = new DcatCache(root);
    }
    return instance;
  }
  
  /**
   * Gets default DCAT cache path.
   * @return default DCAT cache path
   */
  private static String getDefaultDCATPath() {
    File home = new File(System.getProperty("user.home"));
    return new File(home, "dcat/cache").getAbsolutePath();
  }
  
  /**
   * Creates cache stream from the latest cache data.
   * @return input stream
   * @throws FileNotFoundException if cache data file not found
   */
  public InputStream createInputCacheStream() throws FileNotFoundException {
    File [] cacheFiles = listCacheFiles();
    File latestCache = findLatest(cacheFiles);
    if (latestCache==null) {
      throw new FileNotFoundException("No recent cache found.");
    }
    return new FileInputStream(latestCache);
  }
  
  /**
   * Creates output stream to write into the cache.
   * @return output stream
   * @throws FileNotFoundException if can not create stream
   */
  public OutputStream createOutputCacheStream() throws FileNotFoundException {
    File file = new File(root,"cache-"+SDF.format(new Date())+".temp");
    return new CacheOutputStream(file);
  }
  
  /**
   * Creates instance of the cache.
   * @param root root folder of the cache
   */
  public DcatCache(File root) {
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
    if (files!=null) {
      for (File f: files) {
        if (latest==null || f.lastModified()>latest.lastModified()) {
          latest = f;
        }
      }
    }
    return latest;
  }
  
  /**
   * Purges outdated files.
   * @param files list of files
   * @param latest latest file
   */
  private void purgeOutdatedFiles(File [] files, File latest) {
    for (File f: files) {
      if (!f.equals(latest)) {
        f.delete();
      }
    }
  }
  
  /**
   * Cache output stream
   */
  private class CacheOutputStream extends OutputStream {
    
    private File file;
    private FileOutputStream fileOutputStream;
    
    /**
     * Creates instance of the stream.
     * @param file file representing the stream
     * @throws FileNotFoundException if creating stream fails
     */
    public CacheOutputStream(File file) throws FileNotFoundException {
      this.file = file;
      this.file.getParentFile().mkdirs();
      this.fileOutputStream = new FileOutputStream(file);
    }

    @Override
    public void write(int b) throws IOException {
      fileOutputStream.write(b);
    }

    @Override
    public void close() throws IOException {
      fileOutputStream.close();
      String name = file.getName();
      name = name.replaceAll("\\.[^.]+$", ".dcat");
      file.renameTo(new File(file.getParentFile(), name));
      File[] files = listCacheFiles();
      purgeOutdatedFiles(files, findLatest(files));
    }
    
  }
}

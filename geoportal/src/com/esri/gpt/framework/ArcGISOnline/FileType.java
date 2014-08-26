/*
 * Copyright 2014 Esri, Inc..
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

package com.esri.gpt.framework.ArcGISOnline;

/**
 * File type.
 */
public enum FileType {
  KMZ("kmz","kml"),
  DOC("doc","docx"),
  PPT("ppt","pptx"),
  XLS("xls","xlsx"),
  PDF,
  IMG("jpg","jpeg","tif","tiff","png"),
  VSD,
  KEY,
  PAGES,
  NUMBERS,
  MXD,
  MPK,
  BPK,
  TPK,
  PPKX,
  ESRITASKS,
  ZIP,
  NMF,
  GLOBE("3dd"),
  SXD,
  PMF,
  MAPX,
  PAGX,
  LYR("lyr","lyrx"),
  LPK,
  NMC,
  GPK,
  GCPK,
  RPK,
  WPK,
  ESRIADDIN,
  OPDASHBOARDADDIN,
  EAZ,
  CSV,
  SD("sd");
  
  private String [] extensions;
  
  FileType(String...extensions) {
    this.extensions = extensions;
  }
  
  public boolean isTypeOf(String fileName) {
    if (this.extensions!=null && this.extensions.length>0) {
      for (String extension: this.extensions) {
        if (fileName.toLowerCase().endsWith("."+extension.toLowerCase())) {
          return true;
        }
      }
      return false;
    } else {
      return fileName.toLowerCase().endsWith("."+this.name().toLowerCase());
    }
  }
}

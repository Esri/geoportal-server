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
package com.esri.gpt.control.filter;
import com.esri.gpt.framework.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * HTTP servlet request wrapper for a multipart requests.
 * <p>
 * The wrapper uses the org.apache.commons.fileupload package for
 * parsing and handling the incoming request.
 * <p>
 * The typical usage is based upon an incoming form with
 * an enctype of multipart/form-data.
 * <p>
 * org.apache.commons.fileupload.FileUploadBase$FileSizeLimitExceededException
 * is the exception thrown when the uploaded file size exceeds the limit.
 */
public class MultipartWrapper extends HttpServletRequestWrapper {
  
// class variables =============================================================

// instance variables ==========================================================
private Map<String,FileItem> _fileParameters;
private Map<String,String[]> _formParameters;

// constructors ================================================================

/**
 * Construct with a current HTTP servlet request.
 * @param request the current HTTP servlet request
 * @throws FileUploadException if an exception occurs during file upload
 */
public MultipartWrapper(HttpServletRequest request) 
  throws FileUploadException {
  super(request);
  getLogger().finer("Handling multipart content.");
  
  // initialize parameters
  _fileParameters = new HashMap<String,FileItem>(); 
  _formParameters = new HashMap<String,String[]>();
  int nFileSizeMax   = 100000000;
  int nSizeThreshold = 500000;
  String sTmpFolder  = "";
       
  // make the file item factory
  DiskFileItemFactory factory = new DiskFileItemFactory();
  factory.setSizeThreshold(nSizeThreshold);
  if (sTmpFolder.length() > 0) {
    File fTmpFolder = new File(sTmpFolder);
    factory.setRepository(fTmpFolder);
  }
  
  // make the file upload object
  ServletFileUpload fileUpload = new ServletFileUpload();
  fileUpload.setFileItemFactory(factory);
  fileUpload.setFileSizeMax(nFileSizeMax);
  
  // parse the parameters associated with the request
  List items = fileUpload.parseRequest(request);
  String[] aValues;
  ArrayList<String> lValues;
  for(int i=0;i<items.size();i++){
    FileItem item = (FileItem)items.get(i);
    getLogger().finer("FileItem="+item);
    if (item.isFormField()) {
      String sName = item.getFieldName();
      String sValue = item.getString();
      if (_formParameters.containsKey(sName)) {
        aValues = _formParameters.get(sName);
        lValues = new ArrayList<String>(Arrays.asList(aValues));
        lValues.add(sValue);
        aValues = lValues.toArray(new String[0]);
      } else {
        aValues = new String[1];
        aValues[0] = sValue;
      }
      _formParameters.put(sName,aValues); 
    } else {
      _fileParameters.put(item.getFieldName(),item);   
      request.setAttribute(item.getFieldName(),item);
    } 
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Gets the logger.
 * @return the logger
 */
private static Logger getLogger() {
  return LogUtil.getLogger();
}

/**
 * Gets the form parameter value associated with a name.
 * @param name the subject parameter name
 * @return the associated value (null if none)
 */
@Override
public String getParameter(String name) {
  String[] aValues = _formParameters.get(name);
  if (aValues == null) {
    return super.getParameter(name);
  } else {
    return aValues[0];
  }
}

/**
 * Gets the form parameter map.
 * @return the form parameter map
 */
@Override
public Map getParameterMap() {
  return _formParameters;
}

/**
 * Gets the form parameter names.
 * @return the form parameter names
 */
@Override
public Enumeration getParameterNames() {
  return Collections.enumeration(_formParameters.keySet());
}

/**
 * Gets the form parameter values associated with a name.
 * @param name the subject parameter name
 * @return the associated values (null if none)
 */
@Override
public String[] getParameterValues(String name) {
  return _formParameters.get(name);
}

/**
 * Determine if a request contains multipart content.
 * @param request the current request
 * @return true if the request contains multipart content
 */
public static boolean isMultipartContent(ServletRequest request) {
  boolean bMultipart = false;
  if ((request != null) && (request instanceof HttpServletRequest)) {  
    HttpServletRequest httpReq = (HttpServletRequest)request;
    bMultipart = ServletFileUpload.isMultipartContent(httpReq);
  }
  getLogger().finest("isMultipartContent="+bMultipart);
  return bMultipart;
}
  
}

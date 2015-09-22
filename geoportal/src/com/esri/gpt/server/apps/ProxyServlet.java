/*
 * Copyright 2015 Esri, Inc.
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
package com.esri.gpt.server.apps;

import com.esri.gpt.framework.context.CredentialsMap;
import com.esri.gpt.framework.http.ByteArrayHandler;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proxy servlet.
 */
public class ProxyServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(ProxyServlet.class.getName());

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    String postData = "";
    InputStream requestStream = null;
    try {
      requestStream = request.getInputStream();
      postData = readCharacters(requestStream, request.getCharacterEncoding());
    } finally {
      if (requestStream != null) {
        try {
          requestStream.close();
        } catch (Exception ef) {
        }
      }
    }

    String queryString = URLDecoder.decode(request.getParameter("url")!=null?request.getParameter("url"):request.getQueryString(),"UTF-8");
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(queryString);
    if (postData.length() > 0) {
      cr.setContentProvider(new StringProvider(postData, request.getContentType()));
    }

    ByteArrayHandler sh = new ByteArrayHandler();
    cr.setContentHandler(sh);
    CredentialsMap cm = CredentialsMap.extract(request);
    CredentialProvider cp = cm.get(queryString);
    cr.setCredentialProvider(cp);

    cr.execute();

    byte[] content = sh.getContent();
    OutputStream out = response.getOutputStream();
    try {
      if (content.length > 0) {
        response.setCharacterEncoding(cr.getResponseInfo().getContentEncoding());
        response.setContentType(cr.getResponseInfo().getContentType());
        out.write(content);
      }
    } finally {
      if (out != null) {
        try {
          out.flush();
          out.close();
        } catch (Exception ef) {
        }
      }
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    try {
      processRequest(request, response);
    } catch (ServletException ex) {
      LOG.log(Level.SEVERE, "Proxy error", ex);
      throw ex;
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "Proxy error", ex);
      throw ex;
    }
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Proxy servlet";
  }// </editor-fold>

  private String readCharacters(InputStream stream, String charset)
          throws IOException {
    StringBuffer sb = new StringBuffer();
    BufferedReader br = null;
    InputStreamReader ir = null;
    try {
      if ((charset == null) || (charset.trim().length() == 0)) {
        charset = "UTF-8";
      }
      char cbuf[] = new char[2048];
      int n = 0;
      int nLen = cbuf.length;
      ir = new InputStreamReader(stream, charset);
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf, 0, nLen)) > 0) {
        sb.append(cbuf, 0, n);
      }
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (Exception ef) {
      }
      try {
        if (ir != null) {
          ir.close();
        }
      } catch (Exception ef) {
      }
    }
    return sb.toString();
  }

}

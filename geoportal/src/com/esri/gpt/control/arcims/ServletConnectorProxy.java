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
package com.esri.gpt.control.arcims;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides a basic proxy servlet for ArcXML requests
 */
public class ServletConnectorProxy extends HttpServlet {

	// class variables
	// =============================================================
	private static Logger LOGGER = Logger.getLogger(ServletConnectorProxy.class
			.getName());

	// instance variables
	// =============================================================
	private String _redirectURL = null;


	// properties =============================================================

	// methods variables
	// =============================================================

	/**
	 * Loads initial parameters for the servlet
	 * 
	 * @throws ServletException
	 *           if an exception occurs
	 */
	public void init() throws ServletException {
		this._redirectURL = this.getInitParameter("redirectURL");
	}

	/**
	 * Handles a GET request. <p/> The default behavior is the execute the doGet
	 * method.
	 * 
	 * @param request
	 *          the servlet request
	 * @param response
	 *          the servlet response
	 * @throws ServletException
	 * @throws IOException
	 *           if an exception occurs
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	 /* if(true){
	    doPost(request,response);
	    return;
	  }*/
	  
		/*if(!_redirectURL.startsWith("http"))
			setURL(request);

		response.sendRedirect(_redirectURL + "?" + request.getQueryString());*/
	  
	  Principal p = request.getUserPrincipal();
	  if(p != null){
	    LOGGER.finer("UserName : " + p.getName());
	  }
	  
	  LOGGER.finer("Query string=" + request.getQueryString());
	  if ("ping".equalsIgnoreCase(request.getParameter("Cmd"))) {
	    response.getWriter().write("IMS v9.3.0");
	  } else if ("ping".equalsIgnoreCase(request.getParameter("cmd"))) {
	    response.getWriter().write("IMS v9.3.0");
	  } else if ("getVersion".equalsIgnoreCase(request.getParameter("Cmd"))) {
	    response.getWriter().write("Version=9.3.0\nBuild_Number=514.2159");
	  } else {
	    response.sendError(HttpServletResponse.SC_NOT_FOUND);
	  }
	}

	/**
	 * Handles a POST request.
	 * 
	 * @param request
	 *          the servlet request
	 * @param response
	 *          the servlet response
	 * @throws ServletException
	 * @throws IOException
	 *           if an exception occurs
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if(!_redirectURL.startsWith("http"))
			setURL(request);

		Principal p = request.getUserPrincipal();
    if(p != null){
      LOGGER.finer("UserName : " + p.getName());
    }
    
		executeProxy(request, response);
	}

	/**
	 * Sets redirect url to next door GPT application
	 * 
	 * @param request
	 *          the servlet request
	 */
	private void setURL(HttpServletRequest request) {

		StringBuffer sb = new StringBuffer();
		sb.append("http://")
		.append(request.getServerName()).append(":")
		.append(request.getServerPort()).append(_redirectURL);
		
		_redirectURL = sb.toString();
		
		LOGGER.finer(_redirectURL);
	}

	/**
	 * Communicates with redirect url and works as a transparent proxy
	 * 
	 * @param request
	 *          the servlet request
	 * @param response
	 *          the servlet response
	 * @throws IOException
	 *           if an exception occurs
	 */
	private void executeProxy(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		HttpURLConnection httpCon = null;
		URL redirectURL = null;
		InputStream input = null;
		OutputStream output = null;
		InputStream proxyInput = null;
		OutputStream proxyOutput = null;

		try {

			input = request.getInputStream();
			output = response.getOutputStream();

			String sQueryStr = request.getQueryString();
			String sAuthorization = request.getHeader("Authorization");
			String requestBody = readInputCharacters(input);
			String requestMethod = request.getMethod();
			String contentType = request.getContentType();
			String encoding = request.getCharacterEncoding();

			LOGGER.finer(" Request method = " + requestMethod);
			LOGGER.finer(" Query string = " + sQueryStr);
			LOGGER.finer(" Authorization header =" + sAuthorization);
			LOGGER.finer(" Character Encoding = " + encoding);
			LOGGER.finer(" The redirect URL is " + this._redirectURL + "?"
					+ sQueryStr);

			redirectURL = new URL(this._redirectURL + "?" + sQueryStr);

			httpCon = (HttpURLConnection) redirectURL.openConnection();

			httpCon.setDoInput(true);
			httpCon.setDoOutput(true);
			httpCon.setUseCaches(false);
			httpCon.setRequestMethod(requestMethod);
			httpCon.setRequestProperty("Content-type", contentType);

			if (sAuthorization != null) {
				httpCon.addRequestProperty("Authorization", sAuthorization);
			}
	
				proxyOutput = httpCon.getOutputStream();
				send(requestBody, proxyOutput);
					
				String authenticateHdr = httpCon.getHeaderField("WWW-Authenticate");
				if(authenticateHdr != null){
				  LOGGER.finer(" WWW-Authenticate : " + authenticateHdr);
				  response.setHeader("WWW-Authenticate", authenticateHdr);
				}
        LOGGER.finer(" Response Code : " + httpCon.getResponseCode());
        
				if ((httpCon.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN)){
		
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
				}
				else if((httpCon.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)) {			
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}   else if((httpCon.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR)) {     
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
				else {
					proxyInput = httpCon.getInputStream();
					send(proxyInput, output);
				}

		} catch (Exception e) {
		  e.printStackTrace();
		} finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
			}
			if (proxyInput != null) {
				proxyInput.close();
			}
			if (proxyOutput != null) {
				proxyOutput.close();
			}
			if (httpCon != null) {
				httpCon.disconnect();
			}
		}
	}

	/**
	 * Fully reads the characters from an InputStream.
	 * 
	 * @param strm
	 *          the InputStream
	 * @return the characters read
	 * @throws IOException
	 *           if an exception occurs
	 */
	private String readInputCharacters(InputStream inputStream)
			throws IOException {
		StringBuffer sb = new StringBuffer();
		InputStreamReader ir = null;
		BufferedReader br = null;
		try {

			char cbuf[] = new char[2048];
			int n = 0;
			int nLen = cbuf.length;
			String sEncoding = "UTF-8";

			ir = new InputStreamReader(inputStream, sEncoding);
			br = new BufferedReader(ir);
			while ((n = br.read(cbuf, 0, nLen)) > 0) {
				sb.append(cbuf, 0, n);
			}

		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception ef) {
			}
			try {
				if (ir != null)
					ir.close();
			} catch (Exception ef) {
			}
		}
		return sb.toString();
	}

	/**
	 * Sends request to the opened HTTP connection.
	 * 
	 * @param requestBody
	 *          as string
	 * @param output
	 *          to stream the data
	 * 
	 * @throws java.io.IOException
	 *           if sending data failed
	 */
	private void send(String requestBody, OutputStream output) throws IOException {

		PrintWriter outputWriter = null;
		try {
			outputWriter = new PrintWriter(output);
			outputWriter.write(requestBody);
			outputWriter.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception ef) {
				ef.printStackTrace();
			}
		}
	}

	/**
	 * Sends request to the opened HTTP connection.
	 * 
	 * @param input
	 *          data as stream
	 * @param outputWriter
	 *          to send back response
	 * 
	 * @throws java.io.IOException
	 *           if sending data failed
	 */
	private void send(InputStream input, PrintWriter outputWriter)
			throws IOException {

		try {
			outputWriter.write(readInputCharacters(input));
			outputWriter.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends request to the opened HTTP connection.
	 * 
	 * @param input
	 *          data as stream
	 * @param output
	 *          output as stream
	 * 
	 * @throws java.io.IOException
	 *           if sending data failed
	 */
	private void send(InputStream input, OutputStream output) throws IOException {

		PrintWriter outputWriter = null;
		try {
			outputWriter = new PrintWriter(output);
			outputWriter.write(readInputCharacters(input));
			outputWriter.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputWriter != null) {
				outputWriter.close();
			}
		}
	}

}

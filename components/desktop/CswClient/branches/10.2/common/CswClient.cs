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
using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Net;
using System.Security.Cryptography;
using com.esri.gpt.security;


namespace com.esri.gpt.csw
{

    /// <summary>
    /// CswClient class is used to submit CSW search request.
    /// </summary>
    /// <remarks>
    /// CswClient is a wrapper class of .NET HttpWebRequest and HttpWebResponse. It basically submits 
    /// a HTTP request then return a text response.
    /// </remarks>
    /// 
    public class CswClient
    {
        private CredentialCache _credentialCache;
        static CookieContainer _cookieContainer;    //use static variable to be thread safe
        private String downloadFileName = "";
        private bool retryAttempt = true;
        #region Constructor
        /// <summary>
        /// Constructor
        /// </summary>
        public CswClient()
        {
            _credentialCache = new CredentialCache();
            _cookieContainer = new CookieContainer();
        }
        #endregion
        #region PublicMethods
        /// <summary>
        /// Submit HTTP Request 
        /// </summary>
        /// <param name="method">HTTP Method. for example "POST", "GET"</param>
        /// <param name="URL">URL to send HTTP Request to</param>
        /// <param name="postdata">Data to be posted</param>
        /// <returns>Response in plain text</returns>
        public string SubmitHttpRequest(string method, string URL, string postdata)
        {
            return SubmitHttpRequest(method, URL, postdata, "", "");
        }
        
        /// <summary>
        /// Submit HTTP Request 
        /// </summary>
        /// <remarks>
        /// Submit an HTTP request.
        /// </remarks>
        /// <param name="method">HTTP Method. for example "POST", "GET"</param>
        /// <param name="URL">URL to send HTTP Request to</param>
        /// <param name="postdata">Data to be posted</param>
        /// <param name="usr">Username</param>
        /// <param name="pwd">Password</param>
        /// <returns>Response in plain text.</returns>
        public string SubmitHttpRequest(string method, string URL, string postdata, string usr, string pwd)
        {
            String responseText = "";            
            HttpWebRequest request;
            Uri uri = new Uri(URL);
            request = (HttpWebRequest)WebRequest.Create(uri);
            request.AllowAutoRedirect = true;
            
            ClientCertRequest.handleClientCert(request, URL);

            if(method.Equals("SOAP")){
                request.Method = "POST";
                request.Headers.Add("SOAPAction: Some-URI");
                request.ContentType = "text/xml; charset=UTF-8"; 
            }
            else if (method.Equals("DOWNLOAD"))
            {
                request.Method = "GET";                             
            }
            else{
                request.ContentType = "text/xml; charset=UTF-8"; 
                request.Method = method;
            }

            // Credential and cookies
            request.CookieContainer = _cookieContainer;
            
            NetworkCredential nc = null;
            String authType = "Negotiate";
            if (_credentialCache.GetCredential(uri, authType) == null && _credentialCache.GetCredential(uri, "Basic") == null)
            {
                if (!String.IsNullOrEmpty(usr) && !String.IsNullOrEmpty(pwd))
                {
                    nc =  new NetworkCredential(usr, pwd);
                    _credentialCache.Add(uri, "Basic", nc);
                    _credentialCache.Add(uri, authType, nc);
                }
                else{
                     nc =  System.Net.CredentialCache.DefaultNetworkCredentials;
                     _credentialCache.Add(uri, authType, nc);  
                }
            }           
            request.Credentials = _credentialCache;
            // post data
            if (request.Method.Equals("POST", StringComparison.OrdinalIgnoreCase))
            {
                UTF8Encoding encoding = new UTF8Encoding();
                Byte[] byteTemp = encoding.GetBytes(postdata);
                request.ContentLength = byteTemp.Length;

                Stream requestStream = request.GetRequestStream();
                requestStream.Write(byteTemp, 0, byteTemp.Length);
                requestStream.Close();
            }

            HttpWebResponse response = null;
            try{
                response = (HttpWebResponse)request.GetResponse();
            }
            catch (UnauthorizedAccessException ua)
            {
                /*if (ua is CryptographicException)
                {
                    // handle client certificate
                    ClientCertRequest.handleClientCert(request, URL);
                    response = (HttpWebResponse)request.GetResponse();
                    retryAttempt = false;
                }*/

                if (retryAttempt)
                {
                    PromptCredentials pc = new PromptCredentials();
                    pc.ShowDialog();
                    retryAttempt = false;
                    try
                    {
                        _credentialCache.Remove(uri, "Basic");
                    }
                    catch (Exception) { };
                    _credentialCache.Remove(uri, authType);
                    if (!String.IsNullOrEmpty(pc.Username) && !String.IsNullOrEmpty(pc.Password))
                        return SubmitHttpRequest(method, URL, postdata, pc.Username, pc.Password);
                    else
                        return null;
                }
                else
                {
                    retryAttempt = true;
                    throw ua;
                }

            }
                catch (WebException we)
            {
               /* if (we is CryptographicException)
                {
                    // handle client certificate
                    ClientCertRequest.handleClientCert(request, URL);
                    response = (HttpWebResponse)request.GetResponse();
                    retryAttempt = false;
                }*/
                
                if (retryAttempt)
                {
                    PromptCredentials pc = new PromptCredentials();
                    pc.ShowDialog();
                    retryAttempt = false;
                    try
                    {
                        _credentialCache.Remove(uri, "Basic");   
                    }catch(Exception){};
                    _credentialCache.Remove(uri, authType);
                    if (!String.IsNullOrEmpty(pc.Username) && !String.IsNullOrEmpty(pc.Password))
                        return SubmitHttpRequest(method, URL, postdata, pc.Username, pc.Password);
                    else
                        return null;
                    
                }
                else
                {
                    retryAttempt = true;
                    throw we;
                }

            }
            if (_cookieContainer.GetCookies(uri) == null)
            {
                _cookieContainer.Add(uri, response.Cookies);
            }

            Stream responseStream = response.GetResponseStream();            

            if (method.Equals("DOWNLOAD"))
            {
                FileStream file = null;

                string fileName = response.GetResponseHeader("Content-Disposition");

                string[] s = null;
                if (fileName.ToLower().EndsWith(".tif"))
                {
                    s = URL.Split(new String[] { "coverage=" }, 100, StringSplitOptions.RemoveEmptyEntries);
                    s[1] = s[1].Trim() + ".tif";
                }
                else
                {
                    s = fileName.Split('=');
                    s[1] = s[1].Replace('\\', ' ');
                    s[1] = s[1].Replace('"', ' ');
                    s[1] = s[1].Trim();
                }

                try
                {
                    downloadFileName = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.ConfigurationFiles), s[1]);
                    System.IO.File.Delete(downloadFileName);
                    file = System.IO.File.Create(downloadFileName);                    
                    // Buffer to read 10K bytes in chunk:
                    byte[] buffer = new Byte[10000];
                    int length = 10000;
                    int offset = 0;
                    while (length > 0)
                    {
                        length = responseStream.Read(buffer, 0, length);
                        offset += length;
                        file.Write(buffer, 0, length);
                    }
                }
                catch (Exception e)
                {}
                finally
                {
                    if(file != null)
                        file.Close();
                    if(responseStream != null)
                        responseStream.Close();
                    retryAttempt = true;
                }
                
                return downloadFileName;
            }

            StreamReader reader = new StreamReader(responseStream);
            responseText = reader.ReadToEnd();

            reader.Close();
            responseStream.Close();

            return responseText;
        }


        /// <summary>
        /// Encode PostBody 
        /// </summary>
        /// <remarks>
        /// Encode special characters (such as %, space, <, >, \, and &) to percent values.
        /// </remarks>
        /// <param name="postbody">Text to be encoded</param>
        /// <returns>Encoded text.</returns>
        public string EncodePostbody(string postbody)
        {
            postbody = postbody.Replace(@"%", @"%25");
            postbody = postbody.Replace(@" ", @"%20");
            postbody = postbody.Replace(@"<", @"%3c");
            postbody = postbody.Replace(@">", @"%3e");
            postbody = postbody.Replace("\"", @"%22"); // double quote
            postbody = postbody.Replace(@"&", @"%26");

            return postbody;
        }
#endregion
    }
}

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
using System.Text;
using Microsoft.Win32;
using ESRI.ArcGIS.CatalogUI;
using ESRI.ArcGIS.Catalog;

using System.IO;
using System.Net;
using ESRI.ArcGIS.Geodatabase;
using ESRI.ArcGIS.ConversionTools;
using ESRI.ArcGIS.Carto;
namespace com.esri.gpt.publish
{
    /// <summary>
    /// Makes publication request.
    /// </summary>
    public class PublicationRequest : IPublicationRequest
    {
        private static string endPoint = null;
        #region Private Variable(s)
        PublicationParams parameters;
        #endregion        
        #region Constructor
        /// <summary>
        /// Publication request constructor
        /// </summary>
        /// <param name="param"></param>
        public PublicationRequest(PublicationParams param)
        {
            parameters = param;
        }
        #endregion
        #region Methods
        /// <summary>
        /// Translates metadata document 
        /// </summary>
        /// <remarks>
        /// used to translate source metadata to another format using xslt.
        /// </remarks>
        /// <param name="propertySet">IXmlPropertySet2 metadata to be converted</param>
        /// <param name="xmlFilePaths">string[] output string array of file paths.</param>
        /// <returns>Returns true if execution was successful.</returns>
        private bool TranslateMetadata(IXmlPropertySet2 propertySet, out string[] xmlFilePaths)
        {
            const string noHeader = "";
            const bool ansiRestrict = false;
            string sourceXml = Path.GetTempFileName();

            propertySet.SaveAsFile(null, noHeader, ansiRestrict, ref sourceXml);
            ESRITranslator gpTool = GpHelper.xmlTranslator(sourceXml);

            // Request that the GpHelper execute the instantiated tool. A wrapper is
            // used to abstract error handling and allow a simple integer success code
            // equivalency check to determine success/failure.
            return (GpHelper.Execute(gpTool, out xmlFilePaths) == 0);
        }
        /// <summary>
        /// Publishes metadata document 
        /// </summary>
        /// <remarks>
        /// used to publish metadata document.
        /// </remarks>
        /// <param name="xmlMetadata">Metadata to be converted</param>
        /// <param name="mdFileID">Unique file identifier</param>
        /// <param name="publicationResults">string[] output publication results.</param>
        /// <returns>Returns true if execution was successful.</returns>
        private bool PublishMetadata(string xmlMetadata, string mdFileID, out string[] publicationResults)
        {
            // Request that the GpHelper instantiate an MDPublisher geoprocessing
            // tool and set tool's parameters.
            MDPublisher gpTool = GpHelper.xmlPublisher(xmlMetadata, mdFileID, parameters);

            // Request that the GpHelper execute the instantiated tool. A wrapper is
            // used to abstract error handling and allow a simple integer success code
            // equivalency check to determine success/failure.
            return (GpHelper.Execute(gpTool, out publicationResults) == 0);
        }
        /// <summary>
        /// Translates metadata document 
        /// </summary>
        /// <remarks>
        /// used to translate metadata document.
        /// </remarks>
        /// <param name="sourceXml">Metadata to be converted</param>
        /// <param name="xsltFileName">Translation xslt</param>
        /// <param name="xmlFileName">Metadata xml file name.</param>
        /// <returns>Returns true if execution was successful.</returns>
        private bool TranslateMetadata(string sourceXml, String xsltFileName, ref String xmlFileName)
        {

            ESRITranslator gpTool = GpHelper.xmlTranslator(sourceXml, xsltFileName, xmlFileName);

            // Request that the GpHelper execute the instantiated tool. A wrapper is
            // used to abstract error handling and allow a simple integer success code
            // equivalency check to determine success/failure.
            string[] xmlFilePaths;
            return (GpHelper.Execute(gpTool, out xmlFilePaths) == 0);
        }
        /// <summary>
        /// Imports xml metadata document 
        /// </summary>
        /// <remarks>
        /// used to import xml metadata document.
        /// </remarks>
        /// <param name="sourceXml">Metadata to be converted</param>
        /// <param name="mdFileID">Unique file identifier</param>
        /// <returns>Returns true if execution was successful.</returns>
        private bool ImportMetadata(string sourceXml, ref String xmlFileName)
        {

            ImportMetadata gpTool = GpHelper.xmlImporter(sourceXml, xmlFileName);

            // Request that the GpHelper execute the instantiated tool. A wrapper is
            // used to abstract error handling and allow a simple integer success code
            // equivalency check to determine success/failure.
            string[] xmlFilePaths;
            return (GpHelper.Execute(gpTool, out xmlFilePaths) == 0);
        }

        public String makeAgsUrl(IGxObject pGxObject)
        {

            String entity = "";
            String server = pGxObject.Parent.Parent.Category;
            String category = pGxObject.Category;
            String s = "";
            String agsServerUrl = "";
            int httpIndex = server.ToLower().IndexOf("http:");
            int httpsIndex = server.ToLower().IndexOf("https:");
            bool isLeafLevelService = false;
            if (httpIndex == -1 && httpsIndex == -1)
            {
                server = pGxObject.Parent.Category;
                httpIndex = server.ToLower().IndexOf("http:");
                httpsIndex = server.ToLower().IndexOf("https:");
                isLeafLevelService = true;
            }
            if (httpIndex > -1)
            {
                s = server.Substring(server.IndexOf("http:")).ToLower();
            }
            else if (httpsIndex > -1)
            {
                s = server.Substring(server.IndexOf("https:")).ToLower();
            }

            if (category.Equals("Map Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/MapServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/MapServer";
                }

            }
            else if (category.Equals("Geodata Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/GeoDataServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/GeoDataServer";
                }

            }
            else if (category.Equals("Geometry Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/GeometryServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/GeometryServer";
                }

            }
            else if (category.Equals("Geocode Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/GeocodeServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/GeocodeServer";
                }


            }
            else if (category.Equals("Geoprocessing Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/GPServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/GPServer";
                }

            }
            else if (category.Equals("Globe Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/GlobeServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/GlobeServer";
                }

            }
            else if (category.Equals("Image Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/ImageServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/ImageServer";
                }
            }
            else if (category.Equals("Network Service", System.StringComparison.CurrentCultureIgnoreCase))
            {
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                if (!isLeafLevelService)
                {
                    agsServerUrl = s + "/" + pGxObject.Parent.BaseName + "/" + pGxObject.BaseName + "/NetworkServer";
                }
                else
                {
                    agsServerUrl = s + "/" + pGxObject.BaseName + "/NetworkServer";
                }

            }
            else if (category.StartsWith("ArcGIS Server Folder", System.StringComparison.CurrentCultureIgnoreCase))
            {
                server = pGxObject.Parent.Category;
                httpIndex = server.ToLower().IndexOf("http:");
                httpsIndex = server.ToLower().IndexOf("https:");

                if (httpIndex > -1)
                {
                    s = server.Substring(server.IndexOf("http:")).ToLower();
                }
                else if (httpsIndex > -1)
                {
                    s = server.Substring(server.IndexOf("https:")).ToLower();
                }

                s = s.Replace("arcgis/services", "arcgis/rest/services");
                agsServerUrl = s + "/" + pGxObject.BaseName;
            }
            else if (category.StartsWith("ArcGIS Server", System.StringComparison.CurrentCultureIgnoreCase))
            {
                server = pGxObject.Category;
                httpIndex = server.ToLower().IndexOf("http:");
                httpsIndex = server.ToLower().IndexOf("https:");

                if (httpIndex > -1)
                {
                    s = server.Substring(server.IndexOf("http:")).ToLower();
                }
                else if (httpsIndex > -1)
                {
                    s = server.Substring(server.IndexOf("https:")).ToLower();
                }
                s = s.Replace("arcgis/services", "arcgis/rest/services");
                agsServerUrl = s;
            }

            entity += "url=" + agsServerUrl + "&name=" + pGxObject.Name;
           
            // endPoint = "SERVLET CONNECTOR";

            return entity;
        }

        /// <summary>
        /// Converts xml as ArcXml
        /// </summary>
        /// <param name="sXml">xml reference string</param>
        private void makeArcXmlRequest(ref String sXml)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ARCXML version=\"1.1\"><REQUEST><PUBLISH_METADATA><PUT_METADATA><!--")
            .Append(sXml)
            .Append("--></PUT_METADATA></PUBLISH_METADATA></REQUEST></ARCXML>");

            sXml = sb.ToString();
        }
        /// <summary>
        /// Converts xml to Csw Insert xml 
        /// </summary>
        /// <param name="sXml">xml reference string</param>
        private void makeCswInsertRequest(ref String sXml)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("<csw:Transaction service=\"CSW\" version=\"2.0.2\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"><csw:Insert>")
            .Append(sXml).Append("</csw:Insert></csw:Transaction>");

            sXml = sb.ToString();
        }
        /// <summary>
        /// Converts xml to Soap Csw Insert xml
        /// </summary>
        /// <param name="sXml">xml reference string</param>
        private void makeCswSOAPInsertRequest(ref String sXml)
        {
            makeCswInsertRequest(ref sXml);
            StringBuilder sb = new StringBuilder();
            sb.Append("<?xml version=\"1.0\"?><SOAP-ENV:Envelope")
            .Append(" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
            .Append("<SOAP-ENV:Body>")            
            .Append(sXml)
            .Append("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
            sXml = sb.ToString();
        }       
        /// <summary>
        /// Determine type of server url provided to publication request.
        /// Endpoint could be REST (DEFAULT) / CSW / CSW SOAP / SERVLET CONNECTOR
        /// </summary>
        public void determineEndPoint()
        {
            string url = parameters.ServerUrl.Trim().ToLower();
            if (url.Contains("/csw/publication") || url.Contains("/csw202/publication"))               
            {
                if (url.Contains("/csw/publication/soap") || url.Contains("/csw202/publication/soap"))
                {
                    endPoint = "CSW SOAP";
                }else
                {
                    endPoint = "CSW";
                }
            }
            else if (url.Contains("/rest/manage/document"))
            {
                endPoint = "REST";
            }
            else if (url.Contains("/servlet"))
            {
                endPoint = "SERVLET CONNECTOR";
            }
            else 
            {
                endPoint = "DEFAULT";
            }
        }
        /// <summary>
        /// Extracts metadata xml of entity and send publication request to server.
        /// </summary>
        /// <param name="entity">Entity to publish</param>
        /// <returns>Publish results</returns>
        public string[] publish(IGxObject entity)
        {
            string[] results = new string[1];
            IMetadata metaData = (IMetadata)entity;

            //Get the xml property set from the metadata
            IXmlPropertySet2 xml = (IXmlPropertySet2)metaData.Metadata;
            string[] xmlFilePaths;
            TranslateMetadata(xml, out xmlFilePaths);
            string sXml = File.ReadAllText(xmlFilePaths[0], Encoding.UTF8); /// untransformed xml.
            return publish(entity, sXml);
          
        }
        /// <summary>
        /// Sends publication request to server.
        /// </summary>
        /// <param name="entity">Entity to publish</param>
        /// <param name="sXml">metadata sml</param>
        /// <returns>Publish results</returns>
        public string[] publish(IGxObject entity, String sXml)
        {
            string[] results = new string[1];
            String resp = null;
            string sUrl = parameters.ServerUrl.Trim();
        //    string endPoint = determineEndPoint();
            #region Cascade EndPoint
            if (endPoint == "REST" || endPoint == "DEFAULT")
            {
                if (endPoint == "DEFAULT")
                {
                    sUrl += "/rest/manage/document";
                }
                resp = SubmitHttpRequest(sXml, sUrl);
                if (resp.ToLower().Contains("error") || resp.ToLower().Contains("exception"))
                {                
                    String[] parts = sUrl.Split(new String[] { "/rest/manage/document" }, StringSplitOptions.RemoveEmptyEntries);
                    sUrl = parts[0] + "/csw/publication";
                    endPoint = "CSW";                                               
                }
            }
            if (endPoint == "CSW")
            {
                makeCswInsertRequest(ref sXml);
                resp = SubmitHttpRequest(sXml, sUrl);
                if (resp.ToLower().Contains("error") || resp.ToLower().Contains("exception"))
                {
                    String[] parts = null;
                    if (sUrl.ToLower().EndsWith("/csw"))
                    {
                        sUrl = sUrl;
                    }
                    else if(sUrl.Contains("/csw/publication"))
                        parts = sUrl.Split(new String[] { "/csw/publication" }, StringSplitOptions.RemoveEmptyEntries);                                           
                    else
                        parts = sUrl.Split(new String[] { "/csw202/publication" }, StringSplitOptions.RemoveEmptyEntries);                                           
                    sUrl = parts[0];
                    endPoint = "SERVLET CONNECTOR";
                }
            }
            else if (endPoint == "CSW SOAP")
            {
                makeCswSOAPInsertRequest(ref sXml);
                resp = SubmitHttpRequest(sXml, sUrl);
                if (resp.ToLower().Contains("error") || resp.ToLower().Contains("exception"))
                {
                    String[] parts = null;
                    if (sUrl.Contains("/csw/publication/soap"))
                        parts = sUrl.Split(new String[] { "/csw/publication/soap" }, StringSplitOptions.RemoveEmptyEntries);
                    else
                        parts = sUrl.Split(new String[] { "/csw202/publication/soap" }, StringSplitOptions.RemoveEmptyEntries);
                    sUrl = parts[0];
                    endPoint = "SERVLET CONNECTOR";
                }
            }
            if(endPoint == "SERVLET CONNECTOR")
            {
                //   /servlet/com.esri.esrimap.Esrimap?servicename=login
                string baseUrl = sUrl;
                sUrl +=  "/com.esri.esrimap.Esrimap?servicename=login";
                SubmitHttpRequest(sXml, sUrl);
                sUrl = baseUrl + "/com.esri.esrimap.Esrimap?servicename=" + parameters.Service + ":EB:" + entity.FullName;
                makeArcXmlRequest(ref sXml);
                resp = SubmitHttpRequest(sXml, sUrl);
            }
            PublishForm.Logger.writeLog("EndPoint Type : " + endPoint);
            PublishForm.Logger.writeLog("Publish url : " + sUrl);
            PublishForm.Logger.writeLog("Publish request : " + sXml);
            PublishForm.Logger.writeLog("Publish response : " + resp);
            #endregion
            if (resp.ToLower().Contains("error") || resp.ToLower().Contains("exception") || resp.ToLower().Contains("e-r-r-o-r"))
            {
                resp = resp.Replace("e-r-r-o-r", "error");
                results[0] = StringMessages.PublishFailure + " " + entity.FullName + "\n Cause : " + resp;
            }
            else
                results[0] = StringMessages.PublishSuccess + " " + entity.FullName;
            return results;
        }        
        /// <summary>
        /// Submit HTTP Request 
        /// </summary>
        /// <remarks>
        /// Submit an HTTP request.
        /// </remarks>
        /// <param name="url">Publication endpoint url</param>      
        /// <param name="postData">Data to be posted</param>
        /// <returns>Response in plain text.</returns>
        private string SubmitHttpRequest(string postData, string url)
        {
            String responseText = "";
            HttpWebResponse response = null;
            try
            {
            CookieContainer cookieContainer = new CookieContainer();

            HttpWebRequest request;
            Uri uri = new Uri(url);
            request = (HttpWebRequest)WebRequest.Create(uri);
            request.Method = "POST";
            byte[] authBytes = Encoding.UTF8.GetBytes((parameters.UserName + ":" + parameters.Password).ToCharArray());
            request.Headers["Authorization"] = "Basic " + Convert.ToBase64String(authBytes);
            request.PreAuthenticate = true;

            // Credential and cookies
            request.CookieContainer = cookieContainer;          
            UTF8Encoding utf8 = new UTF8Encoding();
            Byte[] encodedBytes = utf8.GetBytes(postData);
            request.ContentType = "text/xml; charset=UTF-8"; 
            request.ContentLength = encodedBytes.Length;

            Stream requestStream = request.GetRequestStream();
            requestStream.Write(encodedBytes, 0, encodedBytes.Length);
            requestStream.Close();
            
            // get response
            response = (HttpWebResponse)request.GetResponse();                
            if (cookieContainer.GetCookies(uri) == null)
            {
                cookieContainer.Add(uri, response.Cookies);
            }
            Stream responseStream = response.GetResponseStream();
            StreamReader reader = new StreamReader(responseStream);
            responseText = reader.ReadToEnd();
            reader.Close();
            responseStream.Close();
                
            }
            catch (Exception e)
            {
               
                if(e is WebException){
                    WebResponse resp = ((WebException)e).Response;
                    Stream respStream = resp.GetResponseStream();
                    StreamReader sReader = new StreamReader(respStream);
                    String respText = sReader.ReadToEnd();
                    sReader.Close();
                    respStream.Close();
                    respText += " e-r-r-o-r";
                    return respText.ToLower().Replace("error", "e-r-r-o-r");
                }

                String sc = e.Message;
                
                if (sc.Contains("404") || sc.Contains("501"))
                {
                    PublishForm.Logger.writeLog(e.StackTrace);
                    return "Error: " + e.Message;
                }
                else
                {
                    sc += " e-r-r-o-r";
                    return sc.ToLower().Replace("error","e-r-r-o-r");
                }
            }

            return responseText;
        }
        /// <summary>
        /// Encodes xml string
        /// </summary>
        /// <param name="sPostbody"></param>
        /// <returns></returns>
        private string encodePostBody(string sPostbody)
        {
            sPostbody = sPostbody.Replace("%", "%25");
            // sPostbody = sPostbody.Replace("?", "%3F");
            sPostbody = sPostbody.Replace(" ", "%20");
            sPostbody = sPostbody.Replace("<", "%3c");
            sPostbody = sPostbody.Replace(">", "%3e");
            sPostbody = sPostbody.Replace("\"\"", "%22");
            sPostbody = sPostbody.Replace("&", "%26");

            return sPostbody;
        }              
        #endregion
    }
}

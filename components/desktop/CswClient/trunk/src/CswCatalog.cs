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
using System.Xml;
using System.Xml.Xsl;
using System.Xml.XPath;


namespace com.esri.gpt.csw { /// <summary>
    /// Maintains information of CSW Catalog
    /// </summary>
    /// <remarks>
    /// The catalogs contain all the information like url, profile information
    /// credentials and capabilities.
    /// </remarks>
    /// 
    public class CswCatalog:IComparable
    {
        private static int IDCounter = 1 ;
        private string id;
        private string name;
        private bool locking;
        private string url;
        private string baseurl;
        private CswProfile profile;
        private CswCatalogCapabilities capabilities;
        private bool isConnect;

        #region "Properties"
        /// <summary>
        /// Name parameter
        /// </summary>
        public string Name {
            set {
                if (value.Length > 0)
                    name = value;
                else
                    name = url;
            }
            get {
                return name;
            }
            
        }
        /// <summary>
        /// URL parameter
        /// </summary>
        public string URL {
            set {
                url = value;
            }
            get {
                return url;
            }
        }
        /// <summary>
        /// BaseURL parameter
        /// </summary>
        public string BaseURL {
            set {
                baseurl = value;
            }
            get {
                return baseurl;
            }
        }
        /// <summary>
        /// Profile parameter
        /// </summary>
        public CswProfile Profile {
            set {
                profile = value;
            }
            get {
                return profile;
            }
        }
        /// <summary>
        /// HashKey parameter
        /// </summary>
        public int HashKey {
            get {
                return url.GetHashCode();
            }
        }

        /// <summary>
        /// ID parameter
        /// </summary>
        public string ID {
            get {
                return id;
            }
        }

        /// <summary>
        /// Locking parameter
        /// </summary>
        public bool Locking {
            set {
                locking = value;
            }
            get {
                return locking;
            }

        }
        /// <summary>
        /// Capabilities parameter
        /// </summary>

        internal CswCatalogCapabilities Capabilities
        {
            get
            {
                return capabilities;
            }
        }


    #endregion                
        # region constructor definition
        /// <summary>
        /// Constructor
        /// </summary>
            public CswCatalog() {
                id = "catalog"+ IDCounter.ToString();
                IDCounter++;
            }
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="surl"></param>
        /// <param name="sname"></param>
        /// <param name="oprofile"></param>
            public CswCatalog(String surl, String sname, CswProfile oprofile) {
                id = "catalog" + IDCounter.ToString();
                IDCounter++;
                URL = surl;
                Profile = oprofile;
                Name = sname;
                Locking = false;
                        
            }

        #endregion
        #region "PrivateFunction"

            /// <summary>
            ///  To retrieve informations about the CSW service.
            /// </summary>
            /// <remarks>
            /// </remarks>
            /// <param name="param1">capabilities baseurl</param>
            /// <returns>Response the get capabilities url</returns>
            private string GetCapabilities(string capabilitiesurl) {
                try {
                    CswClient client = new CswClient();
                    Utils.logger.writeLog("GetCapabilities url : " + capabilitiesurl);
                    string response = client.SubmitHttpRequest("GET", capabilitiesurl, "");
                    Utils.logger.writeLog("GetCapabilities response : " + response);
                    //Console.WriteLine()
                    

                    XmlDocument xmlDoc = new XmlDocument();
                    if (response == null)
                        return null ;
                    xmlDoc.LoadXml(response); 
                    XmlNamespaceManager xmlnsManager = new XmlNamespaceManager(xmlDoc.NameTable);
                    if (this.Profile.CswNamespace.Length <= 0) {
                        this.Profile.CswNamespace = CswProfiles.DEFAULT_CSW_NAMESPACE;
                    }
                    xmlnsManager.AddNamespace("ows", "http://www.opengis.net/ows");
                    xmlnsManager.AddNamespace("csw", this.Profile.CswNamespace);
                    xmlnsManager.AddNamespace("wrs10", "http://www.opengis.net/cat/wrs/1.0");
                    xmlnsManager.AddNamespace("wrs", "http://www.opengis.net/cat/wrs");
                    xmlnsManager.AddNamespace("xlink", "http://www.w3.org/1999/xlink");
                    xmlnsManager.AddNamespace("wcs", "http://www.opengis.net/wcs");
                    if (xmlDoc.SelectSingleNode("/csw:Capabilities|/wrs:Capabilities| /wrs10:Capabilities | /wcs:WCS_Capabilities", xmlnsManager) != null)
                        return response;
                    else
                        return null;
                }
                catch (Exception ex) {
                    Utils.logger.writeLog(ex.StackTrace);
                    throw ex;
                }
            }

            /// <summary>
            ///  To retrieve informations about the CSW service.
            /// </summary>
            /// <remarks>
            /// </remarks>
            /// <param name="param1">capabilitiesxml details </param>
            private bool ParseCapabilities(string capabilitiesxml) {
                try {
                    XmlDocument xmlDoc = new XmlDocument();
                    xmlDoc.LoadXml(capabilitiesxml);
                    XmlNamespaceManager xmlnsManager = new XmlNamespaceManager(xmlDoc.NameTable);
                    if (this.Profile.CswNamespace.Length <= 0) {
                        this.Profile.CswNamespace = CswProfiles.DEFAULT_CSW_NAMESPACE;
                    }
                    xmlnsManager.AddNamespace("ows", "http://www.opengis.net/ows");
                    xmlnsManager.AddNamespace("csw", this.Profile.CswNamespace);
                    xmlnsManager.AddNamespace("wrs", "http://www.opengis.net/cat/wrs");
                    xmlnsManager.AddNamespace("wrs10", "http://www.opengis.net/cat/wrs/1.0");
                    xmlnsManager.AddNamespace("xlink", "http://www.w3.org/1999/xlink");
                    xmlnsManager.AddNamespace("wcs", "http://www.opengis.net/wcs");
                    //setting capabilities
                    if (capabilities == null) capabilities = new CswCatalogCapabilities();

                    if (isWCSService(capabilitiesxml))
                    {
                        capabilities.GetRecordByID_GetURL = xmlDoc.SelectSingleNode("/wcs:WCS_Capabilities/wcs:Capability/wcs:Request/wcs:GetCoverage/wcs:DCPType/wcs:HTTP/wcs:Get/wcs:OnlineResource/@xlink:href", xmlnsManager).Value;

                        if (capabilities.GetRecordByID_GetURL == null || capabilities.GetRecordByID_GetURL.Trim().Length == 0)
                        {
                            Utils.logger.writeLog(Resources.NoValidCoverageUrl);
                            throw new Exception(Resources.NoValidCoverageUrl);
                        }
                    }
                    else
                    {

                        if (this.profile.Name == "INSPIRE CSW 2.0.2 AP ISO")
                        {
                            capabilities.GetRecords_PostURL = xmlDoc.SelectSingleNode("//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[translate(ows:Constraint/ows:Value, 'SOAP', 'soap')='soap']/@xlink:href", xmlnsManager).Value;
                            capabilities.GetRecords_IsSoapEndPoint = true;
                        } else
                            capabilities.GetRecords_PostURL = xmlDoc.SelectSingleNode("//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[translate(ows:Constraint/ows:Value, 'XML', 'xml')='xml']/@xlink:href", xmlnsManager).Value;

                        if (capabilities.GetRecords_PostURL == null || capabilities.GetRecords_PostURL.Trim().Length == 0)
                        {
                            Utils.logger.writeLog(Resources.NoValidPostUrl);
                            throw new Exception(Resources.NoValidPostUrl);
                        }

                        capabilities.GetRecordByID_GetURL = xmlDoc.SelectSingleNode("//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Get/@xlink:href", xmlnsManager).Value;
                    }

                    return true;
                }
                catch (Exception e) {
                    Utils.logger.writeLog(e.StackTrace);
                    return false;
                }
                
            }

            private Boolean isWCSService(String capabilitiesXml)
            {
                return capabilitiesXml.IndexOf("WCS_Capabilities") > -1 ? true : false;

            }

        #endregion
        #region "PublicFunction"

            /// <summary>
            ///  To connect to a catalog service.
            ///  The capabilties details are populated based on the service. 
            /// </summary>
            /// <remarks>
            /// </remarks>
            /// <returns>true if connection can be made to the csw service</returns>
            public bool Connect() {
                StringBuilder sb = new StringBuilder();
                sb.AppendLine(DateTime.Now + " Sending GetCapabilities request to URL : " + URL);
                string capabilitesxml = GetCapabilities(URL);            
                if (capabilitesxml != null){
                    sb.AppendLine(DateTime.Now + " GetCapabilitiesResponse xml : " + capabilitesxml);
                    ParseCapabilities(capabilitesxml);
                    sb.AppendLine(DateTime.Now + " Parsed GetCapabilitiesResponse xml...");                
                    isConnect = true;
                    Utils.logger.writeLog(sb.ToString());
                    return true;
                }
                else {
                    sb.AppendLine(DateTime.Now + " Failed to connect to GetCapabilities endpoint.");
                    Utils.logger.writeLog(sb.ToString());
                    isConnect = false;
                    return false;
                }           
            }

            /// <summary>
            ///  To test if already connected to a catalog service.
            /// </summary>
            /// <remarks>
            /// </remarks>
            /// <returns>true if connection has already been made to the csw service else false</returns>
            public bool IsConnected() {        
                   return isConnect;
           
            }
            /// <summary>
            ///  Resets catalog connection
            /// </summary>
            public void resetConnection()
            {
                isConnect = false;

            }
            /// <summary>
            ///  Compares CswCatalog objects by name
            /// </summary>
            public int CompareTo(object obj)
            {
                if (obj is CswCatalog)
                {
                    CswCatalog catalog = (CswCatalog)obj;
                    return this.Name.CompareTo(catalog.Name);
                }
                throw new ArgumentException("object is not a CswCatalog");
            }

        #endregion
    }
}

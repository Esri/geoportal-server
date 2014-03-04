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
using System.IO;
using System.Globalization;

namespace com.esri.gpt.csw
{
    /// <summary>
    /// CswProfile class represents CswProfile object
    /// </summary>
    public class CswProfile
    {
        private string id;
        private string name;
        private string cswnamespace;
        private string kvp;
        private string description;
        private string requestxslt;
        private string responsexslt;
        private string metadataxslt;
        private string displayResponseXslt;
        private bool filter_livedatamap;
        private bool filter_extentsearch;
        private bool filter_spatialboundary;
        private XslCompiledTransform requestxsltobj;
        private XslCompiledTransform responsexsltobj;
        private XslCompiledTransform metadataxsltobj;
        private XslCompiledTransform displayResponseXsltObj;

        #region "Properties"
        /// <summary>
        /// ID parameter
        /// </summary>
        public string ID
        {
            get
            {
                return id;
            }
            set
            {
                id = value;
            }
        }
        /// <summary>
        /// Name parameter
        /// </summary>
        public string Name
        {
            set
            {
                name = value;
            }
            get
            {
                return name;
            }
        }
        /// <summary>
        /// CswNamespace parameter
        /// </summary>
        public string CswNamespace
        {
            get
            {
                return cswnamespace;
            }
            set
            {
                cswnamespace = value;
            }
        }
        /// <summary>
        /// Description parameter
        /// </summary>
        public string Description
        {
            set
            {
                description = value;
            }
            get
            {
                return description;
            }
        }
        /// <summary>
        /// SupportContentTypeQuery parameter
        /// </summary>
        public bool SupportContentTypeQuery
        {
            set
            {
                filter_livedatamap = value;
            }
            get
            {
                return filter_livedatamap;
            }
        }
        /// <summary>
        /// SupportSpatialQuery parameter
        /// </summary>
        public bool SupportSpatialQuery
        {
            set
            {
                filter_extentsearch = value;
            }
            get
            {
                return filter_extentsearch;
            }
        }
        /// <summary>
        /// SupportSpatialBoundary parameter
        /// </summary>
        public bool SupportSpatialBoundary
        {
            get
            {
                return filter_spatialboundary;
            }
            set
            {
                filter_spatialboundary = value;
            }
        }




        #endregion
        # region "ConstructorDefinition"
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="sid"></param>
        /// <param name="sname"></param>
        /// <param name="sdescription"></param>
        public CswProfile(string sid, string sname, string sdescription)
        {
            ID = sid;
            Name = sname;
            Description = sdescription;
        }
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="sid"></param>
        /// <param name="sname"></param>
        /// <param name="sdescription"></param>
        /// <param name="skvp"></param>
        /// <param name="srequestxslt"></param>
        /// <param name="sresponsexslt"></param>
        /// <param name="smetadataxslt"></param>
        /// <param name="sdisplayResponseXslt"></param>
        /// <param name="livedatamap"></param>
        /// <param name="extentsearch"></param>
        /// <param name="spatialboundary"></param>
        public CswProfile(string sid, string sname, string sdescription, string skvp, string srequestxslt, string sresponsexslt, string smetadataxslt, string sdisplayResponseXslt, bool livedatamap, bool extentsearch, bool spatialboundary)
        {
            ID = sid;
            Name = sname;
            CswNamespace = CswProfiles.DEFAULT_CSW_NAMESPACE;
            Description = sdescription;
            kvp = skvp;
            requestxslt = srequestxslt;
            responsexslt = sresponsexslt;
            metadataxslt = smetadataxslt;
            displayResponseXslt = sdisplayResponseXslt;
            filter_livedatamap = livedatamap;
            filter_extentsearch = extentsearch;
            filter_spatialboundary = spatialboundary;
            //enable document() support
            XsltSettings settings = new XsltSettings(true, true);
            XmlUrlResolver xmlurlresolver = new XmlUrlResolver();
            responsexsltobj = new XslCompiledTransform();
            responsexsltobj.Load(responsexslt, settings, xmlurlresolver);
            requestxsltobj = new XslCompiledTransform();
            requestxsltobj.Load(requestxslt, settings, xmlurlresolver);
            if (metadataxslt.Length > 0)
            {
                metadataxsltobj = new XslCompiledTransform();
                //enable document() support
                metadataxsltobj.Load(metadataxslt, settings, xmlurlresolver);
            }

            if (displayResponseXslt.Length > 0)
            {
                displayResponseXsltObj = new XslCompiledTransform();
                //enable document() support
                displayResponseXsltObj.Load(displayResponseXslt, settings, xmlurlresolver);
            }

        }
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="sid"></param>
        /// <param name="sname"></param>
        /// <param name="scswnamespace"></param>
        /// <param name="sdescription"></param>
        /// <param name="skvp"></param>
        /// <param name="srequestxslt"></param>
        /// <param name="sresponsexslt"></param>
        /// <param name="smetadataxslt"></param>
        /// <param name="sdisplayResponseXslt"></param>
        /// <param name="livedatamap"></param>
        /// <param name="extentsearch"></param>
        /// <param name="spatialboundary"></param>
        public CswProfile(string sid, string sname, string scswnamespace, string sdescription, string skvp, string srequestxslt, string sresponsexslt, string smetadataxslt, string sdisplayResponseXslt, bool livedatamap, bool extentsearch, bool spatialboundary)
        {
            ID = sid;
            Name = sname;
            CswNamespace = scswnamespace;
            Description = sdescription;
            kvp = skvp;
            requestxslt = srequestxslt;
            responsexslt = sresponsexslt;
            metadataxslt = smetadataxslt;
            displayResponseXslt = sdisplayResponseXslt;
            filter_livedatamap = livedatamap;
            filter_extentsearch = extentsearch;
            filter_spatialboundary = spatialboundary;
            //enable document() support
            XsltSettings settings = new XsltSettings(true, true);
            XmlUrlResolver xmlurlresolver = new XmlUrlResolver();
            responsexsltobj = new XslCompiledTransform();
            responsexsltobj.Load(responsexslt, settings, xmlurlresolver);
            requestxsltobj = new XslCompiledTransform();
            requestxsltobj.Load(requestxslt, settings, xmlurlresolver);
            if (metadataxslt.Length > 0)
            {
                metadataxsltobj = new XslCompiledTransform();
                metadataxsltobj.Load(metadataxslt, settings, xmlurlresolver);

            }

            if (displayResponseXslt.Length > 0)
            {
                displayResponseXsltObj = new XslCompiledTransform();
                //enable document() support
                displayResponseXsltObj.Load(displayResponseXslt, settings, xmlurlresolver);
            }

        }
        #endregion
        # region "PublicFunctions"

        /// <summary>
        /// Parse a CSW response. 
        /// </summary>
        /// <remarks>
        /// The CSW response is parsed and the records collection is populated
        /// with the result.The reponse is parsed based on the response xslt.
        /// </remarks>
        /// <param name="param1">The string response</param>
        /// <param name="param2">The recordlist which needs to be populated</param>
        public void ReadCSWGetRecordsResponse(string responsestring, CswRecords recordslist)
        {

            try
            {
                TextReader textreader = new StringReader(responsestring);
                XmlTextReader xmltextreader = new XmlTextReader(textreader);
                //load the Xml doc
                XPathDocument xPathDoc = new XPathDocument(xmltextreader);
                if (responsexsltobj == null)
                {
                    responsexsltobj = new XslCompiledTransform();
                    XsltSettings settings = new XsltSettings(true, true);
                    responsexsltobj.Load(responsexslt, settings, new XmlUrlResolver());
                }
                //create the output stream
                StringWriter writer = new StringWriter();
                //do the actual transform of Xml
                responsexsltobj.Transform(xPathDoc, null, writer);
                writer.Close();
                //populate CswRecords
                XmlDocument doc = new XmlDocument();
                doc.LoadXml(writer.ToString());
                XmlNodeList xmlnodes = doc.GetElementsByTagName("Record");
                foreach (XmlNode xmlnode in xmlnodes)
                {
                    CswRecord record = new CswRecord();
                    record.ID = xmlnode.SelectSingleNode("ID").InnerText;
                    record.Title = xmlnode.SelectSingleNode("Title").InnerText;
                    record.Abstract = xmlnode.SelectSingleNode("Abstract").InnerText;
                    String lowercorner = "";
                    if (this.SupportSpatialBoundary)
                    {
                        lowercorner = xmlnode.SelectSingleNode("LowerCorner").InnerText;
                    }
                    String uppercorner = "";
                    if (this.SupportSpatialBoundary)
                    {
                        uppercorner = xmlnode.SelectSingleNode("UpperCorner").InnerText;
                    }
                    if ((lowercorner.Length > 0 && uppercorner.Length > 0))
                    {
                        /*  record.BoundingBox.Maxx = Double.Parse(lowercorner.Substring(0, lowercorner.IndexOf(' ')));
                            record.BoundingBox.Miny = Double.Parse(lowercorner.Substring(lowercorner.IndexOf(' ') + 1));
                            record.BoundingBox.Minx = Double.Parse(uppercorner.Substring(0, uppercorner.IndexOf(' ')));
                            record.BoundingBox.Maxy = Double.Parse(uppercorner.Substring(uppercorner.IndexOf(' ') + 1));*/
                        Boolean parseFlag = false;
                        CultureInfo cultureInfo = new CultureInfo("en-us");
                        double pareseResult = 0.0;
                        parseFlag = Double.TryParse(lowercorner.Substring(0, lowercorner.IndexOf(' ')), NumberStyles.Number, cultureInfo, out pareseResult);
                        record.BoundingBox.Minx = pareseResult;
                        parseFlag = Double.TryParse(lowercorner.Substring(lowercorner.IndexOf(' ') + 1), NumberStyles.Number, cultureInfo, out pareseResult);
                        record.BoundingBox.Miny = pareseResult;
                        parseFlag = Double.TryParse(uppercorner.Substring(0, uppercorner.IndexOf(' ')), NumberStyles.Number, cultureInfo, out pareseResult);
                        record.BoundingBox.Maxx = pareseResult;
                        parseFlag = Double.TryParse(uppercorner.Substring(uppercorner.IndexOf(' ') + 1), NumberStyles.Number, cultureInfo, out pareseResult);
                        record.BoundingBox.Maxy = pareseResult;
                        if (parseFlag == false)
                        {
                            throw new Exception("Number format error");
                        }

                    }
                    else
                    {
                        record.BoundingBox.Maxx = 500.00;
                        record.BoundingBox.Miny = 500.00;
                        record.BoundingBox.Minx = 500.00;
                        record.BoundingBox.Maxy = 500.00;
                    }
                    XmlNode node = xmlnode.SelectSingleNode("Type");
                    if (node != null)
                    {
                        record.IsLiveDataOrMap = node.InnerText.Equals("liveData", StringComparison.OrdinalIgnoreCase);
                        if (!record.IsLiveDataOrMap)
                        {
                            record.IsLiveDataOrMap = node.InnerText.Equals("downloadableData", StringComparison.OrdinalIgnoreCase);
                        }
                    }
                    else
                    {
                        record.IsLiveDataOrMap = false;
                    }


                    XmlNode referencesNode = xmlnode.SelectSingleNode("References");
                    if (referencesNode != null)
                    {
                        String references = referencesNode.InnerText;

                        DcList list = new DcList();
                        list.add(references);
                        determineResourceUrl(record, list);

                        /*  LinkedList<String> serverList = list.get(DcList.getScheme(DcList.Scheme.SERVER));                    
                          if (serverList.Count > 0)
                          {
                              String serviceType = getServiceType(serverList.First.Value);
                              if (serviceType.Equals("aims") || serviceType.Equals("ags") || serviceType.Equals("wms") || serviceType.Equals("wcs"))
                              {
                                  record.MapServerURL = serverList.First.Value;
                              }
                          }*/
                    }
                    else
                        record.MapServerURL = "";


                    recordslist.AddRecord(record.ID.GetHashCode(), record);
                }
            }
            catch (Exception e)
            {
                throw e;
            }
            //return recordslist;

        }

        /// <summary>
        /// Determins resource urls
        /// </summary>
        /// <param name="cswRecord">CswRecord object</param>
        /// <param name="references">List of references</param>
        private void determineResourceUrl(CswRecord cswRecord, DcList references)
        {

            // initialize
            String resourceUrl = "";
            String serviceType = "";
            String serviceName = "";

            // determine the service url, name and type
            LinkedList<String> schemeVals = references.get(DcList.getScheme(DcList.Scheme.SERVER));
            if (schemeVals.Count > 0)
            {
                resourceUrl = chkStr(schemeVals.First.Value);
            }

            schemeVals = references.get(DcList.getScheme(DcList.Scheme.SERVICE));
            if (schemeVals.Count > 0)
            {
                serviceName = chkStr((schemeVals.First.Value));
            }

            schemeVals = references.get(DcList.getScheme(DcList.Scheme.SERVICE_TYPE));
            if (schemeVals.Count > 0)
            {
                serviceType = (schemeVals.First.Value);
            }
            if ((resourceUrl.Length > 0) && (serviceType.Length == 0))
            {
                serviceType = getServiceType(resourceUrl);
            }

            // handle the case where an ArcIMS service has been specified with 
            // server/service/serviceType parameters
            if ((resourceUrl.Length > 0) &&
                (serviceType.Equals("image", StringComparison.CurrentCultureIgnoreCase) ||
                 serviceType.Equals("feature") ||
                 serviceType.Equals("metadata")))
            {

                if ((serviceName.Length > 0))
                {
                    String esrimap = "servlet/com.esri.esrimap.Esrimap";
                    if (resourceUrl.IndexOf("esrimap") == -1)
                    {
                        if (resourceUrl.IndexOf("?") == -1)
                        {
                            if (!resourceUrl.EndsWith("/")) resourceUrl += "/";
                            resourceUrl = resourceUrl + esrimap + "?ServiceName=" + serviceName;
                        }
                    }
                    else
                    {
                        if (resourceUrl.IndexOf("?") == -1)
                        {
                            resourceUrl = resourceUrl + "?ServiceName=" + serviceName;
                        }
                        else if (resourceUrl.IndexOf("ServiceName=") == -1)
                        {
                            resourceUrl = resourceUrl + "&ServiceName=" + serviceName;
                        }
                    }
                }

                if (serviceType.Equals("image"))
                {
                    serviceType = "aims";
                }
            }

            // if the resource url has not been directly specified through a "scheme" attribute, 
            // then attempt to pick the best fit for the collection of references
            if (resourceUrl.Length == 0)
            {
                foreach (DcList.Value reference in references)
                {
                    if (reference != null)
                    {
                        String url = reference.getValue();
                        String type = getServiceType(url);
                        if (type.Length > 0)
                        {
                            resourceUrl = url;
                            serviceType = type;
                            break;
                        }
                    }
                }

            }

            // update the record
            cswRecord.MapServerURL = resourceUrl;
            cswRecord.ServiceName = serviceName;
            cswRecord.ServiceType = serviceType;

        }
        /// <summary>
        /// Checks if a string is null or empty
        /// </summary>
        /// <param name="s">string to check</param>
        /// <returns></returns>
        public static String chkStr(String s)
        {
            if (s == null)
            {
                return "";
            }
            else
            {
                return s.Trim();
            }
        }
        /// <summary>
        /// Parses service information from url
        /// </summary>
        /// <param name="msinfo">map service information</param>
        /// <param name="mapServerUrl">map service url</param>
        /// <param name="serviceType">map service type</param>
        public static void ParseServiceInfoFromUrl(MapServiceInfo msinfo, String mapServerUrl, String serviceType)
        {

            msinfo.Service = "Generic " + serviceType + " Service Name";
            String[] urlParts = mapServerUrl.Trim().Split('?');
            if (urlParts.Length > 0)
                msinfo.Server = urlParts[0];
            else
                msinfo.Server = mapServerUrl;

            String[] s = msinfo.Server.Split(new String[] { "/servlet/com.esri.esrimap.Esrimap" }, StringSplitOptions.RemoveEmptyEntries);
            msinfo.Server = s[0];

            if (urlParts.Length > 1)
            {
                String[] urlParams = urlParts[1].Trim().Split('&');

                foreach (String param in urlParams)
                {
                    String paramPrefix = param.ToLower().Trim();
                    if (paramPrefix.StartsWith("service=") || paramPrefix.StartsWith("servicename="))
                    {
                        msinfo.Service = param.Trim().Split('=')[1];
                    }
                    else if (paramPrefix.StartsWith("map="))
                    {
                        msinfo.ServiceParam = param.Trim();
                    }
                }

            }

        }

        /// <summary>
        /// Generate a CSW request string. 
        /// </summary>
        /// <remarks>
        /// The CSW request string is built.
        /// The request is string is build based on the request xslt.
        /// </remarks>
        /// <param name="param1">The search criteria</param>
        /// <returns>The request string</returns>
        public string GenerateCSWGetRecordsRequest(CswSearchCriteria search)
        {
            //build xml
            StringWriter writer = new StringWriter();
            StringBuilder request = new StringBuilder();
            request.Append("<?xml version='1.0' encoding='UTF-8'?>");
            request.Append("<GetRecords>");
            request.Append("<StartPosition>" + search.StartPosition + "</StartPosition>");
            request.Append("<MaxRecords>" + search.MaxRecords + "</MaxRecords>");
            request.Append("<KeyWord>" + this.XmlEscape(search.SearchText) + "</KeyWord>");
            request.Append("<LiveDataMap>" + search.LiveDataAndMapOnly.ToString() + "</LiveDataMap>");
            if (search.Envelope != null)
            {
                request.Append("<Envelope>");
                request.Append("<MinX>" + search.Envelope.MinX + "</MinX>");
                request.Append("<MinY>" + search.Envelope.MinY + "</MinY>");
                request.Append("<MaxX>" + search.Envelope.MaxX + "</MaxX>");
                request.Append("<MaxY>" + search.Envelope.MaxY + "</MaxY>");
                request.Append("</Envelope>");
            }
            request.Append("</GetRecords>");
            try
            {
                TextReader textreader = new StringReader(request.ToString());
                XmlTextReader xmltextreader = new XmlTextReader(textreader);
                //load the Xml doc
                XPathDocument myXPathDoc = new XPathDocument(xmltextreader);
                if (requestxsltobj == null)
                {
                    requestxsltobj = new XslCompiledTransform();
                    XsltSettings settings = new XsltSettings(true, true);
                    requestxsltobj.Load(requestxslt, settings, new XmlUrlResolver());
                }
                //do the actual transform of Xml
                requestxsltobj.Transform(myXPathDoc, null, writer);
                //requestxsltobj.Transform(myXPathDoc, null, writer);
                writer.Close();
            }
            catch (Exception e)
            {
                throw e;
            }

            return writer.ToString();
        }

        /// <summary>
        /// Generate a CSW request string to get metadata by ID. 
        /// </summary>
        /// <remarks>
        /// The CSW request string is built.
        /// The request is string is build based on the baseurl and record id
        /// </remarks>
        /// <param name="param1">The base url</param>
        /// <param name="param2">The record ID string</param>
        /// <returns>The request string</returns>
        public string GenerateCSWGetMetadataByIDRequestURL(string baseURL, string recordId)
        {

            StringBuilder requeststring = new StringBuilder();
            requeststring.Append(baseURL);

            if (baseURL.LastIndexOf("?") == (baseURL.Length - 1)) { requeststring.Append(kvp); }
            else { requeststring.Append("?" + kvp); }

            requeststring.Append("&ID=" + recordId);
            return requeststring.ToString();

        }

        /// <summary>
        /// Read a CSW metadata response. 
        /// </summary>
        /// <remarks>
        /// The CSW metadata response is read.
        /// The CSw record is updated with the metadata
        /// </remarks>
        /// <param name="param1">The metadata response string</param>
        /// <param name="param2">The CSW record for the record</param>
        public void ReadCSWGetMetadataByIDResponse(string response, CswRecord record)
        {
            if (metadataxslt == null || metadataxslt.Equals(""))
            {
                record.FullMetadata = response;
                record.MetadataResourceURL = "";
            }
            else
            {
                //create the output stream
                StringWriter writer = new StringWriter();

                TextReader textreader = new StringReader(response);
                XmlTextReader xmltextreader = new XmlTextReader(textreader);
                //load the Xml doc
                XPathDocument xpathDoc = new XPathDocument(xmltextreader);
                if (metadataxsltobj == null)
                {
                    metadataxsltobj = new XslCompiledTransform();
                    //enable document() support
                    XsltSettings settings = new XsltSettings(true, true);
                    metadataxsltobj.Load(metadataxslt, settings, new XmlUrlResolver());
                }
                
                //do the actual transform of Xml
                metadataxsltobj.Transform(xpathDoc, null, writer);

                writer.Close();

                // full metadata or resource url
                String outputStr = writer.ToString();

                if (IsUrl(outputStr))
                {
                    if (outputStr.Contains("\u2715"))
                    {
                        DcList list = new DcList();
                        list.add(outputStr);

                        LinkedList<String> serverList = list.get(DcList.getScheme(DcList.Scheme.SERVER));
                        LinkedList<String> documentList = list.get(DcList.getScheme(DcList.Scheme.METADATA_DOCUMENT));


                        if (serverList.Count > 0)
                        {
                            String serviceType = getServiceType(serverList.First.Value);
                            if (serviceType.Equals("aims") || serviceType.Equals("ags") || serviceType.Equals("wms") || serviceType.Equals("wcs"))
                            {
                                record.MapServerURL = serverList.First.Value;
                            }
                        }
                        else
                            record.MapServerURL = "";

                        if (documentList.Count > 0)
                            record.MetadataResourceURL = documentList.First.Value;

                    }
                    else
                    {
                        if (getServiceType(response).Equals("ags"))
                        {

                            outputStr = outputStr.Replace("http", "|http");
                            string[] s = outputStr.Split('|');
                            for (int i = 0; i < s.Length; i++)
                            {
                                if (s[i].ToString().Contains("MapServer"))
                                    record.MapServerURL = s[i];
                                else
                                    record.MetadataResourceURL = s[i];
                            }
                        }
                        else
                        {
                            record.MapServerURL = "";
                            record.MetadataResourceURL = outputStr;
                        }
                    }

                    record.FullMetadata = "";

                }
                else
                {
                    record.MapServerURL = "";
                    record.MetadataResourceURL = "";
                    record.FullMetadata = outputStr;
                }
            }
        }


        /// <summary>
        /// Transform a CSW metadata response. 
        /// </summary>
        /// <remarks>
        /// The CSW metadata response is read.
        /// The CSw record is updated with the metadata
        /// </remarks>
        /// <param name="param1">The metadata response string</param>
        /// <param name="param2">The CSW record for the record</param>
        public bool TransformCSWGetMetadataByIDResponse(string response, CswRecord record)
        {
            if (displayResponseXslt == null || displayResponseXslt.Equals(""))
            {
                record.FullMetadata = response;
                record.MetadataResourceURL = "";
                return false;
            }
            else
            {
                //create the output stream
                StringWriter writer = new StringWriter();

                TextReader textreader = new StringReader(response);
                XmlTextReader xmltextreader = new XmlTextReader(textreader);
                //load the Xml doc
                XPathDocument xpathDoc = new XPathDocument(xmltextreader);
                if (displayResponseXsltObj == null)
                {
                    displayResponseXsltObj = new XslCompiledTransform();
                    //enable document() support
                    XsltSettings settings = new XsltSettings(true, true);
                    displayResponseXsltObj.Load(displayResponseXslt, settings, new XmlUrlResolver());

                }

                //do the actual transform of Xml
                displayResponseXsltObj.Transform(xpathDoc, null, writer);

                writer.Close();

                // full metadata or resource url
                String outputStr = writer.ToString();
                record.MapServerURL = "";
                record.MetadataResourceURL = "";
                record.FullMetadata = outputStr;

                return true;
            }
        }
        /// <summary>
        /// Returns service type derieved from a url
        /// </summary>
        /// <param name="url">service url</param>
        /// <returns>servuce type</returns>
        public static String getServiceType(String url)
        {

            String serviceType = "unknown";
            url = url.ToLower();
            if (url.Contains("service=wms") || url.Contains("com.esri.wms.esrimap") || url.Contains("/mapserver/wmsserver") || url.Contains("/wmsserver") || url.Contains("wms"))
            {
                serviceType = "wms";
            }
            else if (url.Contains("service=wfs") || url.Contains("wfsserver"))
            {
                serviceType = "wfs";
            }
            else if (url.Contains("service=wcs") || url.Contains("wcsserver"))
            {
                serviceType = "wcs";
            }
            else if (url.Contains("com.esri.esrimap.esrimap"))
            {
                serviceType = "aims";
            }
            else if ((url.Contains("arcgis/rest") || url.Contains("arcgis/services") || url.Contains("rest/services")) && url.Contains("mapserver"))
            {
                serviceType = "ags";
            }
            else if (url.IndexOf("service=csw") > 0)
            {
                serviceType = "csw";
            }
            else if (url.EndsWith(".nmf"))
            {
                serviceType = "ArcGIS:nmf";
            }
            else if (url.EndsWith(".lyr"))
            {
                serviceType = "ArcGIS:lyr";
            }
            else if (url.EndsWith(".mxd"))
            {
                serviceType = "ArcGIS:mxd";
            }
            else if (url.EndsWith(".kml"))
            {
                serviceType = "kml";
            }
            if (serviceType.Equals("image") || serviceType.Equals("feature"))
            {
                serviceType = "aims";
            }
            return serviceType;
        }

        /// <summary>
        /// Check if a string is a URL.
        /// </summary>
        /// <remarks>
        /// This function only checks it the string contains http, https or ftp protocol. It doesn't validate the URL.
        /// </remarks>
        /// <param name="theString">string to be checked</param>
        /// <returns>true if the string is a URL</returns>
        private bool IsUrl(String theString)
        {
            if (theString.Length < 4)
                return false;
            else
            {
                if (theString.Substring(0, 5).Equals("http:", StringComparison.CurrentCultureIgnoreCase))
                    return true;
                else if (theString.Substring(0, 6).Equals("https:", StringComparison.CurrentCultureIgnoreCase))
                    return true;
                else if (theString.Substring(0, 4).Equals("ftp:", StringComparison.CurrentCultureIgnoreCase))
                    return true;
                else if (theString.Substring(0, 5).Equals("file:", StringComparison.CurrentCultureIgnoreCase))
                    return true;
                else if (theString.Contains("Server\u2715http:"))
                    return true;
                else
                    return false;
            }
        }

        #endregion
        #region "PrivateFunctions"
        /// <summary>
        /// replace specia lxml character  
        /// </summary>
        /// <remarks>
        /// Encode special characters (such as &, ", <, >, ') to percent values.
        /// </remarks>
        /// <param name="data">Text to be encoded</param>
        /// <returns>Encoded text.</returns>
        private string XmlEscape(string data)
        {
            data = data.Replace("&", "&amp;");
            data = data.Replace("<", "&lt;");
            data = data.Replace(">", "&gt;");
            data = data.Replace("\"", "&quot;");
            data = data.Replace("'", "&apos;");
            return data;
        }
        #endregion
    }
}

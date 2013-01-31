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

namespace com.esri.gpt.csw
{
    /// <summary>
    /// CswSearchRequest class.
    /// </summary>
    /// <remarks>
    /// CswSearchRequest class is used to submit CSW search queries and to return CSW search results.
    /// Before submiting a request, you need to specify a catalog and provide search criteria.
    /// </remarks>
    public class CswSearchRequest
    {
        private CswCatalog _catalog;
        private CswSearchCriteria _criteria;
        private CswSearchResponse _response;
        private CswClient _cswClient;
        private string _mapServerUrl;  
        #region "Properties"
        /// <summary>
        /// Accessor methods
        /// </summary>
        public CswCatalog Catalog
        {
            get { return _catalog; }
            set { _catalog = value; }
        }

        public CswSearchCriteria Criteria
        {
            get { return _criteria; }
            set { _criteria = value; }
        }
        #endregion
        #region Constructor
        /// <summary>
        /// Constructor
        /// </summary>
        public CswSearchRequest() { }
        #endregion
        #region "Methods and Functions"
        /// <summary>
        /// Search CSW catalog using the provided criteria. Search result can be accessed by calling GetResponse(). 
        /// </summary>
        public void Search()
        {
            _response = new CswSearchResponse();
            StringBuilder sb = new StringBuilder();

            if (_criteria == null) {
                sb.AppendLine(DateTime.Now + " Criteria not specified.");
                throw new NullReferenceException("Criteria not specified."); }
            if (_catalog == null) {
                sb.AppendLine(DateTime.Now + " Catalog not specified.");
                throw new NullReferenceException("Catalog not specified."); }
            if (_catalog.URL == null || _catalog.URL.Length == 0)
            {
                sb.AppendLine(DateTime.Now + " Catalog URL not specified.");
                throw new NullReferenceException("Catalog URL not specified.");
            }
            if (_catalog.Profile == null)
            {
                sb.AppendLine(DateTime.Now + " Catalog profile not specified.");
                throw new NullReferenceException("Catalog profile not specified.");
            }

            CswProfile profile = _catalog.Profile;
            writeLogMessage("Csw profile used : " + profile.Name);
            // generate getRecords query
            string requestUrl = _catalog.Capabilities.GetRecords_PostURL;
            string requestQuery = profile.GenerateCSWGetRecordsRequest(_criteria);

         //   requestQuery = "<csw:GetCapabilities xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" request=\"GetCapabilities\" service=\"CSW\" version=\"2.0.2\"/>";

            requestQuery = requestQuery.Replace("utf-16", "utf-8");
            requestQuery = requestQuery.Replace("UTF-16", "UTF-8");

            // submit search query
            if (_cswClient == null) { _cswClient = new CswClient(); }

            string responseText;
            sb.AppendLine(DateTime.Now + " Sending CSW GetRecords request to endpoint : " + requestUrl);
            sb.AppendLine("Request Query : " + requestQuery);
            if(!_catalog.Capabilities.GetRecords_IsSoapEndPoint)
               responseText = _cswClient.SubmitHttpRequest("POST", requestUrl, requestQuery);
            else
               responseText = _cswClient.SubmitHttpRequest("SOAP", requestUrl, requestQuery);

            // parse out csw search records
            CswRecords records = new CswRecords();
            sb.AppendLine(DateTime.Now + " Response received : " + responseText);
            profile.ReadCSWGetRecordsResponse(responseText, records);
            sb.AppendLine(DateTime.Now + " Parsed GetRecords response.");

            // populate CSW response
            _response.ResponseXML = responseText;
            _response.Records = records;

            writeLogMessage(sb.ToString());
        }

        private void writeLogMessage(String logMessage){
                Utils.logger.writeLog(logMessage);
        }

        /// <summary>
        /// Retrieve metadata from CSW service by its ID 
        /// </summary>
        /// <param name="DocID">Metadata document ID</param>
        public bool GetMetadataByID(string DocID, bool bApplyTransform)
        {
            _response = new CswSearchResponse();
            StringBuilder sb = new StringBuilder();
            if (DocID == null || DocID == "") { throw new ArgumentNullException(); }
            if (_catalog == null) { throw new NullReferenceException("Catalog not specified."); }
            if (_catalog.Capabilities == null) { throw new NullReferenceException("Catalog capabilities not initialized."); }
            if (_catalog.Profile == null) { throw new NullReferenceException("Catalog profile not specified."); }

            if (_catalog.Capabilities.GetRecordByID_GetURL == null || _catalog.Capabilities.GetRecordByID_GetURL.Length == 0)
            {
                throw new NullReferenceException("GetRecordByID URL not specified for the catalog capabilities.");
            }

            CswProfile profile = _catalog.Profile;
            writeLogMessage(" Csw profile used : " + profile.Name);
            // generate request url
            string getRecordByIDBaseUrl = _catalog.Capabilities.GetRecordByID_GetURL;
            string requestUrl = profile.GenerateCSWGetMetadataByIDRequestURL(getRecordByIDBaseUrl, DocID);

            sb.AppendLine(DateTime.Now + " GetRecordsById request URL : " + requestUrl);
            if (_cswClient == null) { _cswClient = new CswClient(); }
            string responseText = _cswClient.SubmitHttpRequest("GET", requestUrl, "");
            _response.ResponseXML = responseText;
            sb.AppendLine(DateTime.Now + " GetRecordsById response xml : " + responseText);
            CswRecord record = new CswRecord(DocID);
            bool isTransformed = false;
            if (bApplyTransform)
            {
                isTransformed = profile.TransformCSWGetMetadataByIDResponse(responseText, record);
                if(isTransformed)
                    sb.AppendLine(DateTime.Now + " Transformed xml : " + record.FullMetadata);
            }
            else
            {
                record.FullMetadata = responseText;
            }

            /*if (!isTransformed)
            {
                XmlDocument responseXml = new XmlDocument();
                try { responseXml.LoadXml(responseText); }
                catch (XmlException xmlEx)
                {
                    throw new XmlException("Error occurred \r\n" + xmlEx.Message);
                }
                record.FullMetadata = responseXml.FirstChild.InnerText ;
            }*/

            // add record to the response
            CswRecords records = new CswRecords();
            if (record != null) { records.Add(record.ID, record); }
            _response.Records = records;

            _mapServerUrl = record.MapServerURL;

            if (_mapServerUrl != null)
                sb.AppendLine(DateTime.Now + " Map Server Url : " + _mapServerUrl);

            writeLogMessage(sb.ToString());

            return isTransformed;

        }
        
        /// <summary>
        /// Get Add to map information
        /// </summary>
        /// <param name="DocID">document identifier</param>
        public void GetAddToMapInfoByID(string DocID)
        {

            _response = new CswSearchResponse();
            StringBuilder sb = new StringBuilder();

            if (DocID == null || DocID == "") { throw new ArgumentNullException(); }
            if (_catalog == null) { throw new NullReferenceException("Catalog not specified."); }
            if (_catalog.Capabilities == null) { throw new NullReferenceException("Catalog capabilities not initialized."); }
            if (_catalog.Profile == null) { throw new NullReferenceException("Catalog profile not specified."); }

            if (_catalog.Capabilities.GetRecordByID_GetURL == null || _catalog.Capabilities.GetRecordByID_GetURL.Length == 0)
            {
                sb.AppendLine(DateTime.Now + " GetRecordByID URL not specified for the catalog capabilities.");
                throw new NullReferenceException("GetRecordByID URL not specified for the catalog capabilities.");
            }

            CswProfile profile = _catalog.Profile;

            // generate request url
            string getRecordByIDBaseUrl = _catalog.Capabilities.GetRecordByID_GetURL;
            string requestUrl = profile.GenerateCSWGetMetadataByIDRequestURL(getRecordByIDBaseUrl, DocID);

            sb.AppendLine(DateTime.Now + " GetRecordsById request URL : " + requestUrl);
            if (_cswClient == null) { _cswClient = new CswClient(); }
            string responseText = _cswClient.SubmitHttpRequest("GET", requestUrl, "");
            _response.ResponseXML = responseText;

            sb.AppendLine(DateTime.Now + " GetRecordsById response xml : " + responseText);
            CswRecord record = new CswRecord(DocID);

            profile.ReadCSWGetMetadataByIDResponse(responseText, record);

                if (record.MetadataResourceURL != null && record.MetadataResourceURL.Equals("") && profile.Name.Equals("terra catalog CSW 2.0.2 AP ISO"))
                {
                    record.FullMetadata = responseText;
                }

                if (record == null) { throw new NullReferenceException("Record not populated."); }

                // check if full metadata or resourceURL has been returned
                bool hasFullMetadata = !(record.FullMetadata == null || record.FullMetadata == "");
                bool hasResourceUrl = !(record.MetadataResourceURL == null || record.MetadataResourceURL == "");
                if (!hasFullMetadata && !hasResourceUrl)
                {
                   // throw new InvalidOperationException("Neither full metadata nor metadata resource URL was found for the CSW record.");
                }
                else if (!hasFullMetadata && record.MetadataResourceURL != null)
                {
                    // need to load metadata from resource URL
                    responseText = _cswClient.SubmitHttpRequest("GET", record.MetadataResourceURL, "", "", "");
                    record.FullMetadata = responseText;
                }

                // add record to the response
                CswRecords records = new CswRecords();
                if (record != null) { records.Add(record.ID, record); }
                _response.Records = records;

                _mapServerUrl = record.MapServerURL;

                if (_mapServerUrl != null)
                    sb.AppendLine(DateTime.Now + " Map Server Url : " + _mapServerUrl);

                writeLogMessage(sb.ToString());
    }



        /// <summary>
        /// Get the CSW search response of a CSW search request 
        /// </summary>
        /// <remarks>
        /// Get the CSW search response of a CSW search request 
        /// </remarks>
        /// <returns>a CswSearchResponse object.</returns>
        public CswSearchResponse GetResponse()
        {
            return _response;
        }
        /// <summary>
        /// Map server url
        /// </summary>
        /// <returns></returns>
        public string GetMapServerUrl()
        {
            return _mapServerUrl;
        }
        #endregion
    }
}

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

namespace com.esri.gpt.csw
{
    /// <summary>
    /// CswRecord class represents CswRecord object
    /// </summary>
    public class CswRecord
    {

        private string id;
        private string title;
        private string abstractData;
        private BoundingBox boundingBox;
        private string briefMetadata;
        private string summaryMetadata;
        private string fullMetadata;
        private string metadataResourceURL;
        private string mapServerURL;
        private bool isLiveDataOrMap;
        private string serviceName;
        private string serviceType;

        #region properties definition
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
        /// ServiceName parameter
        /// </summary>
        public string ServiceName
        {
            get
            {
                return serviceName;
            }
            set
            {
                serviceName = value;
            }
        }
        /// <summary>
        /// ServiceType parameter
        /// </summary>
        public string ServiceType
        {
            get
            {
                return serviceType;
            }
            set
            {
                serviceType = value;
            }
        }
        /// <summary>
        /// Title parameter
        /// </summary>
        public string Title
        {
            get
            {
                return title;
            }
            set
            {
                title = value;
            }
        }
        /// <summary>
        /// Abstract parameter
        /// </summary>
        public string Abstract
        {
            get
            {
                return abstractData;
            }
            set
            {
                abstractData = value;
            }
        }
        /// <summary>
        /// BriefMetadata parameter
        /// </summary>
        public string BriefMetadata
        {
            get
            {
                return briefMetadata;
            }
            set
            {
                briefMetadata = value;
            }
        }
        /// <summary>
        /// BoundingBox parameter
        /// </summary>
        public BoundingBox BoundingBox
        {
            get
            {
                return boundingBox;
            }
            set
            {
                boundingBox = value;
            }
        }
        /// <summary>
        /// SummaryMetadata parameter
        /// </summary>
        public string SummaryMetadata
        {
            get
            {
                return summaryMetadata;
            }
            set
            {
                summaryMetadata = value;
            }
        }
        /// <summary>
        /// FullMetadata parameter
        /// </summary>
        public string FullMetadata
        {
            get
            {
                return fullMetadata;
            }
            set
            {
                fullMetadata = value;
            }
        }
        /// <summary>
        /// MetadataResourceURL parameter
        /// </summary>
        public string MetadataResourceURL
        {
            get
            {
                return metadataResourceURL;
            }
            set
            {
                metadataResourceURL = value;
            }
        }
        /// <summary>
        /// IsLiveDataOrMap parameter
        /// </summary>
        public bool IsLiveDataOrMap
        {
            get
            {
                return isLiveDataOrMap;
            }
            set
            {
                isLiveDataOrMap = value;
            }
        }
        /// <summary>
        /// MapServerURL parameter
        /// </summary>
        public string MapServerURL
        {
            get
            {
                return mapServerURL;
            }
            set
            {
                mapServerURL = value;
            }
        }

        #endregion

        #region constructor definition
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="sid">record identifier</param>
        public CswRecord(string sid)
        {
            ID = sid;
            boundingBox = new BoundingBox();
        }
        /// <summary>
        /// Default constructor
        /// </summary>
        public CswRecord()
        {
            boundingBox = new BoundingBox();
        }
        /// <summary>
        /// Constructor        
        /// </summary>
        /// <param name="sid">record identifier</param>
        /// <param name="stitle">record title</param>
        /// <param name="sabstract">record abstract</param>
        public CswRecord(string sid, string stitle, string sabstract)
        {
            ID = sid;
            Abstract = sabstract;
            Title = stitle;
            boundingBox = new BoundingBox();
        }

        #endregion

    }
}

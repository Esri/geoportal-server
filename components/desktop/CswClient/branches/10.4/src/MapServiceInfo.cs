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
    /// Stores map service information
    /// </summary>
    public class MapServiceInfo
    {
        private string _server;
        private string _service;
        private string _serviceType;
        private string _serviceParam;
        private bool _isSecured;
        #region Constructor(s)
        public MapServiceInfo()
        {
            _server = "";
            _service = "";
            _serviceType = "";
            _serviceParam = "";
            _isSecured = false;
        }
        #endregion
        #region Properties
        /// <summary>
        /// Server property
        /// </summary>
        public string Server
        {
            get { return _server; }
            set { _server = value; }
        }
        /// <summary>
        /// Service property
        /// </summary>
        public string Service
        {
            get { return _service; }
            set { _service = value; }
        }
        /// <summary>
        /// ServiceType property
        /// </summary>
        public string ServiceType
        {
            get { return _serviceType; }
            set { _serviceType = value; }
        }
        /// <summary>
        /// ServiceParam property
        /// </summary>
        public string ServiceParam
        {
            get { return _serviceParam; }
            set { _serviceParam = value; }
        }
        /// <summary>
        /// IsSecured property
        /// </summary>
        public bool IsSecured
        {
            get { return _isSecured; }
            set { _isSecured = value; }
        }
        #endregion
        #region PublicMethods
        /// <summary>
        /// Determines in server is map service
        /// </summary>
        /// <returns></returns>
        public bool IsMapService()
        {
            // check if it contains required map service info
            bool isMapService = false;
            if (_server != "" && (_service != "" || _serviceParam != ""))
            {
                isMapService = true;
            }
            else if (_serviceType.Equals("wms", StringComparison.OrdinalIgnoreCase))
            {
                isMapService = true;
            }
            else
            {
                isMapService = false;
            }

            return isMapService;
        }
        /// <summary>
        /// String representation of object
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            string str;

            str = "Server: " + _server + "; " + "Service: " + _service + "; " + "ServiceType: " + _serviceType + "; " +
                  "ServiceParam: " + _serviceParam + "; " + "IsSecured: " + _isSecured.ToString();
            return str;
        }
        #endregion
    }
}

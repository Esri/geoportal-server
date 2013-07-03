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
    /// CswSearchResponse class.
    /// </summary>
    /// <remarks>
    /// CswSearchResponse class is used to store the response for CSW search request.
    /// </remarks>
    public class CswSearchResponse
    {
        #region Private Variables
        private string _responseXML;
        private CswRecords _records;
        #endregion
        #region Constructor
        /// <summary>
        /// Constructor
        /// </summary>
        public CswSearchResponse()
        {
            _responseXML = "";
            _records = new CswRecords();
        }
        #endregion
        #region PublicMethods
        /// <summary>
        /// Response XML
        /// </summary>
        public string ResponseXML
        {
            get { return _responseXML; }
            protected internal set { _responseXML = value; }
        }

        /// <summary>
        /// CSW Records returned
        /// </summary>
        public CswRecords Records
        {
            get { return _records; }
            protected internal set { _records = value; }
        }
        #endregion
    }
}

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

namespace com.esri.gpt.csw
{
    /// <summary>
    /// CswSearchCriteria class represents the csw search criteria
    /// </summary>
   public class CswSearchCriteria
    {
        #region "Properties"
        /// <summary>
        /// StartPosition parameter
        /// </summary>
        public int StartPosition
        {
            get { return _startPosition; }
            set 
            {
                if (value >= 0) _startPosition = value;
                else throw new Exception("startPosition should be a value equal or larger than 0.");
            }
        }

        /// <summary>
        /// MaxRecords parameter
        /// </summary>
        public int MaxRecords
        {
            get { return _maxRecords; }
            set
            {
                if (value >= 0) _maxRecords = value;
                else throw new Exception("maxRecords should be a value equal or larger than 0.");
            }
        }
        /// <summary>
        /// SearchText parameter
        /// </summary>
        public string SearchText
        {
            get { return _searchText; }
            set { _searchText = value; }
        }
        /// <summary>
        /// LiveDataAndMapOnly parameter
        /// </summary>
        public Boolean LiveDataAndMapOnly
        {
            get { return _isLiveDataAndMapOnly; }
            set { _isLiveDataAndMapOnly = value; }
        }
        /// <summary>
        /// Envelope parameter
        /// </summary>
        public Envelope Envelope
        {
            get { return _envelope; }
            set { _envelope = value; }
        }
        #endregion

        private int _startPosition;
        private int _maxRecords;
        private string _searchText;
        private Boolean _isLiveDataAndMapOnly;
        private Envelope _envelope;
    }

}

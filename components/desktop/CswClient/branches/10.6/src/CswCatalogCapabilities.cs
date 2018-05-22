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

namespace com.esri.gpt.csw {
    /// <summary>
    /// Public catalog containing catalog capabilities information.
    /// </summary>
    /// <remarks>
    /// </remarks>
   public class CswCatalogCapabilities {

       private string _getRecordByIDGetURL;
       private string _getRecordsPostURL;
       private bool _isSoapEndPoint = false;

       #region "Properties"
       /// <summary>
       /// GetRecordByID_GetURL parameter
       /// </summary>
       internal string GetRecordByID_GetURL {
           set {
               _getRecordByIDGetURL = Utils.EnsureTrailingQuestionOrAmpersandInURL(value);
           }
           get {
               return _getRecordByIDGetURL;
           }

       }
       /// <summary>
       /// GetRecords_PostURL parameter
       /// </summary>
       internal string GetRecords_PostURL {
           set {
               _getRecordsPostURL = value;
           }
           get {
               return _getRecordsPostURL;
           }
       }
       /// <summary>
       /// GetRecords_IsSoapEndPoint parameter
       /// </summary>
       internal bool GetRecords_IsSoapEndPoint
       {
           set
           {
               _isSoapEndPoint = value;
           }
           get
           {
               return _isSoapEndPoint;
           }
       }

       #endregion
   }
}

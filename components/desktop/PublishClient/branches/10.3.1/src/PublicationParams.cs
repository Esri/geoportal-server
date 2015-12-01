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
using System.Linq;
using System.Text;

namespace com.esri.gpt.publish
{
    /// <summary>
    /// Publication parameter value object
    /// </summary>
    public class PublicationParams
    {
        /// <summary>
        /// Instance variables
        /// </summary>
        #region Properties
        private String serverUrl;
        private String userName;
        private String service;
        private String password;
        private String logfilePath;
        private String currentWorkDir;
        #endregion
        
        #region Property Accessor Methods
        /// <summary>
        /// CurrentWorkDir parameter
        /// </summary>
        public string CurrentWorkDir
        {
            get
            {
                return currentWorkDir;
            }
            set
            {
                currentWorkDir = value;
            }
        }
        /// <summary>
        /// LogFilePath parameter
        /// </summary>
        public string LogFilePath
        {
            get
            {
                return logfilePath;
            }
            set
            {
                logfilePath = value;
            }
        }
        /// <summary>
        /// Password parameter
        /// </summary>
        public string Password
        {
            get
            {
                return password;
            }
            set
            {
                password = value;
            }
        }
        /// <summary>
        /// ServerUrl parameter
        /// </summary>
        public string ServerUrl
        {
            get
            {
                return serverUrl;
            }
            set
            {
                serverUrl = value;
            }
        }
        /// <summary>
        /// UserName parameter
        /// </summary>
        public string UserName
        {
            get
            {
                return userName;
            }
            set
            {
                userName = value;
            }
        }
        /// <summary>
        /// Service parameter
        /// </summary>
        public string Service
        {
            get
            {
                return service;
            }
            set
            {
                service = value;
            }
        }
        #endregion
    }
}

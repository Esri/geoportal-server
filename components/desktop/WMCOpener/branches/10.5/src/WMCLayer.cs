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

namespace com.esri.gpt.wmc
{
    /// <summary>
    /// Value object class for WMCLayer information
    /// </summary>
    class WMCLayer
    {
        private string strID = "";
        private string isHidden = "false";
        private string isQueryable = "false";
        private string strName = "";
        private string strTitle = "";
        private Server server = new Server();
        private string strSecretName = "";

        public string ID
        {
            set
            {
                strID = value;
            }
            get
            {
                return strID;
            }
        }

        public string IsHidden
        {
            set
            {
                isHidden = value;
            }
            get
            {
                return isHidden;
            }
        }

        public string IsQueryable
        {
            set
            {
                isQueryable = value;
            }
            get
            {
                return isQueryable;
            }
        }

        public string Name
        {
            set
            {
                strName = value;
            }
            get
            {
                return strName;
            }
        }

        public string Title
        {
            set
            {
                strTitle = value;
            }
            get
            {
                return strTitle;
            }
        }

        public Server Server
        {
            set
            {
                server = value;
            }
            get
            {
                return server;
            }
        }

        public string SecretName
        {
            set
            {
                strSecretName = value;
            }
            get
            {
                return strSecretName;
            }
        }

    }
}

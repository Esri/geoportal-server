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
    class OnlineResource
    {
        private string resourceType = "";
        private string href = "";
        private string role = "";
        private string arcrole = "";
        private string title = "";
        private string show = "";
        private string actuate = "";

        public string ResourceType
        {
            set
            {
                resourceType = value;
            }
            get
            {
                return resourceType;
            }
        }

        public string Href
        {
            set
            {
                href = value;
            }
            get
            {
                return href;
            }
        }
        public string Role
        {
            set
            {
                role = value;
            }
            get
            {
                return role;
            }
        }

        public string ArcRole
        {
            set
            {
                arcrole = value;
            }
            get
            {
                return arcrole;
            }
        }

        public string Title
        {
            set
            {
                title = value;
            }
            get
            {
                return title;
            }
        }

        public string Show
        {
            set
            {
                show = value;
            }
            get
            {
                return show;
            }
        }

        public string Actuate
        {
            set
            {
                actuate = value;
            }
            get
            {
                return actuate;
            }
        }

    }
}

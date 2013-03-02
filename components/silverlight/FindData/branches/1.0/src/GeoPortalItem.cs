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
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace GeoportalWidget
{
    //<id>30</id> 
    //<uuid>{07C3964F-A487-4A7B-9CA6-FB3A74AD9704}</uuid> 
    //<protocol>csw</protocol> 
    //<name>NOAA NCDC</name> 
    //<url>http://gis.ncdc.noaa.gov/geoportal/csw/discovery?request=GetCapabilities&service=CSW&version=2.0.2</url> 
    public class GeoPortalItem
    {
        public string id
        {
            get;
            set;
        }
        public string uuid
        {
            get;
            set;
        }
        public string protocol
        {
            get;
            set;
        }
        public string name
        {
            get;
            set;
        }
        public string url
        {
            get;
            set;
        }
    }
}

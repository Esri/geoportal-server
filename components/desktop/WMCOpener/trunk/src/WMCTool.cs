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
using System.IO;

namespace com.esri.gpt.wmc
{
    public class WMCTool : ESRI.ArcGIS.Desktop.AddIns.Button
    {
        /// <summary>
        /// Handles on click event
        /// </summary>
        protected override void OnClick()
        {
            OpenWMC form = new OpenWMC(ArcMap.Application);
            form.ShowDialog();

            ArcMap.Application.CurrentTool = null;
        }
        /// <summary>
        /// Handles on update
        /// </summary>
        protected override void OnUpdate()
        {
            Enabled = ArcMap.Application != null;
        }

    }

}

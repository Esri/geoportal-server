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
using ESRI.ArcGIS.Framework;
using ESRI.ArcGIS.esriSystem;

namespace com.esri.gpt.csw
{
    /// <summary>
    /// CswClient button class
    /// </summary>
    public class CswClientButton : ESRI.ArcGIS.Desktop.AddIns.Button
    {
        #region Private vairables
        private static IDockableWindow _DockWindow;
        #endregion
        #region Constructor
        public CswClientButton()
        {
            UID dockWinID = new UIDClass();
            dockWinID.Value = @"ESRI_CswSearch_CswClientDockableWindow";
            _DockWindow = ArcMap.DockableWindowManager.GetDockableWindow(dockWinID);
        }
        #endregion
        #region Methods
        protected override void OnClick()
        {
            //
            //  TODO: Sample code showing how to access button host
            //
            ArcMap.Application.CurrentTool = null;
            if (_DockWindow == null)
                return;

            _DockWindow.Show(!_DockWindow.IsVisible());
        }
        protected override void OnUpdate()
        {
            Enabled = ArcMap.Application != null;
        }
        #endregion 
    }

}

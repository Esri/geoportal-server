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
using ESRI.ArcGIS.esriSystem;

namespace com.esri.gpt.publish
{
    /// <summary>
    /// Publish client button.
    /// </summary>
    public class PublishClientButton : ESRI.ArcGIS.Desktop.AddIns.Button
    {
        #region Private Variable(s)
        private static LicenseInitializer m_AOLicenseInitializer = new LicenseInitializer();
        #endregion
        #region Constructor
        /// <summary>
        /// PublishClientButton constructor
        /// </summary>
        public PublishClientButton()
        {
            // Load the product code and version to the version manager
            ESRI.ArcGIS.RuntimeManager.Bind(ESRI.ArcGIS.ProductCode.Desktop);

            //ESRI License Initializer generated code.
            m_AOLicenseInitializer.InitializeApplication(new esriLicenseProductCode[] { esriLicenseProductCode.esriLicenseProductCodeAdvanced },
            new esriLicenseExtensionCode[] { });
        }
        #endregion
        #region Event Methods
        /// <summary>
        /// Handles onclick event on publication form
        /// </summary>
        protected override void OnClick()
        {
            PublishForm publishForm = new PublishForm();
            if (publishForm.Visible)
                publishForm.Hide();
            else
                publishForm.ShowDialog();

        }
        /// <summary>
        /// Performs action on update
        /// </summary>
        protected override void OnUpdate()
        {
            Enabled = ArcCatalog.Application != null || ArcMap.Application != null;
        }
        #endregion
    }
}

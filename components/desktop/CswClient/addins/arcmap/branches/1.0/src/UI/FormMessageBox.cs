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
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Globalization;
using ESRI.ArcGIS.esriSystem;
namespace com.esri.gpt.csw
{
    /// <summary>
    /// Displays messages to user
    /// </summary>
    public partial class FormMessageBox : Form
    {
        #region Constructor(s)
        public FormMessageBox()
        {
            InitializeComponent();
            Reset();

            UpdateUI();
        }
        #endregion
        #region Methods
        /// <summary>
        /// Updates UI depending on current locale
        /// </summary>
        private void UpdateUI() {

            // setup locale
            IArcGISLocale agsLocale = new ArcGISLocaleClass();
            CultureInfo ci = new CultureInfo(agsLocale.UILocale);

            // set locale
            System.Threading.Thread.CurrentThread.CurrentUICulture = ci;

            //// set flow direction
            if (agsLocale.RightToLeftUI)
            {
                this.RightToLeft = RightToLeft.Yes;
                this.RightToLeftLayout = true;
            
            }
            else
            {
                this.RightToLeft = RightToLeft.No;
                this.RightToLeftLayout = false;
               
            }
        }
        /// <summary>
        /// Initializes message form
        /// param name="Caption" the message caption
        /// param name="Details" the detailed message
        /// param name="Message" the message to be displayed
        /// </summary>
        public void Init(string Message, string Details, string Caption)
        {
            txtMessage.Text = Message;
            txtMessage.TabStop = false;

            txtDetails.Text = Details;
            txtDetails.SelectionStart = 0;
            txtDetails.SelectionLength = 0;

            this.Text = Caption;
        }

        private void Reset()
        {
            txtMessage.Text = "";
            txtDetails.Text = "";
        }
#endregion
    }
}
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

namespace com.esri.gpt.csw
{
    /// <summary>
    /// FormViewMetadata class is used to display metadata.
    /// </summary>
    public partial class FormViewMetadata : Form
    {
        private string _caption="";     
        private string _filePath = "";

        /// <summary>
        /// Accessor methods
        /// </summary>
        public string MetadataFilePath
        {
            get { return _filePath; }
        }
        /// <summary>
        /// Constructor
        /// </summary>
        public FormViewMetadata()
        {
            InitializeComponent();
            UpdateUI();
        }
        /// <summary>
        /// Updates UI with current locale.
        /// </summary>
        private void UpdateUI()
        {
            string name = System.Threading.Thread.CurrentThread.CurrentUICulture.EnglishName.Trim().ToLower();
            if (name.Contains("hebrew") || name.Contains("arabic"))
            {
                this.RightToLeft = RightToLeft.Yes;
            }
            else
            {
                this.RightToLeft = RightToLeft.Inherit;
            }
        }
        /// <summary>
        /// Navigate to url
        /// </summary>
        /// <param name="urlString">new url</param>
        public void Navigate(string urlString)
        {
            _filePath = urlString;
            Uri tmpUri = new Uri(urlString);
            webBrowserViewer.Navigate(tmpUri);
        }
        /// <summary>
        /// Metadata title
        /// </summary>
        public string MetadataTitle
        {
            set { this.Text = value + " - " + _caption; }
        }

        /// <summary>
        /// Handles form on load event
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">event arguments</param>
        private void FormViewMetadata_Load(object sender, EventArgs e)
        {
            _caption = this.Text;   // "Metadata Viewer";
        }
    }
}
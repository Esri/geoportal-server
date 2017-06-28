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

namespace com.esri.gpt.publish
{
    /// <summary>
    /// Form to display messages.
    /// </summary>
    public partial class FormMessageBox : Form
    {
        public FormMessageBox()
        {
            InitializeComponent();
            Reset();
        }

        /// <summary>
        /// Initializes message display form
        /// param name="Caption" the message caption
        /// param name="Details" the detailed message string
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

        /// <summary>
        /// Resets message form
        /// </summary>
        private void Reset()
        {
            txtMessage.Text = "";
            txtDetails.Text = "";
        }
    }
}
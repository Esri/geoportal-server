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

namespace com.esri.gpt.csw
{
    /// <summary>
    /// Prompt to get user credentials for secured csw service
    /// </summary>
    public partial class PromptCredentials : Form
    {
        private string userName = "";
        private string password = "";
        #region Constructor(s)
        /// <summary>
        /// Constructor
        /// </summary>
        public PromptCredentials()
        {
            InitializeComponent();
            this.KeyPreview = true; //Form handles keyinput 
        }
        #endregion
        #region Properties
        /// <summary>
        /// Password property
        /// </summary>
        public string Password
        {
            get { return password; }
            set { password = value; }
        }
        /// <summary>
        /// Username property
        /// </summary>
        public string Username
        {
            get { return userName; }
            set { userName = value; }
        }
        #endregion
        #region EventMethods
        /// <summary>
        /// Handles close button click event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void closeBtn_Click(object sender, EventArgs e)
        {
            this.Hide();
        }
        /// <summary>
        /// Handle submit button click event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void submitBtn_Click(object sender, EventArgs e)
        {
            if (String.IsNullOrEmpty(userNameTxt.Text))
            {
                MessageBox.Show("Please enter valid user name");
                return;
            }
            else
            {
                Username = userNameTxt.Text;
            }
            if (String.IsNullOrEmpty(passwordTxt.Text))
            {
                MessageBox.Show("Please enter valid password");
                return;
            }
            else
            {
                Password = passwordTxt.Text;
            }
            this.Hide();
        }
        /// <summary>
        /// Handles key down event in prompt credentials form
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void passwordTxt_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyValue == 13)
            {
                submitBtn_Click(sender, e);
            } 
        }
        /// <summary>
        /// Handles key up event on prompt credentials form
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void PromptCredentials_KeyUp(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == (Keys.Enter))
            {
                submitBtn_Click(sender, e);
            } /*else if (e.KeyCode == (Keys.C))
            {
                closeBtn_Click(sender, e);
            }*/
        }
        #endregion
    }
}

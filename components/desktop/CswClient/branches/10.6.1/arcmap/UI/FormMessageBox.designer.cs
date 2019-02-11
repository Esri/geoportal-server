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
namespace com.esri.gpt.csw
{
  partial class FormMessageBox
  {
    /// <summary>
    /// Required designer variable.
    /// </summary>
    private System.ComponentModel.IContainer components = null;

    /// <summary>
    /// Clean up any resources being used.
    /// </summary>
    /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
    protected override void Dispose(bool disposing)
    {
      if (disposing && (components != null))
      {
        components.Dispose();
      }
      base.Dispose(disposing);
    }

    #region Windows Form Designer generated code

    /// <summary>
    /// Required method for Designer support - do not modify
    /// the contents of this method with the code editor.
    /// </summary>
    private void InitializeComponent()
    {
        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(FormMessageBox));
        this.btnOK = new System.Windows.Forms.Button();
        this.txtDetails = new System.Windows.Forms.TextBox();
        this.lblDetails = new System.Windows.Forms.Label();
        this.txtMessage = new System.Windows.Forms.TextBox();
        this.SuspendLayout();
        // 
        // btnOK
        // 
        resources.ApplyResources(this.btnOK, "btnOK");
        this.btnOK.DialogResult = System.Windows.Forms.DialogResult.OK;
        this.btnOK.Name = "btnOK";
        this.btnOK.UseVisualStyleBackColor = true;
        // 
        // txtDetails
        // 
        resources.ApplyResources(this.txtDetails, "txtDetails");
        this.txtDetails.Name = "txtDetails";
        this.txtDetails.ReadOnly = true;
        // 
        // lblDetails
        // 
        resources.ApplyResources(this.lblDetails, "lblDetails");
        this.lblDetails.Name = "lblDetails";
        // 
        // txtMessage
        // 
        resources.ApplyResources(this.txtMessage, "txtMessage");
        this.txtMessage.BackColor = System.Drawing.SystemColors.ButtonFace;
        this.txtMessage.BorderStyle = System.Windows.Forms.BorderStyle.None;
        this.txtMessage.CausesValidation = false;
        this.txtMessage.Name = "txtMessage";
        this.txtMessage.ReadOnly = true;
        this.txtMessage.TabStop = false;
        // 
        // FormMessageBox
        // 
        this.AcceptButton = this.btnOK;
        resources.ApplyResources(this, "$this");
        this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
        this.Controls.Add(this.txtMessage);
        this.Controls.Add(this.lblDetails);
        this.Controls.Add(this.txtDetails);
        this.Controls.Add(this.btnOK);
        this.Name = "FormMessageBox";
        this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Show;
        this.ResumeLayout(false);
        this.PerformLayout();

    }

    #endregion

    private System.Windows.Forms.Button btnOK;
    private System.Windows.Forms.TextBox txtDetails;
    private System.Windows.Forms.Label lblDetails;
    private System.Windows.Forms.TextBox txtMessage;
  }
}
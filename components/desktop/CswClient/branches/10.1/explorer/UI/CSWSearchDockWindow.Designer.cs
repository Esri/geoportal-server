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
    partial class CSWSearchDockWindow
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

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(CSWSearchDockWindow));
            this.tabSearch = new System.Windows.Forms.TabPage();
            this.nudNumOfResults = new System.Windows.Forms.NumericUpDown();
            this.lblResult = new System.Windows.Forms.Label();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.splitContainer1 = new System.Windows.Forms.SplitContainer();
            this.toolstripSearchResults = new System.Windows.Forms.ToolStrip();
            this.tsbViewMetadata = new System.Windows.Forms.ToolStripButton();
            this.tsbDownloadMetadata = new System.Windows.Forms.ToolStripButton();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.tsbAddToMap = new System.Windows.Forms.ToolStripButton();
            this.displayFootPrintTSBtn = new System.Windows.Forms.ToolStripButton();
            this.zoomToFootPrintTSBtn = new System.Windows.Forms.ToolStripButton();
            this.showAllFootPrintTSBtn = new System.Windows.Forms.ToolStripButton();
            this.clearAllFootPrintTSBtn = new System.Windows.Forms.ToolStripButton();
            this.lstSearchResults = new System.Windows.Forms.ListBox();
            this.txtAbstract = new System.Windows.Forms.TextBox();
            this.lblAbstract = new System.Windows.Forms.Label();
            this.btnSearch = new System.Windows.Forms.Button();
            this.cmbCswCatalog = new System.Windows.Forms.ComboBox();
            this.lblCatalog = new System.Windows.Forms.Label();
            this.chkLiveDataAndMapOnly = new System.Windows.Forms.CheckBox();
            this.lblMaximum = new System.Windows.Forms.Label();
            this.txtSearchPhrase = new System.Windows.Forms.TextBox();
            this.lblFind = new System.Windows.Forms.Label();
            this.toolStripButton1 = new System.Windows.Forms.ToolStripButton();
            this.toolStripButton2 = new System.Windows.Forms.ToolStripButton();
            this.toolStripSeparator2 = new System.Windows.Forms.ToolStripSeparator();
            this.toolStripButton3 = new System.Windows.Forms.ToolStripButton();
            this.toolStripButton4 = new System.Windows.Forms.ToolStripButton();
            this.toolStripButton5 = new System.Windows.Forms.ToolStripButton();
            this.toolStripButton6 = new System.Windows.Forms.ToolStripButton();
            this.toolStripButton7 = new System.Windows.Forms.ToolStripButton();
            this.lblCatalogs = new System.Windows.Forms.Label();
            this.combProfile = new System.Windows.Forms.ComboBox();
            this.txtURL = new System.Windows.Forms.TextBox();
            this.lstCatalog = new System.Windows.Forms.ListBox();
            this.txtDisplayName = new System.Windows.Forms.TextBox();
            this.btnAdd = new System.Windows.Forms.Button();
            this.btnDelete = new System.Windows.Forms.Button();
            this.btnSave = new System.Windows.Forms.Button();
            this.imgLst = new System.Windows.Forms.ImageList(this.components);
            this.labelPlaceholder = new System.Windows.Forms.Label();
            this.lblProfile = new System.Windows.Forms.Label();
            this.lblDisplayName = new System.Windows.Forms.Label();
            this.tabConfigure = new System.Windows.Forms.TabPage();
            this.lblCatalogService = new System.Windows.Forms.Label();
            this.tabHelp = new System.Windows.Forms.TabPage();
            this.linkLblAbt = new System.Windows.Forms.LinkLabel();
            this.linkLblHelp = new System.Windows.Forms.LinkLabel();
            this.tabMain = new System.Windows.Forms.TabControl();
            this.toolTip = new System.Windows.Forms.ToolTip(this.components);
            this.tabSearch.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.nudNumOfResults)).BeginInit();
            this.splitContainer1.Panel1.SuspendLayout();
            this.splitContainer1.Panel2.SuspendLayout();
            this.splitContainer1.SuspendLayout();
            this.toolstripSearchResults.SuspendLayout();
            this.tabConfigure.SuspendLayout();
            this.tabHelp.SuspendLayout();
            this.tabMain.SuspendLayout();
            this.SuspendLayout();
            // 
            // tabSearch
            // 
            this.tabSearch.BackColor = System.Drawing.SystemColors.Window;
            this.tabSearch.Controls.Add(this.nudNumOfResults);
            this.tabSearch.Controls.Add(this.lblResult);
            this.tabSearch.Controls.Add(this.groupBox1);
            this.tabSearch.Controls.Add(this.splitContainer1);
            this.tabSearch.Controls.Add(this.btnSearch);
            this.tabSearch.Controls.Add(this.cmbCswCatalog);
            this.tabSearch.Controls.Add(this.lblCatalog);
            this.tabSearch.Controls.Add(this.chkLiveDataAndMapOnly);
            this.tabSearch.Controls.Add(this.lblMaximum);
            this.tabSearch.Controls.Add(this.txtSearchPhrase);
            this.tabSearch.Controls.Add(this.lblFind);
            resources.ApplyResources(this.tabSearch, "tabSearch");
            this.tabSearch.Name = "tabSearch";
            this.tabSearch.Enter += new System.EventHandler(this.tabSearch_Enter);
            // 
            // nudNumOfResults
            // 
            resources.ApplyResources(this.nudNumOfResults, "nudNumOfResults");
            this.nudNumOfResults.BackColor = System.Drawing.SystemColors.Window;
            this.nudNumOfResults.ForeColor = System.Drawing.SystemColors.WindowText;
            this.nudNumOfResults.Maximum = new decimal(new int[] {
            500,
            0,
            0,
            0});
            this.nudNumOfResults.Name = "nudNumOfResults";
            this.nudNumOfResults.Value = new decimal(new int[] {
            10,
            0,
            0,
            0});
            this.nudNumOfResults.ValueChanged += new System.EventHandler(this.nudNumOfResults_ValueChanged);
            // 
            // lblResult
            // 
            resources.ApplyResources(this.lblResult, "lblResult");
            this.lblResult.Name = "lblResult";
            // 
            // groupBox1
            // 
            resources.ApplyResources(this.groupBox1, "groupBox1");
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.TabStop = false;
            // 
            // splitContainer1
            // 
            resources.ApplyResources(this.splitContainer1, "splitContainer1");
            this.splitContainer1.FixedPanel = System.Windows.Forms.FixedPanel.Panel2;
            this.splitContainer1.Name = "splitContainer1";
            // 
            // splitContainer1.Panel1
            // 
            this.splitContainer1.Panel1.Controls.Add(this.toolstripSearchResults);
            this.splitContainer1.Panel1.Controls.Add(this.lstSearchResults);
            // 
            // splitContainer1.Panel2
            // 
            this.splitContainer1.Panel2.Controls.Add(this.txtAbstract);
            this.splitContainer1.Panel2.Controls.Add(this.lblAbstract);
            // 
            // toolstripSearchResults
            // 
            this.toolstripSearchResults.AllowItemReorder = true;
            this.toolstripSearchResults.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.tsbViewMetadata,
            this.tsbDownloadMetadata,
            this.toolStripSeparator1,
            this.tsbAddToMap,
            this.displayFootPrintTSBtn,
            this.zoomToFootPrintTSBtn,
            this.showAllFootPrintTSBtn,
            this.clearAllFootPrintTSBtn});
            resources.ApplyResources(this.toolstripSearchResults, "toolstripSearchResults");
            this.toolstripSearchResults.Name = "toolstripSearchResults";
            // 
            // tsbViewMetadata
            // 
            this.tsbViewMetadata.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            resources.ApplyResources(this.tsbViewMetadata, "tsbViewMetadata");
            this.tsbViewMetadata.Name = "tsbViewMetadata";
            this.tsbViewMetadata.Click += new System.EventHandler(this.tsbViewMetadata_Click);
            // 
            // tsbDownloadMetadata
            // 
            this.tsbDownloadMetadata.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            resources.ApplyResources(this.tsbDownloadMetadata, "tsbDownloadMetadata");
            this.tsbDownloadMetadata.Name = "tsbDownloadMetadata";
            this.tsbDownloadMetadata.Click += new System.EventHandler(this.tsbDownloadMetadata_Click);
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            resources.ApplyResources(this.toolStripSeparator1, "toolStripSeparator1");
            // 
            // tsbAddToMap
            // 
            this.tsbAddToMap.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            resources.ApplyResources(this.tsbAddToMap, "tsbAddToMap");
            this.tsbAddToMap.Name = "tsbAddToMap";
            this.tsbAddToMap.Click += new System.EventHandler(this.tsbAddToMap_Click);
            // 
            // displayFootPrintTSBtn
            // 
            this.displayFootPrintTSBtn.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            resources.ApplyResources(this.displayFootPrintTSBtn, "displayFootPrintTSBtn");
            this.displayFootPrintTSBtn.Name = "displayFootPrintTSBtn";
            this.displayFootPrintTSBtn.Click += new System.EventHandler(this.displayFootPrintTSBtn_Click);
            // 
            // zoomToFootPrintTSBtn
            // 
            this.zoomToFootPrintTSBtn.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            resources.ApplyResources(this.zoomToFootPrintTSBtn, "zoomToFootPrintTSBtn");
            this.zoomToFootPrintTSBtn.Name = "zoomToFootPrintTSBtn";
            this.zoomToFootPrintTSBtn.Click += new System.EventHandler(this.zoomToFootPrintTSBtn_Click);
            // 
            // showAllFootPrintTSBtn
            // 
            this.showAllFootPrintTSBtn.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            resources.ApplyResources(this.showAllFootPrintTSBtn, "showAllFootPrintTSBtn");
            this.showAllFootPrintTSBtn.Name = "showAllFootPrintTSBtn";
            this.showAllFootPrintTSBtn.Click += new System.EventHandler(this.showAllFootPrintTSBtn_Click);
            // 
            // clearAllFootPrintTSBtn
            // 
            this.clearAllFootPrintTSBtn.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            resources.ApplyResources(this.clearAllFootPrintTSBtn, "clearAllFootPrintTSBtn");
            this.clearAllFootPrintTSBtn.Name = "clearAllFootPrintTSBtn";
            this.clearAllFootPrintTSBtn.Click += new System.EventHandler(this.clearAllFootPrintTSBtn_Click);
            // 
            // lstSearchResults
            // 
            resources.ApplyResources(this.lstSearchResults, "lstSearchResults");
            this.lstSearchResults.FormattingEnabled = true;
            this.lstSearchResults.Name = "lstSearchResults";
            this.lstSearchResults.SelectedIndexChanged += new System.EventHandler(this.lstSearchResults_SelectedIndexChanged);
            // 
            // txtAbstract
            // 
            resources.ApplyResources(this.txtAbstract, "txtAbstract");
            this.txtAbstract.Name = "txtAbstract";
            this.txtAbstract.ReadOnly = true;
            // 
            // lblAbstract
            // 
            resources.ApplyResources(this.lblAbstract, "lblAbstract");
            this.lblAbstract.Name = "lblAbstract";
            // 
            // btnSearch
            // 
            resources.ApplyResources(this.btnSearch, "btnSearch");
            this.btnSearch.Name = "btnSearch";
            this.btnSearch.UseVisualStyleBackColor = true;
            this.btnSearch.Click += new System.EventHandler(this.btnSearch_Click);
            // 
            // cmbCswCatalog
            // 
            resources.ApplyResources(this.cmbCswCatalog, "cmbCswCatalog");
            this.cmbCswCatalog.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cmbCswCatalog.FormattingEnabled = true;
            this.cmbCswCatalog.Name = "cmbCswCatalog";
            this.cmbCswCatalog.Sorted = true;
            this.cmbCswCatalog.SelectedIndexChanged += new System.EventHandler(this.cmbCswCatalog_SelectedIndexChanged);
            // 
            // lblCatalog
            // 
            resources.ApplyResources(this.lblCatalog, "lblCatalog");
            this.lblCatalog.Name = "lblCatalog";
            // 
            // chkLiveDataAndMapOnly
            // 
            resources.ApplyResources(this.chkLiveDataAndMapOnly, "chkLiveDataAndMapOnly");
            this.chkLiveDataAndMapOnly.Name = "chkLiveDataAndMapOnly";
            this.chkLiveDataAndMapOnly.UseVisualStyleBackColor = true;
            // 
            // lblMaximum
            // 
            resources.ApplyResources(this.lblMaximum, "lblMaximum");
            this.lblMaximum.Name = "lblMaximum";
            // 
            // txtSearchPhrase
            // 
            resources.ApplyResources(this.txtSearchPhrase, "txtSearchPhrase");
            this.txtSearchPhrase.Name = "txtSearchPhrase";
            this.txtSearchPhrase.TextChanged += new System.EventHandler(this.txtSearchPhrase_TextChanged);
            // 
            // lblFind
            // 
            resources.ApplyResources(this.lblFind, "lblFind");
            this.lblFind.Name = "lblFind";
            // 
            // toolStripButton1
            // 
            this.toolStripButton1.Name = "toolStripButton1";
            resources.ApplyResources(this.toolStripButton1, "toolStripButton1");
            // 
            // toolStripButton2
            // 
            this.toolStripButton2.Name = "toolStripButton2";
            resources.ApplyResources(this.toolStripButton2, "toolStripButton2");
            // 
            // toolStripSeparator2
            // 
            this.toolStripSeparator2.Name = "toolStripSeparator2";
            resources.ApplyResources(this.toolStripSeparator2, "toolStripSeparator2");
            // 
            // toolStripButton3
            // 
            this.toolStripButton3.Name = "toolStripButton3";
            resources.ApplyResources(this.toolStripButton3, "toolStripButton3");
            // 
            // toolStripButton4
            // 
            this.toolStripButton4.Name = "toolStripButton4";
            resources.ApplyResources(this.toolStripButton4, "toolStripButton4");
            // 
            // toolStripButton5
            // 
            this.toolStripButton5.Name = "toolStripButton5";
            resources.ApplyResources(this.toolStripButton5, "toolStripButton5");
            // 
            // toolStripButton6
            // 
            this.toolStripButton6.Name = "toolStripButton6";
            resources.ApplyResources(this.toolStripButton6, "toolStripButton6");
            // 
            // toolStripButton7
            // 
            this.toolStripButton7.Name = "toolStripButton7";
            resources.ApplyResources(this.toolStripButton7, "toolStripButton7");
            // 
            // lblCatalogs
            // 
            resources.ApplyResources(this.lblCatalogs, "lblCatalogs");
            this.lblCatalogs.Name = "lblCatalogs";
            // 
            // combProfile
            // 
            resources.ApplyResources(this.combProfile, "combProfile");
            this.combProfile.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.combProfile.FormattingEnabled = true;
            this.combProfile.Name = "combProfile";
            this.combProfile.Sorted = true;
            // 
            // txtURL
            // 
            resources.ApplyResources(this.txtURL, "txtURL");
            this.txtURL.Name = "txtURL";
            this.txtURL.TextChanged += new System.EventHandler(this.txtURL_TextChanged);
            // 
            // lstCatalog
            // 
            resources.ApplyResources(this.lstCatalog, "lstCatalog");
            this.lstCatalog.FormattingEnabled = true;
            this.lstCatalog.Name = "lstCatalog";
            this.lstCatalog.Sorted = true;
            this.lstCatalog.SelectedIndexChanged += new System.EventHandler(this.lstCatalog_SelectedIndexChanged);
            // 
            // txtDisplayName
            // 
            resources.ApplyResources(this.txtDisplayName, "txtDisplayName");
            this.txtDisplayName.Name = "txtDisplayName";
            this.txtDisplayName.TextChanged += new System.EventHandler(this.txtDisplayName_TextChanged);
            // 
            // btnAdd
            // 
            resources.ApplyResources(this.btnAdd, "btnAdd");
            this.btnAdd.Name = "btnAdd";
            this.toolTip.SetToolTip(this.btnAdd, resources.GetString("btnAdd.ToolTip"));
            this.btnAdd.UseVisualStyleBackColor = true;
            this.btnAdd.Click += new System.EventHandler(this.btnAdd_Click);
            // 
            // btnDelete
            // 
            resources.ApplyResources(this.btnDelete, "btnDelete");
            this.btnDelete.Name = "btnDelete";
            this.toolTip.SetToolTip(this.btnDelete, resources.GetString("btnDelete.ToolTip"));
            this.btnDelete.UseVisualStyleBackColor = true;
            this.btnDelete.Click += new System.EventHandler(this.btnDelete_Click);
            // 
            // btnSave
            // 
            resources.ApplyResources(this.btnSave, "btnSave");
            this.btnSave.Name = "btnSave";
            this.toolTip.SetToolTip(this.btnSave, resources.GetString("btnSave.ToolTip"));
            this.btnSave.UseVisualStyleBackColor = true;
            this.btnSave.Click += new System.EventHandler(this.btnSave_Click);
            // 
            // imgLst
            // 
            this.imgLst.ImageStream = ((System.Windows.Forms.ImageListStreamer)(resources.GetObject("imgLst.ImageStream")));
            this.imgLst.TransparentColor = System.Drawing.Color.Fuchsia;
            this.imgLst.Images.SetKeyName(0, "GetLocation.bmp");
            // 
            // labelPlaceholder
            // 
            resources.ApplyResources(this.labelPlaceholder, "labelPlaceholder");
            this.labelPlaceholder.MinimumSize = new System.Drawing.Size(343, 400);
            this.labelPlaceholder.Name = "labelPlaceholder";
            // 
            // lblProfile
            // 
            resources.ApplyResources(this.lblProfile, "lblProfile");
            this.lblProfile.Name = "lblProfile";
            // 
            // lblDisplayName
            // 
            resources.ApplyResources(this.lblDisplayName, "lblDisplayName");
            this.lblDisplayName.Name = "lblDisplayName";
            // 
            // tabConfigure
            // 
            this.tabConfigure.BackColor = System.Drawing.SystemColors.Window;
            this.tabConfigure.Controls.Add(this.lblCatalogs);
            this.tabConfigure.Controls.Add(this.combProfile);
            this.tabConfigure.Controls.Add(this.btnSave);
            this.tabConfigure.Controls.Add(this.btnDelete);
            this.tabConfigure.Controls.Add(this.btnAdd);
            this.tabConfigure.Controls.Add(this.txtURL);
            this.tabConfigure.Controls.Add(this.lstCatalog);
            this.tabConfigure.Controls.Add(this.txtDisplayName);
            this.tabConfigure.Controls.Add(this.lblProfile);
            this.tabConfigure.Controls.Add(this.lblDisplayName);
            this.tabConfigure.Controls.Add(this.lblCatalogService);
            resources.ApplyResources(this.tabConfigure, "tabConfigure");
            this.tabConfigure.Name = "tabConfigure";
            // 
            // lblCatalogService
            // 
            resources.ApplyResources(this.lblCatalogService, "lblCatalogService");
            this.lblCatalogService.Name = "lblCatalogService";
            // 
            // tabHelp
            // 
            this.tabHelp.Controls.Add(this.linkLblAbt);
            this.tabHelp.Controls.Add(this.linkLblHelp);
            resources.ApplyResources(this.tabHelp, "tabHelp");
            this.tabHelp.Name = "tabHelp";
            this.tabHelp.UseVisualStyleBackColor = true;
            // 
            // linkLblAbt
            // 
            resources.ApplyResources(this.linkLblAbt, "linkLblAbt");
            this.linkLblAbt.Name = "linkLblAbt";
            this.linkLblAbt.TabStop = true;
            this.toolTip.SetToolTip(this.linkLblAbt, resources.GetString("linkLblAbt.ToolTip"));
            this.linkLblAbt.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkLblAbt_LinkClicked);
            // 
            // linkLblHelp
            // 
            resources.ApplyResources(this.linkLblHelp, "linkLblHelp");
            this.linkLblHelp.Name = "linkLblHelp";
            this.linkLblHelp.TabStop = true;
            this.toolTip.SetToolTip(this.linkLblHelp, resources.GetString("linkLblHelp.ToolTip"));
            this.linkLblHelp.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkLblHelp_LinkClicked);
            // 
            // tabMain
            // 
            this.tabMain.Controls.Add(this.tabSearch);
            this.tabMain.Controls.Add(this.tabConfigure);
            this.tabMain.Controls.Add(this.tabHelp);
            resources.ApplyResources(this.tabMain, "tabMain");
            this.tabMain.MinimumSize = new System.Drawing.Size(343, 267);
            this.tabMain.Name = "tabMain";
            this.tabMain.SelectedIndex = 0;
            // 
            // toolTip
            // 
            this.toolTip.UseAnimation = false;
            this.toolTip.UseFading = false;
            // 
            // CSWSearchDockWindow
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Window;
            this.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.Controls.Add(this.tabMain);
            this.Controls.Add(this.labelPlaceholder);
            this.Name = "CSWSearchDockWindow";
            this.tabSearch.ResumeLayout(false);
            this.tabSearch.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.nudNumOfResults)).EndInit();
            this.splitContainer1.Panel1.ResumeLayout(false);
            this.splitContainer1.Panel1.PerformLayout();
            this.splitContainer1.Panel2.ResumeLayout(false);
            this.splitContainer1.Panel2.PerformLayout();
            this.splitContainer1.ResumeLayout(false);
            this.toolstripSearchResults.ResumeLayout(false);
            this.toolstripSearchResults.PerformLayout();
            this.tabConfigure.ResumeLayout(false);
            this.tabConfigure.PerformLayout();
            this.tabHelp.ResumeLayout(false);
            this.tabHelp.PerformLayout();
            this.tabMain.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.TabPage tabSearch;
        private System.Windows.Forms.NumericUpDown nudNumOfResults;
        private System.Windows.Forms.Label lblResult;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.SplitContainer splitContainer1;
        private System.Windows.Forms.ToolStripButton tsbViewMetadata;
        private System.Windows.Forms.ToolStripButton tsbDownloadMetadata;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripButton tsbAddToMap;
        private System.Windows.Forms.ToolStripButton displayFootPrintTSBtn;
        private System.Windows.Forms.ToolStripButton zoomToFootPrintTSBtn;
        private System.Windows.Forms.ToolStripButton showAllFootPrintTSBtn;
        private System.Windows.Forms.ToolStripButton clearAllFootPrintTSBtn;
        private System.Windows.Forms.ListBox lstSearchResults;
     /*   private System.Windows.Forms.ContextMenuStrip contextMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem addToMapToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem zoomToLayerToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem removeFromMapToolStripMenuItem; */
        private System.Windows.Forms.TextBox txtAbstract;
        private System.Windows.Forms.Label lblAbstract;
        private System.Windows.Forms.Button btnSearch;
        private System.Windows.Forms.ComboBox cmbCswCatalog;
        private System.Windows.Forms.Label lblCatalog;
        private System.Windows.Forms.CheckBox chkLiveDataAndMapOnly;
        private System.Windows.Forms.Label lblMaximum;
        private System.Windows.Forms.TextBox txtSearchPhrase;
        private System.Windows.Forms.Label lblFind;
        private System.Windows.Forms.Label lblCatalogs;
        private System.Windows.Forms.ComboBox combProfile;
        private System.Windows.Forms.TextBox txtURL;
        private System.Windows.Forms.ListBox lstCatalog;
        private System.Windows.Forms.TextBox txtDisplayName;
        private System.Windows.Forms.Button btnAdd;
        private System.Windows.Forms.ToolTip toolTip;
        private System.Windows.Forms.Button btnDelete;
        private System.Windows.Forms.Button btnSave;
        private System.Windows.Forms.ImageList imgLst;
        private System.Windows.Forms.Label labelPlaceholder;
        private System.Windows.Forms.Label lblProfile;
        private System.Windows.Forms.Label lblDisplayName;
        private System.Windows.Forms.TabPage tabConfigure;
        private System.Windows.Forms.Label lblCatalogService;
        private System.Windows.Forms.TabPage tabHelp;
        private System.Windows.Forms.TabControl tabMain;
        private System.Windows.Forms.ToolStrip toolstripSearchResults;
        private System.Windows.Forms.ToolStripButton toolStripButton1;
        private System.Windows.Forms.ToolStripButton toolStripButton2;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator2;
        private System.Windows.Forms.ToolStripButton toolStripButton3;
        private System.Windows.Forms.ToolStripButton toolStripButton4;
        private System.Windows.Forms.ToolStripButton toolStripButton5;
        private System.Windows.Forms.ToolStripButton toolStripButton6;
        private System.Windows.Forms.ToolStripButton toolStripButton7;
        private System.Windows.Forms.LinkLabel linkLblAbt;
        private System.Windows.Forms.LinkLabel linkLblHelp;
    }
}

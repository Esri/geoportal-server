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
    partial class CswClientDockableWindow
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(CswClientDockableWindow));
            this.resultsSplitContainer = new System.Windows.Forms.SplitContainer();
            this.clearAllFootprinttoolStripButton = new System.Windows.Forms.Button();
            this.showAllFootprintToolStripButton = new System.Windows.Forms.Button();
            this.zoomtoFootprintToolStripButton = new System.Windows.Forms.Button();
            this.displayFootprinttoolStripButton = new System.Windows.Forms.Button();
            this.addToMapToolStripButton = new System.Windows.Forms.Button();
            this.downloadMetadataToolStripButton = new System.Windows.Forms.Button();
            this.viewMetadataToolStripButton = new System.Windows.Forms.Button();
            this.resultsListBox = new System.Windows.Forms.ListBox();
            this.abstractTextBox = new System.Windows.Forms.TextBox();
            this.abstractLabel = new System.Windows.Forms.Label();
            this.mainTabControl = new System.Windows.Forms.TabControl();
            this.findTabPage = new System.Windows.Forms.TabPage();
            this.maxResultsNumericUpDown = new System.Windows.Forms.NumericUpDown();
            this.dividerGroupBox = new System.Windows.Forms.GroupBox();
            this.useCurrentExtentCheckBox = new System.Windows.Forms.CheckBox();
            this.findButton = new System.Windows.Forms.Button();
            this.catalogComboBox = new System.Windows.Forms.ComboBox();
            this.inCatalogLabel = new System.Windows.Forms.Label();
            this.liveDataAndMapsOnlyCheckBox = new System.Windows.Forms.CheckBox();
            this.maximumLabel = new System.Windows.Forms.Label();
            this.searchPhraseTextBox = new System.Windows.Forms.TextBox();
            this.findLabel = new System.Windows.Forms.Label();
            this.resultsLabel = new System.Windows.Forms.Label();
            this.configureTabPage = new System.Windows.Forms.TabPage();
            this.catalogListBox = new System.Windows.Forms.ListBox();
            this.saveCatalogButton = new System.Windows.Forms.Button();
            this.deleteCatalogButton = new System.Windows.Forms.Button();
            this.newCatalogButton = new System.Windows.Forms.Button();
            this.catalogListLabel = new System.Windows.Forms.Label();
            this.catalogProfileComboBox = new System.Windows.Forms.ComboBox();
            this.catalogUrlTextBox = new System.Windows.Forms.TextBox();
            this.catalogDisplayNameTextBox = new System.Windows.Forms.TextBox();
            this.catalogProfileLabel = new System.Windows.Forms.Label();
            this.catalogDisplayNameLabel = new System.Windows.Forms.Label();
            this.catalogUrlLabel = new System.Windows.Forms.Label();
            this.helpTabPage = new System.Windows.Forms.TabPage();
            this.linkLblAbt = new System.Windows.Forms.LinkLabel();
            this.linkLblHelp = new System.Windows.Forms.LinkLabel();
            this.tooltip = new System.Windows.Forms.ToolTip(this.components);
            this.resultsSplitContainer.Panel1.SuspendLayout();
            this.resultsSplitContainer.Panel2.SuspendLayout();
            this.resultsSplitContainer.SuspendLayout();
            this.mainTabControl.SuspendLayout();
            this.findTabPage.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.maxResultsNumericUpDown)).BeginInit();
            this.configureTabPage.SuspendLayout();
            this.helpTabPage.SuspendLayout();
            this.SuspendLayout();
            // 
            // resultsSplitContainer
            // 
            resources.ApplyResources(this.resultsSplitContainer, "resultsSplitContainer");
            this.resultsSplitContainer.Name = "resultsSplitContainer";
            // 
            // resultsSplitContainer.Panel1
            // 
            this.resultsSplitContainer.Panel1.Controls.Add(this.clearAllFootprinttoolStripButton);
            this.resultsSplitContainer.Panel1.Controls.Add(this.showAllFootprintToolStripButton);
            this.resultsSplitContainer.Panel1.Controls.Add(this.zoomtoFootprintToolStripButton);
            this.resultsSplitContainer.Panel1.Controls.Add(this.displayFootprinttoolStripButton);
            this.resultsSplitContainer.Panel1.Controls.Add(this.addToMapToolStripButton);
            this.resultsSplitContainer.Panel1.Controls.Add(this.downloadMetadataToolStripButton);
            this.resultsSplitContainer.Panel1.Controls.Add(this.viewMetadataToolStripButton);
            this.resultsSplitContainer.Panel1.Controls.Add(this.resultsListBox);
            // 
            // resultsSplitContainer.Panel2
            // 
            this.resultsSplitContainer.Panel2.Controls.Add(this.abstractTextBox);
            this.resultsSplitContainer.Panel2.Controls.Add(this.abstractLabel);
            this.resultsSplitContainer.KeyDown += new System.Windows.Forms.KeyEventHandler(this.ResultsSplitContainer_KeyDown);
            // 
            // clearAllFootprinttoolStripButton
            // 
            resources.ApplyResources(this.clearAllFootprinttoolStripButton, "clearAllFootprinttoolStripButton");
            this.clearAllFootprinttoolStripButton.Name = "clearAllFootprinttoolStripButton";
            this.tooltip.SetToolTip(this.clearAllFootprinttoolStripButton, resources.GetString("clearAllFootprinttoolStripButton.ToolTip"));
            this.clearAllFootprinttoolStripButton.UseVisualStyleBackColor = true;
            this.clearAllFootprinttoolStripButton.Click += new System.EventHandler(this.clearAllFootprinttoolStripButton_Click);
            // 
            // showAllFootprintToolStripButton
            // 
            resources.ApplyResources(this.showAllFootprintToolStripButton, "showAllFootprintToolStripButton");
            this.showAllFootprintToolStripButton.Image = global::com.esri.gpt.csw.StringResources.showAll;
            this.showAllFootprintToolStripButton.Name = "showAllFootprintToolStripButton";
            this.tooltip.SetToolTip(this.showAllFootprintToolStripButton, resources.GetString("showAllFootprintToolStripButton.ToolTip"));
            this.showAllFootprintToolStripButton.UseVisualStyleBackColor = true;
            this.showAllFootprintToolStripButton.Click += new System.EventHandler(this.showAllFootprint_Click);
            // 
            // zoomtoFootprintToolStripButton
            // 
            resources.ApplyResources(this.zoomtoFootprintToolStripButton, "zoomtoFootprintToolStripButton");
            this.zoomtoFootprintToolStripButton.Image = global::com.esri.gpt.csw.StringResources.zoom_to_footprint;
            this.zoomtoFootprintToolStripButton.Name = "zoomtoFootprintToolStripButton";
            this.tooltip.SetToolTip(this.zoomtoFootprintToolStripButton, resources.GetString("zoomtoFootprintToolStripButton.ToolTip"));
            this.zoomtoFootprintToolStripButton.UseVisualStyleBackColor = true;
            this.zoomtoFootprintToolStripButton.Click += new System.EventHandler(this.zoomtoFootprintToolStripButton_Click);
            // 
            // displayFootprinttoolStripButton
            // 
            resources.ApplyResources(this.displayFootprinttoolStripButton, "displayFootprinttoolStripButton");
            this.displayFootprinttoolStripButton.Image = global::com.esri.gpt.csw.StringResources.display_footprint;
            this.displayFootprinttoolStripButton.Name = "displayFootprinttoolStripButton";
            this.tooltip.SetToolTip(this.displayFootprinttoolStripButton, resources.GetString("displayFootprinttoolStripButton.ToolTip"));
            this.displayFootprinttoolStripButton.UseVisualStyleBackColor = true;
            this.displayFootprinttoolStripButton.Click += new System.EventHandler(this.displayFootprinttoolStripButton_Click);
            // 
            // addToMapToolStripButton
            // 
            resources.ApplyResources(this.addToMapToolStripButton, "addToMapToolStripButton");
            this.addToMapToolStripButton.Name = "addToMapToolStripButton";
            this.tooltip.SetToolTip(this.addToMapToolStripButton, resources.GetString("addToMapToolStripButton.ToolTip"));
            this.addToMapToolStripButton.UseVisualStyleBackColor = true;
            this.addToMapToolStripButton.Click += new System.EventHandler(this.AddToMapToolStripButton_Click);
            // 
            // downloadMetadataToolStripButton
            // 
            resources.ApplyResources(this.downloadMetadataToolStripButton, "downloadMetadataToolStripButton");
            this.downloadMetadataToolStripButton.Name = "downloadMetadataToolStripButton";
            this.tooltip.SetToolTip(this.downloadMetadataToolStripButton, resources.GetString("downloadMetadataToolStripButton.ToolTip"));
            this.downloadMetadataToolStripButton.UseVisualStyleBackColor = true;
            this.downloadMetadataToolStripButton.Click += new System.EventHandler(this.DownloadMetadataToolStripButton_Click);
            // 
            // viewMetadataToolStripButton
            // 
            resources.ApplyResources(this.viewMetadataToolStripButton, "viewMetadataToolStripButton");
            this.viewMetadataToolStripButton.Name = "viewMetadataToolStripButton";
            this.tooltip.SetToolTip(this.viewMetadataToolStripButton, resources.GetString("viewMetadataToolStripButton.ToolTip"));
            this.viewMetadataToolStripButton.UseVisualStyleBackColor = true;
            this.viewMetadataToolStripButton.Click += new System.EventHandler(this.ViewMetadataToolStripButton_Click);
            // 
            // resultsListBox
            // 
            resources.ApplyResources(this.resultsListBox, "resultsListBox");
            this.resultsListBox.FormattingEnabled = true;
            this.resultsListBox.Name = "resultsListBox";
            this.resultsListBox.SelectedIndexChanged += new System.EventHandler(this.ResultsListBox_SelectedIndexChanged);
            this.resultsListBox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.ResultsListBox_KeyDown);
            // 
            // abstractTextBox
            // 
            resources.ApplyResources(this.abstractTextBox, "abstractTextBox");
            this.abstractTextBox.BackColor = System.Drawing.SystemColors.Window;
            this.abstractTextBox.Name = "abstractTextBox";
            this.abstractTextBox.ReadOnly = true;
            // 
            // abstractLabel
            // 
            resources.ApplyResources(this.abstractLabel, "abstractLabel");
            this.abstractLabel.Name = "abstractLabel";
            // 
            // mainTabControl
            // 
            this.mainTabControl.Controls.Add(this.findTabPage);
            this.mainTabControl.Controls.Add(this.configureTabPage);
            this.mainTabControl.Controls.Add(this.helpTabPage);
            resources.ApplyResources(this.mainTabControl, "mainTabControl");
            this.mainTabControl.Name = "mainTabControl";
            this.mainTabControl.SelectedIndex = 0;
            // 
            // findTabPage
            // 
            this.findTabPage.Controls.Add(this.maxResultsNumericUpDown);
            this.findTabPage.Controls.Add(this.dividerGroupBox);
            this.findTabPage.Controls.Add(this.useCurrentExtentCheckBox);
            this.findTabPage.Controls.Add(this.resultsSplitContainer);
            this.findTabPage.Controls.Add(this.findButton);
            this.findTabPage.Controls.Add(this.catalogComboBox);
            this.findTabPage.Controls.Add(this.inCatalogLabel);
            this.findTabPage.Controls.Add(this.liveDataAndMapsOnlyCheckBox);
            this.findTabPage.Controls.Add(this.maximumLabel);
            this.findTabPage.Controls.Add(this.searchPhraseTextBox);
            this.findTabPage.Controls.Add(this.findLabel);
            this.findTabPage.Controls.Add(this.resultsLabel);
            resources.ApplyResources(this.findTabPage, "findTabPage");
            this.findTabPage.Name = "findTabPage";
            this.findTabPage.UseVisualStyleBackColor = true;
            this.findTabPage.Enter += new System.EventHandler(this.FindTabPage_Enter);
            // 
            // maxResultsNumericUpDown
            // 
            resources.ApplyResources(this.maxResultsNumericUpDown, "maxResultsNumericUpDown");
            this.maxResultsNumericUpDown.Maximum = new decimal(new int[] {
            500,
            0,
            0,
            0});
            this.maxResultsNumericUpDown.Name = "maxResultsNumericUpDown";
            this.maxResultsNumericUpDown.Value = new decimal(new int[] {
            10,
            0,
            0,
            0});
            this.maxResultsNumericUpDown.Leave += new System.EventHandler(this.MaxResultsNumericUpDown_Leave);
            // 
            // dividerGroupBox
            // 
            resources.ApplyResources(this.dividerGroupBox, "dividerGroupBox");
            this.dividerGroupBox.Name = "dividerGroupBox";
            this.dividerGroupBox.TabStop = false;
            // 
            // useCurrentExtentCheckBox
            // 
            resources.ApplyResources(this.useCurrentExtentCheckBox, "useCurrentExtentCheckBox");
            this.useCurrentExtentCheckBox.Name = "useCurrentExtentCheckBox";
            this.useCurrentExtentCheckBox.UseVisualStyleBackColor = true;
            // 
            // findButton
            // 
            resources.ApplyResources(this.findButton, "findButton");
            this.findButton.Name = "findButton";
            this.findButton.UseVisualStyleBackColor = true;
            this.findButton.Click += new System.EventHandler(this.FindButton_Click);
            // 
            // catalogComboBox
            // 
            resources.ApplyResources(this.catalogComboBox, "catalogComboBox");
            this.catalogComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.catalogComboBox.FormattingEnabled = true;
            this.catalogComboBox.Name = "catalogComboBox";
            this.catalogComboBox.Sorted = true;
            this.catalogComboBox.SelectedIndexChanged += new System.EventHandler(this.CatalogComboBox_SelectedIndexChanged);
            // 
            // inCatalogLabel
            // 
            resources.ApplyResources(this.inCatalogLabel, "inCatalogLabel");
            this.inCatalogLabel.Name = "inCatalogLabel";
            // 
            // liveDataAndMapsOnlyCheckBox
            // 
            resources.ApplyResources(this.liveDataAndMapsOnlyCheckBox, "liveDataAndMapsOnlyCheckBox");
            this.liveDataAndMapsOnlyCheckBox.Name = "liveDataAndMapsOnlyCheckBox";
            this.liveDataAndMapsOnlyCheckBox.UseVisualStyleBackColor = true;
            // 
            // maximumLabel
            // 
            resources.ApplyResources(this.maximumLabel, "maximumLabel");
            this.maximumLabel.Name = "maximumLabel";
            // 
            // searchPhraseTextBox
            // 
            resources.ApplyResources(this.searchPhraseTextBox, "searchPhraseTextBox");
            this.searchPhraseTextBox.Name = "searchPhraseTextBox";
            this.searchPhraseTextBox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.SearchPhraseTextBox_KeyDown);
            // 
            // findLabel
            // 
            resources.ApplyResources(this.findLabel, "findLabel");
            this.findLabel.Name = "findLabel";
            // 
            // resultsLabel
            // 
            resources.ApplyResources(this.resultsLabel, "resultsLabel");
            this.resultsLabel.Name = "resultsLabel";
            // 
            // configureTabPage
            // 
            this.configureTabPage.Controls.Add(this.catalogListBox);
            this.configureTabPage.Controls.Add(this.saveCatalogButton);
            this.configureTabPage.Controls.Add(this.deleteCatalogButton);
            this.configureTabPage.Controls.Add(this.newCatalogButton);
            this.configureTabPage.Controls.Add(this.catalogListLabel);
            this.configureTabPage.Controls.Add(this.catalogProfileComboBox);
            this.configureTabPage.Controls.Add(this.catalogUrlTextBox);
            this.configureTabPage.Controls.Add(this.catalogDisplayNameTextBox);
            this.configureTabPage.Controls.Add(this.catalogProfileLabel);
            this.configureTabPage.Controls.Add(this.catalogDisplayNameLabel);
            this.configureTabPage.Controls.Add(this.catalogUrlLabel);
            resources.ApplyResources(this.configureTabPage, "configureTabPage");
            this.configureTabPage.Name = "configureTabPage";
            this.configureTabPage.UseVisualStyleBackColor = true;
            this.configureTabPage.Enter += new System.EventHandler(this.ConfigureTabPage_Enter);
            // 
            // catalogListBox
            // 
            resources.ApplyResources(this.catalogListBox, "catalogListBox");
            this.catalogListBox.FormattingEnabled = true;
            this.catalogListBox.Name = "catalogListBox";
            this.catalogListBox.Sorted = true;
            this.catalogListBox.SelectedIndexChanged += new System.EventHandler(this.CatalogListBox_SelectedIndexChanged);
            // 
            // saveCatalogButton
            // 
            resources.ApplyResources(this.saveCatalogButton, "saveCatalogButton");
            this.saveCatalogButton.Name = "saveCatalogButton";
            this.tooltip.SetToolTip(this.saveCatalogButton, resources.GetString("saveCatalogButton.ToolTip"));
            this.saveCatalogButton.UseVisualStyleBackColor = true;
            this.saveCatalogButton.Click += new System.EventHandler(this.SaveCatalogButton_Click);
            // 
            // deleteCatalogButton
            // 
            resources.ApplyResources(this.deleteCatalogButton, "deleteCatalogButton");
            this.deleteCatalogButton.Name = "deleteCatalogButton";
            this.tooltip.SetToolTip(this.deleteCatalogButton, resources.GetString("deleteCatalogButton.ToolTip"));
            this.deleteCatalogButton.UseVisualStyleBackColor = true;
            this.deleteCatalogButton.Click += new System.EventHandler(this.DeleteCatalogButton_Click);
            // 
            // newCatalogButton
            // 
            resources.ApplyResources(this.newCatalogButton, "newCatalogButton");
            this.newCatalogButton.Name = "newCatalogButton";
            this.tooltip.SetToolTip(this.newCatalogButton, resources.GetString("newCatalogButton.ToolTip"));
            this.newCatalogButton.UseVisualStyleBackColor = true;
            this.newCatalogButton.Click += new System.EventHandler(this.NewCatalogButton_Click);
            // 
            // catalogListLabel
            // 
            resources.ApplyResources(this.catalogListLabel, "catalogListLabel");
            this.catalogListLabel.Name = "catalogListLabel";
            // 
            // catalogProfileComboBox
            // 
            resources.ApplyResources(this.catalogProfileComboBox, "catalogProfileComboBox");
            this.catalogProfileComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.catalogProfileComboBox.FormattingEnabled = true;
            this.catalogProfileComboBox.Name = "catalogProfileComboBox";
            this.catalogProfileComboBox.Sorted = true;
            this.catalogProfileComboBox.SelectedIndexChanged += new System.EventHandler(this.CatalogProfileComboBox_SelectedIndexChanged);
            // 
            // catalogUrlTextBox
            // 
            resources.ApplyResources(this.catalogUrlTextBox, "catalogUrlTextBox");
            this.catalogUrlTextBox.Name = "catalogUrlTextBox";
            this.catalogUrlTextBox.TextChanged += new System.EventHandler(this.CatalogUrlTextBox_TextChanged);
            this.catalogUrlTextBox.MouseHover += new System.EventHandler(this.CatalogUrlTextBox_MouseHover);
            // 
            // catalogDisplayNameTextBox
            // 
            resources.ApplyResources(this.catalogDisplayNameTextBox, "catalogDisplayNameTextBox");
            this.catalogDisplayNameTextBox.Name = "catalogDisplayNameTextBox";
            this.catalogDisplayNameTextBox.TextChanged += new System.EventHandler(this.CatalogDisplayNameTextBox_TextChanged);
            // 
            // catalogProfileLabel
            // 
            resources.ApplyResources(this.catalogProfileLabel, "catalogProfileLabel");
            this.catalogProfileLabel.Name = "catalogProfileLabel";
            // 
            // catalogDisplayNameLabel
            // 
            resources.ApplyResources(this.catalogDisplayNameLabel, "catalogDisplayNameLabel");
            this.catalogDisplayNameLabel.Name = "catalogDisplayNameLabel";
            // 
            // catalogUrlLabel
            // 
            resources.ApplyResources(this.catalogUrlLabel, "catalogUrlLabel");
            this.catalogUrlLabel.Name = "catalogUrlLabel";
            // 
            // helpTabPage
            // 
            this.helpTabPage.Controls.Add(this.linkLblAbt);
            this.helpTabPage.Controls.Add(this.linkLblHelp);
            resources.ApplyResources(this.helpTabPage, "helpTabPage");
            this.helpTabPage.Name = "helpTabPage";
            this.helpTabPage.UseVisualStyleBackColor = true;
            // 
            // linkLblAbt
            // 
            resources.ApplyResources(this.linkLblAbt, "linkLblAbt");
            this.linkLblAbt.Name = "linkLblAbt";
            this.linkLblAbt.TabStop = true;
            this.tooltip.SetToolTip(this.linkLblAbt, resources.GetString("linkLblAbt.ToolTip"));
            this.linkLblAbt.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkLblAbt_LinkClicked);
            // 
            // linkLblHelp
            // 
            resources.ApplyResources(this.linkLblHelp, "linkLblHelp");
            this.linkLblHelp.Name = "linkLblHelp";
            this.linkLblHelp.TabStop = true;
            this.tooltip.SetToolTip(this.linkLblHelp, resources.GetString("linkLblHelp.ToolTip"));
            this.linkLblHelp.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkLblHelp_LinkClicked);
            // 
            // CswClientDockableWindow
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.mainTabControl);
            this.Name = "CswClientDockableWindow";
            this.KeyDown += new System.Windows.Forms.KeyEventHandler(this.CswSearchForm_KeyDown);
            this.resultsSplitContainer.Panel1.ResumeLayout(false);
            this.resultsSplitContainer.Panel2.ResumeLayout(false);
            this.resultsSplitContainer.Panel2.PerformLayout();
            this.resultsSplitContainer.ResumeLayout(false);
            this.mainTabControl.ResumeLayout(false);
            this.findTabPage.ResumeLayout(false);
            this.findTabPage.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.maxResultsNumericUpDown)).EndInit();
            this.configureTabPage.ResumeLayout(false);
            this.configureTabPage.PerformLayout();
            this.helpTabPage.ResumeLayout(false);
            this.helpTabPage.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListBox resultsListBox;
        private System.Windows.Forms.TabControl mainTabControl;
        private System.Windows.Forms.TabPage findTabPage;
        private System.Windows.Forms.NumericUpDown maxResultsNumericUpDown;
        private System.Windows.Forms.GroupBox dividerGroupBox;
        private System.Windows.Forms.CheckBox useCurrentExtentCheckBox;
        private System.Windows.Forms.SplitContainer resultsSplitContainer;
        private System.Windows.Forms.Button clearAllFootprinttoolStripButton;
        private System.Windows.Forms.ToolTip tooltip;
        private System.Windows.Forms.Button showAllFootprintToolStripButton;
        private System.Windows.Forms.Button zoomtoFootprintToolStripButton;
        private System.Windows.Forms.Button displayFootprinttoolStripButton;
        private System.Windows.Forms.Button addToMapToolStripButton;
        private System.Windows.Forms.Button downloadMetadataToolStripButton;
        private System.Windows.Forms.Button viewMetadataToolStripButton;
        private System.Windows.Forms.TextBox abstractTextBox;
        private System.Windows.Forms.Label abstractLabel;
        private System.Windows.Forms.Button findButton;
        private System.Windows.Forms.ComboBox catalogComboBox;
        private System.Windows.Forms.Label inCatalogLabel;
        private System.Windows.Forms.CheckBox liveDataAndMapsOnlyCheckBox;
        private System.Windows.Forms.Label maximumLabel;
        private System.Windows.Forms.TextBox searchPhraseTextBox;
        private System.Windows.Forms.Label findLabel;
        private System.Windows.Forms.Label resultsLabel;
        private System.Windows.Forms.TabPage configureTabPage;
        private System.Windows.Forms.ListBox catalogListBox;
        private System.Windows.Forms.Button saveCatalogButton;
        private System.Windows.Forms.Button deleteCatalogButton;
        private System.Windows.Forms.Button newCatalogButton;
        private System.Windows.Forms.Label catalogListLabel;
        private System.Windows.Forms.ComboBox catalogProfileComboBox;
        private System.Windows.Forms.TextBox catalogUrlTextBox;
        private System.Windows.Forms.TextBox catalogDisplayNameTextBox;
        private System.Windows.Forms.Label catalogProfileLabel;
        private System.Windows.Forms.Label catalogDisplayNameLabel;
        private System.Windows.Forms.Label catalogUrlLabel;
        private System.Windows.Forms.TabPage helpTabPage;
        private System.Windows.Forms.LinkLabel linkLblAbt;
        private System.Windows.Forms.LinkLabel linkLblHelp;


    }
}

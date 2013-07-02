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
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Xml;
using ESRI.ArcGIS.ADF.CATIDs;
using Microsoft.Win32;
using ESRI.ArcGIS.CatalogUI;
using ESRI.ArcGIS.Catalog;
using System.Text;
using System.IO;

using ESRI.ArcGIS.Carto;
using ESRI.ArcGIS.esriSystem;
using ESRI.ArcGIS.SystemUI;
using ESRI.ArcGIS.Geodatabase;
using ESRI.ArcGIS;
using ESRI.ArcGIS.Geoprocessor;
using ESRI.ArcGIS.Geoprocessing;
using ESRI.ArcGIS.Framework;
using System.Globalization;
using com.esri.gpt.logger;
namespace com.esri.gpt.publish
{
    /// <summary>
    /// CSW search control as a dockable window.
    /// </summary>
    [Guid("A9B6CD32-18EA-4610-875B-170E7F05E1EF")]
    [ClassInterface(ClassInterfaceType.None)]
    [ProgId("GPTPublishClient.Publish")]
    public partial class PublishForm : Form
    {
        #region Private Variable(s)
        private string userSettingFile;
        private UserSettings us = null;
        private PublicationRequest pr = null;
        private string workDirectory = null;
        private string logFilePath = null;        
        #endregion
        #region Class Variable
        /// <summary>
        /// Application logger
        /// </summary>
        private static AppLogger logger = null;
        #endregion
        #region Property Accessor Methods
        /// <summary>
        /// Application logger
        /// </summary>
        public static AppLogger Logger
        {
            get
            {
                return logger;
            }
            set
            {
                logger = value;
            }
        }
        #endregion
        #region COM Registration Function(s)
        [ComRegisterFunction()]
        [ComVisible(false)]
        static void RegisterFunction(Type registerType)
        {
            // Required for ArcGIS Component Category Registrar support
            ArcGISCategoryRegistration(registerType);
            //
            // TODO: Add any COM registration code here
            //
        }

        [ComUnregisterFunction()]
        [ComVisible(false)]
        static void UnregisterFunction(Type registerType)
        {
            // Required for ArcGIS Component Category Registrar support
            ArcGISCategoryUnregistration(registerType);

            //
            // TODO: Add any COM unregistration code here
            //
        }
        #endregion
        #region ArcGIS Component Category Registerer generated code
        /// <summary>
        /// Required method for ArcGIS Component Category registration -
        /// Do not modify the contents of this method with the code editor.
        /// </summary>
        private static void ArcGISCategoryRegistration(Type registerType)
        {
            string regKey = string.Format("HKEY_CLASSES_ROOT\\CLSID\\{{{0}}}", registerType.GUID);
            GxDockableWindows.Register(regKey);
        }
        /// <summary>
        /// Required method for ArcGIS Component Category unregistration -
        /// Do not modify the contents of this method with the code editor.
        /// </summary>
        private static void ArcGISCategoryUnregistration(Type registerType)
        {
            string regKey = string.Format("HKEY_CLASSES_ROOT\\CLSID\\{{{0}}}", registerType.GUID);
            GxDockableWindows.Unregister(regKey);
        }

        #endregion
        #region Constructor/Destructor Function(s)

        /// <summary>
        /// Constructor for Publish Tool
        /// </summary>
        public PublishForm()
        {
            userSettingFile = System.IO.Path.Combine(System.Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), StringMessages.PublishButtonConfigFileName);
            InitializeComponent();
            txtPassword.Text = "";
            try
            {
                InitMyComponents();
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);
            }
        }
        #endregion
        #region Event Methods
        /// <summary>
        /// Loads publication form
        /// </summary>
        /// <param name="e">event arguments</param>
        /// <param name="sender">event sender</param>
        private void PublishForm_Load(object sender, EventArgs e)
        {
            this.Height = 195;
            this.showLogChkBox.Checked = false;
            txtPassword.Text = "";
            this.displayLogsTxtBox.Text = "";
            saveSettings();

        }
        /// <summary>
        /// Closes publication form
        /// </summary>
        /// <param name="e">event arguments</param>
        /// <param name="sender">event sender</param>
        private void btnClose_Click(object sender, EventArgs e)
        {
            this.showLogChkBox.Checked = false;
            txtPassword.Text = "";
            this.displayLogsTxtBox.Text = "";
            saveSettings();
            this.Hide();
        }
        /// <summary>
        /// Invokes publication process
        /// </summary>
        /// <param name="e">event arguments</param>
        /// <param name="sender">event sender</param>
        private void btnPublish_Click(object sender, EventArgs e)
        {
            if (txtServer.Text.Trim().Length == 0)
            {
                ShowWarningMessageBox(StringMessages.InvalidUrl);
                return;
            }

            if (txtService.Text.Trim().Length == 0)
            {
                ShowWarningMessageBox(StringMessages.InvalidService);
                return;
            }
            if (txtUsername.Text.Trim().Length == 0)
            {
                ShowWarningMessageBox(StringMessages.InvalidUserName);
                return;
            }
            if (txtPassword.Text.Trim().Length == 0)
            {
                ShowWarningMessageBox(StringMessages.InvalidPassword);
                return;
            }
            publish();
        }
        /// <summary>
        /// Toggles display of publication summary
        /// </summary>
        /// <param name="e">event arguments</param>
        /// <param name="sender">event sender</param>
        private void showLogChkBox_CheckedChanged(object sender, EventArgs e)
        {
            if (showLogChkBox.Checked)
            {
                this.Height = 538;
                this.groupBox2.Visible = true;
            }
            else
            {
                this.Height = 195;
                this.groupBox2.Visible = false;
            }
        }
        #endregion
        #region Methods

        /// <summary>
        /// Init components for "Publish Metadata" modal window
        /// </summary>
        private void InitMyComponents()
        {
            // version info
            System.Diagnostics.FileVersionInfo fvi = System.Diagnostics.FileVersionInfo.GetVersionInfo(System.Reflection.Assembly.GetExecutingAssembly().Location);
            us = new UserSettings(userSettingFile);
            if (!us.isUserSettingsFileExists())
            {
                us.createUserSettingsFile();
            }
            else
            {
                XmlDocument doc = new XmlDocument();
                doc.Load(userSettingFile);

                XmlNodeList xmlnodes = doc.GetElementsByTagName(StringMessages.Setting);

                foreach (XmlNode xmlnode in xmlnodes)
                {
                    XmlAttributeCollection attributes = xmlnode.Attributes;
                    if (attributes.Item(0).Name.Equals(StringMessages.Server))
                        txtServer.Text = attributes.Item(0).Value;
                    else if (attributes.Item(0).Name.Equals(StringMessages.Service))
                        txtService.Text = attributes.Item(0).Value;
                    else if (attributes.Item(0).Name.Equals(StringMessages.Username))
                        txtUsername.Text = attributes.Item(0).Value;
                }
            }
     
            workDirectory = System.Environment.CurrentDirectory;
            workDirectory = workDirectory.Replace(StringMessages.Bin, StringMessages.PublisherXML);
            workDirectory = workDirectory.Replace("bin", StringMessages.PublisherXML);
            logger = new AppLogger("PublishClient");
            UpdateUI();
        }
        /// <summary>
        /// Apply ArcGIS Locale to Publish tool
        /// </summary>
        private void UpdateUI()
        {
            // setup locale
            IArcGISLocale agsLocale = new ArcGISLocaleClass();
            CultureInfo ci = new CultureInfo(agsLocale.UILocale);

            // set locale
            System.Threading.Thread.CurrentThread.CurrentUICulture = ci;

            //// set flow direction
            if (agsLocale.RightToLeftUI)
            {
                this.RightToLeft = RightToLeft.Yes;
            }
            else
            {
                this.RightToLeft = RightToLeft.No; 
            }
        }
        /// <summary>
        /// Method to initiate publication request
        /// </summary>
        private void publish()
        {
            displayLogsTxtBox.Text = "";              
            PublicationParams parameters = new PublicationParams();
            parameters.Password = txtPassword.Text.Trim();
            parameters.UserName = txtUsername.Text.Trim();
            parameters.ServerUrl = txtServer.Text.Trim();
            parameters.Service = txtService.Text.Trim();
            parameters.LogFilePath = logFilePath;
            parameters.CurrentWorkDir = workDirectory;
            us.Params = parameters;
            System.Windows.Forms.Cursor.Current = Cursors.WaitCursor;
            pr = new PublicationRequest(parameters);
            pr.determineEndPoint();
            try
            {
                IGxApplication catalog = null;
                IGxViewContainer viewContainer = null;
                if (ArcCatalog.Application != null)
                {
                    catalog = (IGxApplication)ArcCatalog.Application;
                    /*viewContainer = (IGxViewContainer)ArcCatalog.Application;
                    IEnumGxView views = viewContainer.Views;
                    IGxView gxView = views.Next();
                    while (gxView != null)
                    {
                        gxView = views.Next();
                    }*/

                }

                if (ArcMap.Application != null)
                {
                    catalog = (IGxApplication)ArcMap.Application;
                } 
                                                           
                IGxObject pGxObject = catalog.Catalog.SelectedObject;
                String category = pGxObject.Category;
                String parentCategory = pGxObject.Parent.Category;

                StringBuilder sb = new StringBuilder();
                sb.AppendLine("************ Metadata Publishing started at   " + System.DateTime.Now + " ************");
                sb.AppendLine("Publication Parameters ");
                sb.AppendLine("============================================================");
                sb.AppendLine("Metadata Server Url           : " + parameters.ServerUrl);
                sb.AppendLine("Publish Metadata Service Name : " + parameters.Service);
                sb.AppendLine("Selected Workspace Name       : " + pGxObject.FullName);
                sb.AppendLine("Selected container category   : " + category);
                sb.AppendLine("============================================================");
                writeLogs(sb.ToString());
           
                publishMetadata(pGxObject, pr);

                //sb.AppendLine(displayLogsTxtBox.Text);
                writeLogs("************ Metadata Publishing completed at " + System.DateTime.Now + " ************");                
                saveSettings();
                System.Windows.Forms.Cursor.Current = Cursors.Default;
                ShowSuccessMessageBox(StringMessages.PublishSuccessMsg + " " + logFilePath + StringMessages.LogFilePath);

            }
            catch (Exception ex)
            {
                PublishForm.ShowErrorMessageBox(StringMessages.PublishFailureMsg + " " + logFilePath + StringMessages.LogFilePath);
            }
        }
        /// <summary>
        /// Recursively Publish metadata documents associated to a container
        /// </summary>
        /// <param name="item">The GxObject object</param>
        /// <param name="pr">The publication request object</param>
        private void publishMetadata(IGxObject item, PublicationRequest pr)
        {
            if (item is IGxObjectContainer && !item.Category.Trim().ToLower().StartsWith("arcgis server"))
            {
                IGxObjectContainer container = (IGxObjectContainer)item;
                IEnumGxObject children = container.Children;
                if (children == null)
                {
                    writeResults(pr.publish(item));
                    return;
                }
                IGxObject child = children.Next();
                while (child != null)
                {
                    publishMetadata(child, pr);
                    child = children.Next();
                }
            }
            else
            {
                if (item is IMetadata)
                {                    
                    writeResults(pr.publish(item));
                }
                else
                {
                    writeResults(pr.publish(item, pr.makeAgsUrl(item)));
                }
            }
        }
        /// <summary>
        /// Displays publication summary
        /// </summary>
        /// <param name="results">Publication results</param>
        private void writeResults(string[] results)
        {
            foreach (string result in results)
            {
                displayLogsTxtBox.Text += result + "\n";
                writeLogs(displayLogsTxtBox.Text);
            }
        }
        /// <summary>
        /// Writes publication summary in logs
        /// </summary>
        /// <param name="contents">Publication results</param>
        private void writeLogs(string contents)
        {            
            logger.writeLog(contents);
        }
        /// <summary>
        /// Display an error message dialog with the provided message string, with default caption, button and icon
        /// </summary>
        /// <param name="ErrorMessage">Error message to be displayed</param>
        public static void ShowSuccessMessageBox(string SuccessMessage)
        {
            MessageBox.Show(SuccessMessage, StringMessages.SuccessMessageDialogCaption, MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
        /// <summary>
        /// Display an error message dialog with the provided message string, with default caption, button and icon
        /// </summary>
        /// <param name="ErrorMessage">Error message to be displayed</param>
        public static void ShowErrorMessageBox(string ErrorMessage)
        {
            MessageBox.Show(ErrorMessage, StringMessages.ErrorMessageDialogCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
        /// <summary>
        /// Display a warning message dialog with the provided message string, with default caption, button and icon
        /// </summary>
        /// <param name="WarningMessage">Warning message to be displayed</param>
        public static void ShowWarningMessageBox(string WarningMessage)
        {
            MessageBox.Show(WarningMessage, StringMessages.WarningMessageDialogCaption, MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
        /// <summary>
        /// Display a error messagebox with details.
        /// </summary>
        /// <param name="message">error message</param>
        /// <param name="detailedMessage">details</param>
        public static void ShowDetailedErrorMessageBox(string message, string details)
        {
            FormMessageBox frmMessageBox = new FormMessageBox();
            frmMessageBox.Init(message, details, StringMessages.ErrorMessageDialogCaption);
            frmMessageBox.ShowDialog();
        }
        /// <summary>
        /// Saves publications parameters
        /// </summary>
        private void saveSettings()
        {
            if (us != null)
            {
                PublicationParams parameters = new PublicationParams();
                parameters.Password = txtPassword.Text.Trim();
                parameters.UserName = txtUsername.Text.Trim();
                parameters.ServerUrl = txtServer.Text.Trim();
                parameters.Service = txtService.Text.Trim();
                us.Params = parameters;
                us.updateUserSettingsFile();
            }
        }
        #endregion
        /// <summary>
        /// Handles click event on help button
        /// </summary>
        /// <param name="e">event arguments</param>
        /// <param name="sender">event sender</param>
        private void hlpBtn_Click(object sender, EventArgs e)
        {
            System.Diagnostics.Process.Start("IExplore", StringMessages.helpUrl);
        }
    }
}



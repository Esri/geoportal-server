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

using System.Runtime.InteropServices;
using ESRI.ArcGIS.esriSystem;
using ESRI.ArcGIS.SystemUI;
using ESRI.ArcGIS.Framework;
using ESRI.ArcGIS.ADF.CATIDs;

using System.IO;
using System.Collections;
using System.Xml;
using System.Xml.Xsl;
using System.Xml.XPath;
using System.Resources;

using ESRI.ArcGIS.ArcMapUI;
using ESRI.ArcGIS.Carto;
using ESRI.ArcGIS.Display;
using ESRI.ArcGIS.GISClient;
using ESRI.ArcGIS.Catalog;
using ESRI.ArcGIS.Geometry;
using ESRI.ArcGIS.DataSourcesRaster;
using Microsoft.Win32;
using System.Reflection;
using ESRI.ArcGIS.ADF;
using System.Globalization;
namespace com.esri.gpt.csw
{
    /// <summary>
    /// Designer class of the dockable window add-in. It contains user interfaces that
    /// make up the dockable window.
    /// </summary>
    public partial class CswClientDockableWindow : UserControl
    {
        #region Private Variable(s)
        private IApplication m_application;

        private CswManager _cswManager = new CswManager();
        private CswProfiles _cswProfiles;
        private CswCatalogs _cswCatalogs;
        private bool _inited = false;
        private string _mapServerUrl = null;
  
        // Search Tab Variables
        private CswSearchRequest _searchRequest;
        private string styledRecordResponse = null;

        // Configure Tab Variables
        private ArrayList _catalogList;
        private ArrayList _profileList;
        private bool _newClicked = false;
        private bool _isCatalogListDirty = false;


        // Help Tab Variables
        private string _helpFilePath = "";
        private bool _isHelpFileLoaded = false;
        private static double NONEXSISTANTNUMBER = 500;
        private bool showAll = false;
        private ResourceManager rm = new ResourceManager("com.esri.gpt.csw.StringResources",
                                    Assembly.GetExecutingAssembly());
        #endregion
        #region Properties(s)
        public ResourceManager ResourceManager
        {
            get
            {
                return rm;
            }
            set
            {
                rm = value;
            }
        }
        #endregion
        #region Constructor(s)
        /// <summary>
        /// 
        /// </summary>
        /// <param name="hook"></param>
        public CswClientDockableWindow(object hook)
        {
            InitializeComponent();
            UpdateUI();
            this.Hook = hook;
            m_application = hook as IApplication;
            try
            {    _helpFilePath = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.Help), "help.htm");           
                InitMyComponents();
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(StringResources.LoadFindServicesFailed + "\r\n" + ex.Message);
            }
        }
        #endregion
        #region Methods
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
                this.mainTabControl.RightToLeftLayout = true;
                this.findTabPage.RightToLeft = RightToLeft.Yes;
                
            }
            else
            {
                this.RightToLeft = RightToLeft.No;
                this.mainTabControl.RightToLeftLayout = false;
                this.findTabPage.RightToLeft = RightToLeft.No;
 
            }
          
        }

        /// <summary>
        /// Init components for "Find Services" dockable window
        /// </summary>
        private void InitMyComponents()
        {
            if (!_inited)
            {
                // version info
                System.Diagnostics.FileVersionInfo fvi = System.Diagnostics.FileVersionInfo.GetVersionInfo(System.Reflection.Assembly.GetExecutingAssembly().Location);
                //    productBuildNoLabel.Text = "(" + StringResources.BuildNo + " " + fvi.ProductVersion + ")";

                // note: To save loading time, help Tab is not loaded.
                _inited = (LoadSearchTab() && LoadConfigTab());
                if (_inited && catalogComboBox.Items.Count > 0) catalogComboBox.SelectedIndex = 0;
            }
        }

        /// <summary>
        /// Host object of the dockable window
        /// </summary>
        private object Hook
        {
            get;
            set;
        }

        /// <summary>
        /// Implementation class of the dockable window add-in. It is responsible for 
        /// creating and disposing the user interface class of the dockable window.
        /// </summary>
        public class AddinImpl : ESRI.ArcGIS.Desktop.AddIns.DockableWindow
        {
            private CswClientDockableWindow m_windowUI;

            public AddinImpl()
            {
            }

            protected override IntPtr OnCreateChild()
            {
                m_windowUI = new CswClientDockableWindow(this.Hook);
                return m_windowUI.Handle;
            }

            protected override void Dispose(bool disposing)
            {
                if (m_windowUI != null)
                    m_windowUI.Dispose(disposing);

                base.Dispose(disposing);
            }

        }
        #endregion
        #region Search Tab Function(s)

        /// <summary>
        /// if there is a change on catalog list, we should refresh 
        /// the csw catalog dropdown control on Search tab as well
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void FindTabPage_Enter(object sender, EventArgs e)
        {
            if (_inited && _isCatalogListDirty)
            {
                catalogComboBox.DataSource = catalogListBox.DataSource;
                catalogComboBox.DisplayMember = catalogListBox.DisplayMember;
                catalogComboBox.ValueMember = catalogListBox.ValueMember;

                _isCatalogListDirty = false;
            }

        }

        /// <summary>
        /// Search CSW catalog with the criteria defined by user
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void FindButton_Click(object sender, EventArgs e)
        {
            StringBuilder sb = new StringBuilder();
            try
            {

                // Todo: add more validation. only minimum validation at this point.
                if (catalogComboBox.SelectedIndex == -1)
                {
                    ShowErrorMessageBox(StringResources.PleaseSelectACswCatalog);
                    return;
                }

                CswCatalog catalog = (CswCatalog)catalogComboBox.SelectedItem;
                if (catalog == null) { throw new NullReferenceException(StringResources.CswCatalogIsNull); }

                // reset GUI for search results
                ResetSearchResultsGUI();
       

                System.Windows.Forms.Cursor.Current = Cursors.WaitCursor;

                if (!catalog.IsConnected())
                {
                    string errMsg = "";
                    try { catalog.Connect(); 
                    }
                    catch (Exception ex) { errMsg = ex.Message; }
                    if (!catalog.IsConnected())
                    {
                         sb.AppendLine("Failed to connect to Catalog");
                        ShowErrorMessageBox (StringResources.ConnectToCatalogFailed + "\r\n" + errMsg);
                        return;
                    }
                }

                // todo: need paging maganism. update SearchCriteria.StartPoistion

                // generate search criteria
                CswSearchCriteria searchCriteria = new CswSearchCriteria();
                searchCriteria.SearchText = searchPhraseTextBox.Text;
                searchCriteria.StartPosition = 1;
                searchCriteria.MaxRecords = (int)maxResultsNumericUpDown.Value;
                searchCriteria.LiveDataAndMapOnly = (liveDataAndMapsOnlyCheckBox.Checked);
                if (useCurrentExtentCheckBox.Checked) 
                {
                    try { searchCriteria.Envelope = CurrentMapViewExtent(); }
                    catch (Exception ex)
                    {
                        String errMsg = StringResources.GetCurrentExtentFailed + "\r\n" +
                                            ex.Message + "\r\n" + "\r\n" +
                                            StringResources.UncheckCurrentExtentAndTryAgain;

                        sb.AppendLine(errMsg);
                        ShowErrorMessageBox(errMsg);
                        return;
                    }
                }
                else { searchCriteria.Envelope = null; }

                // search
                if (_searchRequest == null) { _searchRequest = new CswSearchRequest(); }
                _searchRequest.Catalog = catalog;
                _searchRequest.Criteria = searchCriteria;
                _searchRequest.Search();
                CswSearchResponse response = _searchRequest.GetResponse();
                // show search results
                ArrayList alRecords = CswObjectsToArrayList(response.Records);
                if (alRecords.Count > 0)
                {
                    resultsListBox.BeginUpdate();
                    resultsListBox.DataSource = alRecords;
                    resultsListBox.DisplayMember = "Title";
                    resultsListBox.ValueMember = "ID";
                    resultsListBox.SelectedIndex = 0;
                    resultsListBox.EndUpdate();
                    showAllFootprintToolStripButton.Enabled = catalog.Profile.SupportSpatialBoundary;
                    clearAllFootprinttoolStripButton.Enabled = catalog.Profile.SupportSpatialBoundary;
                    showAll = true;
                }
                else
                {
                    sb.AppendLine(StringResources.NoRecordsFound);
                    ShowErrorMessageBox(StringResources.NoRecordsFound);
                }

                resultsLabel.Text = StringResources.SearchResultsLabelText + " (" + alRecords.Count.ToString() + ")";
            }
            catch (Exception ex)
            {
                sb.AppendLine(ex.Message);
                ShowErrorMessageBox (ex.Message);
            }
            finally
            {
                Utils.logger.writeLog(sb.ToString());
                Cursor.Current = Cursors.Default;
            }
        }

        /// <summary>
        /// Get current view extent (in geographical coordinate system). 
        /// </summary>
        /// <remarks>
        /// If error occurred, exception would be thrown.
        /// </remarks>
        /// <returns>view extent as an envelope object</returns>
        private esri.gpt.csw.Envelope CurrentMapViewExtent()
        {
            esri.gpt.csw.Envelope envCurrentViewExent;

            IMxDocument mxDoc = (IMxDocument)m_application.Document;
            IMap map = mxDoc.FocusMap;
            IActiveView activeView = (IActiveView)map;
            IEnvelope extent = activeView.Extent;
            if (extent == null) return null;

            ISpatialReference CurrentMapSpatialReference = extent.SpatialReference;
            if (CurrentMapSpatialReference == null) throw new Exception(StringResources.SpatialReferenceNotDefined);

            if (CurrentMapSpatialReference is IUnknownCoordinateSystem)
            {
                // unknown cooridnate system
                throw new Exception(StringResources.UnknownCoordinateSystem);
            }
            else if (CurrentMapSpatialReference is IGeographicCoordinateSystem)
            {
                // already in geographical coordinate system, reuse coordinate values
                envCurrentViewExent = new esri.gpt.csw.Envelope(extent.XMin, extent.YMin, extent.XMax, extent.YMax);
            }
            else if (CurrentMapSpatialReference is IProjectedCoordinateSystem)
            {
                // project to geographical coordinate system
                ISpatialReferenceFactory srFactory = new SpatialReferenceEnvironmentClass();
                IGeographicCoordinateSystem gcs = srFactory.CreateGeographicCoordinateSystem((int)esriSRGeoCSType.esriSRGeoCS_NAD1983);
                gcs.SetFalseOriginAndUnits(-180, -90, 1000000);
                extent.Project(gcs);
                envCurrentViewExent = new esri.gpt.csw.Envelope(extent.XMin, extent.YMin, extent.XMax, extent.YMax);
            }
            else
            {
                ShowErrorMessageBox(StringResources.UnsupportedCoordinateSystem);
                envCurrentViewExent = null;
            }

            return envCurrentViewExent;
        }

        /// <summary>
        /// Fire up "search" after user press "Enter" key in the textbox
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void SearchPhraseTextBox_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Return)
            {
                FindButton_Click(sender, e);
            }
        }

        /// <summary>
        /// Refresh GUI when a new catalog is selected 
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void CatalogComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                // reset GUI for search result
                ResetSearchResultsGUI();

                // update GUI for search criteria
                if (catalogComboBox.SelectedIndex > -1)
                {
                    CswCatalog catalog = (CswCatalog)catalogComboBox.SelectedItem;
                    if (catalog == null) { throw new NullReferenceException(StringResources.CswCatalogIsNull); }

                    // update UI
                    useCurrentExtentCheckBox.Enabled = catalog.Profile.SupportSpatialQuery;
                    if (!catalog.Profile.SupportSpatialQuery) useCurrentExtentCheckBox.Checked = false;
                    liveDataAndMapsOnlyCheckBox.Enabled = catalog.Profile.SupportContentTypeQuery;
                    if (!catalog.Profile.SupportContentTypeQuery) liveDataAndMapsOnlyCheckBox.Checked = false;
                }
                else
                {
                    // no catalog was selected
                    useCurrentExtentCheckBox.Checked = false;
                    useCurrentExtentCheckBox.Enabled = false;
                    liveDataAndMapsOnlyCheckBox.Checked = false;
                    liveDataAndMapsOnlyCheckBox.Enabled = false;
                }
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);
            }
        }

        /// <summary>
        /// Reset the maximum number of results to integer value
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void MaxResultsNumericUpDown_Leave(object sender, EventArgs e)
        {
            // reset the Maximum recs to integer value
            maxResultsNumericUpDown.Value = (int)maxResultsNumericUpDown.Value;
        }

        /// <summary>
        /// Update GUI when a new record (or none) is selected in the search results listbox
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void ResultsListBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            Cursor.Current = Cursors.WaitCursor;
            CswCatalog catalog = (CswCatalog)catalogComboBox.SelectedItem;
            if (catalog == null) { throw new NullReferenceException(StringResources.CswCatalogIsNull); }
            // update GUI
            CswRecord record = (CswRecord)resultsListBox.SelectedItem;
            if (record == null)
            {
                // no record selected
                abstractTextBox.Text = "";

                // GUI update for buttons
                addToMapToolStripButton.Enabled = false;
                viewMetadataToolStripButton.Enabled = false;
                downloadMetadataToolStripButton.Enabled = false;
                displayFootprinttoolStripButton.Enabled = false;
                zoomtoFootprintToolStripButton.Enabled = false;
                showAllFootprintToolStripButton.Enabled = catalog.Profile.SupportSpatialBoundary;
                clearAllFootprinttoolStripButton.Enabled = catalog.Profile.SupportSpatialBoundary;
                
            }
            else
            {
                if (record.BoundingBox.Maxx != NONEXSISTANTNUMBER) {
                    displayFootprinttoolStripButton.Enabled = true;
                    zoomtoFootprintToolStripButton.Enabled = true;
                }
                else {
                    displayFootprinttoolStripButton.Enabled = false;
                    zoomtoFootprintToolStripButton.Enabled = false;

                }
                abstractTextBox.Text = record.Abstract;
                addToMapToolStripButton.Enabled = record.IsLiveDataOrMap;
                viewMetadataToolStripButton.Enabled = true;
                downloadMetadataToolStripButton.Enabled = true;
              
                showAllFootprintToolStripButton.Enabled = catalog.Profile.SupportSpatialBoundary;
                clearAllFootprinttoolStripButton.Enabled = catalog.Profile.SupportSpatialBoundary;
                

            }

            Cursor.Current = Cursors.Default;
        }

        /// <summary>
        /// Event handler for "View Metadata" button click event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void ViewMetadataToolStripButton_Click(object sender, EventArgs e)
        {
            ViewMetadata_Clicked();
        }

        /// <summary>
        /// Event handler for "Download Metadata" button click event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void DownloadMetadataToolStripButton_Click(object sender, EventArgs e)
        {
            DownloadMetadata_Clicked();
        }

        /// <summary>
        /// Event handler for "Add To Map" button click event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void AddToMapToolStripButton_Click(object sender, EventArgs e)
        {
            AddToMap_Clicked();
        }

        /// <summary>
        /// Event handler for "Tool Strip" mouse enter event.
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>   
        private void ToolstripButton_MouseEnter(object sender, EventArgs e)
        {
            m_application.Visible = true;
        }

        /// <summary>
        /// Load components for Search Tab
        /// </summary>
        /// <returns>true if successful; false if error occurred. Exception shall be raised if there was any.</returns>
        private bool LoadSearchTab()
        {
            ResetSearchResultsGUI();

            // load CSW Profiles
            try
            {
                _cswProfiles = _cswManager.loadProfile();
                if (_cswProfiles == null) { throw new NullReferenceException(); }
            }
            catch (Exception ex)
            {
                throw new Exception(StringResources.LoadProfilesFailed + "\r\n" + ex.Message, ex);
            }

            // load CSW Catalogs
            try
            {
                _cswCatalogs = _cswManager.loadCatalog();
                if (_cswCatalogs == null) { throw new NullReferenceException(); }

                _catalogList = CswObjectsToArrayList(_cswCatalogs);
                catalogComboBox.BeginUpdate();
                catalogComboBox.DataSource = _catalogList;
                catalogComboBox.DisplayMember = "Name";
                catalogComboBox.ValueMember = "ID";
                catalogComboBox.SelectedIndex = -1;
                catalogComboBox.EndUpdate();
            }
            catch (Exception ex)
            {
                throw new Exception(StringResources.LoadCatalogsFailed + "\r\n" + ex.Message, ex);
            }

            return true;
        }

        /// <summary>
        /// Reset GUI for search results, including listbox, search result label, buttons, abstracts, etc.
        /// </summary>
        private void ResetSearchResultsGUI()
        {
            resultsLabel.Text= StringResources.SearchResultsLabelText;
            resultsLabel.Refresh();
            resultsListBox.DataSource = null;

            abstractTextBox.Text = "";

            // GUI update for buttons
            addToMapToolStripButton.Enabled = false;
            viewMetadataToolStripButton.Enabled = false;
            downloadMetadataToolStripButton.Enabled = false;
            displayFootprinttoolStripButton.Enabled = false;
            zoomtoFootprintToolStripButton.Enabled = false;
            showAllFootprintToolStripButton.Enabled = false;
            clearAllFootprinttoolStripButton.Enabled = false;
            
            
        }

        /// <summary>
        /// Retrieve metadata for the selected record from server, then display it in metadata viwer. 
        /// If failed to retrieve metadata froms erver, display a detailed message including HTTP response from server.
        /// </summary>
        private void ViewMetadata_Clicked()
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;

                CswRecord record = (CswRecord)(resultsListBox.SelectedItem);

                // retrieve metadata
                XmlDocument xmlDoc = RetrieveSelectedMetadataFromCatalog(true);
                if (xmlDoc == null && styledRecordResponse == null) return;
            
                string tmpFilePath = "";
                if (xmlDoc != null && styledRecordResponse == null)
                {
                    // display metadata in XML format
                    tmpFilePath = GenerateTempFilename("Meta", "xml");
                    XmlWriter xmlWriter = new XmlTextWriter(tmpFilePath, Encoding.UTF8);
                    XmlNode binaryNode = xmlDoc.GetElementsByTagName("Binary")[0];
                    if (binaryNode != null)
                    {
                        XmlNode enclosureNode = xmlDoc.GetElementsByTagName("Enclosure")[0];
                        if (enclosureNode != null)
                            binaryNode.RemoveChild(enclosureNode);
                    }

                    String outputStr = xmlDoc.InnerXml.Replace("utf-16", "utf-8");
                    xmlWriter.WriteRaw(outputStr);
                    xmlWriter.Close();
                }
                else if(xmlDoc == null && styledRecordResponse != null
                    && styledRecordResponse.Trim().Length > 0)
                {
                    // display metadata in XML format
                    tmpFilePath = GenerateTempFilename("Meta", "html");
                    FileInfo fileInfo = new FileInfo(tmpFilePath);
                    System.IO.FileStream fileStream = fileInfo.Create();      
                    StreamWriter sr = new StreamWriter(fileStream);
                    sr.Write(styledRecordResponse);
                    sr.Close();
                    fileStream.Close();
                    styledRecordResponse = null;
                }
                
                // pop up a metadata viwer displaying the metadata as HTML
                FormViewMetadata frmViewMetadata = new FormViewMetadata();
                frmViewMetadata.FormClosed += new FormClosedEventHandler(RemoveTempFileAfterMetadataViewerClosed);
                frmViewMetadata.MetadataTitle = record.Title;
                // frmViewMetadata.WindowState = FormWindowState.Maximized;
                frmViewMetadata.Navigate(tmpFilePath);
                frmViewMetadata.Show();
                frmViewMetadata.Activate();

                // note: temp file will be deleted when metadata viwer closes. 
                //       see "RemoveTempFileAfterMetadataViewerClosed()"
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);

            }
            finally
            {
                styledRecordResponse = null;
                Cursor.Current = Cursors.Default;
            }
        }

        /// <summary>
        /// Event handler for Metadata Viwer form closed event. 
        /// To delete temporary metadata file after the viewer is closed.
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void RemoveTempFileAfterMetadataViewerClosed(object sender, FormClosedEventArgs e)
        {
            try
            {
                FormViewMetadata frmViewMetadata = (FormViewMetadata)(sender);
                if (frmViewMetadata != null) { System.IO.File.Delete(frmViewMetadata.MetadataFilePath); }
            }
            catch
            {
                // ignore, if error occurrs when deleting temp file
            }
        }

        /// <summary>
        /// Normalize a string for filename.
        /// </summary>
        /// <param name="filename">File name string to be normalized</param>
        /// <returns>Normalized file name string</returns>
        private string NormalizeFilename(string filename)
        {
            // Get a list of invalid file characters.
            char[] invalidFilenameChars = System.IO.Path.GetInvalidFileNameChars();

            // replace invalid characters with ' ' char
            for (int i = 0; i < invalidFilenameChars.GetLength(0); i++)
            {
                filename = filename.Replace(invalidFilenameChars[i], ' ');
            }
            return filename;
        }


        /// <summary>
        /// Download metadata for the selected record from server. Prop user for saving location. 
        /// </summary>
        private void DownloadMetadata_Clicked()
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;

                // retrieve metadata
                XmlDocument xmlDoc = RetrieveSelectedMetadataFromCatalog(false);
                if (xmlDoc == null) return;

                Cursor.Current = Cursors.Default;

                // save to file
                SaveFileDialog saveFileDialog = new SaveFileDialog();
                saveFileDialog.Filter = StringResources.XmlFilesFilter;
                saveFileDialog.FileName = NormalizeFilename(((CswRecord)resultsListBox.SelectedItem).Title);
                saveFileDialog.DefaultExt = "xml";
                saveFileDialog.Title = StringResources.DownloadMetadataCaption;
                saveFileDialog.CheckFileExists = false;
                saveFileDialog.OverwritePrompt = true;
                if (saveFileDialog.ShowDialog(this) == DialogResult.Cancel) return;
                if (saveFileDialog.FileName.Length > 0)
                {
                    Cursor.Current = Cursors.WaitCursor;
                    try
                    {

                        XmlNode binaryNode = xmlDoc.GetElementsByTagName("Binary")[0];

                        if (binaryNode != null)
                        {
                            XmlNode enclosureNode = xmlDoc.GetElementsByTagName("Enclosure")[0];

                            if (enclosureNode != null)
                                binaryNode.RemoveChild(enclosureNode);
                        }

                        xmlDoc.Save(saveFileDialog.FileName);
                        MessageBox.Show(StringResources.FileSaveSucceed, StringResources.DownloadMetadataCaption, MessageBoxButtons.OK, MessageBoxIcon.Information);
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show(StringResources.FileSaveFailed + "\r\n" + ex.Message, StringResources.DownloadMetadataCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return;
                    }
                    finally
                    {
                        Cursor.Current = Cursors.Default;
                    }
                }
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);
            }
            finally 
            { 
                Cursor.Current = Cursors.Default; 
            }
        }

       

        /// <summary>
        /// Retrieve metadta for the selected record from server. 
        /// Then add the live data or maps that the metadta describes to ArcMap as a layer.
        /// </summary>
        private void AddToMap_Clicked()
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;

                CswRecord record = (CswRecord)resultsListBox.SelectedItem;
                if (record == null) throw new NullReferenceException(StringResources.CswRecordIsNull);

                if (record.MapServerURL == null || record.MapServerURL.Trim().Length == 0)
                {
                    // retrieve metadata
                    RetrieveAddToMapInfoFromCatalog();
                }
                else
                {
                    _mapServerUrl = record.MapServerURL;
                }

                if (_mapServerUrl != null && _mapServerUrl.Trim().Length > 0)
                {

                    String serviceType = record.ServiceType;
                    if (serviceType == null || serviceType.Length == 0)
                    {
                        serviceType = CswProfile.getServiceType(_mapServerUrl);
                    }

                    if (serviceType.Equals("ags"))
                    {
                        if (_mapServerUrl.ToLower().Contains("arcgis/rest"))
                        {
                            _mapServerUrl = _mapServerUrl + "?f=lyr";
                            CswClient client = new CswClient();
                            AddAGSService(client.SubmitHttpRequest("DOWNLOAD", _mapServerUrl, ""));
                        }
                        else
                        {
                            AddAGSService(_mapServerUrl);
                        }
                    }
                    else if (serviceType.Equals("wms"))
                    {
                        MapServiceInfo msinfo = new MapServiceInfo();
                        msinfo.Server = record.MapServerURL;
                        msinfo.Service = record.ServiceName;
                        msinfo.ServiceType = record.ServiceType;
                        CswProfile.ParseServiceInfoFromUrl(msinfo,_mapServerUrl, serviceType);
                        AddLayerWMS(msinfo, true);
                    }
                    else if (serviceType.Equals("aims"))
                    {
                        MapServiceInfo msinfo = new MapServiceInfo();
                        msinfo.Server = record.MapServerURL;
                        msinfo.Service = record.ServiceName;
                        msinfo.ServiceType = record.ServiceType;
                        CswProfile.ParseServiceInfoFromUrl(msinfo,_mapServerUrl, serviceType);
                        AddLayerArcIMS(msinfo);                     
                    }
                    else if (serviceType.Equals("wcs"))
                    {
                        // MapServiceInfo msi = new MapServiceInfo();
                        String[] s = _mapServerUrl.Trim().Split('?');

                        _mapServerUrl = s[0] + "?request=GetCapabilities&service=WCS";
                        CswClient client = new CswClient();
                        String response = client.SubmitHttpRequest("GET", _mapServerUrl, "");

                        XmlDocument xmlDocument = new XmlDocument();
                        try { xmlDocument.LoadXml(response); }
                        catch (XmlException xmlEx)
                        { }

                        XmlNodeList contentMetadata = xmlDocument.GetElementsByTagName("ContentMetadata");

                        if (contentMetadata != null && contentMetadata.Count > 0)
                        {
                            XmlNodeList coverageList = contentMetadata.Item(0).ChildNodes;

                            foreach (XmlNode coverage in coverageList)
                            {

                                XmlNodeList nodes = coverage.ChildNodes;

                                foreach (XmlNode node in nodes)
                                {
                                    if (node.Name.ToLower().Equals("name"))
                                    {
                                        _mapServerUrl = s[0] + "?request=GetCoverage&service=WCS&format=GeoTIFF&coverage=" + node.InnerText;

                                        try
                                        {
                                            String filePath = client.SubmitHttpRequest("DOWNLOAD", _mapServerUrl, "");
                                            AddAGSService(filePath);

                                        }
                                        catch (Exception e)
                                        {
                                            ShowErrorMessageBox("WCS service with no GeoTiff interface");
                                            return;
                                        }
                                    }
                                }

                            }

                        }
                        else
                        {
                          /*  contentMetadata = xmlDocument.GetElementsByTagName("CoverageSummary");

                            if (contentMetadata != null && contentMetadata.Count > 0)
                            {
                                XmlNodeList coverageList = contentMetadata.Item(0).ChildNodes;

                                foreach (XmlNode coverage in coverageList)
                                {

                                    if (coverage.Name.ToLower().Equals("identifier"))
                                    {
                                        _mapServerUrl = s[0] + "?request=GetCoverage&service=WCS&format=GeoTIFF&coverage=" + coverage.InnerText;

                                        try
                                        {
                                            String filePath = client.SubmitHttpRequest("DOWNLOAD", _mapServerUrl, "");
                                            AddAGSService(filePath);

                                        }
                                        catch (Exception e)
                                        {
                                            ShowErrorMessageBox("WCS service with no GeoTiff interface");
                                            return;
                                        }
                                    }
                                }

                            }*/

                        }
                    }
                }
            }           
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }
        }

        /// <summary>
        /// Retrieves the selected metadata (in search result listbox) from CSW catalog. Exception shall be thrown.
        /// </summary>
        /// <remarks>
        /// Called in View Metadata, Download Metadata, and Add to Map
        /// </remarks>
        /// <returns>A XMLDocument object if metadata was retrieved successfully. Null if otherwise.</returns>
        private XmlDocument RetrieveSelectedMetadataFromCatalog(bool bApplyTransform)
        {
            try
            {
                // validate
                if (catalogComboBox.SelectedIndex == -1) { throw new Exception(StringResources.NoCatalogSelected); }
                if (resultsListBox.SelectedIndex == -1) { throw new Exception(StringResources.NoSearchResultSelected); }
                CswCatalog catalog = (CswCatalog)catalogComboBox.SelectedItem;
                if (catalog == null) { throw new NullReferenceException(StringResources.CswCatalogIsNull); }
                CswRecord record = (CswRecord)resultsListBox.SelectedItem;
                if (record == null) throw new NullReferenceException(StringResources.CswRecordIsNull);
             
                // connect to catalog if needed
                if (!catalog.IsConnected())
                {
                    string errMsg = "";
                    try { catalog.Connect(); }
                    catch (Exception ex) { errMsg = ex.Message; }

                    // exit if still not connected
                    if (!catalog.IsConnected())
                    {
                        ShowErrorMessageBox(StringResources.ConnectToCatalogFailed + "\r\n" + errMsg);
                        return null;
                    }
                }

                bool isTransformed = false;

                // retrieve metadata doc by its ID
                if (_searchRequest == null) { _searchRequest = new CswSearchRequest(); }
                _searchRequest.Catalog = catalog;
                try {
                    isTransformed = _searchRequest.GetMetadataByID(record.ID, bApplyTransform);
                    _mapServerUrl = _searchRequest.GetMapServerUrl();
                
                }
                catch (Exception ex)
                {
                    ShowDetailedErrorMessageBox(StringResources.RetrieveMetadataFromCatalogFailed, _searchRequest.GetResponse().ResponseXML);
                    System.Diagnostics.Trace.WriteLine(StringResources.RetrieveMetadataFromCatalogFailed);
                    System.Diagnostics.Trace.WriteLine(ex.Message);
                    System.Diagnostics.Trace.WriteLine(_searchRequest.GetResponse().ResponseXML);
                    return null;
                }

               
                    CswSearchResponse response = _searchRequest.GetResponse();
                    CswRecord recordMetadata = response.Records[0];
                    if (recordMetadata.FullMetadata.Length == 0) { throw new Exception(StringResources.EmptyMetadata); }

                    if (!isTransformed)
                    {
                        XmlDocument xmlDoc = new XmlDocument();
                        try { xmlDoc.LoadXml(recordMetadata.FullMetadata); }
                        catch (XmlException xmlEx)
                        {
                            ShowDetailedErrorMessageBox(StringResources.LoadMetadataFailed + "\r\n" + xmlEx.Message,
                                                        recordMetadata.FullMetadata);
                            return null;
                        }
                        return xmlDoc;
                    }
                    else
                    {
                        styledRecordResponse = recordMetadata.FullMetadata;
                        return null;
                    }

                    
              
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(StringResources.RetrieveMetadataFromCatalogFailed + "\r\n" + ex.Message);
                return null;
            }
        }

        /// <summary>
        /// Retrieves the selected metadata (in search result listbox) from CSW catalog. Exception shall be thrown.
        /// </summary>
        /// <remarks>
        /// Called in View Metadata, Download Metadata, and Add to Map
        /// </remarks>
        /// <returns>A XMLDocument object if metadata was retrieved successfully. Null if otherwise.</returns>
        private void RetrieveAddToMapInfoFromCatalog()
        {
            try
            {
                // validate
                if (catalogComboBox.SelectedIndex == -1) { throw new Exception(StringResources.NoCatalogSelected); }
                if (resultsListBox.SelectedIndex == -1) { throw new Exception(StringResources.NoSearchResultSelected); }
                CswCatalog catalog = (CswCatalog)catalogComboBox.SelectedItem;
                if (catalog == null) { throw new NullReferenceException(StringResources.CswCatalogIsNull); }
                CswRecord record = (CswRecord)resultsListBox.SelectedItem;
                if (record == null) throw new NullReferenceException(StringResources.CswRecordIsNull);

                // connect to catalog if needed
                if (!catalog.IsConnected())
                {
                    string errMsg = "";
                    try { catalog.Connect(); }
                    catch (Exception ex) { errMsg = ex.Message; }

                    // exit if still not connected
                    if (!catalog.IsConnected())
                    {
                        ShowErrorMessageBox(StringResources.ConnectToCatalogFailed + "\r\n" + errMsg);
                    }
                }

                // retrieve metadata doc by its ID
                if (_searchRequest == null) { _searchRequest = new CswSearchRequest(); }
                _searchRequest.Catalog = catalog;
                try
                {
                    _searchRequest.GetAddToMapInfoByID(record.ID);
                    _mapServerUrl = _searchRequest.GetMapServerUrl();

                }
                catch (Exception ex)
                {
                    ShowDetailedErrorMessageBox(StringResources.RetrieveMetadataFromCatalogFailed, _searchRequest.GetResponse().ResponseXML);
                    System.Diagnostics.Trace.WriteLine(StringResources.RetrieveMetadataFromCatalogFailed);
                    System.Diagnostics.Trace.WriteLine(ex.Message);
                    System.Diagnostics.Trace.WriteLine(_searchRequest.GetResponse().ResponseXML);
                 
                }
             
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(StringResources.RetrieveMetadataFromCatalogFailed + "\r\n" + ex.Message);
               
            }
        }

        /// <summary>
        /// Parse out service information (such as service type, server name, service name, etc) from metadta document
        /// </summary>
        /// <param name="xmlDoc">xml metadata doc to be parsed</param>
        /// <param name="msi">A MapServiceInfo object as output</param>
        private MapServiceInfo ParseServiceInfoFromMetadata(XmlDocument xmlDoc)
        {
            // note: some required node may missing if it isn't a metadata for liveData or map
            try
            {
                if (xmlDoc == null) { throw new ArgumentNullException(); }

                MapServiceInfo msi = new MapServiceInfo();

                XmlNamespaceManager xmlNamespaceManager = new XmlNamespaceManager(xmlDoc.NameTable);
                xmlNamespaceManager.AddNamespace("cat", "http://www.esri.com/metadata/csw/");
                xmlNamespaceManager.AddNamespace("csw", "http://www.opengis.net/cat/csw");
                xmlNamespaceManager.AddNamespace("gmd", "http://www.isotc211.org/2005/gmd");

                XmlNode nodeMetadata = xmlDoc.SelectSingleNode("//metadata|//cat:metadata|//csw:metadata|//gmd:MD_Metadata", xmlNamespaceManager);
                if (nodeMetadata == null) { throw new Exception(StringResources.MetadataNodeMissing); }

                // parse out service information
                XmlNode nodeEsri = nodeMetadata.SelectSingleNode("Esri");
                if (nodeEsri == null) throw new Exception(StringResources.EsriNodeMissing);

                // server
                XmlNode node = nodeEsri.SelectSingleNode("Server");
                if (node == null) throw new Exception(StringResources.ServerNodeMissing);
                msi.Server = node.InnerText;

                // service
                node = nodeEsri.SelectSingleNode("Service");
                if (node != null) { msi.Service = node.InnerText; }

                // service type
                node = nodeEsri.SelectSingleNode("ServiceType");
                if (node != null) { msi.ServiceType = node.InnerText; }

                // service param
                node = nodeEsri.SelectSingleNode("ServiceParam");
                if (node != null) { msi.ServiceParam = node.InnerText; }

                // issecured
                node = nodeEsri.SelectSingleNode("issecured");
                if (node != null) { msi.IsSecured = (node.InnerText.Equals("True", StringComparison.OrdinalIgnoreCase)); }

                return msi;
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(StringResources.MapServiceInfoNotAvailable + "\r\n" + ex.Message);
                return null;
            }
        }

        /// <summary>
        /// Add map service layer to map
        /// </summary>
        /// <param name="msi">Map service information</param>
        private void AddMapServiceLayer(esri.gpt.csw.MapServiceInfo msi)
        {
            switch (msi.ServiceType.ToUpper())
            {
                case "WMS": AddLayerWMS(msi, false);
                    break;
                case "WCS": AddLayerWCS(msi, false);
                    break;
                default: AddLayerArcIMS(msi);
                    break;
            }
        }

        /// <summary>
        /// Add WCS map service layer to map
        /// </summary>
        /// <param name="msi">Map service information</param>
        private void AddLayerWCS(esri.gpt.csw.MapServiceInfo msi, Boolean fromServerUrl)
        {
            if (msi == null) { throw new ArgumentNullException(); }

            try
            {
                string _mapServerUrl = AppendQuestionOrAmpersandToUrlString(msi.Server);
                // append serviceParam to server url
                // todo: does msi.ServiceParam have a leading "?" or "&"?
                if (msi.ServiceParam.Length > 0 && !fromServerUrl)
                {
                    _mapServerUrl = _mapServerUrl + msi.ServiceParam;
                    _mapServerUrl = AppendQuestionOrAmpersandToUrlString(_mapServerUrl);
                }

               
                 // MapServiceInfo msi = new MapServiceInfo();
                        String[] s = _mapServerUrl.Trim().Split('?');

                        _mapServerUrl = s[0] + "?request=GetCapabilities&service=WCS";
                        CswClient client = new CswClient();
                        String response = client.SubmitHttpRequest("GET", _mapServerUrl, "");

                         XmlDocument xmlDocument = new XmlDocument();
                         try { xmlDocument.LoadXml(response); }
                         catch (XmlException xmlEx)
                         { }

                         XmlNodeList contentMetadata = xmlDocument.GetElementsByTagName("ContentMetadata");

                         if (contentMetadata != null && contentMetadata.Count > 0)
                         {
                             XmlNodeList coverageList = contentMetadata.Item(0).ChildNodes;

                             foreach (XmlNode coverage in coverageList) {
                                  
                                 XmlNodeList nodes = coverage.ChildNodes;

                                 foreach(XmlNode node in nodes)
                                 {
                                     if (node.Name.ToLower().Equals("name"))
                                     {
                                         _mapServerUrl = s[0] + "?request=GetCoverage&service=WCS&format=GeoTIFF&coverage=" + node.InnerText;

                                         try{
                                            String filePath  = client.SubmitHttpRequest("DOWNLOAD", _mapServerUrl, "");
                                            AddAGSService(filePath);

                                         } catch(Exception e){
                                             ShowErrorMessageBox("WCS service with no GeoTiff interface");
                                             return;
                                         }                                
                                     }
                                 }

                             }

                         }                                            

                    }
            
            catch (Exception ex)
            {
              //  ShowErrorMessageBox(StringResources.AddWcsLayerFailed + "\r\n" + ex.Message);
            }
        }

        /// <summary>
        /// Add WMS map service layer to map
        /// </summary>
        /// <param name="msi">Map service information</param>
        private void AddLayerWMS(esri.gpt.csw.MapServiceInfo msi, Boolean fromServerUrl)
        {
            if (msi == null) { throw new ArgumentNullException(); }

            try
            {
                string url = AppendQuestionOrAmpersandToUrlString(msi.Server);
                // append serviceParam to server url
                // todo: does msi.ServiceParam have a leading "?" or "&"?
                if (msi.ServiceParam.Length > 0 || !fromServerUrl)
                {
                    url = url + msi.ServiceParam;
                    url = AppendQuestionOrAmpersandToUrlString(url);
                }
                IPropertySet propertySet = new PropertySetClass();
                propertySet.SetProperty("URL", url);

                IMxDocument mxDoc = (IMxDocument)m_application.Document;
                IMap map = (IMap)mxDoc.FocusMap;
                IActiveView activeView = (IActiveView)map;
                IWMSGroupLayer wmsGroupLayer = (IWMSGroupLayer)new WMSMapLayerClass();
                IWMSConnectionName wmsConnectionName = new WMSConnectionName();
                wmsConnectionName.ConnectionProperties = propertySet;

                // connect to wms service
                IDataLayer dataLayer;
                bool connected = false;
                try
                {
                    dataLayer = (IDataLayer)wmsGroupLayer;
                    IName connName = (IName)wmsConnectionName;
                    connected = dataLayer.Connect(connName);
                }
                catch (Exception ex) 
                { 
                    ShowErrorMessageBox (StringResources.ConnectToMapServiceFailed + "\r\n" + ex.Message);
                    connected = false;
                }
                if (!connected) return;

                // get service description out of the layer. the service description contains 
                // inforamtion about the wms categories and layers supported by the service
                IWMSServiceDescription wmsServiceDesc = wmsGroupLayer.WMSServiceDescription;
                IWMSLayerDescription wmsLayerDesc;
                ILayer newLayer;
                ILayer layer;
                IWMSLayer newWmsLayer;
                IWMSGroupLayer newWmsGroupLayer;
                for (int i = 0; i < wmsServiceDesc.LayerDescriptionCount; i++)
                {
                    newLayer = null;

                    wmsLayerDesc = wmsServiceDesc.get_LayerDescription(i);
                    if (wmsLayerDesc.LayerDescriptionCount == 0)
                    {
                        // wms layer
                        newWmsLayer = wmsGroupLayer.CreateWMSLayer(wmsLayerDesc);
                        newLayer = (ILayer)newWmsLayer;
                        if (newLayer == null) { throw new Exception(StringResources.CreateWmsLayerFailed); };
                    }
                    else
                    {
                        // wms group layer
                        newWmsGroupLayer = wmsGroupLayer.CreateWMSGroupLayers(wmsLayerDesc);
                        newLayer = (ILayer)newWmsGroupLayer;
                        if (newLayer == null) { throw new Exception(StringResources.CreateWmsGroupLayerFailed); }
                    }

                    // add newly created layer
                 //   if(wmsGroupLayer.get_Layer(i).Name != newLayer.Name)
                        wmsGroupLayer.InsertLayer(newLayer, 0);
                }

                // configure the layer before adding it to the map
                layer = (ILayer)wmsGroupLayer;
                layer.Name = wmsServiceDesc.WMSTitle;
                ExpandLayer(layer, true);
                SetLayerVisibility(layer, true);

                // add to focus map
                map.AddLayer(layer);

                return;
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox (StringResources.AddWmsLayerFailed + "\r\n" + ex.Message);
            }
        }

        /// <summary>
        /// Add ArcIMS map service layer to map
        /// </summary>
        /// <param name="msi">Map service information</param>
        private void AddLayerArcIMS(esri.gpt.csw.MapServiceInfo msi)
        {
            if (msi == null) { throw new ArgumentNullException(); }
            try
            {
                IMxDocument mxDoc = (IMxDocument)m_application.Document;
                IMap map = (IMap)mxDoc.FocusMap;
                IActiveView activeView = (IActiveView)map;

                bool isAlreadyInMap = false;
          
                if (isAlreadyInMap)
                {
                    ShowErrorMessageBox (StringResources.MapServiceLayerAlreadyExistInMap);
                    return;
                }
                else
                {
                    //IIMSConnection imsConnection;
                    IIMSMapLayer imsMapLayer;
                    
                    // create service description based on provided map service info
                    IIMSServiceDescription imsServiceDesc = new IMSServiceNameClass();
                    imsServiceDesc.URL = msi.Server;
                    imsServiceDesc.Name = msi.Service;
                    imsServiceDesc.ServiceType = acServiceType.acMapService;
                    // note: add as image map service
                    // todo: research on the relationship between MapServiceInfo.ServiceType and IIMSServiceDescription
                    #region acServiceType info                
                    #endregion

                    // connect to service
                    try
                    {
                        imsMapLayer = new IMSMapLayerClass();
                        imsMapLayer.ConnectToService(imsServiceDesc);
                    }
                    catch (System.Runtime.InteropServices.COMException ex)
                    {
                        // note: when a site request usr/pwd to connect, user may choose "cancel". Then, imsMapLayer.ConnectToService() 
                        //       would then throw an System.Runtime.InteropServices.COMException. Catch it but no need to display 
                        //       any error message.
                        System.Diagnostics.Trace.WriteLine(ex.Message, "NoConnectionToService");
                        return;
                    }
                    catch (Exception ex)
                    {
                        ShowErrorMessageBox(StringResources.ConnectToMapServiceFailed + "\r\n" + ex.Message);
                        return;
                    }

                    ILayer layer = (ILayer)imsMapLayer;
                    ExpandLayer(layer, true);
                    SetLayerVisibility(layer, true);

                    // Add the layer
                    mxDoc.AddLayer(imsMapLayer);

                }
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(StringResources.AddArcimsLayerFailed + "\r\n" + ex.Message);
            }
        }

        private void  AddAGSService(string fileName){
            try
            {
                IMxDocument mxDoc = (IMxDocument)m_application.Document;
                IMap map = (IMap)mxDoc.FocusMap;
                IActiveView activeView = (IActiveView)map;
                
                bool isAlreadyInMap = false;
            
                if (isAlreadyInMap)
                {
                    ShowErrorMessageBox(StringResources.MapServiceLayerAlreadyExistInMap);
                    return;
                }
                else
                {             
                    if (fileName.ToLower().Contains("http") && !fileName.ToLower().Contains("arcgis/rest"))
                    {
                        if(fileName.EndsWith("MapServer"))
                            fileName = fileName.Remove(fileName.LastIndexOf("MapServer"));

                        String[] s = fileName.ToLower().Split(new String[]{"/services"}, StringSplitOptions.RemoveEmptyEntries);

                        IPropertySet propertySet = new PropertySetClass();
                        propertySet.SetProperty("URL", s[0] + "/services"); // fileName

                       IMapServer mapServer = null;  
  
                        IAGSServerConnectionFactory pAGSServerConFactory =  new AGSServerConnectionFactory();
                        IAGSServerConnection agsCon = pAGSServerConFactory.Open(propertySet,0); 
                        IAGSEnumServerObjectName pAGSSObjs = agsCon.ServerObjectNames;
                        IAGSServerObjectName pAGSSObj = pAGSSObjs.Next();
   
                       while (pAGSSObj != null) {
                        if(pAGSSObj.Type=="MapServer" && pAGSSObj.Name.ToLower() == s[1].TrimStart('/').TrimEnd('/')){               
                            break;
                        }
                        pAGSSObj = pAGSSObjs.Next();
                       }


                        IName pName =  (IName) pAGSSObj;  
                        IAGSServerObject pAGSO = (IAGSServerObject) pName.Open();
                        mapServer = (IMapServer) pAGSO;


                        IPropertySet prop = new PropertySetClass();
                        prop.SetProperty("URL", fileName);
                        prop.SetProperty("Name",pAGSSObj.Name);


                        IMapServerLayer layer = new MapServerLayerClass();
                        layer.ServerConnect(pAGSSObj,mapServer.get_MapName(0));


                        mxDoc.AddLayer((ILayer)layer);

                    }
                    else
                    {
                       
                        IGxFile pGxFile;
                       
                        if (fileName.ToLower().EndsWith(".tif"))
                        {
                            IRasterLayer pGxLayer = (IRasterLayer) new RasterLayer();
                            pGxLayer.CreateFromFilePath(fileName);                          
                            if (pGxLayer.Valid)
                            {
                                map.AddLayer((ILayer) pGxLayer);
                            }
                        }
                        else
                        {
                            IGxLayer pGxLayer = new GxLayer();
                            pGxFile = (GxFile)pGxLayer;
                            pGxFile.Path = fileName;
                        
                            if (pGxLayer.Layer != null)
                            {
                                map.AddLayer(pGxLayer.Layer);
                            }
                        }

                    }

                }
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(StringResources.AddArcGISLayerFailed + "\r\n" + ex.Message);
            }
        }


        /// <summary>
        /// Event handler for "Display Footprint" button click event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>  
        private void displayFootprinttoolStripButton_Click(object sender, EventArgs e)
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                CswRecord record = (CswRecord)(resultsListBox.SelectedItem);
                drawfootprint(record, false, false);
                //add the graphics element to the map
                IMxDocument mxDoc;
                mxDoc = (IMxDocument)m_application.Document;

                IGraphicsContainer graphicsContainer;
                graphicsContainer = (IGraphicsContainer)mxDoc.FocusMap;
                BoundingBox currentExtent = new BoundingBox();
                currentExtent.Maxx = mxDoc.ActiveView.Extent.XMax;
                currentExtent.Minx = mxDoc.ActiveView.Extent.XMin;
                currentExtent.Maxy = mxDoc.ActiveView.Extent.YMax;
                currentExtent.Miny = mxDoc.ActiveView.Extent.YMin;
                BoundingBox newExtent = updatedExtent(currentExtent, record.BoundingBox);

                IPoint point = new ESRI.ArcGIS.Geometry.PointClass();
                point.PutCoords(newExtent.Minx, newExtent.Maxy);

                IPoint point1 = new ESRI.ArcGIS.Geometry.PointClass();
                point1.PutCoords(newExtent.Maxx, newExtent.Maxy);

                IPoint point2 = new ESRI.ArcGIS.Geometry.PointClass();
                point2.PutCoords(newExtent.Maxx, newExtent.Miny);

                IPoint point3 = new ESRI.ArcGIS.Geometry.PointClass();

                point3.PutCoords(newExtent.Minx, newExtent.Miny);

                IPointCollection pointCollection;
                pointCollection = new ESRI.ArcGIS.Geometry.PolygonClass();

                object x = Type.Missing;
                object y = Type.Missing;
                pointCollection.AddPoint(point, ref x, ref y);
                pointCollection.AddPoint(point1, ref x, ref y);
                pointCollection.AddPoint(point2, ref x, ref y);
                pointCollection.AddPoint(point3, ref x, ref y);
                PolygonElementClass element = new PolygonElementClass();
                element.Geometry = (IGeometry)pointCollection;
                graphicsContainer = (IGraphicsContainer)mxDoc.FocusMap;
                mxDoc.ActiveView.Extent = element.Geometry.Envelope;
                mxDoc.ActiveView.Refresh();
            }
            catch (Exception ex)
            {

                ShowErrorMessageBox(ex.Message);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }
        }
        /// <summary>
        /// Event handler for "Show All Footprint" button click event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void showAllFootprint_Click(object sender, EventArgs e)
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                IMxDocument mxDoc;
                mxDoc = (IMxDocument)m_application.Document;

                IGraphicsContainer graphicsContainer;
                graphicsContainer = (IGraphicsContainer)mxDoc.FocusMap;
                BoundingBox currentExtent = new BoundingBox();
                currentExtent.Maxx = mxDoc.ActiveView.Extent.XMax;
                currentExtent.Minx = mxDoc.ActiveView.Extent.XMin;
                currentExtent.Maxy = mxDoc.ActiveView.Extent.YMax;
                currentExtent.Miny = mxDoc.ActiveView.Extent.YMin;
                BoundingBox newExtent = currentExtent;
                if (showAll)
                {
                    showAll = false;
                    System.Windows.Forms.ToolTip toolTipForshowAll = new System.Windows.Forms.ToolTip();
                    toolTipForshowAll.SetToolTip(showAllFootprintToolStripButton, StringResources.hideAllFootprintTooltip);
                    //showAllFootprintToolStripButton = StringResources.hideAllFootprintTooltip;
                    showAllFootprintToolStripButton.Image = StringResources.hideAll;
                    foreach (Object obj in resultsListBox.Items)
                    {
                        currentExtent = newExtent;
                        CswRecord record = (CswRecord)obj;
                        if (record.BoundingBox.Maxx != NONEXSISTANTNUMBER)
                        {
                            drawfootprint(record, true, false);
                            newExtent = updatedExtent(currentExtent, record.BoundingBox);
                        }
                    }
                    IPoint point = new ESRI.ArcGIS.Geometry.PointClass();
                    point.PutCoords(newExtent.Minx, newExtent.Maxy);

                    IPoint point1 = new ESRI.ArcGIS.Geometry.PointClass();
                    point1.PutCoords(newExtent.Maxx, newExtent.Maxy);

                    IPoint point2 = new ESRI.ArcGIS.Geometry.PointClass();
                    point2.PutCoords(newExtent.Maxx, newExtent.Miny);

                    IPoint point3 = new ESRI.ArcGIS.Geometry.PointClass();
                    point3.PutCoords(newExtent.Minx, newExtent.Miny);

                    IPointCollection pointCollection;
                    pointCollection = new ESRI.ArcGIS.Geometry.PolygonClass();

                    object x = Type.Missing;
                    object y = Type.Missing;


                    pointCollection.AddPoint(point, ref x, ref y);
                    pointCollection.AddPoint(point1, ref x, ref y);
                    pointCollection.AddPoint(point2, ref x, ref y);
                    pointCollection.AddPoint(point3, ref x, ref y);


                    PolygonElementClass element = new PolygonElementClass();
                    element.Geometry = (IGeometry)pointCollection;


                    graphicsContainer = (IGraphicsContainer)mxDoc.FocusMap;
                    mxDoc.ActiveView.Extent = element.Geometry.Envelope;
                    mxDoc.ActiveView.Refresh();
                }
                else
                {
                    showAll = true;
                    System.Windows.Forms.ToolTip toolTipForshowAll = new System.Windows.Forms.ToolTip();
                    toolTipForshowAll.SetToolTip(showAllFootprintToolStripButton, StringResources.showAllFootPrintToolTip);
                    showAllFootprintToolStripButton.Image = StringResources.showAll;
                    deleteelements();

                }
                mxDoc.ActiveView.Refresh();
            }
            catch (Exception ex)
            {

                ShowErrorMessageBox(ex.Message);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }



        }

        /// <summary>
        /// Event handler for "Clear All Footprint" button click event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void clearAllFootprinttoolStripButton_Click(object sender, EventArgs e)
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                deleteelements();
                showAll = true;
                System.Windows.Forms.ToolTip toolTipForshowAll = new System.Windows.Forms.ToolTip();
                toolTipForshowAll.SetToolTip(showAllFootprintToolStripButton, StringResources.showAllFootPrintToolTip);
                showAllFootprintToolStripButton.Image = StringResources.showAll;


            }
            catch (Exception ex)
            {

                ShowErrorMessageBox(ex.Message);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }

        }
        /// <summary>
        /// Event handler for link labeled event.
        /// </summary>
        /// <param name="param1">The sender object</param>
        /// <param name="param1">The event arguments</param>
        private void linkLblHelp_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            System.Diagnostics.Process.Start("IExplore", StringResources.helpUrl);
        }

        private void linkLblAbt_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            System.Diagnostics.Process.Start("IExplore", StringResources.aboutUrl);
        }       

        #endregion
        #region Configure Tab Function(s)
        /// <summary>
        /// Highlight current catalog in the list when Configure Tab is activated.
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void ConfigureTabPage_Enter(object sender, EventArgs e)
        {
            if (_inited)
            {
                // in sync with the current csw catalog on Search Tab
                catalogListBox.SelectedItem = catalogComboBox.SelectedItem;
            }
        }

        /// <summary>
        /// Load components for Config Tab
        /// </summary>
        /// <returns>true if successful; false if error occurred. Exception shall be raised if there was any.</returns>
        private bool LoadConfigTab()
        {
            try
            {
                // populate profiles
                _profileList = CswObjectsToArrayList(_cswProfiles);
                catalogProfileComboBox.BeginUpdate();
                catalogProfileComboBox.DataSource = _profileList;
                catalogProfileComboBox.DisplayMember = "Name";
                catalogProfileComboBox.ValueMember = "ID";
                catalogProfileComboBox.SelectedIndex = -1;
                catalogProfileComboBox.EndUpdate();

                AddData();
                UpdateCatalogListLabel();
                return true;
            }
            catch (Exception ex)
            {
                throw new Exception(StringResources.LoadConfigTabFailed  + "\r\n" + ex.Message, ex);
            }
        }

        /// <summary>
        /// Update catalog list label
        /// </summary>
        private void UpdateCatalogListLabel()
        {
            int count = catalogListBox.Items.Count;
            if (count > 0)
            {
                catalogListLabel.Text = StringResources.CatalogListLabelText + " (" + catalogListBox.Items.Count.ToString() + ")";
            }
            else
            {
                catalogListLabel.Text = StringResources.CatalogListLabelText;
            }
        }

        /// <summary>
        /// event handling for catalog listbox selection changed event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void CatalogListBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                if (catalogListBox.SelectedIndex < 0)
                {
                    catalogProfileComboBox.Enabled = true;
                    deleteCatalogButton.Enabled = false;
                    saveCatalogButton.Enabled = false;
                    return;
                }
                else
                {
                    CswCatalog catalog = (CswCatalog)catalogListBox.SelectedItem;
                    catalogProfileComboBox.SelectedItem = catalog.Profile;
                    catalogUrlTextBox.Text = catalog.URL;
                    catalogDisplayNameTextBox.Text = catalog.Name;

                    catalogProfileComboBox.Enabled = true;
                    deleteCatalogButton.Enabled = true;
                    saveCatalogButton.Enabled = false;
                }
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox (ex.Message);
            }
        }

        /// <summary>
        /// delete the selected catalog
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void DeleteCatalogButton_Click(object sender, EventArgs e)
        {
            try
            {
                if (catalogListBox.SelectedItem == null)
                {
                    ShowErrorMessageBox(StringResources.SelectAnItemToDelete);
                }
                else
                {
                    CswCatalog catalog = (CswCatalog)catalogListBox.SelectedItem;
                    if (MessageBox.Show(StringResources.DeleteConfirmation, StringResources.ConfirmDelete, MessageBoxButtons.YesNo) == DialogResult.Yes)
                    {
                        _catalogList.Remove(catalog);
                        catalogListBox.Update();
                        _cswManager.deleteCatalog(catalog);
                        ClearData();
                        AddData();
                    }
                }
                _isCatalogListDirty = true;
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);
            }
            finally
            {
                UpdateCatalogListLabel();
            }
        }

        /// <summary>
        /// Add a new catalog
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void NewCatalogButton_Click(object sender, EventArgs e)
        {
            try
            {
                ClearData();
                
                _newClicked = true;
                deleteCatalogButton.Enabled = false;
                saveCatalogButton.Enabled = false;
                catalogProfileComboBox.SelectedItem = catalogProfileComboBox.Items[0];

                catalogUrlTextBox.Focus();
                catalogListBox.SelectedIndex = -1;
                _isCatalogListDirty = true;
            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);
            }
        }

        /// <summary>
        /// Save/Update a catalog
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void SaveCatalogButton_Click(object sender, EventArgs e)
        {
            try
            {
                if (_newClicked == true)
                {
                    CswProfile profile = catalogProfileComboBox.SelectedItem as CswProfile;

                    string url = "";
                    url = catalogUrlTextBox.Text.Trim();
                    string name = "";
                    name = catalogDisplayNameTextBox.Text.Trim();
                    if (url.Length == 0)
                    {
                        ShowErrorMessageBox(StringResources.UrlIsEmpty);
                    }
                    else
                    {
                        CswCatalog catalog = new CswCatalog(url, name, profile);
                        catalog.resetConnection();
                        _cswManager.addCatalog(catalog);
                        ClearData();
                        AddData();
                        catalogListBox.SelectedIndex = catalogListBox.Items.IndexOf(catalog);
                        _newClicked = false;
                        MessageBox.Show(StringResources.CatalogAddedSuccessfully, StringResources.Success, MessageBoxButtons.OK, MessageBoxIcon.None);
                    }
                }
                else if (catalogListBox.SelectedItem == null)
                {
                    MessageBox.Show(StringResources.SelectAnItemToUpdate);
                }
                else
                {
                    CswCatalog catalog = (CswCatalog)catalogListBox.SelectedItem;
                    int index = catalogListBox.SelectedIndex;
                    CswProfile profile = catalogProfileComboBox.SelectedItem as CswProfile;
                    _cswManager.updateCatalog(catalog, catalogDisplayNameTextBox.Text, catalogUrlTextBox.Text, profile);
                    catalog.resetConnection();
                    ClearData();
                    AddData();
                    catalogListBox.SelectedIndex = index;
                    MessageBox.Show(StringResources.CatalogSavedSuccessfully, StringResources.Success, MessageBoxButtons.OK, MessageBoxIcon.None);
                }
                _isCatalogListDirty = true;

            }
            catch (Exception ex)
            {
                ShowErrorMessageBox(ex.Message);
            }
            finally
            {
                UpdateCatalogListLabel();
            }
        }

        /// <summary>
        /// Display pop up tooltip for catalog URL when mouse hovers the textbox
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void CatalogUrlTextBox_MouseHover(object sender, EventArgs e)
        {
            if (catalogUrlTextBox.Text.Length > 0)
            {
                tooltip.InitialDelay = 1000;
                tooltip.ReshowDelay = 1000;
                tooltip.IsBalloon = false;
                tooltip.ToolTipTitle = "";
                tooltip.SetToolTip(catalogUrlTextBox, catalogUrlTextBox.Text);
            }
        }

        /// <summary>
        /// adding data to the catalog listbox 
        /// </summary>
        private void AddData()
        {
            _catalogList = CswObjectsToArrayList(_cswCatalogs);
            _catalogList.Sort();

            catalogListBox.BeginUpdate();
            catalogListBox.DataSource = _catalogList;
            catalogListBox.DisplayMember = "Name";
            catalogListBox.ValueMember = "ID";
            catalogListBox.SelectedIndex = _cswCatalogs.Count - 1;
            catalogListBox.EndUpdate();
        }

        /// <summary>
        /// Clearing the display and url text boxes
        /// </summary>
        private void ClearData()
        {
            catalogDisplayNameTextBox.Text = "";
            catalogUrlTextBox.Text = "";
        }

        /// <summary>
        /// Function on change of txtDisplayName  text changed event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void CatalogDisplayNameTextBox_TextChanged(object sender, EventArgs e)
        {
            saveCatalogButton.Enabled = (catalogUrlTextBox.Text.Length > 0);
        }

        /// <summary>
        /// Function on change of txtURL  text changed event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void CatalogUrlTextBox_TextChanged(object sender, EventArgs e)
        {
            saveCatalogButton.Enabled = (catalogUrlTextBox.Text.Length > 0);
        }

        /// <summary>
        /// Function on change of combProfile text changed event
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void CatalogProfileComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            saveCatalogButton.Enabled = (catalogUrlTextBox.Text.Length > 0);
        }

        #endregion
   
        #region Tab Key handling Function(s)

        /// <summary>
        /// Intercept "Tab" key and step focus to the next control
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void CswSearchForm_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Tab)
            {
                // todo: enhance "GetNextTabStopControl()"
                Control ctrl = Utils.GetNextTabStopControl(this);
                if (ctrl != null) ctrl.Focus();
            }
        }

        /// <summary>
        /// Step focus to search result listbox when user press "Tab" key on the split container
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void ResultsSplitContainer_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Tab)
            {
                resultsListBox.Focus();
            }
        }

        /// <summary>
        /// Step focus to Abstract when user press "Tab" key on the search result listbox
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void ResultsListBox_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Tab)
            {
                abstractTextBox.Focus();
            }
        }

        #endregion
        #region Utils

        /// <summary>
        /// convert cswobjects to arraylist 
        /// </summary>
        /// <remarks>
        /// will phase out once CswObjects implements IList interface.
        /// </remarks>
        /// <param name="cswObjs">CSWObjects to be converted</param>
        /// <returns>ArrayList that contains a list of CSW objects.</returns>
        private ArrayList CswObjectsToArrayList(CswObjects cswObjs)
        {
            ArrayList alCswObjects = new ArrayList();
            for (int i = 0; i < cswObjs.Count; i++)
            {
                alCswObjects.Add(cswObjs[i]);
            }
            return alCswObjects;
        }

        /// <summary>
        /// Generate temporary filename with specified prefix and surfix. It uses current windows user's temp folder by default.
        /// </summary>
        /// <param name="prefix">Prefix for the temporary file name</param>
        /// <param name="surfix">Surfix for the temporary file name</param>
        /// <returns>Full path to the temporary file</returns>
        private string GenerateTempFilename(string prefix, string surfix)
        {
            // todo: use System.IO.Path.GetRandomFileName() to accomodate prefix;
            //       it will avoid the issue of .tmp file being generated by system
            string tempFilename = System.IO.Path.GetTempFileName();
            try { System.IO.File.Delete(tempFilename); }
            catch { }
            tempFilename = System.IO.Path.ChangeExtension(tempFilename, surfix);

            return tempFilename;
        }

        /// <summary>
        /// Append question mark or ampersand to a url string
        /// </summary>
        /// <param name="urlString">source URL string</param>
        /// <returns>output URL string</returns>
        private string AppendQuestionOrAmpersandToUrlString(string urlString)
        {
            urlString = urlString.Trim();
            string finalChar = urlString.Substring(urlString.Length - 1, 1);    // final char
            if (!finalChar.Equals("?") && !finalChar.Equals("&"))
            {
                if (urlString.LastIndexOf("=") > -1) { urlString = urlString + "&"; }
                else { urlString = urlString + "?"; }
            }
            return urlString;
        }

        /// <summary>
        /// Display an error message dialog with the provided message string, with default caption, button and icon
        /// </summary>
        /// <param name="ErrorMessage">Error message to be displayed</param>
        private void ShowErrorMessageBox(string ErrorMessage)
        {
            MessageBox.Show(ErrorMessage, StringResources.ErrorMessageDialogCaption, MessageBoxButtons.OK, MessageBoxIcon.Error);
        }

        /// <summary>
        /// Display a warning message dialog with the provided message string, with default caption, button and icon
        /// </summary>
        /// <param name="WarningMessage">Warning message to be displayed</param>
        private void ShowWarningMessageBox(string WarningMessage)
        {
            MessageBox.Show(WarningMessage, StringResources.WarningMessageDialogCaption, MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }

        /// <summary>
        /// Display a error messagebox with details.
        /// </summary>
        /// <param name="message">error message</param>
        /// <param name="detailedMessage">details</param>
        private void ShowDetailedErrorMessageBox(string message, string details)
        {
            FormMessageBox frmMessageBox = new FormMessageBox();
            frmMessageBox.Init(message, details, StringResources.ErrorMessageDialogCaption);
            frmMessageBox.ShowDialog(this);
        }

        /// <summary>
        /// Set layer visibility, including its sub layers if applicable.
        /// </summary>
        /// <param name="layer">Layer</param>
        /// <param name="visible">Indicates if the layer is visible</param>
        private void SetLayerVisibility(ILayer layer, bool visible)
        {
            if (layer == null) return;

            layer.Visible = visible;

            if (layer is ICompositeLayer)
            {
                ICompositeLayer compositeLayer = (ICompositeLayer)layer;
                for (int i = 0; i < compositeLayer.Count; i++)
                {
                    SetLayerVisibility(compositeLayer.get_Layer(i), visible);
                }
            }
        }

        /// <summary>
        /// collapse layer or not, including its sub layers if applicable.
        /// </summary>
        /// <param name="layer">Layer</param>
        /// <param name="visible">Indicates if the layer is expanded in the TOC.</param>
        private void ExpandLayer(ILayer layer, bool expanded)
        {
            if (layer == null) return;

            if (layer is ICompositeLayer2)
            {
                ICompositeLayer2 compositeLayer = (ICompositeLayer2)layer;
                compositeLayer.Expanded = true;
                for (int i = 0; i < compositeLayer.Count; i++)
                {
                    ExpandLayer(compositeLayer.get_Layer(i), expanded);
                }
            }
        }

        /// <summary>
        /// draw footprints on the map
        /// </summary>
        /// <param name="record">Record for which the footprint needs to be drwan</param>
        /// <param name="refreshview">Indicates if the view is refreshed or not after the element is drawn</param>
        /// <param name="deleteelements">Indicates if the element can be deleted or not</param>
        private void drawfootprint(CswRecord record, bool refreshview, bool deleteelements) {
 
            //create the triangle outline symbol
            ISimpleLineSymbol lineSymbol = new SimpleLineSymbolClass();
            lineSymbol.Style = esriSimpleLineStyle.esriSLSSolid;
            lineSymbol.Width = 2.0;

            //create the triangle's fill symbol
             ISimpleFillSymbol simpleFillSymbol = new SimpleFillSymbolClass();
            simpleFillSymbol.Outline = (ILineSymbol)lineSymbol;
            simpleFillSymbol.Style = esriSimpleFillStyle.esriSFSDiagonalCross;


            IMxDocument mxDoc;
            mxDoc = (IMxDocument)m_application.Document;

            //the original projection system
            ISpatialReference spatialReference;
            IGeographicCoordinateSystem geographicCoordinateSystem;
            SpatialReferenceEnvironmentClass spatialReferenceEnviorment = new SpatialReferenceEnvironmentClass();
            geographicCoordinateSystem = spatialReferenceEnviorment.CreateGeographicCoordinateSystem((int)esriSRGeoCSType.esriSRGeoCS_WGS1984);
            spatialReference = (ISpatialReference)geographicCoordinateSystem;

            //set the geometry of the element
            IPoint point = new ESRI.ArcGIS.Geometry.PointClass();
            point.SpatialReference = spatialReference;
            point.PutCoords(record.BoundingBox.Minx, record.BoundingBox.Miny);
          
           

            IPoint point1 = new ESRI.ArcGIS.Geometry.PointClass();
            point1.SpatialReference = spatialReference;
            point1.PutCoords(record.BoundingBox.Minx, record.BoundingBox.Maxy);
           

            IPoint point2 = new ESRI.ArcGIS.Geometry.PointClass();
            point2.SpatialReference = spatialReference;
            point2.PutCoords(record.BoundingBox.Maxx, record.BoundingBox.Maxy);
          

            IPoint point3 = new ESRI.ArcGIS.Geometry.PointClass();
         
           point3.SpatialReference = spatialReference;
           point3.PutCoords(record.BoundingBox.Maxx, record.BoundingBox.Miny);
          
            
            IPointCollection pointCollection;
            pointCollection = new ESRI.ArcGIS.Geometry.PolygonClass();
    

            object x = Type.Missing;
            object y = Type.Missing;


            pointCollection.AddPoint(point, ref x, ref y);
            pointCollection.AddPoint(point1, ref x, ref y);
            pointCollection.AddPoint(point2, ref x, ref y);
            pointCollection.AddPoint(point3, ref x, ref y);


            PolygonElementClass element = new PolygonElementClass();
            element.Symbol = simpleFillSymbol;
            element.SpatialReference = spatialReference;
            element.Geometry.SpatialReference = spatialReference;
            element.Geometry = (IGeometry)pointCollection;
            element.Geometry.Project(mxDoc.ActiveView.Extent.SpatialReference);
          

            //add the graphics element to the map                    
            IGraphicsContainer graphicsContainer;
            graphicsContainer = (IGraphicsContainer)mxDoc.FocusMap;
            if (deleteelements) {
                graphicsContainer.DeleteAllElements();
            }
            graphicsContainer.AddElement(element, 0);
            if (refreshview) {
                mxDoc.ActiveView.Extent = element.Geometry.Envelope;
                mxDoc.ActiveView.Refresh();
            }
        }

        private void zoomtoFootprintToolStripButton_Click(object sender, EventArgs e) {
            try {
                Cursor.Current = Cursors.WaitCursor;
                CswRecord record = (CswRecord)(resultsListBox.SelectedItem);
                drawfootprint(record, true, true);

            }
            catch (Exception ex) {
                ShowErrorMessageBox(ex.Message);
            }
            finally {
                Cursor.Current = Cursors.Default;
            }

        }

        private void deleteelements() {
            //add the graphics element to the map
            IMxDocument mxDoc;
            mxDoc = (IMxDocument)m_application.Document;
            IGraphicsContainer graphicsContainer;
            graphicsContainer = (IGraphicsContainer)mxDoc.FocusMap;
            graphicsContainer.DeleteAllElements();
            // mxDoc.ActiveView.Extent = element.Geometry.Envelope;
            mxDoc.ActiveView.Refresh();

        }

        private BoundingBox updatedExtent(BoundingBox currentExtent, BoundingBox footprintExtent) {
            double xmax = currentExtent.Maxx;
            double xmin = currentExtent.Minx;
            double ymin = currentExtent.Miny;
            double ymax = currentExtent.Maxy;

            double bxmax = footprintExtent.Maxx;
            double bxmin = footprintExtent.Minx;
            double bymin = footprintExtent.Miny;
            double bymax = footprintExtent.Maxy;
            double rxmax = 0.0, rxmin = 0.0, rymax = 0.0, rymin = 0.0;
            if (xmax > bxmax) {
                rxmax = xmax;
            }
            else {
                rxmax = bxmax;
            }
            if (xmin < bxmin) {
                rxmin = xmin;
            }
            else {
                rxmin = bxmin;
            }
            if (ymax > bymax) {
                rymax = ymax;
            }
            else {
                rymax = bymax;
            }
            if (ymin < bymin) {
                rymin = ymin;
            }
            else {
                rymin = bymin;
            }
            BoundingBox newExtent = new BoundingBox();
            newExtent.Maxx = rxmax;
            newExtent.Maxy = rymax;
            newExtent.Minx = rxmin;
            newExtent.Miny = rymin;

            return newExtent;
        }


        #endregion                 
    }
 }



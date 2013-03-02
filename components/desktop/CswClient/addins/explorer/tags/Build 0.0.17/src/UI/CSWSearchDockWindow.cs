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
using System.Collections;
using System.Drawing;
using System.IO;
using System.Resources;
using System.Text;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Xsl;
using AGX = ESRI.ArcGISExplorer.Application;
using AGXD = ESRI.ArcGISExplorer.Data;
using AGXG = ESRI.ArcGISExplorer.Geometry;
using AGXM = ESRI.ArcGISExplorer.Mapping;



namespace com.esri.gpt.csw
{
    public partial class CSWSearchDockWindow : ESRI.ArcGISExplorer.Application.DockWindow
    {

        private CswProfiles cswProfiles;
        private CswCatalogs cswCatalogs;
        private ArrayList catalogList;
        private ArrayList profileList;
        private bool newClicked = false;
        private int cswCatalogCount = 0;
        private bool _isCatalogListDirty = false;
        private Hashtable addedLayer = new Hashtable();
        ResourceManager resourceManager = new ResourceManager("com.esri.gpt.csw.Resources.CswResources", typeof(com.esri.gpt.csw.CSWSearchDockWindow).Assembly);

        public bool showAll = false;
        private static double NONEXSISTANTNUMBER = 500;
        private string styledRecordResponse = null;
        private String _mapServerUrl = null;
     //   private bool _isWriteLogs = false;
     //   private string _logFilePath = "";

        #region "Constructor"

        /// <summary>
        /// Constructor 
        /// </summary>
        /// <remarks>
        /// initialize all gui components and load the search and configure catalogs
        /// </remarks>
        public CSWSearchDockWindow()
        {
            InitializeComponent();
            //  productBuildNoLabel.Text = resourceManager.GetString("buildVersion");
            string _helpFilePath = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.Help), "help.htm");
            //   wbViewHelp.Url = new Uri(_helpFilePath);
            // file path
            //  _xsltMetadataToHtmlFullFilePath = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.ConfigurationFiles), "metadata_to_html_full.xsl");
            // _xsltPrepareMetadataFilePath = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.ConfigurationFiles), "xml_prepare.xslt");

           /* String logFilePath = Utils.GetSpecialFolderPath(SpecialFolder.LogFiles);
            if (logFilePath != null && logFilePath.Trim().Length > 0)
            {
                _isWriteLogs = true;
                _logFilePath = logFilePath + "//CswSearchDockWindow.log";
                Utils.setLogFile(_logFilePath);
            }*/

            LoadCswSearchDialog();
        }
        #endregion


        #region "CSWSearch Dialog variables"

        // private string _xsltPrepareMetadataFilePath;
        // private XslCompiledTransform _xsltPrepare;
        // private string _xsltMetadataToHtmlFullFilePath;
        //  private XslCompiledTransform _xsltFull; 
        private bool inited = false;
        private CswManager _cswManager = new CswManager();

        #endregion

        /// <summary>
        /// Event handler for find tab enter event.
        /// </summary>
        private void tabSearch_Enter(object sender, EventArgs e)
        {
            ArrayList alCatalogs = CswObjectsToArrayList(cswCatalogs);
            if ((cswCatalogCount != alCatalogs.Count) || (_isCatalogListDirty))
            {
                cswCatalogCount = alCatalogs.Count;
                alCatalogs.Sort();
                cmbCswCatalog.DataSource = null;
                cmbCswCatalog.DataSource = alCatalogs;
                cmbCswCatalog.DisplayMember = "Name";
                cmbCswCatalog.ValueMember = "URL";
                _isCatalogListDirty = false;
            }
            else
            {
                cmbCswCatalog.Refresh();
            }

        }

        #region "CSWSearch Configure tab"


        /// <summary>
        /// adding data to the catalog listbox 
        /// </summary>
        private void adddata()
        {
            catalogList = CswObjectsToArrayList(cswCatalogs);
            catalogList.Sort();
            lstCatalog.BeginUpdate();
            lstCatalog.DataSource = catalogList;
            lstCatalog.DisplayMember = "Name";
            lstCatalog.ValueMember = "ID";
            lstCatalog.SelectedIndex = cswCatalogs.Count - 1;
            lstCatalog.EndUpdate();
        }


        /// <summary>
        /// Clearing the display and url text boxes
        /// </summary> 
        private void cleardata()
        {
            txtDisplayName.Text = "";
            txtURL.Text = "";
        }





        /// <summary>
        /// Function on change of combProfile text changed event
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void combProfile_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (txtURL.Text.Length > 0)
            {
                btnSave.Enabled = true;
            }
            else
            {
                btnSave.Enabled = false;
            }
        }
        #endregion


        #region "CSWSearch Dialog UI Event Handling Function(s)"

        /// <summary>
        /// Function on change of SearchDialog event
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void CswSearchDialog_Resize(object sender, EventArgs e)
        {
            if (!inited) { inited = LoadCswSearchDialog(); }
        }

        /// <summary>
        /// Function on click of find button
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void btnSearch_Click(object sender, EventArgs e)
        {
            try
            {
                if (cmbCswCatalog.SelectedIndex == -1)
                {
                    MessageBox.Show(resourceManager.GetString("catalogNotSelected"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;

                }
                CswCatalog catalog = (CswCatalog)cmbCswCatalog.SelectedItem;
                if (catalog == null) { throw new NullReferenceException(resourceManager.GetString("catalogNotSpecified")); }
                if (!catalog.IsConnected())
                {
                    string errMsg = "";
                    try { catalog.Connect(); }
                    catch (Exception ex) { errMsg = ex.ToString(); }
                    if (!catalog.IsConnected())
                    {
                        MessageBox.Show(resourceManager.GetString("catalogConnectFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return;
                    }
                }

                // clear up result list
                lstSearchResults.DataSource = null;
                Cursor.Current = Cursors.WaitCursor;
                // genrate search criteria
                CswSearchCriteria searchCriteria = new CswSearchCriteria();
                searchCriteria.SearchText = txtSearchPhrase.Text;
                searchCriteria.StartPosition = 1;
                searchCriteria.MaxRecords = (int)nudNumOfResults.Value; ;
                searchCriteria.LiveDataAndMapOnly = (chkLiveDataAndMapOnly.CheckState == CheckState.Checked);
                searchCriteria.Envelope = null;   // place holder

                CswSearchRequest searchRequest = new CswSearchRequest();
                searchRequest.Catalog = catalog;
                searchRequest.Criteria = searchCriteria;

                searchRequest.Search();
                CswSearchResponse response = searchRequest.GetResponse();
                ArrayList alRecords = CswObjectsToArrayList(response.Records);
                if (alRecords.Count == 0)
                {
                    MessageBox.Show(resourceManager.GetString("noRecordFound"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
                if (alRecords.Count > 0)
                {
                    lstSearchResults.DataSource = alRecords;
                    lstSearchResults.DisplayMember = "Title";
                    lstSearchResults.ValueMember = "ID";
                    showAllFootPrintTSBtn.Enabled = catalog.Profile.SupportSpatialBoundary;
                    clearAllFootPrintTSBtn.Enabled = catalog.Profile.SupportSpatialBoundary;
                    displayFootPrintTSBtn.Enabled = catalog.Profile.SupportSpatialBoundary;
                    zoomToFootPrintTSBtn.Enabled = catalog.Profile.SupportSpatialBoundary;
                    showAll = true;
                }
                lblResult.Text = resourceManager.GetString("resultTxt") + " (" + alRecords.Count + ")";

            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("searchFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }
        }

        /// <summary>
        /// Function on change of search results
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void lstSearchResults_SelectedIndexChanged(object sender, EventArgs e)
        {
            Cursor.Current = Cursors.WaitCursor;
            CswRecord record = (CswRecord)lstSearchResults.SelectedItem;
            if (record == null)
            {
                //display abstract
                txtAbstract.Text = "";

                // GUI update for buttons
                tsbAddToMap.Enabled = false;
                tsbViewMetadata.Enabled = false;
                tsbDownloadMetadata.Enabled = false;
            }
            else
            {
                txtAbstract.Text = record.Abstract;
                tsbAddToMap.Enabled = record.IsLiveDataOrMap;
                tsbViewMetadata.Enabled = true;
                tsbDownloadMetadata.Enabled = true;
            }

            Cursor.Current = Cursors.Default;
        }

        /// <summary>
        /// Function to reset the maximum number of results
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void nudNumOfResults_Leave(object sender, EventArgs e)
        {
            // reset the NumOfResult to integer value
            nudNumOfResults.Value = (int)nudNumOfResults.Value;
        }



        /// <summary>
        /// Function on click of view metadata button or menu
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void tsbViewMetadata_Click(object sender, EventArgs e)
        {
            ViewMetadata_Clicked();
        }

        /// <summary>
        /// Function on click of download metadata button or menu
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void tsbDownloadMetadata_Click(object sender, EventArgs e)
        {
            DownloadMetadata_Clicked();
        }

        /// <summary>
        /// Function on click of add to map  button or menu
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void tsbAddToMap_Click(object sender, EventArgs e)
        {
            AddToMap_Clicked();
        }



        #endregion

        #region "CSWSearch Dialog Function(s)"

        /// <summary>
        /// Constructor loads profile and catalogs
        /// </summary>
        private bool LoadCswSearchDialog()
        {
            // load CSW Profiles
            try
            {

                cswProfiles = _cswManager.loadProfile();

                if (cswProfiles == null) { throw new NullReferenceException(resourceManager.GetString("loadProfileFailed")); }

                // populate profiles
                profileList = CswObjectsToArrayList(cswProfiles);
                combProfile.BeginUpdate();
                combProfile.DataSource = profileList;
                combProfile.DisplayMember = "Name";
                combProfile.ValueMember = "ID";
                combProfile.SelectedIndex = -1;
                combProfile.EndUpdate();
            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("loadProfileFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            // load CSW Catalogs
            try
            {
                cswCatalogs = _cswManager.loadCatalog();

                if (cswCatalogs == null) { throw new NullReferenceException("Failed to load catalogs. _cswCatalogs is null."); }

                ArrayList alCatalogs = CswObjectsToArrayList(cswCatalogs);
                cswCatalogCount = alCatalogs.Count;
                alCatalogs.Sort();
                lblCatalogs.Text = resourceManager.GetString("catalogsTxt") + " (" + alCatalogs.Count + ")";
                cmbCswCatalog.DataSource = alCatalogs;
                cmbCswCatalog.DisplayMember = "Name";
                cmbCswCatalog.ValueMember = "URL";
                //populate lst box for configure tab
                lstCatalog.DataSource = alCatalogs;
                lstCatalog.DisplayMember = "Name";
                lstCatalog.ValueMember = "ID";
                lstCatalog.SelectedIndex = cswCatalogs.Count - 1;
            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("loadCatalogFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            return true;
        }

        /// <summary>
        /// prepare metadata. Metadata xml is transformed to an intermediate format. 
        /// Map service information shall be generated and put in <Esri></Esri> tags.
        /// </summary>
        /// <param name="xmlDoc">Metadata XML document</param>
        /// <returns>Transformed XML document</returns>
        /*  private XmlDocument PrepareMetadata(XmlDocument xmlDoc) {
              try {
                  if (xmlDoc == null) { throw new ArgumentNullException(); }

                  //load the Xsl if necessary
                 /* if (_xsltPrepare == null) {
                      _xsltPrepare = new XslCompiledTransform();
                      try { _xsltPrepare.Load(_xsltPrepareMetadataFilePath); }
                      catch (Exception ex) {
                          ShowErrorMessageBox(resourceManager.GetString("LoadMetadataPreparationStylesheetFailed"));
                          return null;
                      }
                  }*/

        // todo: clean metadata xml. remove namespaces (to be consistant with the behavior on Portal) (?) 

        // transform
        /*    StringWriter strWriter = new StringWriter();
            _xsltPrepare.Transform(new XmlNodeReader(xmlDoc), null, (TextWriter)strWriter);
            strWriter.Close();

            XmlDocument newXmlDoc = new XmlDocument();
            newXmlDoc.LoadXml(strWriter.ToString());

            return newXmlDoc;
        }
        catch (Exception ex) {
            ShowErrorMessageBox(resourceManager.GetString("PrepareMetadataFailed"));
            return null;
        }
    }*/

        /// <summary>
        /// Display an error message dialog with the provided message string, with default caption, button and icon
        /// </summary>
        /// <param name="ErrorMessage">Error message to be displayed</param>
        private void ShowErrorMessageBox(string ErrorMessage)
        {
            MessageBox.Show(ErrorMessage, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }

        /// <summary>
        /// Retrieves the selected metadata (in search result listbox) from CSW catalog. 
        /// It handles including GUI validation and metadata retrieveal. 
        /// </summary>
        /// <remarks>
        /// Reused in View Summary, View Detail, and Download buttons. 
        /// </remarks>
        /// <returns>A XMLDocument object if successfully retrieved metadata. Null if otherwise.</returns>
        private XmlDocument RetrieveSelectedMetadataFromCatalog(bool bApplyTransform)
        {
            if (cmbCswCatalog.SelectedIndex == -1)
            {
                MessageBox.Show(resourceManager.GetString("catalogNotSelected"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return null;
            }

            CswCatalog catalog = (CswCatalog)cmbCswCatalog.SelectedItem;
            if (catalog == null) { throw new NullReferenceException(resourceManager.GetString("catalogNotSpecified")); }

            CswRecord record = (CswRecord)lstSearchResults.SelectedItem;
            if (record == null) throw new NullReferenceException(resourceManager.GetString("searchNotSelected"));

            // connect to catalog if not connected already
            if (!catalog.IsConnected())
            {
                string errMsg = "";
                try { catalog.Connect(); }
                catch (Exception ex) { errMsg = ex.ToString(); }
                if (!catalog.IsConnected())
                {
                    MessageBox.Show(resourceManager.GetString("catalogConnectFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return null;
                }
            }
            bool isTransformed = false;
            // retrieve metadata doc by its ID
            CswSearchRequest searchRequest = new CswSearchRequest();
            searchRequest.Catalog = catalog;
            try
            {
                //MessageBox.Show(record.ID);         
                isTransformed = searchRequest.GetMetadataByID(record.ID, bApplyTransform);
                _mapServerUrl = searchRequest.GetMapServerUrl();
                //MessageBox.Show(record.FullMetadata);
            }
            catch (Exception ex)
            {
                FormMessageBox frmMessageBox = new FormMessageBox();
                frmMessageBox.Init("Failed to retrieve metadata from service: " + ex.Message,
                                    "Response from CSW service:\r\n" + searchRequest.GetResponse().ResponseXML,
                                    "Error");
                frmMessageBox.ShowDialog(this);
                return null;
            }

            CswSearchResponse response = searchRequest.GetResponse();
            CswRecord recordMetadata = response.Records[0];

            if (!isTransformed)
            {
                XmlDocument xmlDoc = new XmlDocument();
                try { xmlDoc.LoadXml(recordMetadata.FullMetadata); }
                catch (XmlException xmlEx)
                {
                    MessageBox.Show(resourceManager.GetString("loadXMLException"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                if (cmbCswCatalog.SelectedIndex == -1)
                {
                    MessageBox.Show(resourceManager.GetString("catalogNotSelected"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }
                if (lstSearchResults.SelectedIndex == -1) { throw new Exception(resourceManager.GetString("searchNotSelected")); }
                CswCatalog catalog = (CswCatalog)cmbCswCatalog.SelectedItem;
                if (catalog == null) { throw new NullReferenceException(resourceManager.GetString("catalogNotSpecified")); }
                CswRecord record = (CswRecord)lstSearchResults.SelectedItem;
                if (record == null) throw new NullReferenceException(resourceManager.GetString("searchNotSelected"));

                // connect to catalog if needed
                if (!catalog.IsConnected())
                {
                    string errMsg = "";
                    try { catalog.Connect(); }
                    catch (Exception ex) { errMsg = ex.Message; }

                    // exit if still not connected
                    if (!catalog.IsConnected())
                    {
                        MessageBox.Show(resourceManager.GetString("catalogConnectFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return;
                    }
                }

                CswSearchRequest searchRequest = new CswSearchRequest();
                searchRequest.Catalog = catalog;
                try
                {
                    searchRequest.GetAddToMapInfoByID(record.ID);
                    _mapServerUrl = searchRequest.GetMapServerUrl();

                }
                catch (Exception ex)
                {
                    FormMessageBox frmMessageBox = new FormMessageBox();
                    frmMessageBox.Init("Failed to retrieve metadata from service: " + ex.Message,
                                        "Response from CSW service:\r\n" + searchRequest.GetResponse().ResponseXML,
                                        "Error");
                    frmMessageBox.ShowDialog(this);
                }

            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("loadXMLException"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);

            }
        }
        #endregion

        #region "Utils"

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
        /// Genearate temporary filename 
        /// </summary>
        /// <remarks>
        /// Generate temporary filename with specified prefix and surfix. 
        /// It uses current windows user's temp folder by default.
        /// </remarks>
        /// <param name="param1">Desription of the First Param</param>
        /// <returns>Description of the return value.</returns>
        private string GenerateTempFilename(string prefix, string surfix)
        {
            //string tempPath = System.IO.Path.GetTempPath();
            //return GenerateTempFilename(prefix, surfix, tempPath, false);

            return System.IO.Path.GetTempFileName();
        }

        /// <summary>
        /// Generates temp file name with prefix and suffix
        /// </summary>
        /// <param name="prefix">file prefix</param>
        /// <param name="surfix">file suffix</param>
        /// <param name="directory">folder</param>
        /// <param name="overwrite">can be overwritten ?</param>
        /// <returns></returns>
        private string GenerateTempFilename(string prefix, string surfix, string directory, Boolean overwrite)
        {
            // todo: need to generate number and check if file already exist
            return (directory + "~" + prefix + "Temp" + "." + surfix);
        }

        private string AppendQuestionOrAmpersandToUrlString(string urlString)
        {
            string finalChar = urlString.Substring(urlString.Length - 1, 1);    // final char
            if (!finalChar.Equals("?") && !finalChar.Equals("&"))
            {
                if (urlString.LastIndexOf("=") > -1) { urlString = urlString + "&"; }
                else { urlString = urlString + "?"; }
            }
            return urlString;
        }

        /// <summary>
        /// Gets application path
        /// </summary>
        /// <returns></returns>
        private string GetApplicationPath()
        {
            return System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location);
        }

        #endregion


        #region "Private Functions"

        /// <summary>
        /// Function to view metadata
        /// </summary>
        private void ViewMetadata_Clicked()
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                XmlDocument xmlDoc = RetrieveSelectedMetadataFromCatalog(true);
                if (xmlDoc == null && styledRecordResponse == null) return;

                // prepare metadata for service info
                /*         xmlDoc = PrepareMetadata(xmlDoc);
                         if (xmlDoc == null) return;

                         // transform metadata using style sheet

                         //load the Xsl if necessary
                         if (_xsltFull == null) {
                             _xsltFull = new XslCompiledTransform();
                             try { _xsltFull.Load(_xsltMetadataToHtmlFullFilePath);
                             //MessageBox.Show(_xsltFull.ToString() + _xsltMetadataToHtmlFullFilePath);
                         }
                             catch { throw new Exception(resourceManager.GetString("loadMetadataException")); }
                         }*/

                /*   string tmpFilePath = GenerateTempFilename("Meta", "html");
                   XmlWriter xmlWriter = new XmlTextWriter(tmpFilePath, Encoding.UTF8);
                   _xsltFull.Transform(new XmlNodeReader(xmlDoc), xmlWriter);
                   xmlWriter.Close(); */

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
                else if (xmlDoc == null && styledRecordResponse != null
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

                // pop up a window displaying the summary HTML
                FormViewMetadata frmViewMetadata = new FormViewMetadata();

                //tmp
                CswRecord record = (CswRecord)(lstSearchResults.SelectedItem);
                frmViewMetadata.MetadataTitle = record.Title;
                //tmp

                frmViewMetadata.Navigate(tmpFilePath);
                frmViewMetadata.Show();
                frmViewMetadata.Activate();

                return;
            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("viewMetadataFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                styledRecordResponse = null;
                Cursor.Current = Cursors.Default;
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
        /// Function to download metadata
        /// </summary>
        private void DownloadMetadata_Clicked()
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                // retrieve metadata
                XmlDocument xmlDoc = RetrieveSelectedMetadataFromCatalog(false);
                if (xmlDoc == null) return;

                // save to file
                SaveFileDialog saveFileDialog = new SaveFileDialog();
                saveFileDialog.Filter = "XML Files|*.xml";
                saveFileDialog.FileName = NormalizeFilename(((CswRecord)lstSearchResults.SelectedItem).Title);
                saveFileDialog.DefaultExt = "xml";
                saveFileDialog.Title = resourceManager.GetString("downloadXMLFile");
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
                        MessageBox.Show(resourceManager.GetString("fileSaved"), "Download Metadata", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show(resourceManager.GetString("fileSavedFailed"), "Download Metadata", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
                MessageBox.Show(resourceManager.GetString("downloadMetadataFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }
        }

        /// <summary>
        /// Function to add to map
        /// </summary>
        private void AddToMap_Clicked()
        {
            try
            {

                /*    if (addedLayer[lstSearchResults.SelectedItem.GetHashCode()] != null) {
                        AGXM.ServiceLayer resultLayer = (AGXM.ServiceLayer)addedLayer[lstSearchResults.SelectedItem.GetHashCode()];
                        try {
                            if (resultLayer.Extent.GetEnvelope() != null) {
                                MessageBox.Show(resourceManager.GetString("layerAdded"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                            }
                        }
                        catch (Exception e) {
                            addedLayer.Remove(lstSearchResults.SelectedItem.GetHashCode());
                        }
                    }*/
                //     if (addedLayer[lstSearchResults.SelectedItem.GetHashCode()] == null) {
                Cursor.Current = Cursors.WaitCursor;
                // retrieve metadata

                CswRecord record = (CswRecord)lstSearchResults.SelectedItem;
                if (record == null) throw new NullReferenceException("CswRecord is null.");

                if (record.MapServerURL == null || record.MapServerURL.Trim().Length == 0)
                {
                    // retrieve metadata
                    RetrieveAddToMapInfoFromCatalog();
                }
                else
                {
                    _mapServerUrl = record.MapServerURL;
                }
                //  if (xmlDoc == null) return;

                // prepare metadata for service info
                //  xmlDoc = PrepareMetadata(xmlDoc);
                //   if (xmlDoc == null) return;

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
                            _mapServerUrl = _mapServerUrl + "?f=nmf";
                            CswClient client = new CswClient();
                            AddAGSService(client.SubmitHttpRequest("DOWNLOAD", _mapServerUrl, ""));
                        }
                        else
                        {
                            AddAGSService(_mapServerUrl);
                        }
                    }
                    else if (serviceType.Equals("wms") || serviceType.Equals("aims"))
                    {
                        try
                        {
                            MapServiceInfo msinfo = new MapServiceInfo();
                            msinfo.Server = record.MapServerURL;
                            msinfo.Service = record.ServiceName;
                            msinfo.ServiceType = record.ServiceType;
                            CswProfile.ParseServiceInfoFromUrl(msinfo, _mapServerUrl, serviceType);
                            addMapServiceLayer(msinfo);

                        }
                        catch (Exception e)
                        {
                            AddAGSService(_mapServerUrl);
                        }

                    }

                }
                else
                {
                    MapServiceInfo msi = new MapServiceInfo();

                    // parse out service information            
                    //   ParseServiceInfoFromMetadata(xmlDoc, ref msi);

                    if (msi.IsMapService())
                    {
                        addMapServiceLayer(msi);
                    }
                    else
                    {
                        MessageBox.Show(resourceManager.GetString("invalidService"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return;
                    }
                }
                //  }

            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("addMapFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }

        }

        /// <summary>
        /// Function to add to map serivce both IMS and WMS
        /// </summary>
        ///<param name="msi">The map service info</param>
        private void addMapServiceLayer(MapServiceInfo msi)
        {
            if (msi.ServiceType.Equals("wms", StringComparison.OrdinalIgnoreCase))
            {
                if (!addLayerWMS2(msi, false))
                    addLayerWMS(msi, false);
                // MessageBox.Show("WMS service not supported in this version.", "Info", MessageBoxButtons.OK, MessageBoxIcon.Information);
            }
            else
            {
                addLayerArcIMS(msi);
                // MessageBox.Show("ArcIMS service not supported in this version.", "Info", MessageBoxButtons.OK, MessageBoxIcon.Information);
            }
        }
        /// <summary>
        /// Adds WMS layer to map
        /// </summary>
        /// <param name="msi">map service information</param>
        /// <param name="fromServerUrl">service url</param>
        /// <returns></returns>
        private bool addLayerWMS2(MapServiceInfo msi, Boolean fromServerUrl)
        {
            bool flag = false;
            if (msi == null) { throw new ArgumentNullException("msi"); }
            string service = msi.Service;
            string url = AppendQuestionOrAmpersandToUrlString(msi.Server);
            // append serviceParam to server url?
            if (msi.ServiceParam.Length > 0 && !fromServerUrl)
            {
                url = url + msi.ServiceParam;
                url = AppendQuestionOrAmpersandToUrlString(url);
            }


            CswClient client = new CswClient();
            string response = client.SubmitHttpRequest("GET", url + "request=GetCapabilities&service=WMS", "");
            XmlDocument xmlDoc = new XmlDocument();
            xmlDoc.LoadXml(response);
            XmlNamespaceManager xmlnsManager = new XmlNamespaceManager(xmlDoc.NameTable);
            xmlnsManager.AddNamespace("wms", "http://www.opengis.net/wms");
            XmlNodeList nl = null;
            if (xmlDoc.SelectSingleNode("//wms:Layer", xmlnsManager) != null)
            {
                nl = xmlDoc.SelectNodes("/wms:WMS_Capabilities/wms:Capability/wms:Layer/wms:Layer/wms:Title", xmlnsManager);

            }

            if (nl != null)
            {
                flag = true;
                for (int i = nl.Count - 1; i >= 0; i--)
                {

                    AGXD.ServiceConnectionProperties conn = new AGXD.ServiceConnectionProperties
                        (AGXD.ServiceType.Wms,
                        new Uri(url),
                        "", nl.Item(i).InnerText);

                    AGXM.ServiceLayer sl = new AGXM.ServiceLayer(conn);

                    bool connected = sl.Connect();

                    if (connected)
                    {
                        addLayer(sl);
                        addedLayer.Add(DateTime.Now.Millisecond, sl);
                    }
                }
            }
            return flag;
        }


        /// <summary>
        /// Parse out service information (such as service type, server name, service name, etc) from metadta document
        /// </summary>
        /// <param name="xmlDoc">xml metadata doc to be parsed</param>
        /// <param name="msi">MapServiceInfo object as output</param>
        private void ParseServiceInfoFromMetadata(XmlDocument xmlDoc, ref MapServiceInfo msi)
        {
            // todo: service info as an object?
            // also, what about service param and isSecured?
            if (xmlDoc == null) { throw new ArgumentNullException("xmlDoc"); }

            msi = new MapServiceInfo();

            XmlNamespaceManager xmlNamespaceManager = new XmlNamespaceManager(xmlDoc.NameTable);
            xmlNamespaceManager.AddNamespace("cat", "http://www.esri.com/metadata/csw/");
            xmlNamespaceManager.AddNamespace("csw", "http://www.opengis.net/cat/csw");
            XmlNode nodeMetadata = xmlDoc.SelectSingleNode("//metadata|//cat:metadata|//csw:metadata", xmlNamespaceManager);
            if (nodeMetadata == null) { throw new Exception("No metadata node was found in the XML"); }

            // parse out service information
            XmlNode nodeEsri = nodeMetadata.SelectSingleNode("Esri");
            if (nodeEsri == null) throw new Exception("<Esri> node missing");

            // server
            XmlNode node = nodeEsri.SelectSingleNode("Server");
            if (node == null) throw new Exception("'//Esri/Server' node missing");
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

            return;
        }

        /// <summary>
        /// Function to add WMS map serivce
        /// </summary>
        ///<param name="msi">The map service info</param>
        ///<param name="fromServerUrl">from server url</param>
        private void addLayerWMS(MapServiceInfo msi, Boolean fromServerUrl)
        {
            if (msi == null) { throw new ArgumentNullException("msi"); }
            string service = msi.Service;
            string url = AppendQuestionOrAmpersandToUrlString(msi.Server);
            // append serviceParam to server url?
            if (msi.ServiceParam.Length > 0 && !fromServerUrl)
            {
                url = url + msi.ServiceParam;
                url = AppendQuestionOrAmpersandToUrlString(url);
            }
            // connect to wms service
            try
            {
                AGXD.ServiceConnectionProperties sc = new AGXD.ServiceConnectionProperties();
                sc.ServiceType = (AGXD.ServiceType)Enum.Parse(typeof(AGXD.ServiceType), "Wms");
                AGXM.ServiceLayer resultLayer = new AGXM.ServiceLayer();
                if (msi.Service != null && msi.Service.Trim().Length > 0)
                    sc.ServiceName = msi.Service;
                else
                    sc.ServiceName = "Unknown Service Name";
                sc.SubServiceName = "";
                sc.Url = new System.Uri(url);
                //     sc.Username = "";
                //    sc.Password = "";           
                resultLayer.Visible = true;
                resultLayer.ServiceConnectionProperties = sc;

                resultLayer.Connect();
                addLayer(resultLayer);
                addedLayer.Add(DateTime.Now, resultLayer);

            }
            catch (Exception ex)
            {
                throw new Exception(resourceManager.GetString("failedWMSService"), ex);
            }

            return;
        }
        /// <summary>
        /// Adds layer to current map display
        /// </summary>
        /// <param name="layer"></param>
        private void addLayer(AGXM.ServiceLayer layer)
        {
            AGXM.MapDisplay md = AGX.Application.ActiveMapDisplay;
            if (!layer.IsConnected)
            {
                layer.Connect();
            }
            md.Map.ChildItems.Add(layer);
        }

        /// <summary>
        /// Function to add ArcIMS map serivce
        /// </summary>
        ///<param name="msi">The map service info</param>
        private void addLayerArcIMS(MapServiceInfo msi)
        {
            if (msi == null) { throw new ArgumentNullException("msi"); }
            //IIMSConnection imsConnection;
            try
            {
                AGXD.ServiceConnectionProperties sc = new AGXD.ServiceConnectionProperties();

                sc.ServiceType = (AGXD.ServiceType)Enum.Parse(typeof(AGXD.ServiceType), "Ims");
                AGXM.ServiceLayer resultLayer = new AGXM.ServiceLayer();
                sc.ServiceName = msi.Service;
                sc.SubServiceName = "";
                sc.Url = new System.Uri(msi.Server);
                //   resultLayer.Username = "";
                //  resultLayer.Password = "";
                //resultLayer.Name = "Layer";
                //   resultLayer.Copyright = "";
                resultLayer.Visible = true;
                resultLayer.ServiceConnectionProperties = sc;
                resultLayer.Connect();
                addLayer(resultLayer);
                addedLayer.Add(DateTime.Now, resultLayer);
            }
            catch (Exception ex)
            {
                throw new Exception(resourceManager.GetString("failedIMSService"), ex);
            }
            return;
        }

        /// <summary>
        /// Event handler for  label place holder
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void labelPlaceHolder_PreviewKeyDown(object sender, PreviewKeyDownEventArgs e)
        {
            if (e.KeyCode == Keys.Tab)
            {
                this.GetNextControl(this.ActiveControl, true).Focus();
            }
        }

        /// <summary>
        /// Event handler for  list search results mouse move.
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void lstSearchResults_MouseMove(object sender, MouseEventArgs e)
        {
            int idx = lstSearchResults.IndexFromPoint(e.Location);
            if ((idx >= 0) && (idx < lstSearchResults.Items.Count))
            {
                CswRecord record = (CswRecord)lstSearchResults.SelectedItem;
                string msg = record.Abstract;
                if (msg.Length > 200) msg = msg.Substring(0, 200);
                toolTip.SetToolTip(lstSearchResults, msg);
            }
        }

        /// <summary>
        /// Event handler for catalog list selected index changed.
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void lstCatalog_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                btnDelete.Enabled = true;
                CswCatalog catalog = (CswCatalog)lstCatalog.SelectedItem;
                combProfile.SelectedItem = catalog.Profile;
                txtURL.Text = catalog.URL;
                txtDisplayName.Text = catalog.Name;
                txtURL.BackColor = Color.White;
                btnSave.Enabled = false;
            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("selectedIndexChanged"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        /// <summary>
        /// Event handler for configure tab delete button.
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void btnDelete_Click(object sender, EventArgs e)
        {
            int index = lstCatalog.SelectedIndex;
            cleardata();
            adddata();
            lstCatalog.SelectedIndex = index;
            try
            {
                if (lstCatalog.SelectedItem == null)
                {
                    MessageBox.Show(resourceManager.GetString("resultNotSelected"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
                else
                {
                    CswCatalog catalog = (CswCatalog)lstCatalog.SelectedItem;
                    if (MessageBox.Show(resourceManager.GetString("deleteConfirm"), "Confirm delete", MessageBoxButtons.YesNo) == DialogResult.Yes)
                    {
                        catalogList.Remove(catalog);
                        lstCatalog.Update();
                        _cswManager.deleteCatalog(catalog);
                        cleardata();
                        adddata();
                        lblCatalogs.Text = resourceManager.GetString("catalogsTxt") + " (" + lstCatalog.Items.Count + ")";


                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("deleteFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }

        }

        /// <summary>
        /// Event handler for configure tab add  button.
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void btnAdd_Click(object sender, EventArgs e)
        {
            try
            {
                cleardata();
                newClicked = true;
                btnDelete.Enabled = false;
                btnSave.Enabled = false;
                combProfile.SelectedItem = combProfile.Items[0];
                txtURL.BackColor = Color.White;
            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("addCatalogFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        /// <summary>
        /// Event handler for configure tab save  button.
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void btnSave_Click(object sender, EventArgs e)
        {
            try
            {
                if (newClicked == true)
                {
                    newClicked = false;
                    CswProfile profile = combProfile.SelectedItem as CswProfile;
                    string url = "";
                    url = txtURL.Text.Trim();
                    string name = "";
                    name = txtDisplayName.Text.Trim();
                    if (url.Length == 0)
                    {
                        MessageBox.Show(resourceManager.GetString("urlEmptyError"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                    else
                    {
                        CswCatalog catalog = new CswCatalog(url, name, profile);
                        _cswManager.addCatalog(catalog);
                        MessageBox.Show(resourceManager.GetString("catalogAdded"), "Success", MessageBoxButtons.OK, MessageBoxIcon.None);
                        cleardata();
                        adddata();
                        catalog.resetConnection();
                        lblCatalogs.Text = resourceManager.GetString("catalogsTxt") + " (" + lstCatalog.Items.Count + ")";
                        _isCatalogListDirty = true;
                    }
                }
                else if (lstCatalog.SelectedItem == null)
                {
                    MessageBox.Show(resourceManager.GetString("resultNotSelected"));
                }
                else
                {
                    CswCatalog catalog = (CswCatalog)lstCatalog.SelectedItem;
                    int index = lstCatalog.SelectedIndex;
                    CswProfile profile = combProfile.SelectedItem as CswProfile;
                    _cswManager.updateCatalog(catalog, txtDisplayName.Text, txtURL.Text, profile);
                    cleardata();
                    adddata();
                    catalog.resetConnection();
                    lstCatalog.SelectedIndex = index;
                    MessageBox.Show(resourceManager.GetString("dataSaved"), "Success", MessageBoxButtons.OK, MessageBoxIcon.None);
                    _isCatalogListDirty = true;
                }

            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("dataSavedFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);

            }
        }


        #endregion

        /// <summary>
        /// Event handler for link labeled event.
        /// </summary>
        /// <param name="sender">The sender object</param>
        /// <param name="e">The event arguments</param>
        private void linkLabel1_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            System.Diagnostics.Process.Start("IExplore", " http://www.esri.com");

        }

        /// <summary>
        /// Event handler for add to map context menu clicked.
        /// </summary>
        /// <param name="param1">The sender object</param>
        /// <param name="param1">The event arguments</param>
        /*  private void addToMapToolStripMenuItem_Click(object sender, EventArgs e) {
              if (lstSearchResults.Items.Count == 0) {
                  MessageBox.Show(resourceManager.GetString("layerNotFound"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                  return;
              }
              AddToMap_Clicked();

          }*/

        /// <summary>
        /// Event handler for remove from map context menu clicked.
        /// </summary>
        /// <param name="param1">The sender object</param>
        /// <param name="param1">The event arguments</param>
        /*   private void removeFromMapToolStripMenuItem_Click(object sender, EventArgs e) {
               RemoveFromMap_Clicked();
           }*/

        /// <summary>
        /// Function to remove layer from map.
        /// </summary>
        private void RemoveFromMap_Clicked()
        {
            if (lstSearchResults.Items.Count == 0)
            {
                MessageBox.Show(resourceManager.GetString("layerNotFoundToRemove"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                AGXM.ServiceLayer resultLayer = (AGXM.ServiceLayer)addedLayer[lstSearchResults.SelectedItem.GetHashCode()];
                if (resultLayer == null)
                {
                    MessageBox.Show(resourceManager.GetString("layerNotAdded"));
                }
                else if (resultLayer.IsConnected)
                {
                    addedLayer.Remove(lstSearchResults.SelectedItem.GetHashCode());
                    // View3D currView = (View3D)taskUI.E2.CurrentView; ////
                    // currView.RemoveLayer(resultLayer); // TODO
                }
                else
                {
                    //nothing
                }
            }
            catch (Exception ex)
            {
                throw new Exception(resourceManager.GetString("layerNotAdded"), ex);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }

        }


        /// <summary>
        /// Event handler for remove from map clicked.
        /// </summary>
        /// <param name="param1">The sender object</param>
        /// <param name="param1">The event arguments</param>
        /*   private void zoomToLayerToolStripMenuItem1_Click(object sender, EventArgs e) {
               ZoomToLayer_Clicked();
           }*/

        /// <summary>
        /// Function to zoom to layer from map.
        /// </summary>
        private void ZoomToLayer_Clicked()
        {
            if (lstSearchResults.Items.Count == 0)
            {
                MessageBox.Show(resourceManager.GetString("layerNotAdded"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }
            try
            {
                AGXM.ServiceLayer resultLayer = (AGXM.ServiceLayer)addedLayer[lstSearchResults.SelectedItem.GetHashCode()];
                if (resultLayer == null)
                {
                    MessageBox.Show(resourceManager.GetString("layerNotfoundToZoom"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
                else if (resultLayer.IsConnected)
                {
                    AGXG.Envelope envelope = new AGXG.Envelope();
                    envelope.XMin = resultLayer.Extent.XMin;
                    envelope.YMin = resultLayer.Extent.YMin;
                    envelope.XMax = resultLayer.Extent.XMax;
                    envelope.YMax = resultLayer.Extent.YMax;
                    //  View3D currView = (View3D)taskUI.E2.CurrentView;
                    //  currView.DoActionOnGeometry(esriE2GeometryAction.Zoom, envelope); TODO

                    AGXM.MapDisplay md = AGX.Application.ActiveMapDisplay;
                    md.ZoomTo(envelope);

                }
                else
                {
                    //nothing
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(resourceManager.GetString("layerZoomFailed"), "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                Cursor.Current = Cursors.Default;
            }

        }
        /// <summary>
        /// Adds ArcGIS service endpoint to map
        /// </summary>
        /// <param name="fileName">service url or layer filename</param>
        private void AddAGSService(string fileName)
        {
            try
            {

                if (fileName.ToLower().Contains("http") && !fileName.ToLower().Contains("arcgis/rest"))
                {
                    if (fileName.EndsWith("MapServer"))
                        fileName = fileName.Remove(fileName.LastIndexOf("MapServer"));

                    if (fileName.EndsWith("GlobeServer"))
                    {
                        ShowErrorMessageBox("AddArcGISLayerFailed" + "\r\n" + "Adding Globe Server service not supported");
                        return;
                    }

                    String[] s = fileName.Split(new String[] { "/services" }, StringSplitOptions.RemoveEmptyEntries);
                    AGXD.ServiceConnectionProperties sc = new AGXD.ServiceConnectionProperties();
                    AGXM.ServiceLayer mapServerLayer = new AGXM.ServiceLayer();
                    sc.Url = new System.Uri(s[0] + "/services");
                    sc.ServiceType = AGXD.ServiceType.MapServer;
                    String[] s1 = null;
                    if (s.Length > 1)
                    {
                        s1 = s[1].Split('?');
                    }
                    else
                        return;

                    String[] s2 = null;
                    if (s1 != null)
                    {
                        s2 = s1[0].ToLower().Split(new String[] { "/mapserver" }, StringSplitOptions.RemoveEmptyEntries);
                    }
                    else
                        return;

                    String[] serviceName = null;

                    if (s2 != null)
                        serviceName = s2[0].Split(new String[] { "/" }, StringSplitOptions.RemoveEmptyEntries);
                    else
                        return;

                    if (serviceName != null)
                        mapServerLayer.Name = serviceName[serviceName.Length - 1];
                    else
                        return;

                    sc.ServiceName = s2[0];
                    mapServerLayer.CachePolicy = AGXM.LayerCachePolicy.RemoveOnApplicationExit; // esriE2LayerCacheType.Session;

                    try
                    {
                        mapServerLayer.Connect();
                    }
                    catch (Exception e) { }

                    if (mapServerLayer.IsConnected)
                    {
                        addLayer(mapServerLayer);
                        addedLayer.Add(DateTime.Now, mapServerLayer);
                    }
                }
                else
                {
                    System.IO.StreamReader fStream = System.IO.File.OpenText(fileName);
                    string xml = fStream.ReadToEnd().Trim();
                    XmlDocument doc = new XmlDocument();
                    doc.LoadXml(xml);

                    XmlNodeList layersNode = doc.GetElementsByTagName("E2Layers");
                    foreach (XmlNode xmlnode in layersNode)
                    {
                        XmlNodeList layerNodes = xmlnode.ChildNodes;
                        foreach (XmlNode node in layerNodes)
                        {
                            if (node.HasChildNodes && node.Attributes.Item(0).Value.Equals("esri:E2ServerLayer"))
                            {
                                AGXD.ServiceConnectionProperties sc = new AGXD.ServiceConnectionProperties();
                                AGXM.ServiceLayer svrLyr = new AGXM.ServiceLayer();
                                foreach (XmlNode childNode in node)
                                {
                                    if (childNode.Name.Equals("DisplayName"))
                                        svrLyr.Name = childNode.InnerText;
                                    else if (childNode.Name.Equals("ServerType"))
                                        sc.ServiceType = (AGXD.ServiceType)Int16.Parse(childNode.InnerText);
                                    else if (childNode.Name.Equals("Url"))
                                        sc.Url = new System.Uri(childNode.InnerText);
                                    else if (childNode.Name.Equals("ServiceName"))
                                        sc.ServiceName = childNode.InnerText;
                                    else if (childNode.Name.Equals("SubserviceName"))
                                        sc.SubServiceName = childNode.InnerText;
                                }
                                svrLyr.ServiceConnectionProperties = sc;
                                svrLyr.Connect();
                                if (svrLyr.IsConnected)
                                {
                                    addLayer(svrLyr);
                                    addedLayer.Add(DateTime.Now, svrLyr);
                                }

                            }
                        }
                    }
                }

            }
            catch (Exception ex)
            {
                ShowErrorMessageBox("AddArcGISLayerFailed" + "\r\n" + ex.Message);
            }
        }

        /// <summary>
        /// Zooms to selected record footprint
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">Event arguments</param>
        private void zoomToFootPrintTSBtn_Click(object sender, EventArgs e)
        {
            CswRecord record = (CswRecord)lstSearchResults.SelectedItem;
            AGXM.MapDisplay md = AGX.Application.ActiveMapDisplay;
            md.ZoomTo(makeFootPrint(record).GetEnvelope());
        }

        /// <summary>
        /// Displays all footprints in map
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">Event arguments</param>
        private void showAllFootPrintTSBtn_Click(object sender, EventArgs e)
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                AGXM.MapDisplay md = AGX.Application.ActiveMapDisplay;

                if (showAll)
                {
                    showAll = false;
                    showAllFootPrintTSBtn.ToolTipText = "Hide All Footprints";
                    showAllFootPrintTSBtn.Image = (Image)this.resourceManager.GetObject("hideAll");

                    foreach (Object obj in lstSearchResults.Items)
                    {
                        CswRecord record = (CswRecord)obj;
                        if (record.BoundingBox.Maxx != NONEXSISTANTNUMBER)
                        {
                            AGXM.Graphic pointGraphic = makeFootPrintGraphic(record);

                            //Create a new Note to hold the graphic
                            AGXM.Note newNote = new AGXM.Note(record.Title, pointGraphic.Geometry, pointGraphic.Symbol);

                            //Add the Result to the map
                            md.Map.ChildItems.Add(newNote);
                        }
                    }
                }
                else
                {

                    // md.Map.ChildItems.Clear();
                    clearFootprints();
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
        /// Clear all footprint displayed on map
        /// </summary>
        private void clearFootprints()
        {

            showAll = true;
            showAllFootPrintTSBtn.ToolTipText = "Show All Footprints";
            showAllFootPrintTSBtn.Image = (Image)this.resourceManager.GetObject("showAll");

            AGXM.MapDisplay md = AGX.Application.ActiveMapDisplay;
            foreach (Object obj in lstSearchResults.Items)
            {
                CswRecord record = (CswRecord)obj;
                if (record.BoundingBox.Maxx != NONEXSISTANTNUMBER)
                {
                    AGXM.MapItemCollection mic = md.Map.ChildItems;
                    IEnumerator e = mic.GetEnumerator();
                    while (e.MoveNext())
                    {
                        AGXM.MapItem mi = (AGXM.MapItem)e.Current;
                        if (mi.Name == record.Title)
                        {
                            md.Map.ChildItems.Remove(mi);
                            break;
                        }

                    }
                }

                /* if (record.BoundingBox.Maxx != NONEXSISTANTNUMBER)
                 {
                     AGXM.Graphic pointGraphic = makeFootPrintGraphic(record);

                     //Create a new Note to hold the graphic
                     AGXM.Note newNote = new AGXM.Note(record.Title, pointGraphic.Geometry, pointGraphic.Symbol);

                     //Add the Result to the map
                     md.Map.ChildItems.Remove(newNote);
                 }*/
            }
        }
        /// <summary>
        /// Handles clears all footprints event
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">Event arguments</param>
        private void clearAllFootPrintTSBtn_Click(object sender, EventArgs e)
        {
            clearFootprints();
        }

        /// <summary>
        /// Handles display footprints event
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">Event arguments</param>
        private void displayFootPrintTSBtn_Click(object sender, EventArgs e)
        {
            CswRecord record = (CswRecord)lstSearchResults.SelectedItem;

            try
            {
                AGXM.Graphic pointGraphic = makeFootPrintGraphic(record);
                //Create a new Note to hold the graphic
                AGXM.Note newNote = new AGXM.Note(record.Title, pointGraphic.Geometry, pointGraphic.Symbol);

                AGXM.MapDisplay md = AGX.Application.ActiveMapDisplay;
                //Add the Result to the map
                md.Map.ChildItems.Add(newNote);
            }
            catch (Exception ex)
            {
                string msg = ex.Message;
            }
        }


        /// <summary>
        /// Makes footprint envelope for a metadata record
        /// </summary>
        /// <param name="record">CswRecord object</param>
        /// <returns>envelope polygon</returns>
        private AGXG.Polygon makeFootPrint(CswRecord record)
        {

            AGXG.Envelope envelope = new AGXG.Envelope();
            try
            {
                envelope.XMax = record.BoundingBox.Maxx;
                envelope.YMax = record.BoundingBox.Maxy;
                envelope.XMin = record.BoundingBox.Minx;
                envelope.YMin = record.BoundingBox.Miny;
            }
            catch (System.ArgumentException e)
            {
                try
                {
                    envelope.XMax = record.BoundingBox.Minx;
                    envelope.YMax = record.BoundingBox.Maxy;
                    envelope.XMin = record.BoundingBox.Maxx;
                    envelope.YMin = record.BoundingBox.Miny;
                }
                catch (System.ArgumentException e1)
                {
                    try
                    {
                        envelope.XMax = record.BoundingBox.Minx;
                        envelope.YMax = record.BoundingBox.Miny;
                        envelope.XMin = record.BoundingBox.Maxx;
                        envelope.YMin = record.BoundingBox.Maxy;
                    }
                    catch (System.ArgumentException e2)
                    {
                        envelope.XMax = record.BoundingBox.Maxx;
                        envelope.YMax = record.BoundingBox.Miny;
                        envelope.XMin = record.BoundingBox.Minx;
                        envelope.YMin = record.BoundingBox.Maxy;
                    }
                }

            }

            AGXG.Point p1 = new AGXG.Point();
            p1.SetCoordinates(record.BoundingBox.Maxx, record.BoundingBox.Maxy);

            AGXG.Point p2 = new AGXG.Point();
            p2.SetCoordinates(record.BoundingBox.Maxx, record.BoundingBox.Miny);

            AGXG.Point p3 = new AGXG.Point();
            p3.SetCoordinates(record.BoundingBox.Minx, record.BoundingBox.Miny);

            AGXG.Point p4 = new AGXG.Point();
            p4.SetCoordinates(record.BoundingBox.Minx, record.BoundingBox.Maxy);

            AGXG.Polygon footPrint = new AGXG.Polygon();
            footPrint.AddPoint(p1);
            footPrint.AddPoint(p2);
            footPrint.AddPoint(p3);
            footPrint.AddPoint(p4);
            footPrint.AddPoint(p1);

            return footPrint;
        }
        /// <summary>
        /// Makes graphic to display footprint
        /// </summary>
        /// <param name="record"></param>
        /// <returns></returns>
        private AGXM.Graphic makeFootPrintGraphic(CswRecord record)
        {

            AGXG.Polygon footPrint = makeFootPrint(record);

            //Turn the Polygon in to a Graphic
            AGXM.Graphic graphic = new AGXM.Graphic(footPrint);

            //Set the Graphic's symbol
            // graphic.Symbol = AGXM.Symbol.Fill.Outline.Yellow;
            graphic.Symbol.OutlineColor = Color.Yellow;
            graphic.Symbol.Color = Color.Transparent;
            // graphic.Symbol.Size = 100;
            return graphic;
        }
        /// <summary>
        /// Handles Help link click event
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">Event arguments</param>
        private void linkLblHelp_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            System.Diagnostics.Process.Start("IExplore", CswResources.helpUrl);
        }
        
        /// <summary>
        /// Handles About link click event
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">Event arguments</param>
        private void linkLblAbt_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            System.Diagnostics.Process.Start("IExplore", CswResources.abtUrl);
        }

        /// <summary>
        /// Handles url text changed event
        /// </summary>
        /// <param name="sender">Event sender</param>
        /// <param name="e">Event arguments</param>
        private void txtURL_TextChanged(object sender, EventArgs e)
        {
            if (txtURL.Text.Length > 0)
            {
                btnSave.Enabled = true;
            }
            else
            {
                btnSave.Enabled = false;
            }
        }
        /// <summary>
        /// Handles name changed event
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void txtDisplayName_TextChanged(object sender, EventArgs e)
        {
            if (txtURL.Text.Length > 0)
            {
                btnSave.Enabled = true;
            }
            else
            {
                btnSave.Enabled = false;
            }
        }
        /// <summary>
        /// Function on click of search phrase
        /// </summary>
        /// <param name="param1">The sender object</param>
        /// <param name="param1">The event arguments</param>
        private void txtSearchPhrase_TextChanged(object sender, EventArgs e)
        {
            btnSearch.Enabled = true;
        }

        /// <summary>
        /// Function to reset the maximum number of results
        /// </summary>
        /// <param name="param1">The sender object</param>
        /// <param name="param1">The event arguments</param>
        private void cmbCswCatalog_SelectedIndexChanged(object sender, EventArgs e)
        {

            lstSearchResults.DataSource = null;
            // reset GUI
            txtAbstract.Text = "";
            tsbAddToMap.Enabled = false;
            tsbViewMetadata.Enabled = false;
            tsbDownloadMetadata.Enabled = false;
            if (cmbCswCatalog.SelectedIndex == -1)
            {
                cmbCswCatalog.SelectedIndex = 0;
            }
            CswCatalog catalog = (CswCatalog)cmbCswCatalog.SelectedItem;
            if (catalog == null)
            {
                throw new NullReferenceException(resourceManager.GetString("catalogNotSpecified"));
            }
            if (catalog.Profile.SupportContentTypeQuery)
            {
                chkLiveDataAndMapOnly.Enabled = true;
            }
            else
            {
                chkLiveDataAndMapOnly.Enabled = false;
            }

        }

        /// <summary>
        /// Set the maximum number of results to integer value
        /// </summary>
        /// <param name="sender">event sender</param>
        /// <param name="e">event args</param>
        private void nudNumOfResults_ValueChanged(object sender, EventArgs e)
        {

            if (nudNumOfResults.Value > 500)
            {
                nudNumOfResults.Value = 500;
            }
        }
    }

}

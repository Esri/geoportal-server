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
    /// Displays metadata to user
    /// </summary>
    public partial class FormViewMetadata : Form
    {
        #region Private variables
        private string _caption="";     
        private string _filePath = "";
        #endregion
        #region Properties
        /// <summary>
        /// MetadataFilePath property
        /// </summary>
        public string MetadataFilePath
        {
            get { return _filePath; }
        }
        #endregion
        #region Constructor
        /// <summary>
        /// Constructor
        /// </summary>
        public FormViewMetadata()
        {
            InitializeComponent();
            UpdateUI();
        }
        #endregion
        #region Methods
        /// <summary>
        /// Updates UI depending on current locale
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
                this.RightToLeftLayout = true;

            }
            else
            {
                this.RightToLeft = RightToLeft.No;
                this.RightToLeftLayout = false;

            }
        }
        /// <summary>
        /// Navigate to given url
        /// param name="urlString" the new url
        /// </summary>
        public void Navigate(string urlString)
        {
            _filePath = urlString;
            Uri tmpUri = new Uri(urlString);
            webBrowserViewer.Navigate(tmpUri);
        }
        /// <summary>
        /// Title of the form
        /// </summary>
        public string MetadataTitle
        {
            set { this.Text = value + " - " + _caption; }
        }
        /// <summary>
        /// Handles view metadata form load event
        /// </summary>
        private void FormViewMetadata_Load(object sender, EventArgs e)
        {
            _caption = this.Text;   // "Metadata Viewer";
        }
        #endregion
    }
}
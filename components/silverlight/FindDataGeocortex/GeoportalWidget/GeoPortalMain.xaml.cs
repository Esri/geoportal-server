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
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Xml.Linq;
using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Geometry;
using System.IO;
using System.Windows.Browser;
using System.Text.RegularExpressions;
using ESRI.ArcGIS.Client.Tasks;
using GeoportalWidget.Common;
using System.Globalization;

namespace GeoportalWidget
{
    public class AddServiceEvent {}

    public class AddServiceEventArgs : EventArgs
    {
        public string ServiceURL { get; set; }
        public eServiceType ServiceType { get; set; }
    }

    public delegate void AddServiceEventHandler(object sender, AddServiceEventArgs e);

    public partial class GeoPortalMain : UserControl
    {
        /// <summary>
        /// Used to create result boxes graphics layer
        /// </summary>
        internal class RssGraphic : Graphic
        {
            public Dictionary<string, object> RssAttributes { get; set; }
            public string StringPolygon { get; set; }
            public string title { get; set; }
            public string Description { get; set; }
            public string ResourceURL { get; set; }
            public string ResourceType { get; set; }
        }

        #region Enums

        enum MapServiceType
        {
            MapService,
            ImageService,
            Rss,
            ImsService,
            WmsService,
            None
        }

        enum ConfigTags
        {
            useProxy,
            gptEndpoint,
            urlHelp
        }

        #endregion

        #region Variables

        private XElement _configDoc;
        private MapServiceManager _mapServiceManager = null;

        private static string _proxyUrl;
        private bool _useProxy;
        private string _urlHelp;
        private string _completeMapServiceUrl = string.Empty;

        private ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer _imslyr;
        private List<GeoPortalItem> _geoPortalList = new List<GeoPortalItem>();
        private List<ConfigTags> _configTagsList;
        private static string _format = "&f=georss";
        private static string _searchText = "&searchText=";
        private static string _liveMaps = "&contentType=liveData";
        private static string _bbox = "&bbox=";
        private static string _overlaps = "&spatialRel=esriSpatialRelOverlaps";
        private static string _within = "&spatialRel=esriSpatialRelWithin";
        private GraphicsLayer _graphicsLyr;
        private GraphicsLayer _infoGraphicsLyr;
        private static string _layerName = "Metadata Extents"; 
        private static string _infoLayerName = "Metadata Details";
        private bool _isFullyWithin;

        private List<Graphic> _lstGraphics = new List<Graphic>();

        private List<QueryResultData> _queryResultData = null;

        public List<QueryResultData> QueryResultData
        {
            get { return _queryResultData; }
            set
            {
                _queryResultData = value;

            }
        }

        private int[] WebMercatorWkids = { 102113, 102100, 3857, 900913 };

        private bool bInitialized = false;

        private Dictionary<string, string> _dctGeoRSSLinkNames = new Dictionary<string, string>();

        public event AddServiceEventHandler AddToMapEvent;

        #endregion
        
        #region Dependency Properties

        public static ESRI.ArcGIS.Client.Map _Map = null;

        public ESRI.ArcGIS.Client.Map Map
        {
            get { return _Map; }
            set
            {
                _Map = value;
            }
        }

        public string _Status
        {
            get
            {
                if (_mapServiceManager != null)
                    return _mapServiceManager._status;

                return "Empty";
            }
        }

        #endregion
        
        #region Constructor and Initialize

        public GeoPortalMain()
        {
            InitializeComponent();
        }

        /// <summary>
        /// Initialize() function must be called when the map has been initialized
        /// - this is called when the control is loaded, however, it will only initialize once 
        /// </summary>
        /// <param name="map"></param>
        public void Initialize(ESRI.ArcGIS.Client.Map map)
        {
            this.Map = map;
            
            if (bInitialized) return;

            _configTagsList = new List<ConfigTags>();
            _configTagsList.Add(ConfigTags.gptEndpoint);
            _configTagsList.Add(ConfigTags.useProxy);

            //Create and Add the Extents Graphic Layer to the Map
            _graphicsLyr = new GraphicsLayer();
            _graphicsLyr.ID = _layerName;
            _graphicsLyr.Renderer = this.glRenderer;
            
            //Create and Add the Layer Information Graphic Layer to the Map
            _infoGraphicsLyr = new GraphicsLayer();
            _infoGraphicsLyr.ID = _infoLayerName;

            WebClient getXmlClient = new WebClient();

            getXmlClient.DownloadStringCompleted += new DownloadStringCompletedEventHandler(getXmlClient_DownloadStringCompleted);
            getXmlClient.DownloadStringAsync(new Uri("geoportalWidgetConfig.xml", UriKind.Relative));

            if (this.Map != null)
                bInitialized = true;
        }

        #endregion

        #region Xml Config Read and Populate Repository List

        /// <summary>
        /// Read the downloaded xml config file
        /// </summary>
        void getXmlClient_DownloadStringCompleted(object sender, DownloadStringCompletedEventArgs e)
        {
            if (e.Error == null)
            {
                try
                {
                    //add the graphics layers (Extents and Layer Information) to the map
                    if (!MapHasGraphicsLayer(_layerName))
                        Map.Layers.Add(_graphicsLyr);
                    if (!MapHasGraphicsLayer(_infoLayerName))
                        Map.Layers.Add(_infoGraphicsLyr);

                    _configDoc = XElement.Parse(e.Result);
                    if (_mapServiceManager != null)
                    {
                        _mapServiceManager = null;
                    }
                    _mapServiceManager = new MapServiceManager(Map);
                    if (ValidConfigFile())
                    {
                        PopulateFromConfigSettings();
                    }
                    else
                    {
                        //TODO DECIDE HOW TO HANDLE DISABLE OR ?
                        //SendErrorMsg("Missing critical information in configuration file.");
                    }

                }
                catch (Exception ex)
                {
                    //TODO DISABLE? MAKE NOT VISIBLE
                    MessageBox.Show("Unable to read configuration file: " + ex.Message, "Error", MessageBoxButton.OK);
                }
            }
            else
            {
                //TODO DISABLE? MAKE NOT VISIBLE
                MessageBox.Show("Unable to retrieve configuration file: " + e.Error.Message, "Error", MessageBoxButton.OK);
            }
        }

        /// <summary>
        /// Check if map has a search results graphics layer
        /// </summary>
        /// <param name="lyrName">layer name</param>
        /// <returns>true or false</returns>
        private bool MapHasGraphicsLayer(string lyrName)
        {
            ESRI.ArcGIS.Client.Layer lyr = Map.Layers[lyrName];
            if (lyr == null)
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        /// <summary>
        /// Ensure all needed tags are present - see ConfigTags enum
        /// </summary>
        /// <returns></returns>
        private bool ValidConfigFile()
        {
            for (int i = 0; i < _configTagsList.Count; i++)
            {
                int count = _configDoc.Descendants(_configTagsList[i].ToString()).Count();
                if (count == 0)
                {
                    return false;
                }
            }
            return true;

        }

        /// <summary>
        /// Populate variables and repository list
        /// </summary>
        private void PopulateFromConfigSettings()
        {
            PopulateProxyVariables();
            PopulateHelpUrl();
            PopulateGeoPortalList();
            PopulateGeoRSSLinkNames();
        }

        /// <summary>
        /// Populate the link for the help file
        /// </summary>
        private void PopulateHelpUrl()
        {

            var helpinfo = from a in _configDoc.Descendants(ConfigTags.urlHelp.ToString()) select a;
            XElement info = helpinfo.First() as XElement;

            _urlHelp = info.Value;
        }

        /// <summary>
        /// Populate the proxy variables
        /// </summary>
        private void PopulateProxyVariables()
        {
            var proxyinfo = from a in _configDoc.Descendants(ConfigTags.useProxy.ToString()) select a;
            var entry = proxyinfo.First();
            XElement b = entry as XElement;
            if (b.Value == "true")
            {
                _useProxy = true;
            }
            else
            {
                _useProxy = false;
            }
            XAttribute url = b.Attribute("proxyUrl");
            if (url.Value.Length > 0)
                _proxyUrl = url.Value;
            _mapServiceManager.ProxyUrl = _proxyUrl;
            _mapServiceManager.UseProxy = _useProxy;
        }

        /// <summary>
        /// Populate the repositories list and set to first
        /// </summary>
        private void PopulateGeoPortalList()
        {
            var geoPortalList = from a in _configDoc.Descendants(ConfigTags.gptEndpoint.ToString()) select a;
            foreach (var item in geoPortalList)
            {
                XAttribute nameAttr = item.Attribute("name");
                this.GeoList.Items.Add(nameAttr.Value);
                GeoPortalItem newItem = new GeoPortalItem();
                newItem.name = nameAttr.Value;
                XAttribute urlAttr = item.Attribute("url");
                newItem.url = urlAttr.Value;
                _geoPortalList.Add(newItem);
            }
            GeoList.SelectedIndex = 0;
        }

        /// <summary>
        /// Populates a dictionary of hyperlink names and aliases from the configuration
        /// The hyperlinks in the GeoRSS (Geoportal results) have whitespaces that are invalid for a xaml binding name
        /// </summary>
        private void PopulateGeoRSSLinkNames()
        {
            //Dictionary of GeoRSS hyperlink names/aliases 
            if (_dctGeoRSSLinkNames == null)
                _dctGeoRSSLinkNames = new Dictionary<string, string>();

            _dctGeoRSSLinkNames.Clear();

            XElement eleGeoRSSLinks = _configDoc.Element(ModuleConstants.cGeoRSSLinks);
            var links = (from elem in eleGeoRSSLinks.Elements(ModuleConstants.cLink)
                        select elem);
            foreach (XElement link in links)
                _dctGeoRSSLinkNames.Add(link.Attribute(ModuleConstants.cPortal).Value, link.Attribute(ModuleConstants.cViewer).Value);
        }

        #endregion

        #region Clearing

        private void ClearButton_Click(object sender, RoutedEventArgs e)
        {
            ClearInfoCallout();
            ClearGraphicExtents();
            ClearResults();
        }

        private void ClearInfoCallout()
        {
            if (_infoGraphicsLyr != null)
                _infoGraphicsLyr.ClearGraphics();
        }

        private void ClearGraphicExtents()
        {
            if (_graphicsLyr != null)
                _graphicsLyr.ClearGraphics();
        }

        private void ClearResults()
        {
            if (_queryResultData != null)
                _queryResultData.Clear();
            QueryDetailsDataGrid.ItemsSource = null;

            SearchText.Text = "";
            TotalFoundText.Text = "";
        }

        #endregion

        #region Searching

        /// <summary>
        /// Enter key is pressed and Fires search based on selected repository and search text - populates grid
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SearchText_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
                GeoportalSearchButton_Click(null, null);
        }

        /// <summary>
        /// Fires search based on selected repository and search text - populates grid
        /// </summary>
        private void GeoportalSearchButton_Click(object sender, RoutedEventArgs e)
        {
            //TODO need to amend for Default Site, ArcGIS Online and determine if need to make changes for xml
            //Clear the items first 
            if (_graphicsLyr != null)
                _graphicsLyr.ClearGraphics();
            if (_infoGraphicsLyr != null)
                _infoGraphicsLyr.ClearGraphics();

            LayoutRootData.Visibility = Visibility.Collapsed;
            if (QueryDetailsDataGrid.ItemsSource != null)
                QueryDetailsDataGrid.ItemsSource = null;

            if (_mapServiceManager != null)
                _mapServiceManager.Close();

            if (_queryResultData == null)
                _queryResultData = new List<QueryResultData>();
            else
                _queryResultData.Clear();

            QueryDetailsDataGrid.ItemsSource = null;
            QueryDetailsDataGrid.ItemsSource = QueryResultData;
            QueryDetailsDataGrid.UpdateLayout();

            //restart
            if (_mapServiceManager == null)
                _mapServiceManager = new MapServiceManager(Map);

            //begin parse
            string lstVal = GeoList.SelectionBoxItem.ToString();

            var match = _geoPortalList.Where(a => a.name == lstVal);
            GeoPortalItem itm = match.First();
            string baseUrl = string.Empty;

            baseUrl = ProcessUrl(itm.url) + _format;
            if (this.LiveDataMaps.IsChecked.Value == true)
            {
                baseUrl = baseUrl + _liveMaps;
            }

            if (SearchText.Text.Length > 0)
            {
                baseUrl = baseUrl + _searchText + HttpUtility.UrlEncode(SearchText.Text);
            }
            bool includeBbox = false;
            _isFullyWithin = false;
            if (IntersectingRadio.IsChecked == true)
            {
                baseUrl += _overlaps;
                includeBbox = true;
            }
            else if (FullyWithinRadio.IsChecked == true)
            {
                baseUrl += _within;
                includeBbox = true;
                _isFullyWithin = true;
            }

            //Get current Map Extent
            Envelope extEnv = Map.Extent;
            
            SpatialReference srGeo = new SpatialReference(4326);
            if (Map.SpatialReference.WKID != srGeo.WKID)
            {
                //Determine if map is Web Mercator
                var ms = WebMercatorWkids.Where(m => m.Equals(Map.SpatialReference.WKID));

                if (ms.Count() > 0)
                {
                    //Convert the map extent from Web Mercator
                    //-- Geoportal search is always expecting WGS84 Geographic (4326)
                    extEnv = WebMercator.ToWGS84Envelope(extEnv);
                }
                else
                {
                    //TODO: if other coord sys, call geometry service
                }
            }            

            System.Diagnostics.Debug.WriteLine("original extent: " + Map.Extent);
            if (includeBbox)
            {
                string bboxString = _bbox + extEnv.XMin.ToString() + "," + extEnv.YMin.ToString() + "," + extEnv.XMax.ToString() + "," + extEnv.YMax.ToString();
                baseUrl = baseUrl + bboxString;
            }

            if (_useProxy)
            {
                baseUrl = _proxyUrl + baseUrl;
            }

            WebClient searchClient = new WebClient();
            searchClient.OpenReadCompleted += new OpenReadCompletedEventHandler(searchClient_OpenReadCompleted);
            string abc = HttpUtility.UrlEncode(baseUrl);
            searchClient.OpenReadAsync(new Uri(baseUrl));
                     
        }

        /// <summary>
        /// Detects error if none calls to parse results
        /// </summary>
        private void searchClient_OpenReadCompleted(object sender, OpenReadCompletedEventArgs e)
        {
            if (e.Error != null)
            {
                MessageBox.Show("Error in reading search results: " + e.Error.Message, "Error", MessageBoxButton.OK);
                return;
            }

            PopulateSearchResultsList(e.Result);
            return;
        }

        /// <summary>
        /// Append the host name and the app name to related urls
        /// </summary>
        private string ProcessUrl(string url)
        {
            string ret = url;
            // we assume the silverlight application is deployed under "widgets/SilverlightExample" directory
            if (!url.StartsWith("http"))
            {
                string source = Application.Current.Host.Source.AbsoluteUri;
                string appPath = source.Substring(0, source.IndexOf("widgets/SilverlightExample/"));
                ret = appPath + url;
            }
            return ret;
        }

        /// <summary>
        /// Parses the xml to create boxes graphics layer and result grid
        /// </summary>
        /// <param name="s">results stream from client open read</param>
        private void PopulateSearchResultsList(Stream s)
        {
            if (_graphicsLyr != null)
            {
                _graphicsLyr.ClearGraphics();
            }

            try
            {
                XDocument doc = XDocument.Load(s);
                XNamespace geo = "http://www.georss.org/georss";
                var a = from b in doc.Descendants("item") select b;
               
                var rssList = from rssGraphic in doc.Descendants("item") select rssGraphic;
                List<RssGraphic> rssGraphics = new List<RssGraphic>();

                foreach (XElement r in rssList)
                {
                    string resURL = string.Empty;
                    string resType = string.Empty;

                    IEnumerable<XElement> de = from el in r.Descendants(geo + "polygon") select el;

                    if (de.Count() > 0)
                    {
                        RssGraphic rg = new RssGraphic();
                        if (r.Element(geo + "polygon") != null)
                           rg.StringPolygon = r.Element(geo + "polygon").Value;
                        
                        //HACK: to get resource URL ------------------------------------------
                        XNamespace gp = "http://www.esri.com/geoportal";
                        var re = from li in r.Descendants(gp + "resourceUrl") select li;

                        if (re.Count() > 0)
                        {
                            if (r.Element(gp + "resourceUrl") != null)
                                rg.ResourceURL = r.Element(gp + "resourceUrl").Value;
                            if (r.Element(gp + "resourceUrl").Attribute("resourceType") != null)
                                rg.ResourceType = r.Element(gp + "resourceUrl").Attribute("resourceType").Value;
                        }
                        //HACK: end ----------------------------------------------------------

                        if (r.Element("title") != null)
                            rg.title = r.Element("title").Value;
                        if (r.Element("description") != null)
                            rg.Description = r.Element("description").Value;

                        rssGraphics.Add(rg);
                    }
                }

                SpatialReference srGeo = new SpatialReference(4326);
                if (Map.SpatialReference.WKID != srGeo.WKID)
                {
                    //if 102100 call webmercator class
                    //find if map is web mercator
                    var ms = WebMercatorWkids.Where(m => m.Equals(Map.SpatialReference.WKID));
                    if (ms.Count() > 0)
                    {
                        AddWebMercatorGraphics(rssGraphics);
                    }
                    else
                    {
                        //TODO: if other call geometry service
                        //if is geo create poly now
                        AddConvertedGraphics(rssGraphics);
                    }
                }
                else
                {
                    Add4326Graphics(rssGraphics);
                }

            }
            catch (Exception ex)
            {
                MessageBox.Show("An error occurred trying to read the search results: " + ex.Message);
            }
        }

        /// <summary>
        /// Create graphics when base is web mercator
        /// </summary>
        /// <param name="rssGraphics"></param>
        private void AddWebMercatorGraphics(List<RssGraphic> rssGraphics)
        {
            int counter = 0;
            foreach (RssGraphic rssGraphic in rssGraphics)
            {
                Graphic graphic = new Graphic();
                //mouse events
                graphic.MouseEnter += graphic_MouseEnter;
                graphic.MouseLeave += graphic_MouseLeave;
                //title
                graphic.Attributes.Add("Title", rssGraphic.title);

                //abstract
                string result = (Regex.Replace(rssGraphic.Description, @"<(.|\n)*?>", string.Empty)).TrimStart(new char[] { '\n' });
                int loc = result.IndexOf("\n");
                string abstactString = result.Substring(0, loc);
                if (abstactString.ToLower() == "null")
                {
                    abstactString = "No description available";
                }
                graphic.Attributes.Add("Abstract", abstactString);

                //Add Layer Resource URL to graphic's attribute for adding layers to map
                //-- this will only appear as a link on callout if both URL and Type have values
                if ((rssGraphic.ResourceURL != null) && (rssGraphic.ResourceType != null))
                {
                    //TODO: constants
                    graphic.Attributes.Add("Add", rssGraphic.ResourceURL);
                    graphic.Attributes.Add("AddType", rssGraphic.ResourceType);
                    graphic.Attributes.Add("AddVisible", true);
                }

                //Get related links for the graphic from the Description
                List<GeoLinks> links = ParseDescription(rssGraphic.Description);

                //Add Links into graphic attributes
                foreach (GeoLinks link in links)
                {
                    graphic.Attributes.Add(link.Title, link.Url);
                    graphic.Attributes.Add(link.Title + "Visible", true);
                }

                //Construct the Polygon Geometry of Graphic
                string[] sPoints = rssGraphic.StringPolygon.Split(' ');

                ESRI.ArcGIS.Client.Geometry.PointCollection pColl = new ESRI.ArcGIS.Client.Geometry.PointCollection();
                for (int i = 0; i < sPoints.Length; i++)
                {
                    MapPoint mp = new MapPoint();

                    double x = WebMercator.ToWebMercatorX(Convert.ToDouble(sPoints[i + 1], CultureInfo.InvariantCulture));
                    double y = WebMercator.ToWebMercatorY(Convert.ToDouble(sPoints[i], CultureInfo.InvariantCulture));
                    if (Double.IsNegativeInfinity(y))
                        y = -20000000;
                    if (Double.IsPositiveInfinity(y))
                        y = 20000000;

                    if (!IsExtremeNumber(x) && !IsExtremeNumber(y))
                    {
                        mp.X = x;
                        mp.Y = y;

                        pColl.Add(mp);
                    }
                    i++;
                }
                if (pColl.Count > 0)
                {
                    ESRI.ArcGIS.Client.Geometry.Polygon poly = new ESRI.ArcGIS.Client.Geometry.Polygon();
                    poly.Rings.Add(pColl);
                    
                    //Assign polygon to graphic's geometry
                    graphic.Geometry = poly;
                    System.Diagnostics.Debug.WriteLine("poly: xmax: " + poly.Extent.XMax + " xmin: " + poly.Extent.XMin + " ymax: " + poly.Extent.YMax + " ymin: " + poly.Extent.YMin);

                    //Get the Service URL and Type
                    string sResURL = string.Empty;
                    string sResType = string.Empty;
                    if (graphic != null)
                    {
                        object oResURL;
                        if (graphic.Attributes.TryGetValue("Add", out oResURL))
                        {
                            if (oResURL != null)
                                sResURL = oResURL.ToString();
                        }
                        object oResType;
                        if (graphic.Attributes.TryGetValue("AddType", out oResType))
                        {
                            if (oResType != null)
                                sResType = oResType.ToString();
                        }                     
                    }

                    _queryResultData.Add(new QueryResultData()
                    {
                        Title = rssGraphic.title,
                        Description = rssGraphic.Description,
                        IsEnable = false, 
                        graphic = graphic,
                        ServiceLink = sResURL,
                        ServiceType = sResType,
                        ID = rssGraphic.title
                    });
                }
                counter++;
            }
            UpdateUIAfterSearch(counter);
        }

        private bool IsExtremeNumber(double num)
        {

            if (Double.IsNaN(num) || Double.IsNegativeInfinity(num) || Double.IsPositiveInfinity(num))
            {
                return true;
            }
            return false;
        }

        /// <summary>
        /// Convert graphics from current Map coordinate system 
        /// </summary>
        /// <param name="rssGraphics"></param>
        private void AddConvertedGraphics(List<RssGraphic> rssGraphics)
        {
            int counter = 0;
            foreach (RssGraphic rssGraphic in rssGraphics)
            {
                Graphic graphic = new Graphic();
                //mouse events
                graphic.MouseEnter += graphic_MouseEnter;
                graphic.MouseLeave += graphic_MouseLeave;
                //title
                graphic.Attributes.Add("Title", rssGraphic.title);

                //abstract
                string result = (Regex.Replace(rssGraphic.Description, @"<(.|\n)*?>", string.Empty)).TrimStart(new char[] { '\n' });
                int loc = result.IndexOf("\n");
                string abstactString = result.Substring(0, loc);
                if (abstactString.ToLower() == "null")
                {
                    abstactString = "No description available";
                }
                graphic.Attributes.Add("Abstract", abstactString);

                //magic for links happens here
                List<GeoLinks> links = ParseDescription(rssGraphic.Description);

                //links
                List<string> currentLinks = new List<string>();
                foreach (GeoLinks link in links)
                {

                    graphic.Attributes.Add(link.Title, link.Url);
                    graphic.Attributes.Add(link.Title + "Visible", true);

                    currentLinks.Add(link.Title);
                    if (link.AddToMapInfo != null)
                    {

                        graphic.Attributes.Add("Add", link.AddToMapInfo);
                        graphic.Attributes.Add("AddVisible", true);
                        currentLinks.Add("Add");
                    }

                }

                //geometry
                string[] sPoints = rssGraphic.StringPolygon.Split(' ');

                ESRI.ArcGIS.Client.Geometry.PointCollection pColl = new ESRI.ArcGIS.Client.Geometry.PointCollection();
                for (int i = 0; i < sPoints.Length; i++)
                {

                    pColl.Add(new MapPoint(Convert.ToDouble(sPoints[i + 1], CultureInfo.InvariantCulture), Convert.ToDouble(sPoints[i], CultureInfo.InvariantCulture)));
                    i++;
                }

                ESRI.ArcGIS.Client.Geometry.Polygon poly = new ESRI.ArcGIS.Client.Geometry.Polygon();
                poly.Rings.Add(pColl);
                poly.SpatialReference = new SpatialReference(4326);
                graphic.Geometry = poly;

                _lstGraphics.Add(graphic);

                counter++;
            }
            UpdateUIAfterSearch(counter);

            ProjectAndDisplayGraphics(_lstGraphics);
        }

        private void ProjectAndDisplayGraphics(List<Graphic> lstGraphics)
        {
            GeometryService geometryService;
            geometryService = new GeometryService("http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer");
            geometryService.ProjectCompleted += geometryService_ProjectCompleted;
            geometryService.Failed += geometryService_Failed;

            geometryService.ProjectAsync(lstGraphics, Map.SpatialReference);
        }

        private void geometryService_ProjectCompleted(object sender, GraphicsEventArgs e)
        {
            IList<Graphic> lstProjected = e.Results;

            foreach (Graphic graphic in lstProjected)
            {
                System.Diagnostics.Debug.WriteLine("Graphic extent: " + graphic.Geometry.Extent);
                _graphicsLyr.Graphics.Add(graphic);

            }
        }

        private void geometryService_Failed(object sender, TaskFailedEventArgs e)
        {
            MessageBox.Show("Geometry Service error: " + e.Error);
        }

        /// <summary>
        /// Create graphics when base is 4326
        /// </summary>
        /// <param name="rssGraphics"></param>
        private void Add4326Graphics(List<RssGraphic> rssGraphics)
        {
            int counter = 0;
            foreach (RssGraphic rssGraphic in rssGraphics)
            {
                Graphic graphic = new Graphic();
                //mouse events
                graphic.MouseEnter += graphic_MouseEnter;
                graphic.MouseLeave += graphic_MouseLeave;
                //title
                graphic.Attributes.Add("Title", rssGraphic.title);

                //abstract
                string result = (Regex.Replace(rssGraphic.Description, @"<(.|\n)*?>", string.Empty)).TrimStart(new char[] { '\n' });
                int loc = result.IndexOf("\n");
                string abstactString = result.Substring(0, loc);
                if (abstactString.ToLower() == "null")
                {
                    abstactString = "No description available";
                }
                graphic.Attributes.Add("Abstract", abstactString);

                //magic for links happens here
                List<GeoLinks> links = ParseDescription(rssGraphic.Description);

                //links
                List<string> currentLinks = new List<string>();
                foreach (GeoLinks link in links)
                {

                    graphic.Attributes.Add(link.Title, link.Url);
                    graphic.Attributes.Add(link.Title + "Visible", true);

                    currentLinks.Add(link.Title);
                    if (link.AddToMapInfo != null)
                    {

                        graphic.Attributes.Add("Add", link.AddToMapInfo);
                        graphic.Attributes.Add("AddVisible", true);
                        currentLinks.Add("Add");
                    }

                }

                //geometry
                string[] sPoints = rssGraphic.StringPolygon.Split(' ');

                ESRI.ArcGIS.Client.Geometry.PointCollection pColl = new ESRI.ArcGIS.Client.Geometry.PointCollection();
                for (int i = 0; i < sPoints.Length; i++)
                {

                    pColl.Add(new MapPoint(Convert.ToDouble(sPoints[i + 1], CultureInfo.InvariantCulture), Convert.ToDouble(sPoints[i], CultureInfo.InvariantCulture)));
                    i++;
                }
                ESRI.ArcGIS.Client.Geometry.Polygon poly = new ESRI.ArcGIS.Client.Geometry.Polygon();
                poly.Rings.Add(pColl);
                graphic.Geometry = poly;
                System.Diagnostics.Debug.WriteLine("Graphic extent: " + graphic.Geometry.Extent);
                _graphicsLyr.Graphics.Add(graphic);

                //result grid
                string sCleanLink = FindServiceLink(rssGraphic.Description);

                string sID = rssGraphic.title;
                sCleanLink = sCleanLink + "," + sID;

                //Query Results assigned to Query Details Grid
                _queryResultData.Add(new QueryResultData()
                {
                    Title = rssGraphic.title,
                    Description = rssGraphic.Description,
                    IsEnable = false,
                    graphic = graphic,
                    //sLink = sCleanLink,
                    ID = sID
                });
                counter++;
            }
            UpdateUIAfterSearch(counter);
        }

        private string FindServiceLink(string sDescription)
        {
            string sFirst = sDescription.Substring(sDescription.IndexOf("http://"));
            string sLast = sFirst.Substring(0, sFirst.IndexOf('"'));
            if (sLast.IndexOf(' ') > 0)
                sLast = sLast.Substring(0, sLast.IndexOf(' '));
            if (sLast.IndexOf('\n') > 0)
                sLast = sLast.Substring(0, sLast.IndexOf('\n'));

            return sLast;
        }

        private void UpdateUIAfterSearch(int counter)
        {
            if (counter > 0)
            {
                QueryDetailsDataGrid.Visibility = Visibility.Visible;
                //ResetAll.Visibility = Visibility.Visible;
            }
            else
            {
                QueryDetailsDataGrid.Visibility = Visibility.Collapsed;
               // ResetAll.Visibility = Visibility.Collapsed;
            }
            LayoutRootData.Visibility = Visibility.Visible;

            // Refresh the datagrid
            if (QueryDetailsDataGrid.ItemsSource != null)
                QueryDetailsDataGrid.ItemsSource = null;

            TotalFoundText.Text = _queryResultData.Count() + " documents found";

            QueryDetailsDataGrid.ItemsSource = _queryResultData;
            //added so does not zoom in
            if (!_isFullyWithin)
                Map.Extent = _graphicsLyr.FullExtent;
        }

        #endregion

        #region Map Mouse Events

        void graphic_MouseLeave(object sender, MouseEventArgs e)
        {
            Graphic gr = sender as Graphic;
            if (gr.Selected)
                gr.UnSelect();
        }

        void graphic_MouseEnter(object sender, MouseEventArgs e)
        {
            Graphic gr = sender as Graphic;

            if (!gr.Selected)
                gr.Select();
        }

        #endregion

        #region Results Grid Events

        /// <summary>
        /// Adds the cell events for the grid
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void QueryDetailsDataGrid_LoadingRow(object sender, DataGridRowEventArgs e)
        {
            DataGridColumn col = QueryDetailsDataGrid.Columns[0];
            FrameworkElement cellContent = col.GetCellContent(e.Row);
            cellContent.MouseLeftButtonDown -= cell_MouseLeftButtonDown;
            cellContent.MouseLeftButtonDown += new MouseButtonEventHandler(cell_MouseLeftButtonDown);
            DataGridCell cell = cellContent.Parent as DataGridCell;
            cell.MouseEnter -= cell_MouseEnter;
            cell.MouseEnter += new MouseEventHandler(cell_MouseEnter);
            cell.MouseLeave -= cell_MouseLeave;
            cell.MouseLeave += new MouseEventHandler(cell_MouseLeave);
        }

        /// <summary>
        /// Add new Map Service in the map
        /// </summary>
        private void RowFilterButton_Checked(object sender, RoutedEventArgs e)
        {
            CheckBox bx = sender as CheckBox;
            if ((_graphicsLyr.Graphics.Count > 0) || (bx.IsChecked == true))
            {                
                ChangeGraphicSymbol(bx);
            }
        }

        private void ResetAll_Checked(object sender, RoutedEventArgs e)
        {
            if (_graphicsLyr != null)
            {
                CheckBox bx = sender as CheckBox;
                bool val = bx.IsChecked.Value;
                if (val)
                {
                    _graphicsLyr.ClearGraphics();
                    foreach (QueryResultData qr in QueryResultData)
                    {
                        _graphicsLyr.Graphics.Add(qr.graphic);
                    }
                }
                else
                {
                    _graphicsLyr.ClearGraphics();
                }

                foreach (QueryResultData rstl in QueryResultData)
                {

                    rstl.IsEnable = val;
                }
                QueryDetailsDataGrid.ItemsSource = null;
                QueryDetailsDataGrid.ItemsSource = QueryResultData;
                QueryDetailsDataGrid.UpdateLayout();
                _infoGraphicsLyr.ClearGraphics();
            }
        }
        
        #endregion

        #region Create Links

        /// <summary>
        /// Parse the description in HTML using LINQ
        /// </summary>
        /// <param name="sDescription"></param>
        /// <returns></returns>
        private List<GeoLinks> ParseDescription(string sDescription)
        {
            XDocument doc = XDocument.Parse("<?xml version=\"1.0\"?>" + sDescription);

            //Get the list of links in the Item's Description
            var listOfLinks = from item in doc.Descendants("div").Descendants("div").Descendants("A")
                              select new GeoLinks
                              {
                                  Title = item.Value,
                                  Url = HttpUtility.UrlDecode(item.Attribute("HREF").Value)
                              };

            List<GeoLinks> geoLinks = new List<GeoLinks>();

            foreach (GeoLinks tempitem in listOfLinks)
            {               
                if (IsValidUrl(tempitem.Url))
                {
                    //Replace the GeoRSS link names (from Geoportal search results) with configured values
                    //-- this is done since the XAML binding names cannot have whitespaces
                    string sLinkViewerName = string.Empty;
                    if (_dctGeoRSSLinkNames.TryGetValue(tempitem.Title, out sLinkViewerName))
                    {
                        tempitem.Title = sLinkViewerName;
                    }

                    geoLinks.Add(tempitem);
                }
            }

            return geoLinks;
        }

        private bool IsValidUrl(string url)
        {
            System.Globalization.CompareInfo cmpUrl = System.Globalization.CultureInfo.InvariantCulture.CompareInfo;
            if ((cmpUrl.IsPrefix(url, "http://") == false) && (cmpUrl.IsPrefix(url, "https://") == false))
            {
                return false;
            }
            else
            {
                Regex RgxUrl = new Regex("(([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)?/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?");
                if (RgxUrl.IsMatch(url))
                {
                    return true;
                }
                else
                {
                    return false;
                }

            }


        }

        #endregion

        #region Hyperlink Event


        /// <summary>
        /// Handles the clicking of hyperlinks on the InfoBox Callout
        /// - raises the Event for Adds layer to the map 
        /// - opens another browser to navigate to target uri
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void HyperlinkButton_Click(object sender, RoutedEventArgs e)
        {
            HyperlinkButton check = e.OriginalSource as HyperlinkButton;

            ESRI.ArcGIS.Client.DataBinding b = check.DataContext as ESRI.ArcGIS.Client.DataBinding;

            IDictionary<string, object> c = b.Attributes;

            if (check.Name == "Add")
            {
                AddServiceEventArgs argAddMapService = new AddServiceEventArgs();
                argAddMapService.ServiceURL = c["Add"].ToString();
                if (c["AddType"].ToString() == "ags")
                    argAddMapService.ServiceType = eServiceType.ags;
                else if (c["AddType"].ToString() == "wms")
                    argAddMapService.ServiceType = eServiceType.wms;

                ClearInfoCallout();

                AddToMapEvent(this, argAddMapService);
            }
            else
            {
                foreach (KeyValuePair<string, object> k in c)
                {
                    string d = k.Key;
                    if (d == check.Name)
                    {
                        if (k.Value != null)
                        {
                            string v = k.Value.ToString();

                            try
                            {
                                System.Windows.Browser.HtmlPage.Window.Navigate(new Uri(v), "_blank", "toolbar=no,location=no,status=no,menubar=no,resizable=yes,scrollbars=yes");
                            }
                            catch (Exception ex)
                            {
                                MessageBox.Show("Cannot navigate to link: " + ex.Message);
                            }
                            return;
                        }
                    }
                }
            }
        }

        #endregion
        
        #region Results Grid Cell Mouse Events

        void cell_MouseLeave(object sender, MouseEventArgs e)
        {
            if (sender.GetType() == typeof(DataGridCell))
            {
                DataGridCell cell = sender as DataGridCell;

                TextBlock txt = cell.Content as TextBlock;
                if (txt != null)
                {
                    var lst = from a in QueryResultData where a.Title == txt.Text select a;
                    QueryResultData itm = lst.First() as QueryResultData;
                    Graphic gr = itm.graphic;
                      
                    _graphicsLyr.Graphics.Remove(gr);
                }
            }
        }

        void cell_MouseEnter(object sender, MouseEventArgs e)
        {
            if (sender.GetType() == typeof(DataGridCell))
            {
                DataGridCell cell = sender as DataGridCell;
                TextBlock txt = cell.Content as TextBlock;
                if (txt != null)
                {
                    var lst = from a in QueryResultData where a.Title == txt.Text select a;
                    QueryResultData itm = lst.First() as QueryResultData;
                    Graphic gr = itm.graphic;

                    _graphicsLyr.Graphics.Add(gr);
                }
            }
        }

        void cell_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            TextBlock txt = null;
            if (sender.GetType() == typeof(DataGridCell))
            {
                DataGridCell cell = sender as DataGridCell;
                txt = cell.Content as TextBlock;
            }
            else if (sender.GetType() == typeof(TextBlock))
            {
                txt = sender as TextBlock;
            }
            if (txt != null)
            {
                //Re-add the Graphic Layers just in case other layers have been added (i.e. selection set, address callout, markup, etc)
                //-- this will ensure that each time a user clicks on the portal results, the Extents and Information Layer remain on top
                ReAddGraphicLayers();

                _infoGraphicsLyr.ClearGraphics();

                var lst = from a in QueryResultData where a.Title == txt.Text select a;
                QueryResultData itm = lst.First() as QueryResultData;
                Graphic gr = itm.graphic; 

                ESRI.ArcGIS.Client.Geometry.Polygon poly = gr.Geometry as ESRI.ArcGIS.Client.Geometry.Polygon;
                MapPoint pt = poly.Extent.GetCenter();
                Graphic newGr = new Graphic();
                foreach (KeyValuePair<string, object> p in gr.Attributes)
                {
                    if (p.Key.StartsWith("O"))
                    {
                        string x = string.Empty;
                    }
                    newGr.Attributes.Add(p.Key, p.Value);
                }
                newGr.Geometry = pt;

                newGr.Symbol = this.MySymbol;

                _infoGraphicsLyr.Graphics.Add(newGr);

                _infoGraphicsLyr.Refresh();
                Map.ZoomTo(gr.Geometry);
            }
        }

        #endregion

        #region Graphic Layers

        /// <summary>
        /// Removes then Adds the Graphic Layers (Extents and LayerInformation callout) to the map
        /// -- this ensures that these layers are always on top despite newly added layers
        /// </summary>
        public void ReAddGraphicLayers()
        {            
            //Bug #191: Layer information check box (when adding GDA data) 
            _graphicsLyr.Visible = true;
            _infoGraphicsLyr.Visible = true;

            //Remove the Extents Graphics Layer and add it back to ensure 
            //that it is always on top of any Added layers (i.e. dynamically added layers from portal search)
            if (MapHasGraphicsLayer(_layerName))
            {
                if (Map.Layers[Map.Layers.Count - 2].ID != _layerName)
                    Map.Layers.Remove(_graphicsLyr);
            }
            if (!MapHasGraphicsLayer(_layerName))
                Map.Layers.Add(_graphicsLyr);

            //Remove the Info Graphics Layer and add it back to ensure 
            //that it is always on top of any Added layers (i.e. dynamically added layers from portal search)
            if (MapHasGraphicsLayer(_infoLayerName))
            {
                if (Map.Layers[Map.Layers.Count - 1].ID != _infoLayerName)
                    Map.Layers.Remove(_infoGraphicsLyr);
            }
            if (!MapHasGraphicsLayer(_infoLayerName))
                Map.Layers.Add(_infoGraphicsLyr);
            
        }

        #endregion

        #region Unused ----------------------------------------------------------------------------------------------

        //TODO NEED TO ADD TO UI
        private void HelpButton_Click(object sender, RoutedEventArgs e)
        {
            HtmlPopupWindowOptions options = new HtmlPopupWindowOptions();
            options.Left = 0;
            options.Top = 0;
            options.Width = 800;
            options.Height = 600;
            if (true == HtmlPage.IsPopupWindowAllowed)
                HtmlPage.PopupWindow(new Uri(_urlHelp), "new", options);
        }


        private void ChangeGraphicSymbol(CheckBox bx)
        {            
            if (bx.Tag != null)
            {
                bool val = bx.IsChecked.Value;

                if (val)
                {

                    var lst = from a in QueryResultData where a.Title == (string)bx.Tag select a;
                    if (lst.Count() > 0)
                    {
                        QueryResultData rslt = (QueryResultData)lst.First();
                        Graphic gr = rslt.graphic;
                        _graphicsLyr.Graphics.Add(gr);
                    }

                }
                else
                {
                    var grs = from a in _graphicsLyr.Graphics where (string)a.Attributes["Title"] == (string)bx.Tag select a;
                    if (grs.Count() > 0)
                    {
                        Graphic gr = (Graphic)grs.First();

                        _graphicsLyr.Graphics.Remove(gr);                        
                    }
                }

                _graphicsLyr.Refresh();

            }
        }

        private void CloseImg_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            _infoGraphicsLyr.ClearGraphics();
        }

        private bool CheckProvider(string urlStr)
        {

            string[] arr = { "addtomap", "resources=map", "livedata", "preview.page" };

            for (int i = 0; i < arr.Length; i++)
            {
                if (urlStr.ToLower().Contains(arr[i]))
                {
                    return true;
                }
            }
            //WMS CGI
            string[] wmsarr = { "request=getcapabilities", "service=wms" };
            string wmsFactors = string.Empty;
            for (int j = 0; j < wmsarr.Length; j++)
            {
                if (urlStr.ToLower().Contains(wmsarr[j]))
                {
                    return true;
                }
            }
            return false;
        }

        private MapServiceType GetMapServiceType(string urlStr)
        {
            MapServiceType returnVal = MapServiceType.None;
            string[] arr = { "mapserver", "imageserver", "wmsserver", "servicetype=image", "server=", "service=", "resourcetype=wms", "service=wms", "resourcetype=ags", "request=getcapabilities" };
            string xxx = string.Empty;
            for (int i = 0; i < arr.Length; i++)
            {
                if (urlStr.ToLower().Contains(arr[i]))
                {
                    xxx = xxx + "--" + arr[i];

                }
            }
            if (xxx.Contains("mapserver") && xxx.Contains("resourcetype=ags"))
            {
                returnVal = MapServiceType.MapService;
            }
            else if (xxx.Contains("imageserver"))
            {
                returnVal = MapServiceType.ImageService;
            }
            else if (xxx.Contains("servicetype=image") && xxx.Contains("server=") && xxx.Contains("service="))
            {
                returnVal = MapServiceType.ImsService;
            }
            else if (xxx.Contains("service=wms"))
            {
                returnVal = MapServiceType.WmsService;

            }
            else if (xxx.Contains("wmsserver"))
            {
                returnVal = MapServiceType.WmsService;
            }
            return returnVal;
        }
        
        private string GetMapServiceLink(string urlStr)
        {
            string returnVal = string.Empty;

            string[] arr = urlStr.Split('&');
            foreach (string s in arr)
            {
                if (s.Contains("MapServer"))
                {
                    returnVal = MapServiceType.MapService.ToString() + ";";
                    returnVal = returnVal + s.Substring(s.IndexOf('=') + 1);
                    break;
                }
            }
            return returnVal;
        }

        private string GetImsServiceLink(string urlStr)
        {
            string returnVal = string.Empty;
            string host = string.Empty;
            string serviceName = string.Empty;

            string[] arr = urlStr.Split('&');
            foreach (string s in arr)
            {
                if (s.Contains("server="))
                {
                    host = s.Substring(s.IndexOf('=') + 1);
                }
                if (s.Contains("service="))
                {
                    serviceName = s.Substring(s.IndexOf('=') + 1);
                }
            }
            if (host.Length > 0 && serviceName.Length > 0)
            {
                returnVal = MapServiceType.ImsService.ToString() + ";" + host + ";" + serviceName;
            }

            return returnVal;

        }

        private string GetWmsServiceLink(string urlStr)
        {

            string returnVal = MapServiceType.WmsService.ToString() + ";" + urlStr;
            return returnVal;

        }

        #region Add ArcIMS Layer

        private void ProcessImsService(string url)
        {
            try
            {

                HttpWebRequest webRequest = WebRequest.Create(_completeMapServiceUrl) as HttpWebRequest;

                webRequest.BeginGetResponse(new AsyncCallback(AfterImsServiceRequest), webRequest);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Unable to access this map service. An error has occurred: " + ex.Message);

            }
        }

        private void AfterImsServiceRequest(IAsyncResult result)
        {
            //geodata search ims - som is good search h-gac - is bad not found
            bool error = false;
            string errorMsg = string.Empty;
            try
            {
                HttpWebRequest request = result.AsyncState as HttpWebRequest;

                HttpWebResponse response = request.EndGetResponse(result) as HttpWebResponse;
                response.Close();
                request.Abort();

                Dispatcher.BeginInvoke(() =>
                {

                    GraphicsLayer gridWindowLyr = Map.Layers[Map.Layers.Count - 1] as GraphicsLayer;

                    string[] sFields = _completeMapServiceUrl.Split('?');
                    _imslyr = new ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer();

                    _imslyr.GetImageFailed += new EventHandler(_imslyr_GetImageFailed);
                    _imslyr.ProxyUrl = sFields[0];
                    string host = sFields[1].Replace("/servlet/com.esri.esrimap.Esrimap", "");
                    _imslyr.ServiceHost = host;
                    string nm = sFields[2].Replace("ServiceName=", "");
                    _imslyr.ServiceName = nm;
                    _imslyr.ID = nm + "Lyr";
                    Map.Layers.RemoveAt(Map.Layers.Count - 1);
                    Map.Layers.Add(_imslyr);
                    Map.Layers.Add(gridWindowLyr);
                    _imslyr = null;
                });

            }
            catch (Exception ex)
            {
                error = true;
                errorMsg = ex.Message;
            }
            finally
            {
                if (error)
                {
                    Dispatcher.BeginInvoke(() =>
                    {
                        MessageBox.Show("Unable to add this ArcIMS layer. An error occurred: " + errorMsg);
                    });

                }
            }
        }

        void _imslyr_GetImageFailed(object sender, EventArgs e)
        {

            ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer imslyr = sender as ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer;
            string layerName = imslyr.ID;
            string error = imslyr.ErrorMsg;
            int lyrcnt = Map.Layers.Where(a => a.ID == imslyr.ID).Count();
            if (lyrcnt > 0)
            {
                Map.Layers.Remove(imslyr);
            }

            MessageBox.Show("Unable to add " + layerName + " at this time. An error has occurred: " + error);
        }

        #endregion

        #region Add ArcGIS server Map Service Layer

        private void ProcessMapService(string url)
        {
            //bogus url to test failure
            //completeUrl = "http://services.arcgisonline.com/ArcGIS/rest/services/Specialty/World_Navigation_Chart/MapServer";
            //completeUrl = "abc";
            //HttpWebRequest webRequest;
            try
            {
                HttpWebRequest webRequest = WebRequest.Create(_completeMapServiceUrl) as HttpWebRequest;

                webRequest.BeginGetResponse(new AsyncCallback(AfterMapServiceRequest), webRequest);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Unable to access this map service. An error has occurred: " + ex.Message);

            }

        }

        private void AfterMapServiceRequest(IAsyncResult result)
        {

            try
            {
                HttpWebRequest request = result.AsyncState as HttpWebRequest;
                HttpWebResponse response = request.EndGetResponse(result) as HttpWebResponse;

                Stream stream = response.GetResponseStream();
                StreamReader reader = new StreamReader(stream);

                if (reader.ReadLine() != null)
                {
                    Dispatcher.BeginInvoke(() =>
                    {
                        ArcGISDynamicMapServiceLayer lyr = new ArcGISDynamicMapServiceLayer();
                        int counter = Map.Layers.Count();
                        lyr.ID = "mapServiceLyr" + counter;


                        GraphicsLayer gridWindowLyr = Map.Layers[Map.Layers.Count - 1] as GraphicsLayer;

                        lyr.Url = _completeMapServiceUrl;
                        lyr.InitializationFailed += new EventHandler<EventArgs>(lyr_InitializationFailed);
                        Map.Layers.RemoveAt(Map.Layers.Count - 1);
                        Map.Layers.Add(lyr);
                        Map.Layers.Add(gridWindowLyr);
                    });
                }
                else
                {
                    MessageBox.Show("Unable to add this Map Service.");
                }
                response.Close();

            }
            catch (Exception ex)
            {
                MessageBox.Show("Unable to add this Map Service. An error has occurred: " + ex.Message);
            }


        }

        void lyr_InitializationFailed(object sender, EventArgs e)
        {
            ArcGISDynamicMapServiceLayer lyr = sender as ArcGISDynamicMapServiceLayer;
            Map.Layers.Remove(lyr);
            lyr = null;
            MessageBox.Show("Unable to add Map Service at this time.");
        }

        #endregion

        #region Add WMS Service Layer

        internal void wmsclient_DownloadStringCompleted(object sender, DownloadStringCompletedEventArgs e)
        {
            string err = string.Empty;
            if (e.Error != null)
            {
                err = e.Error.Message;
            }
            if (e.Error == null)
            {
                XElement xEle = XElement.Parse(e.Result);
                XNamespace aw = xEle.GetDefaultNamespace();
                //version
                bool canGet = true;

                //capability
                XElement capEle = LinqXmlHelper.GetWmsSingleElement(xEle, "Capability");  //capabilities is required
                if (capEle == null) err = "Missing Capability in XML.";
                if (capEle != null)
                {
                    string srsTag = ""; // LinqXmlHelper.GetSpatialReferenceTagName(_wmsLyr.Version);
                    //main Layer element
                    XElement mainLyrEle = LinqXmlHelper.GetWmsSingleElement(capEle, "Layer");  //layer is required
                    if (mainLyrEle == null) err = "No Layers available in XML.";
                    if (mainLyrEle != null)
                    {
                        List<int> supportedSRs = LinqXmlHelper.GetSupportedWKIDs(mainLyrEle, srsTag);
                        bool isWebMercator = false;
                        //find is supported is web mercator
                        foreach (int i in supportedSRs)
                        {
                            var b = WebMercatorWkids.Where(a => a.Equals(i));
                            if (b.Count() > 0)
                            {
                                isWebMercator = true;
                            }
                        }
                        int mapSr = Map.SpatialReference.WKID;
                        //find if map is web mercator
                        var ms = WebMercatorWkids.Where(m => m.Equals(mapSr));

                        //both are not web mercator so compare literally
                        if (!(ms.Count() > 0 && isWebMercator))
                        {
                            var inList = supportedSRs.Where(f => f.Equals(mapSr));
                            if (inList.Count() == 0)
                                canGet = false;
                        }
                        if (!canGet) err = "WMS service spatial reference not compatible with current map.";
                        if (canGet)
                        {
                            string[] lyrList = LinqXmlHelper.GetLayerList(capEle);
                            if (lyrList == null) err = "No layer names available in XML.";
                            if (lyrList != null)
                            {

                                GraphicsLayer gridWindowLyr = Map.Layers[Map.Layers.Count - 1] as GraphicsLayer;
                                Map.Layers.Remove(gridWindowLyr);
                                Map.Layers.Add(gridWindowLyr);
                            }

                        }
                    }
                }
                //}
            }
            //}
            if (err.Length > 0)
                MessageBox.Show("Unable to add this service layer to map. " + err);
        }

        void _wmsLyr_GetMapFailed(object sender, EventArgs e)
        {
        }

        void _wmsLyr_Initialized(object sender, EventArgs e)
        {
            //throw new NotImplementedException();
        }

        void _wmsLyr_InitializationFailed(object sender, EventArgs e)
        {
            //throw new NotImplementedException();
        }

        private Uri PrefixProxy(string url)
        {
            if (string.IsNullOrEmpty(_proxyUrl))
                return new Uri(url, UriKind.RelativeOrAbsolute);
            string proxyUrl = _proxyUrl;
            if (!proxyUrl.Contains("?"))
            {
                if (!proxyUrl.EndsWith("?"))
                    proxyUrl = _proxyUrl + "?";
            }
            else
            {
                if (!proxyUrl.EndsWith("&"))
                    proxyUrl = _proxyUrl + "&";
            }
            if (_proxyUrl.StartsWith("~") || _proxyUrl.StartsWith("../")) //relative to xap root
            {
                string uri = Application.Current.Host.Source.AbsoluteUri;
                int count = proxyUrl.Split(new string[] { "../" }, StringSplitOptions.None).Length;
                for (int i = 0; i < count; i++)
                {
                    uri = uri.Substring(0, uri.LastIndexOf("/"));
                }
                if (!uri.EndsWith("/"))
                    uri += "/";
                proxyUrl = uri + proxyUrl.Replace("~", "").Replace("../", "");
            }
            else if (_proxyUrl.StartsWith("/")) //relative to domain root
            {
                proxyUrl = _proxyUrl.Replace("/", string.Format("{0}://{1}:{2}",
                    Application.Current.Host.Source.Scheme,
                    Application.Current.Host.Source.Host,
                    Application.Current.Host.Source.Port));
            }
            UriBuilder b = new UriBuilder(proxyUrl);
            b.Query = url;
            return b.Uri;
        }

        #endregion

  
        #endregion Unused ----------------------------------------------------------------------------------------------
    }
}

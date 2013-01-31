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
using System.ComponentModel;
using System.Text.RegularExpressions;
using System.Text;

namespace GeoportalWidget
{
    public partial class GeoPortalMain : UserControl
    {
        #region Variables

        private XElement _configDoc;
        private MapServiceManager _mapServiceManager = null;

        private static string _proxyUrl;
        private bool _useProxy;
        private string _urlHelp;
        private string _completeMapServiceUrl;

        private ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer _imslyr;
        private ESRI.ArcGIS.Samples.WMS.WMSMapServiceLayer _wmsLyr;
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
        private static string _layerName = "GeoRssLayer";
        private static string _infoLayerName = "InfoLayer";
        private bool _isFullyWithin;

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

        #endregion

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

        #region Constructor

        public GeoPortalMain()
        {

            InitializeComponent();

            _configTagsList = new List<ConfigTags>();
            _configTagsList.Add(ConfigTags.gptEndpoint);
            _configTagsList.Add(ConfigTags.useProxy);

            _graphicsLyr = new GraphicsLayer();
            _graphicsLyr.ID = _layerName;
            _graphicsLyr.Renderer = this.glRenderer;

            _infoGraphicsLyr = new GraphicsLayer();
            _infoGraphicsLyr.ID = _infoLayerName;

            WebClient getXmlClient = new WebClient();
            //set the default proxy
//            _proxyUrl = getXmlClient.BaseAddress.Substring(0, getXmlClient.BaseAddress.IndexOf("ClientBin/")+10) + "proxy.jsp?";
            _proxyUrl = getXmlClient.BaseAddress.Substring(0, getXmlClient.BaseAddress.IndexOf("SilverlightExample/")) + @"FlexExample/proxy.jsp?";
            getXmlClient.DownloadStringCompleted += new DownloadStringCompletedEventHandler(getXmlClient_DownloadStringCompleted);
            getXmlClient.DownloadStringAsync(new Uri("geoportalWidgetConfig.xml", UriKind.Relative));
        }

        #endregion

        #region Dependency Properties
        public static readonly DependencyProperty MapProperty =
                             DependencyProperty.Register("Map", typeof(ESRI.ArcGIS.Client.Map), typeof(GeoPortalMain), null);

        public ESRI.ArcGIS.Client.Map Map
        {
            get { return (ESRI.ArcGIS.Client.Map)GetValue(MapProperty); }
            set
            {
                SetValue(MapProperty, value);
            }
        }

        #endregion

        #region Map Service Manager

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
                    //add the graphics layers to the map
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
                    //SendErrorMsg("Unable to read configuration file " + ex.Message);
                }
            }
            else
            {
                //TODO DISABLE? MAKE NOT VISIBLE
                //SendErrorMsg("Unable to load configuration file.");
            }

        }

        /// <summary>
        /// Check if map has a search results graphics layer
        /// </summary>
        /// <param name="lyrName">layer name</param>
        /// <returns>true or false</returns>
        private bool MapHasGraphicsLayer(string lyrName)
        {
            Layer lyr = Map.Layers[lyrName];
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

        #endregion

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

        #region Searching

        /// <summary>
        /// Fires search based on selected repository and search text - populates grid
        /// </summary>
        private void SearchButton_Click(object sender, RoutedEventArgs e)
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

            //            baseUrl = itm.url + _format;
            baseUrl = ProcessUrl(itm.url) + _format;
            if (this.LiveDataMaps.IsChecked.Value == true)
            {
                baseUrl = baseUrl + _liveMaps;
            }

            if (SearchText.Text.Length > 0)
            {
                baseUrl = baseUrl + _searchText + SearchText.Text;
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

            Envelope extEnv = Map.Extent;
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
        /// Used to create result boxes graphics layer
        /// </summary>
        internal class RssGraphic : Graphic
        {
            public Dictionary<string, object> RssAttributes { get; set; }
            public string StringPolygon { get; set; }
            public string title { get; set; }
            public string Description { get; set; }

        }

        /// <summary>
        /// Detects error if none calls to parse results
        /// </summary>
        private void searchClient_OpenReadCompleted(object sender, OpenReadCompletedEventArgs e)
        {

            if (e.Error != null)
            {
                MessageBox.Show("Error in reading search results: " + e.Error.Message);
                return;
            }

            PopulateSearchResultsList(e.Result);
            return;

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
                foreach (XElement c in a)
                {
                    var d = c.Element("description");
                    string e = string.Empty;
                }
                SpatialReference srMap = Map.SpatialReference;

                var rssList = from rssGraphic in doc.Descendants("item") select rssGraphic;
                List<RssGraphic> rssGraphics = new List<RssGraphic>();

                foreach (XElement r in rssList)
                {
                    IEnumerable<XElement> de = from el in r.Descendants(geo + "polygon") select el;

                    if (de.Count() > 0)
                    {
                        RssGraphic rg = new RssGraphic();
                        rg.StringPolygon = r.Element(geo + "polygon").Value;
                        rg.RssAttributes = new Dictionary<string, object>() { { "MAGNITUDE", r.Element("title").Value } };
                        rg.title = r.Element("title").Value;
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
                    //if other call geometry service
                    //if is geo create poly now
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
                if (counter == 6)
                {
                    string aaaaa = string.Empty;
                }

                ESRI.ArcGIS.Client.Geometry.PointCollection pColl = new ESRI.ArcGIS.Client.Geometry.PointCollection();
                for (int i = 0; i < sPoints.Length; i++)
                {

                    MapPoint mp = new MapPoint();

                    double x = WebMercator.ToWebMercatorX(Convert.ToDouble(sPoints[i + 1]));
                    double y = WebMercator.ToWebMercatorY(Convert.ToDouble(sPoints[i]));
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

                    graphic.Geometry = poly;
                    System.Diagnostics.Debug.WriteLine("poly: xmax: " + poly.Extent.XMax + " xmin: " + poly.Extent.XMin + " ymax: " + poly.Extent.YMax + " ymin: " + poly.Extent.YMin);
                    _graphicsLyr.Graphics.Add(graphic);

                    //result grid
                    string sCleanLink = FindServiceLink(rssGraphic.Description);

                    string sID = rssGraphic.title;
                    sCleanLink = sCleanLink + "," + sID;

                    _queryResultData.Add(new QueryResultData()
                    {
                        Title = rssGraphic.title,
                        Description = rssGraphic.Description,
                        IsEnable = true,
                        graphic = graphic,
                        sLink = sCleanLink,
                        ID = sID
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

                    pColl.Add(new MapPoint(Convert.ToDouble(sPoints[i + 1]), Convert.ToDouble(sPoints[i])));
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

                _queryResultData.Add(new QueryResultData()
                {
                    Title = rssGraphic.title,
                    Description = rssGraphic.Description,
                    IsEnable = true,
                    graphic = graphic,
                    sLink = sCleanLink,
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
                ResetAll.Visibility = Visibility.Visible;
            }
            else
            {
                QueryDetailsDataGrid.Visibility = Visibility.Collapsed;
                ResetAll.Visibility = Visibility.Collapsed;
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
        /// 
        /// </summary>
        private void QueryDetailsDataGrid_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            QueryResultData item = e.AddedItems[0] as QueryResultData;
            if (item != null)
            {
                Graphic gr = item.graphic as Graphic;

                _graphicsLyr.Refresh();

            }
        }

        /// <summary>
        /// Adds the cell events for the grid
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void QueryDetailsDataGrid_LoadingRow(object sender, DataGridRowEventArgs e)
        {

            DataGridColumn col = QueryDetailsDataGrid.Columns[1];
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
            if (_graphicsLyr.Graphics.Count > 0)
            {
                CheckBox bx = sender as CheckBox;
                ChangeGraphicSymbol(bx);
            }
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
                DataGridColumn col = QueryDetailsDataGrid.Columns[0];

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

            var listOfLinks = from item in doc.Descendants("div").Descendants("div").Descendants("A")
                              select new GeoLinks
                              {
                                  Title = item.Value,
                                  Url = HttpUtility.UrlDecode(item.Attribute("HREF").Value)

                              };

            List<GeoLinks> geoLinks = new List<GeoLinks>();
            int addcounter = 0;
            foreach (GeoLinks tempitem in listOfLinks)
            {
                if (tempitem.Title != "Open")
                {
                    if (CheckProvider(tempitem.Url))
                    {
                        string linkStr = string.Empty;
                        MapServiceType mapType = GetMapServiceType(tempitem.Url);
                        if (mapType != MapServiceType.None)
                        {
                            switch (mapType)
                            {
                                case MapServiceType.MapService:
                                    linkStr = GetMapServiceLink(tempitem.Url);
                                    tempitem.AddToMapInfo = linkStr;
                                    addcounter++;
                                    break;
                                case MapServiceType.ImageService:
                                    break;
                                case MapServiceType.ImsService:
                                    linkStr = GetImsServiceLink(tempitem.Url);
                                    tempitem.AddToMapInfo = linkStr;
                                    addcounter++;
                                    break;
                                case MapServiceType.WmsService:
                                    linkStr = GetWmsServiceLink(tempitem.Url);
                                    tempitem.AddToMapInfo = linkStr;
                                    addcounter++;
                                    break;
                            }
                        }
                    }
                }

                if (IsValidUrl(tempitem.Url))
                {
                    geoLinks.Add(tempitem);
                }

            }
            var m = from l in geoLinks where l.AddToMapInfo != null select l;

            if (m.Count() > 1)
            {
                var n = from b in m where b.Title.ToLower() == "add to map" select b;
                if (n.Count() > 0)
                {
                    foreach (GeoLinks lk in m)
                    {
                        if (lk.Title.ToLower() != "add to map")
                            lk.AddToMapInfo = null;
                    }
                }
                else
                {
                    GeoLinks lnk = m.First();
                    foreach (GeoLinks lnks in m)
                    {
                        if (lnks != lnk)
                            lnks.AddToMapInfo = null;
                    }
                }


            }
            return geoLinks;
        }

        private bool IsValidUrl(string url)
        {
            System.Globalization.CompareInfo cmpUrl = System.Globalization.CultureInfo.InvariantCulture.CompareInfo;
            if (cmpUrl.IsPrefix(url, "http://") == false)
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

        #endregion

        private void SearchText_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
                SearchButton_Click(null, null);
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
            if (_wmsLyr == null) err = "No WMS layer available.";
            if (_wmsLyr != null)
            {
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

                    _wmsLyr.Version = LinqXmlHelper.GetWmsVersion(xEle); //version is required
                    if (_wmsLyr.Version.Length == 0) err = "No version available.";
                    if (_wmsLyr.Version.Length > 0)
                    {
                        //capability
                        XElement capEle = LinqXmlHelper.GetWmsSingleElement(xEle, "Capability");  //capabilities is required
                        if (capEle == null) err = "Missing Capability in XML.";
                        if (capEle != null)
                        {
                            string srsTag = LinqXmlHelper.GetSpatialReferenceTagName(_wmsLyr.Version);
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
                                        _wmsLyr.Layers = lyrList;

                                        GraphicsLayer gridWindowLyr = Map.Layers[Map.Layers.Count - 1] as GraphicsLayer;
                                        Map.Layers.Remove(gridWindowLyr);
                                        _wmsLyr.SkipGetCapabilities = true;
                                        _wmsLyr.InitializationFailed += new EventHandler<EventArgs>(_wmsLyr_InitializationFailed);
                                        _wmsLyr.Initialized += new EventHandler<EventArgs>(_wmsLyr_Initialized);
                                        _wmsLyr.GetMapFailed += new EventHandler(_wmsLyr_GetMapFailed);
                                        Map.Layers.Add(_wmsLyr);
                                        Map.Layers.Add(gridWindowLyr);
                                    }

                                }
                            }
                        }
                    }
                }
            }
            if (err.Length > 0)
                MessageBox.Show("Unable to add this service layer to map. " + err);
        }

        void _wmsLyr_GetMapFailed(object sender, EventArgs e)
        {
            ESRI.ArcGIS.Samples.WMS.WMSMapServiceLayer wmslyr = sender as ESRI.ArcGIS.Samples.WMS.WMSMapServiceLayer;
            string layerName = wmslyr.ID;
            string error = wmslyr.ErrorMsg;
            int lyrcnt = Map.Layers.Where(a => a.ID == wmslyr.ID).Count();
            if (lyrcnt > 0)
            {
                Map.Layers.Remove(wmslyr);
            }

            MessageBox.Show("Unable to add " + layerName + " at this time. An error has occurred: " + error);
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

        /// <summary>
        /// Adds the layer to the map
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void HyperlinkButton_Click(object sender, RoutedEventArgs e)
        {
            HyperlinkButton check = e.OriginalSource as HyperlinkButton;

            ESRI.ArcGIS.Client.DataBinding b = check.DataContext as ESRI.ArcGIS.Client.DataBinding;

            IDictionary<string, object> c = b.Attributes;
            foreach (KeyValuePair<string, object> k in c)
            {

                string d = k.Key;
                if (d == check.Name)
                {
                    if (k.Value != null)
                    {
                        string v = k.Value.ToString();
                        if (check.Name == "Add")
                        {
                            //add to map here
                            GraphicsLayer gridWindowLyr = Map.Layers[Map.Layers.Count - 1] as GraphicsLayer;

                            string[] sFields = v.Split(';');
                            if (sFields[0] == MapServiceType.MapService.ToString())
                            {
                                _completeMapServiceUrl = _proxyUrl + sFields[1];
                                //check first is valid link to map service
                                ProcessMapService(_completeMapServiceUrl);

                            }
                            else if (sFields[0] == MapServiceType.ImsService.ToString())
                            {
                                if (_imslyr != null)
                                {
                                    _imslyr = null;
                                }

                                int cutOff = _proxyUrl.IndexOf("?");
                                string proxy = _proxyUrl.Substring(0, cutOff);
                                _completeMapServiceUrl = proxy + "?" + sFields[1] + "/servlet/com.esri.esrimap.Esrimap" + "?ServiceName=" + sFields[2];
                                ProcessImsService(_completeMapServiceUrl);

                            }
                            else if (sFields[0] == MapServiceType.WmsService.ToString())
                            {
                                string url = string.Empty;
                                string version = string.Empty;
                                if (sFields[1].Contains("url="))
                                {
                                    //strip off the unneeded  
                                    string urlStr = sFields[1].Substring(sFields[1].IndexOf("url=") + 4);
                                    string s = urlStr.Substring(urlStr.IndexOf('?') + 1);
                                    url = urlStr.Replace(s, "");
                                    url = url.Substring(0, url.IndexOf('?'));
                                    string[] arr = s.Split('&');

                                    Dictionary<string, string> dict = new Dictionary<string, string>();
                                    for (int j = 0; j < arr.Length; j++)
                                    {
                                        if (arr[j].Contains('='))
                                        {
                                            string[] kvArr = arr[j].Split('=');
                                            dict.Add(kvArr[0], kvArr[1]);
                                        }
                                    }


                                    var versionList = from entry in dict where (entry.Key.ToLower() == "version") select entry.Value;
                                    if (versionList.Count() > 0)
                                    {
                                        version = versionList.First();
                                    }
                                }
                                else
                                {
                                    if (sFields[1].Contains("server="))
                                    {
                                        url = sFields[1].Substring(sFields[1].IndexOf("server=") + 7);
                                        url = url.Substring(0, url.IndexOf("WMSServer") + 9);
                                    }
                                    else
                                        url = sFields[1].Substring(sFields[1].IndexOf("@http:") + 1);
                                    //http://atlas.resources.ca.gov/arcgis/services/Inland_Waters/USGSWaterWatch/MapServer/WMSServer&service=&serviceType=wms
                                }

                                string proxyUrl = _proxyUrl;

                                if (_wmsLyr != null)
                                {
                                    _wmsLyr = null;
                                }

                                //_wmsLyr = new ESRI.ArcGIS.Client.Toolkit.DataSources.WmsLayer();
                                _wmsLyr = new ESRI.ArcGIS.Samples.WMS.WMSMapServiceLayer();

                                _wmsLyr.Url = url;
                                if (version != string.Empty)
                                {
                                    _wmsLyr.Version = version;
                                }
                                _wmsLyr.SkipGetCapabilities = false;
                                _wmsLyr.ProxyUrl = _proxyUrl;

                                string wmsUrl = string.Empty;

                                if (version.Length > 0)
                                {
                                    wmsUrl = string.Format("{0}{1}{2}", url, "?service=WMS&request=GetCapabilities&version=", version);
                                }
                                else
                                {
                                    wmsUrl = string.Format("{0}{1}", url, "?service=WMS&request=GetCapabilities");
                                }

                                WebClient wmsclient = new WebClient();

                                wmsclient.DownloadStringCompleted += wmsclient_DownloadStringCompleted;
                                wmsclient.DownloadStringAsync(PrefixProxy(wmsUrl));


                            }
                        }
                        else
                        {

                            //System.Windows.Browser.HtmlPage.Window.Navigate(new Uri(v), "_blank", "toolbar=no,location=no,status=no,menubar=no,resizable=yes,height=400,width=500,top=100,left=100");
                            try
                            {
                                System.Windows.Browser.HtmlPage.PopupWindow(new Uri(v), null, new System.Windows.Browser.HtmlPopupWindowOptions()
                                {
                                    Resizeable = true
                                });
                            }
                            catch (Exception ex)
                            {
                                MessageBox.Show("Cannot navigate to link: " + ex.Message);
                            }
                            return;
                        }
                    }
                    else
                        MessageBox.Show("No link available for " + check.Name);
                }
            }
        }

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
                    if (gr.Selected)
                        gr.UnSelect();
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


                    if (!gr.Selected)
                        gr.Select();
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

        private void CloseImg_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            _infoGraphicsLyr.ClearGraphics();
        }

    }

    // The RequestState class passes data across async calls.
    public class RequestState
    {
        const int BufferSize = 1024;
        public StringBuilder RequestData;
        public byte[] BufferRead;
        public WebRequest Request;
        public Stream ResponseStream;
        // Create Decoder for appropriate enconding type.
        public Decoder StreamDecode = Encoding.UTF8.GetDecoder();

        public RequestState()
        {
            BufferRead = new byte[BufferSize];
            RequestData = new StringBuilder(String.Empty);
            Request = null;
            ResponseStream = null;
        }
    }

}

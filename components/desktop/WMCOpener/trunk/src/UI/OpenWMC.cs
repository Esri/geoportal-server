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
using ESRI.ArcGIS.Framework;
using ESRI.ArcGIS.esriSystem;
using ESRI.ArcGIS.ADF.BaseClasses;
using ESRI.ArcGIS.ADF.CATIDs;
using ESRI.ArcGIS.ArcMapUI;
using ESRI.ArcGIS.Carto;
using ESRI.ArcGIS.Display;
using ESRI.ArcGIS.GISClient;
using ESRI.ArcGIS.Catalog;
using ESRI.ArcGIS.Geometry;
using ESRI.ArcGIS.DataSourcesRaster;
using System.Globalization;
using com.esri.gpt.logger;
namespace com.esri.gpt.wmc
{
    public partial class OpenWMC : Form
    {
        private IApplication m_pApp;
        private List<string> colVisibleLayers = new List<string>();
        private List<string> colServiceList = new List<string>();
        public static AppLogger logger = null;
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="application">the application</param>
        public OpenWMC(IApplication application)
        {
            InitializeComponent();
            m_pApp = application;
            logger = new AppLogger("WMCOpener");
            UpdateUI();
        }
        /// <summary>
        /// Updates UI with current local inhertied from ArcGIS
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
        /// Handles open button click event.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void openBtn_Click(object sender, EventArgs e)
        {
            WMCDocument document = new WMCDocument();
            document.LoadFromFile(textBox1.Text);

            if (document.IsValidWMCDoc())
            {
                this.openBtn.Enabled = false;
                AddToMap(document);
            }

        }
        /// <summary>
        /// Handles browse button click event.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void browseBtn_Click(object sender, EventArgs e)
        {
            DialogResult userResponse = openFileDialog1.ShowDialog();
            switch (userResponse)
            {
                case DialogResult.OK:
                    this.openBtn.Enabled = true;
                    break;
                case DialogResult.Cancel:
                    this.openBtn.Enabled = false;
                    break;
            }
        }
        /// <summary>
        /// Handles open file dialog click event.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void openFileDialog1_FileOk(object sender, CancelEventArgs e)
        {
            textBox1.Text = openFileDialog1.FileName;
        }

        /// <summary>
        /// Adds wmc document information to map
        /// </summary>
        /// <param name="theDoc">the WMCDocument object</param>
        private void AddToMap(WMCDocument theDoc)
        {
            IMxDocument ipMxDoc = (IMxDocument)m_pApp.Document;

            foreach (WMCLayer layer in theDoc.Layers)
            {
                AddTheLayer(layer, ipMxDoc);
            }

            // set spatial reference system / projection
            SetSpatialReference(ipMxDoc, theDoc.SRS);

            //do the layer visibility
            SetLayerVisibility(ipMxDoc);

            if (theDoc.HasExtent)
            {
                ipMxDoc.ActiveView.Extent = theDoc.Extent;
                ipMxDoc.ActiveView.Refresh();
            }

        }
        /// <summary>
        /// Adds wmc layer to current map document
        /// </summary>
        /// <param name="theLayer">the WMCLayer object</param>
        /// <param name="ipMxDoc">the map document</param>
        private void AddTheLayer(WMCLayer theLayer, IMxDocument ipMxDoc)
        {
            bool bIsLoaded = false;
            bool bIsArcIMS = false;
            bool bIsAGS = false;
            string strServiceType;
            strServiceType = theLayer.Server.Service.ToUpper();

            string strThisServerID = null;

            // check the service type and retrieve the service ID
            if (strServiceType == "ESRI:ARCIMS" || strServiceType == "ESRI:ARCIMS:HTTP")
            {
                strThisServerID = theLayer.Server.OnlineResource.Href + ":" + theLayer.Name;
                bIsArcIMS = true;
            }
            else if (strServiceType == "ESRI:AGS:MAP:SOAP")
            {
                strThisServerID = theLayer.Server.OnlineResource.Href + ":" + theLayer.Name;
                bIsAGS = true;
            }
            else if (strServiceType.Contains("WMS"))
            {
                strThisServerID = theLayer.Server.OnlineResource.Href;
                bIsArcIMS = false;
            }
            else
            {
                strThisServerID = "";
            }

            logger.writeLog("strServiceType = " + strServiceType + " strThisServerID = " + strThisServerID);

            //  'was there something?
            if (strThisServerID.Length == 0)
            {
                logger.writeLog(StringResources.UnrecognizedServiceType + strServiceType + StringResources.Exiting);
                MessageBox.Show(StringResources.UnrecognizedServiceType + strServiceType + StringResources.Exiting);
                return;
            }

            // has it been added to the map already?
            string strServer;
            string strLayerName;
            string strSecretName;
            string layer = null;
            foreach (string lyr in colServiceList)
            {
                layer = lyr;
                string[] s = DecodeServiceList(layer);
                strServer = s[0];
                strLayerName = s[1];
                strSecretName = s[2];

                if (strServer == strThisServerID)
                {
                    bIsLoaded = true;
                    break;
                }
                else if ((strServer + strLayerName) == strThisServerID)
                {
                    bIsLoaded = true;
                    break;
                }
                else if ((strServer + ":" + strLayerName) == strThisServerID)
                {
                    bIsLoaded = true;
                    break;
                }
            }

            bool bAddedToList = false;

            //it its not loaded
            if (!bIsLoaded)
            {
                //add it to map (and service list)
                if (bIsArcIMS)
                {
                    // arcims - .name is the group layername in arcmap.  .title is the sublayer display name
                    bAddedToList = addLayerArcIMS(ipMxDoc, theLayer.Server.OnlineResource.Href, theLayer.Name, theLayer.SecretName);
                }
                else if (bIsAGS)
                {
                    bAddedToList = addLayerAGS(ipMxDoc, theLayer.Server.OnlineResource.Href, theLayer.Server.Title, theLayer.SecretName);
                }
                else
                {
                    //  wms wants the .Title to be displayed as layername in arcmap.  thelayer.name is the WMSLayername - internal
                    // wms group layer name comes from servicedescription
                    bAddedToList = addLayerWMS(ipMxDoc, theLayer.Server.OnlineResource.Href, theLayer.Title, theLayer.SecretName);
                }
            }
            else
            {
                //  get the service that matches
                string[] s = DecodeServiceList(layer);
                strServer = s[0];
                strLayerName = s[1];
                strSecretName = s[2];

                if (bIsArcIMS)
                {
                    //  use layer.title for arcims
                    logger.writeLog(StringResources.DontNeedToAddService + strServer + StringResources.Colon + strLayerName + StringResources.Colon + theLayer.SecretName);
                    colServiceList.Add(EncodeServiceList(strServer, strLayerName, theLayer.SecretName));
                }
                else if (bIsAGS)
                {
                    //  use layer.title for ags
                    logger.writeLog(StringResources.DontNeedToAddService + strServer + StringResources.Colon + strLayerName + StringResources.Colon + theLayer.SecretName);
                    colServiceList.Add(EncodeServiceList(strServer, strLayerName, theLayer.SecretName));
                }
                else
                {
                    // create a NEW entry using the new layers internal Name
                    logger.writeLog(StringResources.DontNeedToAddService + strServer + StringResources.Colon + strLayerName + StringResources.Colon + theLayer.SecretName);
                    colServiceList.Add(EncodeServiceList(strServer, strLayerName, theLayer.SecretName));
                }
                bAddedToList = true;
            }

            // add to layer visibility
            if (bAddedToList && (theLayer.IsHidden == "false" || theLayer.IsHidden == "0"))
            {
                colVisibleLayers.Add(colServiceList.ToArray()[(colServiceList.Count - 1)]);
            }
        }
        /// <summary>
        /// Encodes service list with server, layer and secret name
        /// </summary>
        /// <param name="strServer"></param>
        /// <param name="strLayerName"></param>
        /// <param name="strSecretName"></param>
        /// <returns></returns>
        private string EncodeServiceList(string strServer, string strLayerName, string strSecretName)
        {
            string s = (strServer + ";" + strLayerName + ";" + strSecretName);
            return s;
        }
        /// <summary>
        /// Decodes item to return server, layer and secret name
        /// </summary>
        /// <param name="strItem"></param>
        /// <returns></returns>
        private string[] DecodeServiceList(string strItem)
        {
            string[] arrString = strItem.Split(';');
            return arrString;
        }
        /// <summary>
        /// Sets layer visibility in current map document
        /// </summary>
        /// <param name="ipMxDoc"></param>
        private void SetLayerVisibility(IMxDocument ipMxDoc)
        {
            IMap ipMap = ipMxDoc.FocusMap;

            //get all the layers from the map
            IEnumLayer ipEnumLayer;
            ILayer ipLayer;
            ipEnumLayer = ipMap.get_Layers(null, true);

            //  'is there anything visible???
            if (colVisibleLayers.Count == 0) return;

            string strServer = "";
            string strLayerName = "";
            string strSecretName = "";


            //  'is it in visible layer list?
            foreach (string layer in colVisibleLayers)
            {
                string[] s = DecodeServiceList(layer);
                if (s.Length == 3)
                {
                    strServer = s[0];
                    strLayerName = s[1];
                    strSecretName = s[2];
                }
                else if (s.Length == 2)
                {
                    strServer = s[0];
                    strLayerName = s[1];
                    strSecretName = "";
                }
                else if (s.Length == 1)
                {
                    strServer = "";
                    strLayerName = s[0];
                    strSecretName = "";
                }
                ipEnumLayer.Reset();
                ipLayer = ipEnumLayer.Next();
                do
                {
                    if (ipLayer.Name == strLayerName)
                    {
                        SetLayerVisible(ipLayer, strLayerName, strSecretName);
                        break;
                    }
                    ipLayer = ipEnumLayer.Next();
                } while (ipLayer != null);
            }

            //'update toc
            ipMxDoc.UpdateContents();

            // 'and update the view
            ipMxDoc.ActiveView.Refresh();
        }
        /// <summary>
        /// Sets layer visibility for layer
        /// </summary>
        /// <param name="ipLayer">the layer object</param>
        /// <param name="strLayerName">the layer name</param>
        /// <param name="strSecretName">the secret name</param>
        /// <returns>visibility</returns>
        private bool SetLayerVisible(ILayer ipLayer, string strLayerName, string strSecretName)
        {
            bool bFound;

            // if it is composite
            if (ipLayer is ICompositeLayer)
            {
                ICompositeLayer ipCompLayer = (ICompositeLayer)ipLayer;

                //for each of the layers in the composite layer
                for (int idx = 0; idx < ipCompLayer.Count; idx++)
                {
                    //  recurse
                    bFound = SetLayerVisible(ipCompLayer.get_Layer(idx), strLayerName, strSecretName);
                    //if something was found, set me to be visible and set return value
                    if (bFound)
                    {
                        ipLayer.Visible = true;
                        return true;
                    }

                }
            }
            else if (ipLayer is IWMSLayer)
            {
                IWMSLayer ipWMSLayer = (IWMSLayer)ipLayer;
                if (ipWMSLayer.WMSLayerDescription.Name == strSecretName)
                {
                    ipLayer.Visible = true;
                    return true;
                }
            }
            else if (ipLayer is IIMSSubLayer)
            {
                IIMSSubLayer ipSubLayer = (IIMSSubLayer)ipLayer;
                IACLayer ipACLayer = ipSubLayer.IMSLayer;
                // use the secret ID
                if (ipACLayer.ID == strSecretName)
                {
                    ipLayer.Visible = true;
                    return true;
                }
            }
            else
            {

                if (ipLayer.Name == strSecretName)
                {
                    ipLayer.Visible = true;
                    return true;
                }
            }

            return false;
        }

        /// <summary>
        /// Add ArcIMS layer to map
        /// </summary>
        /// <param name="ipMxDoc"></param>
        /// <param name="strServer"></param>
        /// <param name="strLayerName"></param>
        /// <param name="strSecretName"></param>
        /// <returns></returns>
        private bool addLayerArcIMS(IMxDocument ipMxDoc, string strServer, string strLayerName, string strSecretName)
        {

            // Connect to service and create new IMS layer
            IIMSServiceDescription pIMSServiceDescription = new IMSServiceName();
            pIMSServiceDescription.URL = strServer;
            pIMSServiceDescription.Name = strLayerName;
            pIMSServiceDescription.ServiceType = acServiceType.acMapService;


            IIMSMapLayer pIMSLayer = new IMSMapLayer();
            pIMSLayer.ConnectToService(pIMSServiceDescription);
            pIMSLayer.Visible = false;

            //make them all non-visible
            if (pIMSLayer is ICompositeLayer)
            {
                ICompositeLayer ipCompLayer;
                int idx;

                ipCompLayer = (ICompositeLayer)pIMSLayer;
                for (idx = 0; idx < ipCompLayer.Count; idx++)
                    ipCompLayer.get_Layer(idx).Visible = false;
            }

            //add it
            ipMxDoc.AddLayer(pIMSLayer);

            // add to the service list
            string strItem = EncodeServiceList(strServer, strLayerName, strSecretName);
            logger.writeLog(StringResources.AddingServiceList + strItem);
            colServiceList.Add(strItem);

            logger.writeLog("strServer = " + strServer + "strServer & strLayerName = " + strServer + strLayerName);
            return true;
        }
        /// <summary>
        /// Adds WMS document to map
        /// </summary>
        /// <param name="ipMxDoc"></param>
        /// <param name="strServer"></param>
        /// <param name="strLayerName"></param>
        /// <param name="strSecretName"></param>
        /// <returns></returns>
        private bool addLayerWMS(IMxDocument ipMxDoc, string strServer, string strLayerName, string strSecretName)
        {
            IPropertySet ipPropSet = new PropertySet();
            string strFinalChar;

            // check the final char
            strFinalChar = strServer.Substring(strServer.Length - 1);
            if (strFinalChar == "?" && strFinalChar != "&")
            {
                if (strServer.Contains("?"))
                {
                    ipPropSet.SetProperty("URL", (strServer + '&'));
                }
                else
                {
                    ipPropSet.SetProperty("URL", (strServer + '?'));
                }
            }
            else
            {
                ipPropSet.SetProperty("URL", strServer + '?');
            }

            IMap map = (IMap)ipMxDoc.FocusMap;
            IActiveView activeView = (IActiveView)map;
            IWMSGroupLayer wmsGroupLayer = (IWMSGroupLayer)new WMSMapLayerClass();
            IWMSConnectionName wmsConnectionName = new WMSConnectionName();
            wmsConnectionName.ConnectionProperties = ipPropSet;

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
                connected = false;
            }
            if (!connected) return false;

            // get service description out of the layer. the service description contains 
            // information about the wms categories and layers supported by the service
            IWMSServiceDescription wmsServiceDesc = wmsGroupLayer.WMSServiceDescription;
            IWMSLayerDescription wmsLayerDesc = null;
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
                    if (newLayer == null) { }
                }
                else
                {
                    // wms group layer
                    newWmsGroupLayer = wmsGroupLayer.CreateWMSGroupLayers(wmsLayerDesc);
                    newLayer = (ILayer)newWmsGroupLayer;
                    if (newLayer == null) { }
                }

                // add newly created layer
                // wmsGroupLayer.InsertLayer(newLayer, 0);
            }

            // configure the layer before adding it to the map
            layer = (ILayer)wmsGroupLayer;
            layer.Name = wmsServiceDesc.WMSTitle;

            // add to focus map
            map.AddLayer(layer);

            // add to the service list
            string strItem = EncodeServiceList(strServer, wmsServiceDesc.WMSTitle, strSecretName);
            //  m_pLogger.Msg "adding to service list : " & strItem
            colServiceList.Add(strItem);

            // set flag
            return true;

        }
        /// <summary>
        /// Adds ArcGIS layer to map
        /// </summary>
        /// <param name="ipMxDoc"></param>
        /// <param name="strServer"></param>
        /// <param name="strLayerName"></param>
        /// <param name="strSecretName"></param>
        /// <returns></returns>
        private bool addLayerAGS(IMxDocument ipMxDoc, string strServer, string strLayerName, string strSecretName)
        {
            IPropertySet2 pProps = null;
            string pServerUrl;
            string strServerObj;

            pServerUrl = GetAGSServiceUrl(strServer);

            // connect to the GIS server
            IAGSServerConnectionFactory pAGSServerConnectionFactory = new AGSServerConnectionFactory();
            pProps = (IPropertySet2)new PropertySet();
            pProps.SetProperty("URL", pServerUrl);

            IAGSServerConnection pAGSConnection = pAGSServerConnectionFactory.Open(pProps, 0);

            //get server objectname from url
            strServerObj = GetServerObjectName(strServer);

            // enumerate over server objects
            IAGSEnumServerObjectName pAGSSObjs = pAGSConnection.ServerObjectNames;
            IAGSServerObjectName pAGSSObj = pAGSSObjs.Next();

            while (pAGSSObj != null)
            {
                if (pAGSSObj.Type == "MapServer" && pAGSSObj.Name == strServerObj)
                {
                    break;
                }
                pAGSSObj = pAGSSObjs.Next();
            }


            IName pName = (IName)pAGSSObj;
            IAGSServerObject pAGSO = (IAGSServerObject)pName.Open();
            IMapServer mapServer = (IMapServer)pAGSO;


            IPropertySet prop = new PropertySetClass();
            prop.SetProperty("URL", pServerUrl);
            prop.SetProperty("Name", pAGSSObj.Name);

            //Create new layer
            IMapServerLayer pMSLayer = (IMapServerLayer)new MapServerLayer();
            pMSLayer.ServerConnect(pAGSSObj, mapServer.DefaultMapName);

            if (!isMapServerAdded(strServer))
            {
                setAGSLayerVisiblity((MapServerLayer)pMSLayer, strSecretName);
                IMap ipMap = ipMxDoc.FocusMap;
                ipMap.AddLayer((ILayer)pMSLayer);
                logger.writeLog(StringResources.AddAGSLayer + strSecretName);
            }
            else
            {
                // set visibility
                setAGSLayerVisiblity((MapServerLayer)pMSLayer, strSecretName);
            }

            //add to the service list
            string strItem = EncodeServiceList(strServer, strLayerName, strSecretName);

            // m_pLogger.Msg "adding to service list : " & strItem
            colServiceList.Add(strItem);

            //set flag           
            logger.writeLog("strServer = " + strServer + " strServer & strLayerName = " + strServer + strLayerName);
            return true;
        }

        /// <summary>
        /// sets spatial reference for the map using SRS value in wmc
        /// </summary>
        /// <param name="ipMxDoc"></param>
        /// <param name="sSRS"></param>
        private void SetSpatialReference(IMxDocument ipMxDoc, string sSRS)
        {
            try
            {
                string sSpatRefID;  // Spatial Reference ID

                // get spatial reference factory code from SRS (for example EPSG:NNNN - NNNN is the factory code)
                int iPos;
                sSRS = sSRS.Trim();
                iPos = sSRS.IndexOf(":");
                if (iPos > 0)
                {
                    sSpatRefID = sSRS.Substring(iPos + 1);
                }
                else
                {
                    sSpatRefID = sSRS;
                }
                int wkid = int.Parse(sSpatRefID);
                if (wkid != 0)
                {
                    ISpatialReferenceFactory2 pSpatRefFactory = (ISpatialReferenceFactory2)new SpatialReferenceEnvironment();
                    ISpatialReference pSpatRef = pSpatRefFactory.CreateSpatialReference(wkid);
                    if (pSpatRef != null)
                    {
                        // set spatial reference for the map
                        ipMxDoc.FocusMap.SpatialReference = pSpatRef;
                    }
                    else
                    {
                        MessageBox.Show(StringResources.FailedSpatRef);

                    }
                }
            }
            catch (Exception e)
            {
                logger.writeLog(e.StackTrace);
            }
        }
        /// <summary>
        /// Sets AGS layer visibility
        /// </summary>
        /// <param name="pMSLayer"></param>
        /// <param name="strSecretName"></param>
        private void setAGSLayerVisiblity(MapServerLayer pMSLayer, string strSecretName)
        {

            //make them all non-visible
            if (pMSLayer is ICompositeLayer)
            {
                ICompositeLayer ipCompLayer = (ICompositeLayer)pMSLayer;
                for (int idx = 0; idx < ipCompLayer.Count; idx++)
                {
                    if (ipCompLayer.get_Layer(idx).Name == strSecretName)
                    {
                        ipCompLayer.get_Layer(idx).Visible = true;
                    }
                    else
                        ipCompLayer.get_Layer(idx).Visible = false;
                }
            }
        }
        /// <summary>
        /// checks if map server layer already added
        /// </summary>
        /// <param name="strUrl"></param>
        /// <returns></returns>
        private bool isMapServerAdded(string strUrl)
        {
            string strServer;
            foreach (string layer in colServiceList)
            {
                string[] s = DecodeServiceList(layer);
                strServer = s[0];
                if (strServer == strUrl)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            return false;
        }
        /// <summary>
        /// Gets server object name from url
        /// </summary>
        /// <param name="strServiceUrl"></param>
        /// <returns></returns>
        private string GetServerObjectName(string strServiceUrl)
        {
            string strServicesUrl = GetAGSServiceUrl(strServiceUrl);
            strServicesUrl = strServicesUrl + "/";
            string[] tokens = strServiceUrl.Split(new String[] { strServicesUrl }, StringSplitOptions.RemoveEmptyEntries);
            string[] token = tokens[0].Split(new String[] { "/MapServer" }, StringSplitOptions.RemoveEmptyEntries);
            return token[0];
        }
        /// <summary>
        /// Gets ArcGIS server service name from service url
        /// </summary>
        /// <param name="serviceUrl"></param>
        /// <returns></returns>
        private string GetAGSServiceUrl(string serviceUrl)
        {
            string[] tokens = serviceUrl.Split('/');
            return (tokens[0] + "//" + tokens[2] + "/" + tokens[3] + "/" + tokens[4]);
        }
        /// <summary>
        /// Handles click event on cancel button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void cancelBtn_Click(object sender, EventArgs e)
        {
            this.Close();
        }
        /// <summary>
        /// Handles click event on help button
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void button3_Click(object sender, EventArgs e)
        {
            System.Diagnostics.Process.Start("IExplore", StringResources.helpUrl);
        }

    }
}

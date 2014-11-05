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
using System.Windows;
using ESRI.ArcGIS.Client;
using System.Collections.Generic;

namespace GeoportalWidget
{
    public class MapServiceManager
    {
        Map _map = null;
        //public static string MyProxy = "http://al.cloudapp.net/proxy.ashx";
        private static List<string> _mapServicesID = null;

        public string _status
        {
            get;
            set;
        }
        
        public MapServiceManager(Map map)
        {
            _map = map;
            _mapServicesID = new List<string>();
        }

        private bool _useProxy;

        public bool UseProxy
        {
            get { return _useProxy; }
            set { _useProxy = value; }
        }

        private string _proxyUrl;

        public string ProxyUrl
        {
            get { return _proxyUrl; }
            set { _proxyUrl = value; }
        }

        public void Close()
        {
            foreach (string sTemp in _mapServicesID)
            {
                Remove(sTemp);
            }

            _mapServicesID.Clear();
        }

        public void Add(string sLink, string sID)
        {
            ArcGISServiceType myType = ArcGISServiceType.Unknown;
            
            if (sLink.ToUpper().IndexOf("SERVICE=WMS") > 0)
                myType = ArcGISServiceType.WMS;
            else if (sLink.ToUpper().IndexOf("IMAGESERVER") > 0)
                myType = ArcGISServiceType.ImageServer;
            else if (sLink.ToUpper().IndexOf("ARCGIS/REST") > 0)
                if (sLink.ToUpper().IndexOf("MAPSERVER") > 0)
                    myType = ArcGISServiceType.ArcGIS;


            _status = "Adding new service...";

            ESRI.ArcGIS.Client.Layer layer = null;

            switch (myType)
            {
                case ArcGISServiceType.ArcGIS:
                    layer = new ArcGISDynamicMapServiceLayer();
                    ArcGISDynamicMapServiceLayer temp = layer as ArcGISDynamicMapServiceLayer;
                    temp.InitializationFailed += new EventHandler<EventArgs>(layer_InitializationFailed);
                    temp.Initialized += new EventHandler<EventArgs>(temp_Initialized);
                    temp.Url = sLink;
                    temp.ID = sID;                    
                    break;

                case ArcGISServiceType.ImageServer:
                    layer = new ArcGISImageServiceLayer();
                    ArcGISImageServiceLayer imageServer = layer as ArcGISImageServiceLayer;
                    imageServer.Url = sLink;
                    imageServer.ID = sID;
                    imageServer.Initialized += new EventHandler<EventArgs>(temp_Initialized);
                    imageServer.InitializationFailed += new EventHandler<EventArgs>(imageServer_InitializationFailed);
                    break;


                case ArcGISServiceType.IMS:
                    layer = new ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer();
                    ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer imsLayer = layer as ESRI.ArcGIS.IMS.ArcIMSMapServiceLayer;
                    imsLayer.InitializationFailed += new EventHandler<EventArgs>(imsLayer_InitializationFailed);
                    imsLayer.Initialized += new EventHandler<EventArgs>(temp_Initialized);
                    imsLayer.ID = sID;
                    imsLayer.ServiceHost = sLink;
                    imsLayer.ServiceHost = sLink;
                    break;
            }

            if (layer != null)
            {
                _mapServicesID.Add(layer.ID);
                _map.Layers.Add(layer);
            }

        }

        void imsLayer_InitializationFailed(object sender, EventArgs e)
        {
            throw new NotImplementedException();
        }

        void wmsLayer_InitializationFailed(object sender, EventArgs e)
        {
  
        }

        void temp_Initialized(object sender, EventArgs e)
        {            
            if (sender is ArcGISDynamicMapServiceLayer)
            {
                try { _map.ZoomTo(((ArcGISDynamicMapServiceLayer)sender).InitialExtent); }
                catch { }

                _status = "ArcGIS Service added";
            }
            else if (sender is ArcGISImageServiceLayer)
            {
    

                _status = "ImageService added";
            }

            Layer LayerCheck = sender as Layer;
        }

        void imageServer_InitializationFailed(object sender, EventArgs e)
        {
            ArcGISImageServiceLayer image = sender as ArcGISImageServiceLayer;
            if (image != null)
            {
                if (image.ProxyURL == null)
                {
                    _status = "ImageServer FAILED using Proxy";
                    ArcGISImageServiceLayer newImage = new ArcGISImageServiceLayer();
                    newImage.InitializationFailed += new EventHandler<EventArgs>(imageServer_InitializationFailed);
                    newImage.Url = image.Url;
                    newImage.ID = image.ID;

                    newImage.ProxyURL = _proxyUrl;

                    _map.Layers.Remove(image);
                    _map.Layers.Add(image);
                }
                else
                    MessageBox.Show("Cannot access that service");
            }
        }

        void layer_InitializationFailed(object sender, EventArgs e)
        {
            ArcGISDynamicMapServiceLayer dynamic = sender as ArcGISDynamicMapServiceLayer;
            if (dynamic != null)
            {
                if (dynamic.ProxyURL == null)
                {
                    _status = "ArcGIS FAILED using Proxy";
                    ArcGISDynamicMapServiceLayer newDynamic = new ArcGISDynamicMapServiceLayer();
                    newDynamic.InitializationFailed += new EventHandler<EventArgs>(layer_InitializationFailed);
                    newDynamic.Url = dynamic.Url;
                    newDynamic.ID = dynamic.ID;

                    newDynamic.ProxyURL = _proxyUrl;
                    _map.Layers.Remove(dynamic);
                    _map.Layers.Add(newDynamic);
                }
                else
                    MessageBox.Show("Cannot access that service");
            }
        }

        public void Remove(string sID)
        {
            _map.Layers.Remove(_map.Layers[sID]);
        }

        public enum ArcGISServiceType
        {
            WMS = 1,
            IMS = 2,
            ArcGIS = 3,
            Unknown = 4,
            ImageServer = 5
        }

        private string CleanWMSSErvices(string wmsService)
        {
            if (wmsService.IndexOf('?') > 0)
            {
                wmsService = wmsService.Substring(0, wmsService.IndexOf('?'));
            }

            return wmsService;
        }
    }
}

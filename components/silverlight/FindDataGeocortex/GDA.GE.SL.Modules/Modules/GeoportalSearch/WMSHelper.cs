using System;
using System.Net;
using System.Xml.Linq;
using System.Collections.Generic;
using ESRI.ArcGIS.Client.Geometry;
using Geocortex.Essentials.Client.Ows;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
   
    public delegate void GetCapabilitiesEventHandler(object sender, GetCapabilitiesEventArgs e);

    public static class WMSHelper
    {
        const string cVersion111 = "1.1.1";
        const string cVersion130 = "1.3.0";
        const string cGetCapabilitiesV111 = "service=WMS&request=GetCapabilities&version=" + cVersion111;
        const string cGetCapabilitiesV130 = "service=WMS&request=GetCapabilities&version=" + cVersion130;
        const string cFormatHTML = "text/html";
        const string cFormatXML = "text/xml";

        public static event GetCapabilitiesEventHandler eventGetCapabilities;

        private static string _url = string.Empty;

        #region Public Methods

        /// <summary>
        /// Gets the WMS Capabilities and raises GetCapabilitiesEvent that contains GetCapabilitiesEventArgs
        /// </summary>
        /// <param name="url"></param>
        public static void GetCapabilities(string url)
        {
            if (String.IsNullOrEmpty(url))
            {
                GetCapabilitiesEventArgs arg = new GetCapabilitiesEventArgs();
                arg.ServiceInfo = null;
                arg.Error = "Null URL parameter passed";
                eventGetCapabilities(null, arg);
                return;
            }

            _url = url;
            //Format WMS url
            if (!url.EndsWith("?"))
                url = url + "?";

            url = url + cGetCapabilitiesV130;

            WebClient wmsClient = new WebClient();

            wmsClient.DownloadStringCompleted += (s, e) =>
            {
                GetCapabilitiesEventArgs arg = new GetCapabilitiesEventArgs();
                IServiceInfo servInfo = new ServiceInfo();
                servInfo.Url = _url;

                if (e.Error != null)
                {
                    arg.ServiceInfo = servInfo;
                    arg.Error = e.Error.ToString();
                    eventGetCapabilities(null, arg);
                    return;
                }

                try
                {
                    //Parse Capabilities response
                    XElement xEle = XElement.Parse(e.Result);
                    XNamespace aw = xEle.GetDefaultNamespace();

                    //Check version
                    string version = LinqXmlHelper.GetWmsVersion(xEle);
                    if (version != cVersion130)
                    {
                        arg.ServiceInfo = servInfo;
                        eventGetCapabilities(null, arg);
                        return;
                    }
                    servInfo.Version = version;

                    //Get Title
                    XElement servEle = LinqXmlHelper.GetWmsSingleElement(xEle, "Service");
                    if (servEle == null) return;
                    servInfo.Title = LinqXmlHelper.GetWmsSingleElement(servEle, "Title").Value;

                    //Capability
                    XElement capEle = LinqXmlHelper.GetWmsSingleElement(xEle, "Capability");
                    if (capEle == null) return;

                    //main Layer element
                    XElement mainLyrEle = LinqXmlHelper.GetWmsSingleElement(capEle, "Layer");
                    if (mainLyrEle == null) return;

                    List<ILayerInfo> lstLyrInfo = LinqXmlHelper.GetLayerListInfo(mainLyrEle);

                    if (lstLyrInfo != null)
                        servInfo.LayersInfo = lstLyrInfo;

                    arg.ServiceInfo = servInfo;
                    eventGetCapabilities(null, arg);
                }
                catch (Exception ex)
                {
                    arg.ServiceInfo = servInfo;
                    arg.Error = "WMSHelper::wmsclient_DownloadGetCapabilitiesCompleted() -- " + ex.Message.ToString();
                    eventGetCapabilities(null, arg);
                    return;
                }
            };

            wmsClient.DownloadStringAsync(new Uri(url, UriKind.Absolute));
        }

        /// <summary>
        /// Retrieves a Layer List of a WMS service that accommodates Nested layers
        /// Called by: MapServiceFactory.AddWMSMapService() in order to add WMS map services 
        /// </summary>
        /// <param name="mapservice"></param>
        /// <returns></returns>
        public static IEnumerable<WmsLayer.LayerInfo> FlattenWmsLayerList(WmsLayer mapservice)
        {
            foreach (var layerInfo in mapservice.LayerList)
            {
                yield return layerInfo;

                if (layerInfo.ChildLayers.Count > 0)
                {
                    foreach (var childLayerInfo in FlattenWmsChildLayerList(layerInfo))
                    {
                        yield return childLayerInfo;
                    }
                }
            }
        }

        /// <summary>
        /// https://viswaug.wordpress.com/2009/03/15/reversed-co-ordinate-axis-order-for-epsg4326-vs-crs84-when-requesting-wms-130-images/
        /// </summary>
        /// <param name="layer"></param>
        /// <returns></returns>
        public static Envelope GetCorrectedExtent(WmsLayer layer)
        {
            if (layer == null) return null;

            Envelope wmsExtent = layer.FullExtent;

            if ((wmsExtent == null) || (wmsExtent.SpatialReference == null)) return null;
            int wkid = wmsExtent.SpatialReference.WKID;

            if (wkid == 4326)
            {  // WGS84 Geographic
                if (wmsExtent.YMax > 90 || wmsExtent.YMin < -90)
                {
                    // Geographic coords were initialized incorrectly, so swap x-y coords to work around this problem
                    Envelope correctedExtent = new Envelope(wmsExtent.YMin, wmsExtent.XMin, wmsExtent.YMax, wmsExtent.XMax);
                    correctedExtent.SpatialReference = wmsExtent.SpatialReference;
                    wmsExtent = correctedExtent;
                }
            }

            return wmsExtent;
        }
               
        
        #endregion

        #region Private Methods

        /// <summary>
        /// Subroutine for FlattenWmsLayerList(). Gets the nested child layers in WMS Map Service
        /// </summary>
        /// <param name="layerInfo"></param>
        /// <returns></returns>
        private static IEnumerable<WmsLayer.LayerInfo> FlattenWmsChildLayerList(WmsLayer.LayerInfo layerInfo)
        {
            foreach (var childLayerInfo in layerInfo.ChildLayers)
            {
                yield return childLayerInfo;

                if (childLayerInfo.ChildLayers.Count > 0)
                {
                    foreach (var subchildLayerInfo in FlattenWmsChildLayerList(childLayerInfo))
                    {
                        yield return subchildLayerInfo;
                    }
                }
            }
        }
  
        #endregion
    }
}
